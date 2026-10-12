package imu.GS.engine.trade;

import imu.GS.engine.Fakes;
import imu.GS.engine.ItemCategory;
import imu.GS.engine.ItemKey;
import imu.GS.engine.customer.Customer;
import imu.GS.engine.pricing.PriceCalculator;
import imu.GS.engine.pricing.PriceRule;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Restrictions;
import imu.GS.engine.shop.Shop;
import imu.GS.engine.shop.ShopSettings;
import imu.GS.engine.trade.TradeResult.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static imu.GS.engine.Fakes.item;
import static org.junit.jupiter.api.Assertions.*;

class TradeServiceTest
{
    private Fakes.Rules _rules;
    private Fakes.Bank _bank;
    private TradeService _trades;
    private Shop _shop;
    private Fakes.Inventory _inventory;
    private Fakes.Context _context;
    private Customer _alex;
    private final List<Trade> _log = new ArrayList<>();

    @BeforeEach
    void setUp()
    {
        _rules = new Fakes.Rules().value("diamond", 10).value("stone", 1).value("water_bucket", 2);
        _bank = new Fakes.Bank();
        _trades = new TradeService(new PriceCalculator(_rules), _bank);
        _trades.addListener((shop, customer, trade) -> _log.add(trade));
        _shop = new Shop(UUID.randomUUID(), "Test", "Test", ShopSettings.DEFAULT);
        _inventory = new Fakes.Inventory();
        _context = new Fakes.Context();
        _alex = Fakes.customer("Alex", _inventory, _context);
    }

    private Listing listing(String item, int amount, PriceRule price)
    {
        Listing listing = new Listing(UUID.randomUUID(), item(item), amount, price);
        _shop.addListing(listing);
        return listing;
    }

    private double balance(Customer customer)
    {
        return _bank.balance(customer.id());
    }

    // Buying =========================================================================================================

    @Test
    void buyingMovesMoneyItemsAndStock()
    {
        Listing diamonds = listing("diamond", 10, PriceRule.MATERIAL);
        _bank.balances.put(_alex.id(), 500.0);

        TradeResult result = _trades.buy(_shop, _alex, diamonds, 8);

        assertTrue(result.isSuccess());
        assertEquals(400, result.price());
        assertEquals(100, balance(_alex));
        assertEquals(2, diamonds.getAmount());
        assertEquals(8, _inventory.count(item("diamond")));
        assertEquals(Trade.Type.BUY, _log.get(0).type());
    }

    @Test
    void notEnoughMoneyChangesNothing()
    {
        Listing diamonds = listing("diamond", 10, PriceRule.MATERIAL);
        _bank.balances.put(_alex.id(), 49.0);

        assertEquals(Status.NOT_ENOUGH_MONEY, _trades.buy(_shop, _alex, diamonds, 1).status());
        assertEquals(10, diamonds.getAmount());
        assertEquals(0, _inventory.count(item("diamond")));
        assertTrue(_log.isEmpty());
    }

    @Test
    void aFailedPaymentChangesNothing()
    {
        Listing diamonds = listing("diamond", 10, PriceRule.MATERIAL);
        _bank.balances.put(_alex.id(), 500.0);
        _bank.failPayments = true;

        assertEquals(Status.PAYMENT_FAILED, _trades.buy(_shop, _alex, diamonds, 1).status());
        assertEquals(10, diamonds.getAmount());
        assertEquals(0, _inventory.count(item("diamond")));
    }

    @Test
    void twoCustomersCantBothBuyTheLastUnits()
    {
        Listing diamonds = listing("diamond", 5, PriceRule.MATERIAL);
        Customer steve = Fakes.customer("Steve", new Fakes.Inventory(), new Fakes.Context());
        _bank.balances.put(_alex.id(), 1000.0);
        _bank.balances.put(steve.id(), 1000.0);

        assertTrue(_trades.buy(_shop, _alex, diamonds, 5).isSuccess());
        assertEquals(Status.NOT_ENOUGH_STOCK, _trades.buy(_shop, steve, diamonds, 5).status());
        assertEquals(1000, balance(steve));
    }

    @Test
    void cantBuyAListingThatWasRemoved()
    {
        Listing diamonds = listing("diamond", 5, PriceRule.MATERIAL);
        _shop.removeListing(diamonds);
        _bank.balances.put(_alex.id(), 1000.0);
        assertEquals(Status.NO_LISTING, _trades.buy(_shop, _alex, diamonds, 1).status());
    }

    @Test
    void restrictionsKeepOthersOut()
    {
        Listing diamonds = listing("diamond", 5, PriceRule.MATERIAL);
        _shop.setRestrictions(diamonds, new Restrictions(null, List.of(), List.of("vip")));
        _bank.balances.put(_alex.id(), 1000.0);

        assertEquals(Status.NOT_AVAILABLE, _trades.buy(_shop, _alex, diamonds, 1).status());
        _context.permissions.add("vip");
        assertTrue(_trades.buy(_shop, _alex, diamonds, 1).isSuccess());
    }

    @Test
    void aCustomPriceCantBeBoughtWithMoneyAlone()
    {
        Listing totem = listing("totem", 5, new PriceRule.Custom(List.of(), 10, 1));
        _bank.balances.put(_alex.id(), 1000.0);
        assertEquals(Status.WRONG_PRICE_TYPE, _trades.buy(_shop, _alex, totem, 1).status());
    }

    @Test
    void customPriceTakesItemsAndMoney()
    {
        ItemKey emerald = item("emerald");
        Listing totem = listing("totem", 8, new PriceRule.Custom(List.of(new PriceRule.ItemCost(emerald, 3)), 10, 2));
        _inventory.put(20, "emerald", 10, ItemCategory.BLOCKS);
        _bank.balances.put(_alex.id(), 100.0);

        TradeResult result = _trades.buyCustom(_shop, _alex, totem, 4); // 2 bundles

        assertTrue(result.isSuccess());
        assertEquals(4, _inventory.count(emerald));
        assertEquals(80, balance(_alex));
        assertEquals(4, _inventory.count(item("totem")));
        assertEquals(4, totem.getAmount());
    }

    @Test
    void customPriceNeedsEveryItem()
    {
        Listing totem = listing("totem", 8, new PriceRule.Custom(List.of(new PriceRule.ItemCost(item("emerald"), 3)), 0, 1));
        _inventory.put(20, "emerald", 2, ItemCategory.BLOCKS);

        assertEquals(Status.NOT_ENOUGH_ITEMS, _trades.buyCustom(_shop, _alex, totem, 1).status());
        assertEquals(2, _inventory.count(item("emerald")));
    }

    @Test
    void customPriceNeedsAtLeastOneBundle()
    {
        Listing totem = listing("totem", 8, new PriceRule.Custom(List.of(), 1, 4));
        assertEquals(Status.INVALID_AMOUNT, _trades.buyCustom(_shop, _alex, totem, 3).status());
    }

    // Selling ========================================================================================================

    @Test
    void sellingPaysAndPutsTheItemsInTheShop()
    {
        _inventory.put(10, "stone", 64, ItemCategory.BLOCKS);

        TradeResult result = _trades.sell(_shop, _alex, item("stone"), 64);

        assertTrue(result.isSuccess());
        assertEquals(64, balance(_alex));
        assertEquals(0, _inventory.count(item("stone")));
        assertEquals(64, _shop.findSameKind(item("stone")).getAmount());
        assertEquals(Trade.Type.SELL, _log.get(0).type());
    }

    @Test
    void cantSellMoreThanTheCustomerHas()
    {
        _inventory.put(10, "stone", 3, ItemCategory.BLOCKS);
        assertEquals(Status.NOT_ENOUGH_ITEMS, _trades.sell(_shop, _alex, item("stone"), 4).status());
        assertEquals(0, balance(_alex));
    }

    @Test
    void aShopThatOnlySellsBuysNothing()
    {
        _shop.setSettings(new ShopSettings(5, 1, 0.1, 60, false, true, true, true));
        _inventory.put(10, "stone", 3, ItemCategory.BLOCKS);
        assertEquals(Status.SHOP_DOESNT_BUY, _trades.sell(_shop, _alex, item("stone"), 1).status());
    }

    @Test
    void aFailedDepositKeepsTheItems()
    {
        _inventory.put(10, "stone", 3, ItemCategory.BLOCKS);
        _bank.failPayments = true;
        assertEquals(Status.PAYMENT_FAILED, _trades.sell(_shop, _alex, item("stone"), 3).status());
        assertEquals(3, _inventory.count(item("stone")));
        assertNull(_shop.findSameKind(item("stone")));
    }

    @Test
    void sellingAWaterBucketGivesTheBucketBack()
    {
        _rules.leftovers.put(item("water_bucket"), item("bucket"));
        _inventory.put(10, "water_bucket", 1, ItemCategory.BLOCKS);

        _trades.sell(_shop, _alex, item("water_bucket"), 1);
        assertEquals(1, _inventory.count(item("bucket")));
    }

    @Test
    void aWorthlessFilledBucketGivesNothingBack()
    {
        _rules.value("water_bucket", 0);
        _rules.leftovers.put(item("water_bucket"), item("bucket"));
        _inventory.put(10, "water_bucket", 1, ItemCategory.BLOCKS);

        _trades.sell(_shop, _alex, item("water_bucket"), 1);
        assertEquals(0, _inventory.count(item("bucket")));
    }

    @Test
    void soldUniqueItemsAreListedWithTheirUniquePrice()
    {
        ItemKey sword = item("sword");
        _rules.uniques.put(sword, 100.0);
        _inventory.put(10, "sword", 1, ItemCategory.GEAR);

        _trades.sell(_shop, _alex, sword, 1);
        assertEquals(new PriceRule.Unique(100), _shop.findSameKind(sword).getPrice());
    }

    @Test
    void sellingAShulkerSellsItsContentsAndKeepsTheBox()
    {
        _inventory.putShulker(15, "box", Map.of(item("stone"), 64, item("diamond"), 2));

        TradeResult result = _trades.sellShulker(_shop, _alex, 15);

        assertTrue(result.isSuccess());
        assertEquals(64 + 20, balance(_alex));
        assertEquals(1, _inventory.count(item("box")));
        assertTrue(_inventory.shulkerContents(15).isEmpty());
        assertEquals(64, _shop.findSameKind(item("stone")).getAmount());
        assertEquals(2, _shop.findSameKind(item("diamond")).getAmount());
        assertEquals(2, _log.size());
    }

    @Test
    void anEmptyShulkerCantBeSold()
    {
        _inventory.putShulker(15, "box", Map.of());
        assertEquals(Status.NOTHING_TO_SELL, _trades.sellShulker(_shop, _alex, 15).status());
    }

    // Selling for nothing ============================================================================================

    private TradeService noZeroSells()
    {
        return new TradeService(new PriceCalculator(_rules), _bank, () -> false);
    }

    @Test
    void worthlessItemsSellForNothingByDefault()
    {
        _inventory.put(10, "junk", 5, ItemCategory.BLOCKS);

        TradeResult result = _trades.sell(_shop, _alex, item("junk"), 5);

        assertTrue(result.isSuccess());
        assertEquals(0, result.price());
        assertEquals(0, _inventory.count(item("junk")));
    }

    @Test
    void withZeroSellsOffWorthlessItemsStay()
    {
        _inventory.put(10, "junk", 5, ItemCategory.BLOCKS);
        assertEquals(Status.ZERO_PRICE, noZeroSells().sell(_shop, _alex, item("junk"), 5).status());
        assertEquals(5, _inventory.count(item("junk")));
        assertNull(_shop.findSameKind(item("junk")));
    }

    @Test
    void aWorthlessShulkerEmptiesByDefault()
    {
        _inventory.putShulker(15, "box", Map.of(item("junk"), 64));
        assertTrue(_trades.sellShulker(_shop, _alex, 15).isSuccess());
        assertTrue(_inventory.shulkerContents(15).isEmpty());
    }

    @Test
    void withZeroSellsOffTheShulkerKeepsWhatTheShopDoesntBuy()
    {
        _inventory.putShulker(15, "box", Map.of(item("junk"), 64, item("diamond"), 2));

        TradeResult result = noZeroSells().sellShulker(_shop, _alex, 15);

        assertTrue(result.isSuccess());
        assertEquals(20, balance(_alex));
        assertEquals(Map.of(item("junk"), 64), _inventory.shulkerContents(15));
        assertNull(_shop.findSameKind(item("junk")));
    }

    @Test
    void withZeroSellsOffAWorthlessShulkerCantBeSold()
    {
        _inventory.putShulker(15, "box", Map.of(item("junk"), 64));
        assertEquals(Status.NOTHING_TO_SELL, noZeroSells().sellShulker(_shop, _alex, 15).status());
        assertEquals(64, _inventory.shulkerContents(15).get(item("junk")));
    }
}
