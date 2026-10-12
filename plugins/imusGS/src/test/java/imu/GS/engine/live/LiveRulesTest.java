package imu.GS.engine.live;

import imu.GS.engine.pricing.PriceRule;
import imu.GS.engine.shop.Shop;
import imu.GS.engine.shop.ShopSettings;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static imu.GS.engine.Fakes.item;
import static org.junit.jupiter.api.Assertions.*;

class LiveRulesTest
{
    @Test
    void rarerItemsAreFoundLessOften()
    {
        double cobble = LiveRules.foundChance(0.5, 0.006, 5);
        double iron = LiveRules.foundChance(80, 0.006, 5);
        double diamond = LiveRules.foundChance(1000, 0.006, 5);
        assertTrue(cobble > iron && iron > diamond);
        assertEquals(0.006 / Math.sqrt(1 + 1000 / 5.0), diamond, 1e-12);
    }

    @Test
    void aRestockIsAboutTheBudget()
    {
        assertEquals(64, LiveRules.restockAmount(0.5, 2000, 64));
        assertEquals(25, LiveRules.restockAmount(80, 2000, 64));
        assertEquals(2, LiveRules.restockAmount(1000, 2000, 64));
        assertEquals(1, LiveRules.restockAmount(30_000, 2000, 64));
    }

    @Test
    void restockNeverGoesOverTheCap()
    {
        Shop shop = new Shop(UUID.randomUUID(), "Test", "Test", ShopSettings.DEFAULT);
        assertEquals(40, LiveRules.restock(shop, item("stone"), 40, 64, PriceRule.MATERIAL));
        assertEquals(24, LiveRules.restock(shop, item("stone"), 40, 64, PriceRule.MATERIAL));
        assertEquals(0, LiveRules.restock(shop, item("stone"), 1, 64, PriceRule.MATERIAL));
        assertEquals(64, shop.findSameKind(item("stone")).getAmount());
    }

    @Test
    void aFlashPriceIsDiscounted()
    {
        // Normally 50, the shop pays 10 back: 40% off
        assertEquals(30, LiveRules.flashPrice(50, 10, 0.4));
    }

    @Test
    void aFlashPriceNeverPaysToSellBack()
    {
        // 90% off would be 5, under what the shop pays back
        assertEquals(11, LiveRules.flashPrice(50, 10, 0.9));
    }
}
