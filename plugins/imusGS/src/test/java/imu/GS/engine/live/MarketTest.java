package imu.GS.engine.live;

import imu.GS.engine.Fakes;
import imu.GS.engine.ItemKey;
import imu.GS.engine.pricing.Market;
import imu.GS.engine.pricing.PriceCalculator;
import imu.GS.engine.pricing.PriceRule;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Shop;
import imu.GS.engine.shop.ShopSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static imu.GS.engine.Fakes.item;
import static org.junit.jupiter.api.Assertions.*;

class MarketTest
{
    private PriceCalculator _prices;
    private Shop _shop;
    private Shop _other;
    private final Map<ItemKey, Double> _moods = new HashMap<>();
    private final WantedRequests _wanted = new WantedRequests();
    private long _now = 1_000_000;

    @BeforeEach
    void setUp()
    {
        Fakes.Rules rules = new Fakes.Rules().value("pumpkin", 10).value("wheat", 2);
        _prices = new PriceCalculator(rules);
        _prices.setMarket(new Market()
        {
            @Override
            public double moodFactor(ItemKey item)
            {
                return _moods.getOrDefault(item, 1.0);
            }

            @Override
            public double wantedBonus(UUID shopId, ItemKey item)
            {
                return _wanted.bonus(shopId, item, _now);
            }
        });
        // sell x5, buy x1
        _shop = new Shop(UUID.randomUUID(), "Test", "Test", ShopSettings.DEFAULT);
        _other = new Shop(UUID.randomUUID(), "Other", "Other", ShopSettings.DEFAULT);
    }

    private Listing listing(String item, PriceRule price)
    {
        Listing listing = new Listing(UUID.randomUUID(), item(item), 10, price);
        _shop.addListing(listing);
        return listing;
    }

    @Test
    void theMoodMovesBuyingAndSelling()
    {
        _moods.put(item("pumpkin"), 1.1);
        assertEquals(55, _prices.buyPrice(_shop, listing("pumpkin", PriceRule.MATERIAL), 1));
        assertEquals(11, _prices.sellPrice(_shop, item("pumpkin"), 1));
    }

    @Test
    void aWantedItemSellsForMoreInThatShopOnly()
    {
        _wanted.add(new WantedRequests.Wanted(_shop.getId(), item("pumpkin"), 0.5, _now + 1000));
        assertEquals(15, _prices.sellPrice(_shop, item("pumpkin"), 1));
        assertEquals(10, _prices.sellPrice(_other, item("pumpkin"), 1));
        assertEquals(2, _prices.sellPrice(_shop, item("wheat"), 1));
    }

    @Test
    void theBonusEnds()
    {
        _wanted.add(new WantedRequests.Wanted(_shop.getId(), item("pumpkin"), 0.5, _now + 1000));
        _now += 1000;
        assertEquals(10, _prices.sellPrice(_shop, item("pumpkin"), 1));
        assertTrue(_wanted.removeEnded(_now));
        assertTrue(_wanted.all().isEmpty());
    }

    @Test
    void aBonusNeverPaysAsMuchAsTheShopSellsFor()
    {
        _shop.setSettings(new ShopSettings(1.2, 1, 0.1, 60, false, false, true, true));
        _wanted.add(new WantedRequests.Wanted(_shop.getId(), item("pumpkin"), 0.8, _now + 1000));
        // 18 would be over the shop's own price of 12: capped at 90% of it
        assertEquals(10.8, _prices.sellPrice(_shop, item("pumpkin"), 1));
    }

    @Test
    void aCheapFixedListingCapsTheBonusToo()
    {
        Listing cheap = listing("pumpkin", new PriceRule.Fixed(14));
        _wanted.add(new WantedRequests.Wanted(_shop.getId(), item("pumpkin"), 0.8, _now + 1000));
        assertEquals(12.6, _prices.sellPrice(_shop, item("pumpkin"), 1));
        assertEquals(0, _prices.buyBackProfit(_shop, cheap));
    }

    @Test
    void theDailyMoodIsTheSameAllDayAndWithinTheSwing()
    {
        DailyMood mood = new DailyMood(42, 0.1);
        LocalDate day = LocalDate.of(2026, 10, 12);
        assertEquals(mood.factor("ores", day), new DailyMood(42, 0.1).factor("ores", day));

        Set<Double> seen = new HashSet<>();
        for (int i = 0; i < 60; i++)
        {
            double factor = mood.factor("ores", day.plusDays(i));
            assertTrue(factor >= 0.9 && factor <= 1.1, "factor " + factor);
            seen.add(factor);
        }
        assertTrue(seen.size() > 5, "the mood changes from day to day");
        assertNotEquals(mood.factor("ores", day), new DailyMood(43, 0.1).factor("ores", day), "another server, another mood");
    }
}
