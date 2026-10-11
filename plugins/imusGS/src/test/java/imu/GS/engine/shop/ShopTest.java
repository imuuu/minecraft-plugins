package imu.GS.engine.shop;

import imu.GS.engine.pricing.PriceRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static imu.GS.engine.Fakes.item;
import static org.junit.jupiter.api.Assertions.*;

class ShopTest
{
    private Shop _shop;
    private final List<String> _events = new ArrayList<>();

    @BeforeEach
    void setUp()
    {
        _shop = new Shop(UUID.randomUUID(), "Test", "Test", ShopSettings.DEFAULT);
        _shop.addListener(new ShopListener()
        {
            @Override
            public void listingChanged(Shop shop, Listing listing)
            {
                _events.add("changed " + listing.getItem().id());
            }

            @Override
            public void layoutChanged(Shop shop)
            {
                _events.add("layout");
            }
        });
    }

    private Listing listing(String item, int amount)
    {
        return new Listing(UUID.randomUUID(), item(item), amount, PriceRule.MATERIAL);
    }

    private Listing stockable(String item, int amount, StockRules rules)
    {
        Listing listing = listing(item, amount);
        listing.setStock(rules);
        return listing;
    }

    @Test
    void receiveAddsToTheSameKindListing()
    {
        Listing stone = listing("stone", 10);
        _shop.addListing(stone);
        _events.clear();

        assertSame(stone, _shop.receive(item("stone"), 5, PriceRule.MATERIAL));
        assertEquals(15, stone.getAmount());
        assertEquals(List.of("changed stone"), _events);
    }

    @Test
    void receiveMakesANewListingInTheFirstFreeSlot()
    {
        _shop.placeListing(listing("stone", 1), 0, 0);
        _shop.placeListing(listing("dirt", 1), 0, 2);

        Listing sand = _shop.receive(item("sand"), 3, PriceRule.MATERIAL);
        assertArrayEquals(new int[] { 0, 1 }, _shop.positionOf(sand));
    }

    @Test
    void aFullShopGetsANewPage()
    {
        for (int i = 0; i < Shop.PAGE_SIZE; i++)
            _shop.addListing(listing("item" + i, 1));
        Listing extra = _shop.receive(item("extra"), 1, PriceRule.MATERIAL);
        assertEquals(2, _shop.getPageCount());
        assertArrayEquals(new int[] { 1, 0 }, _shop.positionOf(extra));
    }

    @Test
    void compactPacksNormalListingsAroundStockables()
    {
        Listing stock = stockable("bread", 10, new StockRules(10, 0, 0));
        _shop.placeListing(stock, 0, 0);
        _shop.placeListing(listing("stone", 5), 0, 3);
        _shop.placeListing(listing("dirt", 0), 0, 5);
        _shop.placeListing(listing("sand", 2), 0, 7);

        _shop.compact(true);

        assertSame(stock, _shop.getListing(0, 0));
        assertEquals("stone", _shop.getListing(0, 1).getItem().id());
        assertEquals("sand", _shop.getListing(0, 2).getItem().id());
        assertNull(_shop.getListing(0, 3));
    }

    @Test
    void compactKeepsSoldOutListingsWhenAsked()
    {
        _shop.placeListing(listing("dirt", 0), 0, 4);
        _shop.compact(false);
        assertEquals("dirt", _shop.getListing(0, 0).getItem().id());
    }

    @Test
    void compactRemovesEmptyPagesAtTheEnd()
    {
        _shop.placeListing(listing("stone", 1), 0, 0);
        _shop.placeListing(listing("dirt", 0), 2, 0);
        _shop.compact(true);
        assertEquals(1, _shop.getPageCount());
    }

    @Test
    void anEmptyMiddlePageDoesntRemoveTheLastPage()
    {
        _shop.placeListing(listing("stone", 1), 0, 0);
        _shop.placeListing(listing("dirt", 1), 2, 0);
        _shop.compact(true);
        assertEquals(3, _shop.getPageCount());
        assertEquals("dirt", _shop.getListing(2, 0).getItem().id());
    }

    @Test
    void listingsDontMoveWhileCustomersHaveTheShopOpen()
    {
        Listing dirt = listing("dirt", 10);
        _shop.placeListing(listing("stone", 1), 0, 0);
        _shop.placeListing(dirt, 0, 1);
        _shop.addViewer();

        _shop.take(_shop.getListing(0, 0), 1);
        _shop.expire();
        assertNotNull(_shop.getListing(0, 0));

        _shop.removeViewer();
        assertSame(dirt, _shop.getListing(0, 0));
    }

    @Test
    void expireTakesItsShareOfNormalListingsOnly()
    {
        Listing stone = listing("stone", 15);
        Listing bread = stockable("bread", 10, new StockRules(10, 1, 5));
        _shop.addListing(stone);
        _shop.addListing(bread);

        _shop.expire(); // 10% of 15 = 1.5, rounded down keeps 13
        assertEquals(13, stone.getAmount());
        assertEquals(10, bread.getAmount());
    }

    @Test
    void tickExpiresOnlyAfterTheCooldown()
    {
        Listing stone = listing("stone", 100);
        _shop.addListing(stone);
        _shop.tick(0); // starts the clock
        _shop.tick(59 * 60_000L);
        assertEquals(100, stone.getAmount());
        _shop.tick(60 * 60_000L);
        assertEquals(90, stone.getAmount());
    }

    @Test
    void stockableRefillsUpToItsMax()
    {
        Listing bread = stockable("bread", 0, new StockRules(10, 4, 1));
        _shop.addListing(bread);
        _shop.tick(1); // first delay starts
        _shop.tick(60_001);
        assertEquals(4, bread.getAmount());
        _shop.tick(120_001);
        _shop.tick(180_001);
        assertEquals(10, bread.getAmount());
    }

    @Test
    void unlimitedStockableNeverRunsOut()
    {
        Listing bread = stockable("bread", 10, new StockRules(10, 0, 0));
        _shop.addListing(bread);
        _shop.take(bread, 10);
        assertEquals(10, bread.getAmount());
    }

    @Test
    void stockableTakesSoldItemsOnlyUpToItsMax()
    {
        Listing bread = stockable("bread", 8, new StockRules(10, 1, 5));
        _shop.addListing(bread);
        _shop.receive(item("bread"), 5, PriceRule.MATERIAL);
        assertEquals(10, bread.getAmount());
    }

    @Test
    void takeNeverGoesBelowZero()
    {
        Listing stone = listing("stone", 2);
        _shop.addListing(stone);
        _shop.take(stone, 5);
        assertEquals(0, stone.getAmount());
    }
}
