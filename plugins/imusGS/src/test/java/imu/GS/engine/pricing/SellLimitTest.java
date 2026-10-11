package imu.GS.engine.pricing;

import imu.GS.engine.Fakes;
import imu.GS.engine.ItemCategory;
import imu.GS.engine.ItemKey;
import imu.GS.engine.customer.Customer;
import imu.GS.engine.shop.Shop;
import imu.GS.engine.shop.ShopSettings;
import imu.GS.engine.trade.TradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static imu.GS.engine.Fakes.item;
import static org.junit.jupiter.api.Assertions.*;

class SellLimitTest
{
    private static final long HOUR = 3_600_000L;

    private Fakes.Rules _rules;
    private PriceCalculator _prices;
    private TradeService _trades;
    private Shop _shop;
    private long _now = 1_000_000;

    @BeforeEach
    void setUp()
    {
        _rules = new Fakes.Rules().value("wheat", 10).value("hay_block", 90).value("diamond", 1000);
        _rules.limitGroups.put(item("wheat"), "crops");
        _rules.limitGroups.put(item("hay_block"), "crops");
        // 1000 worth at the full price, 500 back every hour, never under 20%
        _rules.limits.put("crops", new SellLimit(1000, 500, 0.2));
        _prices = new PriceCalculator(_rules, new SellPressure(), () -> _now);
        _trades = new TradeService(_prices, new Fakes.Bank());
        _shop = new Shop(UUID.randomUUID(), "Test", "Test", ShopSettings.DEFAULT);
    }

    private Customer customer(String name, String item, int amount)
    {
        Fakes.Inventory inventory = new Fakes.Inventory().put(10, item, amount, ItemCategory.BLOCKS);
        return Fakes.customer(name, inventory, new Fakes.Context());
    }

    @Test
    void theMultiplierFallsFromFullToTheMinimumOverOneAllowance()
    {
        SellLimit limit = new SellLimit(1000, 500, 0.2);
        assertEquals(1.0, limit.multiplier(1000));
        assertEquals(0.6, limit.multiplier(1500), 1e-9);
        assertEquals(0.2, limit.multiplier(2000), 1e-9);
        assertEquals(0.2, limit.multiplier(5000), 1e-9);
    }

    @Test
    void theAllowanceSellsAtTheFullPrice()
    {
        // 100 wheat x 10 = exactly the allowance
        assertEquals(1000, _prices.sellPrice(_shop, item("wheat"), 100));
    }

    @Test
    void pastTheAllowanceThePriceDrops()
    {
        double price = _prices.sellPrice(_shop, item("wheat"), 200);
        assertTrue(price < 2000, "200 wheat should sell under the full 2000, got " + price);
        assertTrue(price > 1000 + 100 * 10 * 0.2, "but over the minimum, got " + price);
    }

    @Test
    void sellersShareTheShopsLimit()
    {
        Customer alex = customer("Alex", "wheat", 100);
        Customer steve = customer("Steve", "wheat", 100);

        assertEquals(1000, _trades.sell(_shop, alex, item("wheat"), 100).price());
        double steveGot = _trades.sell(_shop, steve, item("wheat"), 100).price();

        assertTrue(steveGot < 1000, "Steve sells after Alex filled the shop's limit, got " + steveGot);
    }

    @Test
    void anotherShopHasItsOwnLimit()
    {
        _trades.sell(_shop, customer("Alex", "wheat", 100), item("wheat"), 100);
        Shop other = new Shop(UUID.randomUUID(), "Other", "Other", ShopSettings.DEFAULT);
        assertEquals(1000, _prices.sellPrice(other, item("wheat"), 100));
    }

    @Test
    void theLimitRefillsOverTime()
    {
        _trades.sell(_shop, customer("Alex", "wheat", 100), item("wheat"), 100);
        assertTrue(_prices.sellPrice(_shop, item("wheat"), 50) < 500);

        _now += HOUR; // 500 back
        assertEquals(500, _prices.sellPrice(_shop, item("wheat"), 50));
    }

    @Test
    void craftingDoesntGetAroundIt()
    {
        // 10 hay bales are worth 900, most of the limit: only 11 more wheat at the full price
        _trades.sell(_shop, customer("Alex", "hay_block", 10), item("hay_block"), 10);
        assertEquals(11, _prices.limitStatus(_shop, item("wheat")).unitsLeft());
        assertTrue(_prices.sellPrice(_shop, item("wheat"), 100) < 1000);
    }

    @Test
    void itemsWithoutAGroupAreNotLimited()
    {
        assertEquals(100_000, _prices.sellPrice(_shop, item("diamond"), 100));
        assertEquals(-1, _prices.limitStatus(_shop, item("diamond")).unitsLeft());
    }

    @Test
    void theStatusTellsHowManyCanStillBeSold()
    {
        assertEquals(101, _prices.limitStatus(_shop, item("wheat")).unitsLeft());
        _trades.sell(_shop, customer("Alex", "wheat", 60), item("wheat"), 60);
        assertEquals(41, _prices.limitStatus(_shop, item("wheat")).unitsLeft());
        _trades.sell(_shop, customer("Steve", "wheat", 60), item("wheat"), 60);
        assertEquals(0, _prices.limitStatus(_shop, item("wheat")).unitsLeft());
        assertTrue(_prices.limitStatus(_shop, item("wheat")).multiplier() < 1);
    }

    @Test
    void aShulkerCountsItsItemsOneAfterAnother()
    {
        Map<ItemKey, Integer> contents = Map.of(item("wheat"), 100, item("hay_block"), 10);
        double whole = _prices.shulkerPrice(_shop, contents);
        assertTrue(whole < 1000 + 900, "The hay comes after the wheat used up the limit, got " + whole);
    }
}
