package imu.GS.engine.pricing;

import imu.GS.engine.Fakes;
import imu.GS.engine.ItemKey;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Shop;
import imu.GS.engine.shop.ShopSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static imu.GS.engine.Fakes.item;
import static org.junit.jupiter.api.Assertions.*;

class PricingTest
{
    private Fakes.Rules _rules;
    private PriceCalculator _prices;
    private Shop _shop;

    @BeforeEach
    void setUp()
    {
        _rules = new Fakes.Rules().value("diamond", 10).value("stone", 0.5);
        _prices = new PriceCalculator(_rules);
        // sell x5, buy x1 like a new shop
        _shop = new Shop(UUID.randomUUID(), "Test", "&6Test", ShopSettings.DEFAULT);
    }

    private Listing listing(String item, int amount, PriceRule price)
    {
        Listing listing = new Listing(UUID.randomUUID(), item(item), amount, price);
        _shop.addListing(listing);
        return listing;
    }

    // Overflow =======================================================================================================

    @Test
    void overflowKeepsFullPriceBelowSoftCap()
    {
        Overflow overflow = new Overflow(10, 0.5, 0);
        assertEquals(30, overflow.totalPrice(10, 0, 3));
        assertEquals(10, overflow.unitPrice(10, 9));
    }

    @Test
    void overflowDropsEveryUnitOverSoftCap()
    {
        Overflow overflow = new Overflow(2, 0.5, 0);
        // stock 0,1 full; stock 2 -> x1, stock 3 -> x0.5, stock 4 -> x0.25
        assertEquals(10 + 10 + 10 + 5 + 2.5, overflow.totalPrice(10, 0, 5), 1e-9);
    }

    @Test
    void overflowNeverGoesUnderMinPrice()
    {
        Overflow overflow = new Overflow(0, 0.1, 2);
        assertEquals(2, overflow.unitPrice(10, 5));
    }

    @Test
    void overflowSellingInOneGoOrOneByOnePaysTheSame()
    {
        Overflow overflow = new Overflow(5, 0.9, 0.1);
        double oneGo = overflow.totalPrice(4, 3, 20);
        double oneByOne = 0;
        for (int i = 0; i < 20; i++)
            oneByOne += overflow.totalPrice(4, 3 + i, 1);
        assertEquals(oneGo, oneByOne, 1e-9);
    }

    // Buying =========================================================================================================

    @Test
    void materialListingCostsValueTimesSellMultiplier()
    {
        Listing diamonds = listing("diamond", 64, PriceRule.MATERIAL);
        assertEquals(50, _prices.buyUnitPrice(_shop, diamonds));
        assertEquals(400, _prices.buyPrice(_shop, diamonds, 8));
    }

    @Test
    void aFixedPriceUnderWhatTheShopPaysIsAProfit()
    {
        // The shop pays 10 for a diamond
        assertEquals(7, _prices.buyBackProfit(_shop, listing("diamond", 1, new PriceRule.Fixed(3))));
        assertEquals(0, _prices.buyBackProfit(_shop, listing("diamond", 1, new PriceRule.Fixed(12))));
        assertEquals(0, _prices.buyBackProfit(_shop, listing("diamond", 1, PriceRule.MATERIAL)));
    }

    @Test
    void aBuyMultiplierOverTheSellMultiplierIsAProfit()
    {
        _shop.setSettings(new ShopSettings(1, 2, 0.1, 60, false, false, true));
        assertEquals(10, _prices.buyBackProfit(_shop, listing("diamond", 1, PriceRule.MATERIAL)));
    }

    @Test
    void fixedPriceIgnoresMultipliers()
    {
        Listing diamonds = listing("diamond", 64, new PriceRule.Fixed(3));
        assertEquals(24, _prices.buyPrice(_shop, diamonds, 8));
    }

    @Test
    void uniquePriceUsesSellMultiplier()
    {
        Listing sword = listing("sword", 1, new PriceRule.Unique(100));
        assertEquals(500, _prices.buyPrice(_shop, sword, 1));
    }

    @Test
    void customCostScalesPerBundle()
    {
        PriceRule.Custom custom = new PriceRule.Custom(List.of(new PriceRule.ItemCost(item("emerald"), 3)), 10, 4);
        Listing listing = listing("totem", 16, custom);
        PriceCalculator.CustomCost cost = _prices.customCost(listing, 8);
        assertEquals(6, cost.items().get(0).amount());
        assertEquals(20, cost.money());
    }

    // Selling ========================================================================================================

    @Test
    void sellingPaysValueTimesBuyMultiplier()
    {
        assertEquals(30, _prices.sellPrice(_shop, item("diamond"), 3));
    }

    @Test
    void overflowCountsTheSameKindListing()
    {
        _rules.overflows.put(item("diamond"), new Overflow(10, 0.5, 0));
        listing("diamond", 10, PriceRule.MATERIAL);
        assertEquals(10 + 5, _prices.sellPrice(_shop, item("diamond"), 2));
    }

    @Test
    void overflowGroupCountsEveryListingOfTheGroup()
    {
        ItemKey sword = item("sword");
        ItemKey damagedSword = item("sword-damaged");
        _rules.value("sword", 20).value("sword-damaged", 20);
        _rules.groups.put(sword, "DIAMOND_SWORD");
        _rules.groups.put(damagedSword, "DIAMOND_SWORD");
        _rules.overflows.put(sword, new Overflow(2, 0.5, 0));
        listing("sword-damaged", 2, PriceRule.MATERIAL);

        assertEquals(2, _prices.overflowStock(_shop, sword));
        assertEquals(20, _prices.sellPrice(_shop, sword, 1));
    }

    @Test
    void uniqueSellPriceUsesConditionAndNeverOverflows()
    {
        ItemKey sword = item("sword");
        _rules.uniques.put(sword, 100.0);
        _rules.conditions.put(sword, 0.5);
        _rules.overflows.put(sword, new Overflow(0, 0.1, 0));
        assertEquals(100, _prices.sellPrice(_shop, sword, 2));
        assertEquals(new PriceRule.Unique(100), _prices.priceForSoldItem(sword));
    }

    @Test
    void shulkerPriceIsTheSumOfItsContents()
    {
        assertEquals(10 * 2 + 0.5 * 64, _prices.shulkerPrice(_shop, Map.of(item("diamond"), 2, item("stone"), 64)));
    }

    @Test
    void pricesAreRoundedToCents()
    {
        _rules.value("dust", 0.333);
        assertEquals(1.0, _prices.sellPrice(_shop, item("dust"), 3));
        assertEquals(0.33, _prices.sellPrice(_shop, item("dust"), 1));
    }
}
