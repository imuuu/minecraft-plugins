package imu.GS.engine.pricing;

import imu.GS.engine.ItemKey;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;

/**
 * Prices of buying from and selling to a shop. Only reads; prices are rounded to cents.
 */
public class PriceCalculator
{
    private final PriceRules _rules;
    private final SellPressure _pressure;
    private final LongSupplier _clock;
    private Market _market = Market.NONE;

    public PriceCalculator(PriceRules rules)
    {
        this(rules, new SellPressure(), System::currentTimeMillis);
    }

    /**
     * @param clock current time in milliseconds; tests move it by hand
     */
    public PriceCalculator(PriceRules rules, SellPressure pressure, LongSupplier clock)
    {
        _rules = rules;
        _pressure = pressure;
        _clock = clock;
    }

    public PriceRules getRules()
    {
        return _rules;
    }

    public Market getMarket()
    {
        return _market;
    }

    /**
     * The day's mood and the shops' wanted requests; {@link Market#NONE} turns them off.
     */
    public void setMarket(Market market)
    {
        _market = market == null ? Market.NONE : market;
    }

    /**
     * The item's value with today's mood.
     */
    private double marketValue(ItemKey item)
    {
        return _rules.value(item) * _market.moodFactor(item);
    }

    // Players buying =================================================================================================

    /**
     * Price of one unit of the listing. Not for {@link PriceRule.Custom}, see {@link #customCost}.
     */
    public double buyUnitPrice(Shop shop, Listing listing)
    {
        double sellMultiplier = shop.getSettings().sellMultiplier();
        return switch (listing.getPrice())
        {
            case PriceRule.Material m -> marketValue(listing.getItem()) * sellMultiplier;
            case PriceRule.Fixed fixed -> fixed.price();
            case PriceRule.Unique unique -> unique.price() * sellMultiplier;
            case PriceRule.Custom custom -> throw new IllegalArgumentException("A custom price is paid with customCost");
        };
    }

    public double buyPrice(Shop shop, Listing listing, int amount)
    {
        return round(buyUnitPrice(shop, listing) * amount);
    }

    /**
     * What {@code amount} units of a custom priced listing cost. Items are counted per bundle, rounding down.
     */
    public CustomCost customCost(Listing listing, int amount)
    {
        if (!(listing.getPrice() instanceof PriceRule.Custom custom))
            throw new IllegalArgumentException("Not a custom price: " + listing.getPrice());

        List<PriceRule.ItemCost> items = new ArrayList<>();
        for (PriceRule.ItemCost cost : custom.items())
            items.add(new PriceRule.ItemCost(cost.item(), cost.amount() * amount / custom.bundleSize()));
        return new CustomCost(items, round(custom.money() * amount / custom.bundleSize()));
    }

    public record CustomCost(List<PriceRule.ItemCost> items, double money) {}

    /**
     * How much more the shop pays for one unit of the listing than it sells it for. Over 0, players make money
     * buying it and selling it back: a fixed price under what the shop pays, or a buy multiplier over the sell
     * multiplier. 0 for a custom price.
     */
    public double buyBackProfit(Shop shop, Listing listing)
    {
        if (listing.getPrice() instanceof PriceRule.Custom)
            return 0;
        return Math.max(0, round(sellUnitPrice(shop, listing.getItem()) - buyUnitPrice(shop, listing)));
    }

    // Players selling ================================================================================================

    /**
     * What the shop pays for one unit before any overflow: the value with today's mood, and the shop's wanted
     * bonus. A bonus never makes the shop pay as much as it sells the item for, so buying it to sell back never
     * pays.
     */
    public double sellUnitPrice(Shop shop, ItemKey item)
    {
        double buyMultiplier = shop.getSettings().buyMultiplier();
        Double unique = _rules.uniquePrice(item);
        if (unique != null)
            return unique * _rules.condition(item) * buyMultiplier;
        double value = marketValue(item);
        double price = value * buyMultiplier;
        double bonus = _market.wantedBonus(shop.getId(), item);
        if (bonus <= 0)
            return price;
        double cap = 0.9 * cheapestBuyPrice(shop, item, value);
        return Math.max(price, Math.min(price * (1 + bonus), cap));
    }

    /**
     * The least the shop sells one unit of the item for: by its material price, or a listing of it.
     */
    private double cheapestBuyPrice(Shop shop, ItemKey item, double marketValue)
    {
        double cheapest = marketValue * shop.getSettings().sellMultiplier();
        for (Listing listing : shop.getListings())
        {
            if (listing.getItem().equals(item) && !(listing.getPrice() instanceof PriceRule.Custom))
                cheapest = Math.min(cheapest, buyUnitPrice(shop, listing));
        }
        return cheapest;
    }

    /**
     * What the shop pays for {@code amount} units. With an overflow every unit over the soft cap is worth less,
     * and past the shop's {@link SellLimit} of the item's group the price drops too; unique items never drop.
     */
    public double sellPrice(Shop shop, ItemKey item, int amount)
    {
        return quote(shop, item, amount, new HashMap<>()).price();
    }

    /**
     * What the shop pays for everything in a shulker box. Items of the same limit group count against the
     * limit one after another.
     */
    public double shulkerPrice(Shop shop, Map<ItemKey, Integer> contents)
    {
        Map<String, Double> pending = new HashMap<>();
        double total = 0;
        for (Map.Entry<ItemKey, Integer> entry : contents.entrySet())
            total += quote(shop, entry.getKey(), entry.getValue(), pending).price();
        return round(total);
    }

    // Sell limits ====================================================================================================


    /**
     * @param price     what the shop pays
     * @param shopId    the shop sold to
     * @param group     the item's sell limit group, null when not limited
     * @param fullValue the units' worth at the full price, which is what the sale adds to the pressures
     */
    public record SellQuote(double price, UUID shopId, String group, double fullValue) {}

    /**
     * The price of selling, with the shop stock's overflow and the shop's sell limit of the item's group.
     *
     * @param pending pressure of this batch not yet recorded, per group; this quote's units are added to it
     */
    public SellQuote quote(Shop shop, ItemKey item, int amount, Map<String, Double> pending)
    {
        double fullPrice = sellUnitPrice(shop, item);
        boolean unique = _rules.uniquePrice(item) != null;
        Overflow overflow = unique ? null : _rules.overflow(item);
        int stock = overflow == null ? 0 : overflowStock(shop, item);
        String group = unique ? null : _rules.sellLimitGroup(item);
        SellLimit limit = group == null ? null : _rules.sellLimit(group);

        double pressure = limit == null ? 0
                : _pressure.get(shop.getId(), group, limit, _clock.getAsLong()) + pending.getOrDefault(group, 0.0);

        double total = 0;
        for (int i = 0; i < amount; i++)
        {
            double unit = overflow == null ? fullPrice : overflow.unitPrice(fullPrice, stock + i);
            if (limit != null)
            {
                unit *= limit.multiplier(pressure);
                pressure += fullPrice;
            }
            total += unit;
        }

        double fullValue = fullPrice * amount;
        if (limit == null)
            group = null;
        else
            pending.merge(group, fullValue, Double::sum);
        return new SellQuote(round(total), shop.getId(), group, fullValue);
    }

    /**
     * Adds a finished sale to the shop's pressure on the group.
     */
    public void recordSale(SellQuote quote)
    {
        if (quote.group() == null)
            return;
        SellLimit limit = _rules.sellLimit(quote.group());
        if (limit != null)
            _pressure.add(quote.shopId(), quote.group(), limit, quote.fullValue(), _clock.getAsLong());
    }

    /**
     * How the shop's sell limit of the item stands.
     *
     * @param unitsLeft  units anyone can still sell before the price drops, -1 when the item has no limit
     * @param multiplier share of the full price the next unit gets, 1.0 = full price
     */
    public record LimitStatus(int unitsLeft, double multiplier) {}

    public LimitStatus limitStatus(Shop shop, ItemKey item)
    {
        String group = _rules.uniquePrice(item) != null ? null : _rules.sellLimitGroup(item);
        SellLimit limit = group == null ? null : _rules.sellLimit(group);
        double fullPrice = sellUnitPrice(shop, item);
        if (limit == null || fullPrice <= 0)
            return new LimitStatus(-1, 1.0);
        double pressure = _pressure.get(shop.getId(), group, limit, _clock.getAsLong());
        double left = limit.allowance() - pressure;
        int units = left < 0 ? 0 : (int) Math.floor(left / fullPrice) + 1;
        return new LimitStatus(units, limit.multiplier(pressure));
    }

    public SellPressure getPressure()
    {
        return _pressure;
    }

    /**
     * The stock the overflow of this item counts: the whole group, or the first listing of exactly this item.
     */
    public int overflowStock(Shop shop, ItemKey item)
    {
        String group = _rules.overflowGroup(item);
        if (group != null)
            return shop.countItems(other -> group.equals(_rules.overflowGroup(other)));

        Listing listing = shop.findSameKind(item);
        return listing == null ? 0 : listing.getAmount();
    }

    /**
     * How a listing made from items sold to the shop is priced.
     */
    public PriceRule priceForSoldItem(ItemKey item)
    {
        Double unique = _rules.uniquePrice(item);
        return unique != null ? new PriceRule.Unique(unique) : PriceRule.MATERIAL;
    }

    public static double round(double price)
    {
        return Math.round(price * 100.0) / 100.0;
    }
}
