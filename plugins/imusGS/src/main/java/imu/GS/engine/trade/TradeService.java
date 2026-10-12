package imu.GS.engine.trade;

import imu.GS.engine.ItemKey;
import imu.GS.engine.customer.Customer;
import imu.GS.engine.customer.CustomerInventory;
import imu.GS.engine.customer.Wallet;
import imu.GS.engine.live.FlashDeal;
import imu.GS.engine.live.FlashDeals;
import imu.GS.engine.live.FlashOffer;
import imu.GS.engine.pricing.PriceCalculator;
import imu.GS.engine.pricing.PriceRule;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Shop;
import imu.GS.engine.trade.TradeResult.Status;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

/**
 * Buying from and selling to shops. Every check, the payment and moving the items happen in one call, so on the
 * main thread no other trade can come in between: two customers can't both buy the last units.
 */
public class TradeService
{
    private final PriceCalculator _prices;
    private final Wallet _wallet;
    private final BooleanSupplier _allowZeroPriceSells;
    private final List<TradeListener> _listeners = new CopyOnWriteArrayList<>();

    public TradeService(PriceCalculator prices, Wallet wallet)
    {
        this(prices, wallet, () -> true);
    }

    /**
     * @param allowZeroPriceSells whether customers may sell items the shop pays nothing for, to empty their
     *                            inventory; when false such items stay with them
     */
    public TradeService(PriceCalculator prices, Wallet wallet, BooleanSupplier allowZeroPriceSells)
    {
        _prices = prices;
        _wallet = wallet;
        _allowZeroPriceSells = allowZeroPriceSells;
    }

    /**
     * Whether the shop takes the item: it isn't one shops never buy (a flash deal item), and the shop pays
     * something for it or selling for nothing is allowed.
     */
    public boolean buysItem(Shop shop, ItemKey item)
    {
        if (_prices.getRules().neverBuys(item))
            return false;
        return _allowZeroPriceSells.getAsBoolean() || _prices.sellUnitPrice(shop, item) > 0;
    }

    public PriceCalculator getPrices()
    {
        return _prices;
    }

    public void addListener(TradeListener listener)
    {
        _listeners.add(listener);
    }

    public void removeListener(TradeListener listener)
    {
        _listeners.remove(listener);
    }

    /**
     * Whether the customer may buy the listing at all (its time, area and permission restrictions).
     */
    public boolean isAvailable(Listing listing, Customer customer)
    {
        return listing.getRestrictions().allows(customer.context());
    }

    // Buying =========================================================================================================

    /**
     * The customer buys units of a listing for money.
     */
    public TradeResult buy(Shop shop, Customer customer, Listing listing, int amount)
    {
        if (amount <= 0)
            return TradeResult.fail(Status.INVALID_AMOUNT);
        if (shop.positionOf(listing) == null)
            return TradeResult.fail(Status.NO_LISTING);
        if (listing.getPrice() instanceof PriceRule.Custom)
            return TradeResult.fail(Status.WRONG_PRICE_TYPE);
        if (!isAvailable(listing, customer))
            return TradeResult.fail(Status.NOT_AVAILABLE);
        if (amount > listing.getAmount())
            return TradeResult.fail(Status.NOT_ENOUGH_STOCK);

        double price = _prices.buyPrice(shop, listing, amount);
        if (_wallet.balance(customer.id()) < price)
            return TradeResult.fail(Status.NOT_ENOUGH_MONEY);
        if (!_wallet.withdraw(customer.id(), price))
            return TradeResult.fail(Status.PAYMENT_FAILED);

        shop.take(listing, amount);
        customer.inventory().give(listing.getItem(), amount);
        notify(shop, customer, new Trade(Trade.Type.BUY, shop.getId(), customer.id(), customer.name(), listing.getId(), listing.getItem(), amount, price));
        return TradeResult.success(amount, price);
    }

    /**
     * The customer buys units of a listing with a custom price, paying with items and money.
     */
    public TradeResult buyCustom(Shop shop, Customer customer, Listing listing, int amount)
    {
        if (shop.positionOf(listing) == null)
            return TradeResult.fail(Status.NO_LISTING);
        if (!(listing.getPrice() instanceof PriceRule.Custom custom))
            return TradeResult.fail(Status.WRONG_PRICE_TYPE);
        if (amount <= 0 || amount < custom.bundleSize())
            return TradeResult.fail(Status.INVALID_AMOUNT);
        if (!isAvailable(listing, customer))
            return TradeResult.fail(Status.NOT_AVAILABLE);
        if (amount > listing.getAmount())
            return TradeResult.fail(Status.NOT_ENOUGH_STOCK);

        PriceCalculator.CustomCost cost = _prices.customCost(listing, amount);
        CustomerInventory inventory = customer.inventory();
        for (PriceRule.ItemCost item : cost.items())
        {
            if (inventory.count(item.item()) < item.amount())
                return TradeResult.fail(Status.NOT_ENOUGH_ITEMS);
        }
        if (_wallet.balance(customer.id()) < cost.money())
            return TradeResult.fail(Status.NOT_ENOUGH_MONEY);
        // Money first: if the payment fails the customer keeps their items
        if (cost.money() > 0 && !_wallet.withdraw(customer.id(), cost.money()))
            return TradeResult.fail(Status.PAYMENT_FAILED);

        for (PriceRule.ItemCost item : cost.items())
            inventory.remove(item.item(), item.amount());

        shop.take(listing, amount);
        inventory.give(listing.getItem(), amount);
        notify(shop, customer, new Trade(Trade.Type.BUY, shop.getId(), customer.id(), customer.name(), listing.getId(), listing.getItem(), amount, cost.money()));
        return TradeResult.success(amount, cost.money());
    }

    /**
     * The customer buys units of a flash deal offer of the shop. They get the offer's item, marked so no shop
     * buys it back.
     */
    public TradeResult buyFlash(Shop shop, Customer customer, FlashDeals deals, UUID offerId, int amount, long nowMillis)
    {
        if (amount <= 0)
            return TradeResult.fail(Status.INVALID_AMOUNT);
        FlashDeal deal = deals.active(shop.getId(), nowMillis);
        if (deal == null)
            return TradeResult.fail(Status.DEAL_OVER);
        FlashOffer offer = deal.getOffer(offerId);
        if (offer == null)
            return TradeResult.fail(Status.NO_LISTING);
        if (amount > offer.getStock())
            return TradeResult.fail(Status.NOT_ENOUGH_STOCK);
        if (amount > offer.canBuy(customer.id()))
            return TradeResult.fail(Status.OVER_LIMIT);

        double price = PriceCalculator.round(offer.getUnitPrice() * amount);
        if (_wallet.balance(customer.id()) < price)
            return TradeResult.fail(Status.NOT_ENOUGH_MONEY);
        if (!_wallet.withdraw(customer.id(), price))
            return TradeResult.fail(Status.PAYMENT_FAILED);

        deals.recordPurchase(deal, offer, customer.id(), amount);
        customer.inventory().give(offer.getItem(), amount);
        notify(shop, customer, new Trade(Trade.Type.BUY, shop.getId(), customer.id(), customer.name(), offer.getId(), offer.getItem(), amount, price));
        return TradeResult.success(amount, price);
    }

    // Selling ========================================================================================================

    /**
     * The customer sells units of an item from anywhere in their inventory.
     */
    public TradeResult sell(Shop shop, Customer customer, ItemKey item, int amount)
    {
        if (shop.getSettings().customersCanOnlyBuy())
            return TradeResult.fail(Status.SHOP_DOESNT_BUY);
        if (amount <= 0)
            return TradeResult.fail(Status.INVALID_AMOUNT);
        CustomerInventory inventory = customer.inventory();
        if (inventory.count(item) < amount)
            return TradeResult.fail(Status.NOT_ENOUGH_ITEMS);
        if (_prices.getRules().neverBuys(item))
            return TradeResult.fail(Status.NOT_BOUGHT);
        if (!buysItem(shop, item))
            return TradeResult.fail(Status.ZERO_PRICE);

        PriceCalculator.SellQuote quote = _prices.quote(shop, item, amount, new HashMap<>());
        double price = quote.price();
        if (!_wallet.deposit(customer.id(), price))
            return TradeResult.fail(Status.PAYMENT_FAILED);
        if (!inventory.remove(item, amount))
        {
            // Counted a moment ago in the same tick, so this shouldn't happen; take the money back
            _wallet.withdraw(customer.id(), price);
            return TradeResult.fail(Status.NOT_ENOUGH_ITEMS);
        }

        _prices.recordSale(quote);
        Listing listing = shop.receive(item, amount, _prices.priceForSoldItem(item));
        giveLeftover(shop, customer, item, amount);
        notify(shop, customer, new Trade(Trade.Type.SELL, shop.getId(), customer.id(), customer.name(), listing.getId(), item, amount, price));
        return TradeResult.success(amount, price);
    }

    /**
     * The customer sells everything in the shulker box in this slot that the shop buys (see {@link #buysItem}).
     * The box itself stays with them, with only what the shop didn't buy.
     */
    public TradeResult sellShulker(Shop shop, Customer customer, int slot)
    {
        if (shop.getSettings().customersCanOnlyBuy())
            return TradeResult.fail(Status.SHOP_DOESNT_BUY);

        CustomerInventory inventory = customer.inventory();
        Map<ItemKey, Integer> contents = new LinkedHashMap<>(inventory.shulkerContents(slot));
        Set<ItemKey> kept = new HashSet<>();
        contents.keySet().removeIf(item -> !buysItem(shop, item) && kept.add(item));
        if (contents.isEmpty())
            return TradeResult.fail(Status.NOTHING_TO_SELL);

        // Each item is priced before any of the box goes into the shop, like the price the menu showed
        Map<ItemKey, PriceCalculator.SellQuote> quotes = new LinkedHashMap<>();
        Map<String, Double> pending = new HashMap<>();
        double total = 0;
        for (Map.Entry<ItemKey, Integer> entry : contents.entrySet())
        {
            PriceCalculator.SellQuote quote = _prices.quote(shop, entry.getKey(), entry.getValue(), pending);
            quotes.put(entry.getKey(), quote);
            total += quote.price();
        }
        total = PriceCalculator.round(total);

        if (!_wallet.deposit(customer.id(), total))
            return TradeResult.fail(Status.PAYMENT_FAILED);
        if (!inventory.emptyShulker(slot, kept))
        {
            _wallet.withdraw(customer.id(), total);
            return TradeResult.fail(Status.NOTHING_TO_SELL);
        }

        int units = 0;
        for (Map.Entry<ItemKey, Integer> entry : contents.entrySet())
        {
            ItemKey item = entry.getKey();
            int amount = entry.getValue();
            PriceCalculator.SellQuote quote = quotes.get(item);
            _prices.recordSale(quote);
            Listing listing = shop.receive(item, amount, _prices.priceForSoldItem(item));
            giveLeftover(shop, customer, item, amount);
            notify(shop, customer, new Trade(Trade.Type.SELL, shop.getId(), customer.id(), customer.name(), listing.getId(), item, amount, quote.price()));
            units += amount;
        }
        return TradeResult.success(units, total);
    }

    /**
     * Selling a water bucket gives the empty bucket back, when the shop paid anything for it.
     */
    private void giveLeftover(Shop shop, Customer customer, ItemKey item, int amount)
    {
        ItemKey leftover = _prices.getRules().leftover(item);
        if (leftover != null && _prices.sellUnitPrice(shop, item) > 0)
            customer.inventory().give(leftover, amount);
    }

    private void notify(Shop shop, Customer customer, Trade trade)
    {
        for (TradeListener listener : _listeners)
            listener.traded(shop, customer, trade);
    }
}
