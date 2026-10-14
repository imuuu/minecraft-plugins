package imu.GS.data;

import imu.GS.engine.ItemKey;
import imu.GS.engine.trade.Trade;
import imu.iAPI.Database.Database;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * imusGS's tables and queries on SQLite, the default database. The same code runs on MySQL in the bot tests.
 */
class GsDatabaseSqliteTest
{
    private static final Logger LOGGER = Logger.getLogger("test");

    @TempDir
    Path folder;

    private GsDatabase open()
    {
        File file = folder.resolve("data.db").toFile();
        return new GsDatabase(Database.sqlite(file, "imusGS", LOGGER), LOGGER);
    }

    @Test
    void tablesAreMadeAndMadeAgain() throws Exception
    {
        GsDatabase db = open();
        db.createTables();
        db.createTables();
        assertTrue(db.loadShops().isEmpty());
        assertTrue(db.loadMaterials().isEmpty());
        db.close();
    }

    @Test
    void aShopIsSavedAndLoaded() throws Exception
    {
        GsDatabase db = open();
        db.createTables();
        UUID shopId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        db.saveShop(new GsDatabase.ShopRow(shopId, "smith", "&6Smith ⚒", "{}",
                List.of(new GsDatabase.ListingRow(listingId, 0, 4, "minecraft:diamond", 12, "{}"))), () -> fail("save failed"));
        // Closing waits for the writes
        db.close();

        GsDatabase again = open();
        List<GsDatabase.ShopRow> shops = again.loadShops();
        assertEquals(1, shops.size());
        assertEquals("&6Smith ⚒", shops.getFirst().displayName(), "any character");
        assertEquals(12, shops.getFirst().listings().getFirst().amount());
        again.close();
    }

    @Test
    void tradeStatsAndOldTradesUseSqlitesTime() throws Exception
    {
        GsDatabase db = open();
        db.createTables();
        UUID shopId = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        db.logTrade(new Trade(Trade.Type.SELL, shopId, player, "Alex", null, new ItemKey("minecraft:iron_ingot"), 8, 80), "smith");
        db.logTrade(new Trade(Trade.Type.BUY, shopId, player, "Alex", null, new ItemKey("minecraft:diamond"), 1, 50), "smith");
        db.close();

        // A trade from long ago, as an old database would have it
        Database raw = Database.sqlite(folder.resolve("data.db").toFile(), "imusGS", LOGGER);
        try (Connection con = raw.getConnection(); Statement st = con.createStatement())
        {
            st.executeUpdate("INSERT INTO gs_trades (time, type, shop_id, shop_name, player_id, player_name, item, amount, price) "
                    + "VALUES ('2020-01-01 10:00:00', 'SELL', '" + shopId + "', 'smith', '" + player + "', 'Alex', 'minecraft:dirt', 64, 1)");
        }
        raw.close();

        GsDatabase again = open();
        GsDatabase.TradeStats stats = again.loadTradeStats(7, shopId);
        assertEquals(1, stats.sold().trades(), "the old trade is outside the week");
        assertEquals(80, stats.sold().money());
        assertEquals(1, stats.bought().trades());
        assertEquals(1, stats.days().size(), "both today");

        assertEquals(1, again.deleteOldTrades(90));
        assertEquals(1, again.loadTradeStats(10_000, null).sold().trades(), "only the old one went");
        again.close();
    }
}
