package imu.GS.data;

import imu.GS.engine.ItemKey;
import imu.GS.engine.pricing.PriceRule;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Restrictions;
import imu.GS.engine.shop.Shop;
import imu.GS.engine.shop.ShopSettings;
import imu.GS.engine.shop.StockRules;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ListingCodecTest
{
    @Test
    void everythingOfAListingComesBack()
    {
        Shop shop = new Shop(UUID.randomUUID(), "Test", "Test", ShopSettings.DEFAULT);
        PriceRule.Custom price = new PriceRule.Custom(List.of(new PriceRule.ItemCost(new ItemKey("minecraft:emerald[]"), 3)), 12.5, 4);
        Listing listing = new Listing(UUID.randomUUID(), new ItemKey("minecraft:totem_of_undying[]"), 8, price);
        shop.addListing(listing);
        shop.setStock(listing, new StockRules(16, 2, 30));
        shop.setRestrictions(listing, new Restrictions(new Restrictions.SellTime(13000, 1000),
                List.of(new Restrictions.Area("world", 1, 64, -5, 50)), List.of("shop.vip")));
        shop.setTags(listing, Set.of("rare"));

        ListingCodec.ListingData data = ListingCodec.fromJson(ListingCodec.toJson(listing));

        assertEquals(price, data.price());
        assertEquals(listing.getStock(), data.stock());
        assertEquals(listing.getRestrictions(), data.restrictions());
        assertEquals(Set.of("rare"), data.tags());
    }

    @Test
    void simplePricesComeBack()
    {
        for (PriceRule price : List.of(PriceRule.MATERIAL, new PriceRule.Fixed(3.25), new PriceRule.Unique(100)))
        {
            Listing listing = new Listing(UUID.randomUUID(), new ItemKey("minecraft:stone[]"), 1, price);
            ListingCodec.ListingData data = ListingCodec.fromJson(ListingCodec.toJson(listing));
            assertEquals(price, data.price());
            assertNull(data.stock());
            assertEquals(Restrictions.NONE, data.restrictions());
        }
    }

    @Test
    void shopSettingsComeBack()
    {
        ShopSettings settings = new ShopSettings(3.5, 0.8, 0.25, 30, true, true, false);
        assertEquals(settings, ListingCodec.settingsFromJson(ListingCodec.toJson(settings)));
    }
}
