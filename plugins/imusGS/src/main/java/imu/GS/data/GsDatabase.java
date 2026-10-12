package imu.GS.data;

import imu.GS.engine.ItemKey;
import imu.GS.engine.pricing.Overflow;
import imu.GS.engine.pricing.SellLimit;
import imu.GS.engine.pricing.SellPressure;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Shop;
import imu.GS.engine.trade.Trade;
import imu.GS.prices.EnchantPrices;
import imu.GS.prices.GearTier;
import imu.GS.prices.MaterialPrices;
import imu.iAPI.Other.MySQL;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Everything imusGS keeps in MySQL. Reads happen at startup; every write goes through one background thread, in
 * order, so two saves of the same shop never overlap. {@link #close()} waits for the writes still queued.
 *
 * <p>Writes take snapshots made on the main thread ({@link ShopRow}...), never live shops.</p>
 */
public class GsDatabase
{
    private final MySQL _sql;
    private final Logger _logger;
    private final ExecutorService _writer = Executors.newSingleThreadExecutor(r ->
    {
        Thread thread = new Thread(r, "imusGS database");
        thread.setDaemon(true);
        return thread;
    });

    public GsDatabase(MySQL sql, Logger logger)
    {
        _sql = sql;
        _logger = logger;
    }

    // Snapshots ======================================================================================================

    public record ListingRow(UUID id, int page, int slot, String item, int amount, String data) {}

    public record ShopRow(UUID id, String name, String displayName, String settings, List<ListingRow> listings)
    {
        /**
         * Copies the shop. Main thread.
         */
        public static ShopRow of(Shop shop)
        {
            List<ListingRow> listings = new ArrayList<>();
            for (int page = 0; page < shop.getPageCount(); page++)
            {
                for (int slot = 0; slot < Shop.PAGE_SIZE; slot++)
                {
                    Listing listing = shop.getListing(page, slot);
                    if (listing != null)
                        listings.add(new ListingRow(listing.getId(), page, slot, listing.getItem().id(), listing.getAmount(), ListingCodec.toJson(listing)));
                }
            }
            return new ShopRow(shop.getId(), shop.getName(), shop.getDisplayName(), ListingCodec.toJson(shop.getSettings()), listings);
        }
    }

    /**
     * @param limitGroup null when the material isn't in a sell limit group
     */
    public record MaterialRow(String material, double price, Double smartMultiplier, Overflow overflow, Set<String> tags, String limitGroup) {}

    public record EnchantRow(String enchant, EnchantPrices.EnchantValue value) {}

    public record UniqueRow(String item, double price) {}

    // Tables =========================================================================================================

    public void createTables() throws SQLException
    {
        try (Connection con = _sql.GetConnection(); Statement st = con.createStatement())
        {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS gs_shops ("
                    + "id CHAR(36) NOT NULL PRIMARY KEY, "
                    + "name VARCHAR(100) NOT NULL, "
                    + "display_name VARCHAR(200) NOT NULL, "
                    + "settings TEXT NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS gs_listings ("
                    + "id CHAR(36) NOT NULL PRIMARY KEY, "
                    + "shop_id CHAR(36) NOT NULL, "
                    + "page INT NOT NULL, "
                    + "slot INT NOT NULL, "
                    + "item MEDIUMTEXT NOT NULL, "
                    + "amount INT NOT NULL, "
                    + "data TEXT NOT NULL, "
                    + "INDEX (shop_id))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS gs_materials ("
                    + "material VARCHAR(64) NOT NULL PRIMARY KEY, "
                    + "price DOUBLE NOT NULL, "
                    + "smart_multiplier DOUBLE NULL, "
                    + "overflow_softcap INT NULL, "
                    + "overflow_drop DOUBLE NULL, "
                    + "overflow_min DOUBLE NULL, "
                    + "tags VARCHAR(1000) NOT NULL DEFAULT '', "
                    + "limit_group VARCHAR(64) NULL)");
            addColumnIfMissing(con, st, "gs_materials", "limit_group", "VARCHAR(64) NULL");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS gs_sell_limits ("
                    + "grp VARCHAR(64) NOT NULL PRIMARY KEY, "
                    + "allowance DOUBLE NOT NULL, "
                    + "refill_per_hour DOUBLE NOT NULL, "
                    + "min_fraction DOUBLE NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS gs_flash_deals ("
                    + "shop_id CHAR(36) NOT NULL PRIMARY KEY, "
                    + "data MEDIUMTEXT NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS gs_sell_pressure ("
                    + "shop_id CHAR(36) NOT NULL, "
                    + "grp VARCHAR(64) NOT NULL, "
                    + "pressure DOUBLE NOT NULL, "
                    + "updated BIGINT NOT NULL, "
                    + "PRIMARY KEY (shop_id, grp))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS gs_enchants ("
                    + "enchant VARCHAR(100) NOT NULL PRIMARY KEY, "
                    + "max_level INT NOT NULL, "
                    + "min_price DOUBLE NOT NULL, "
                    + "max_price DOUBLE NOT NULL, "
                    + "book_multiplier DOUBLE NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS gs_enchant_settings ("
                    + "name VARCHAR(50) NOT NULL PRIMARY KEY, "
                    + "value DOUBLE NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS gs_uniques ("
                    + "item MEDIUMTEXT NOT NULL, "
                    + "item_hash CHAR(64) NOT NULL PRIMARY KEY, "
                    + "price DOUBLE NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS gs_trades ("
                    + "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                    + "time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "type VARCHAR(4) NOT NULL, "
                    + "shop_id CHAR(36) NOT NULL, "
                    + "shop_name VARCHAR(100) NOT NULL, "
                    + "player_id CHAR(36) NOT NULL, "
                    + "player_name VARCHAR(16) NOT NULL, "
                    + "item MEDIUMTEXT NOT NULL, "
                    + "amount INT NOT NULL, "
                    + "price DOUBLE NOT NULL, "
                    + "INDEX (player_id), INDEX (shop_id))");
            // Item components and names can hold any character, e.g. the flash deal mark or an emoji
            for (String table : List.of("gs_shops", "gs_listings", "gs_materials", "gs_enchants", "gs_enchant_settings",
                    "gs_uniques", "gs_trades", "gs_sell_limits", "gs_flash_deals", "gs_sell_pressure"))
                useUtf8mb4(con, st, table);
        }
    }

    private static void useUtf8mb4(Connection con, Statement st, String table) throws SQLException
    {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT TABLE_COLLATION FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?"))
        {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery())
            {
                if (!rs.next() || (rs.getString(1) != null && rs.getString(1).startsWith("utf8mb4")))
                    return;
            }
        }
        st.executeUpdate("ALTER TABLE " + table + " CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
    }

    private static void addColumnIfMissing(Connection con, Statement st, String table, String column, String definition) throws SQLException
    {
        try (ResultSet rs = con.getMetaData().getColumns(con.getCatalog(), null, table, column))
        {
            if (rs.next())
                return;
        }
        st.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
    }

    /**
     * Deletes logged trades older than this many days. Startup only.
     *
     * @return rows deleted
     */
    public int deleteOldTrades(int days) throws SQLException
    {
        try (Connection con = _sql.GetConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM gs_trades WHERE time < NOW() - INTERVAL ? DAY"))
        {
            ps.setInt(1, days);
            return ps.executeUpdate();
        }
    }

    // Loading, startup only ==========================================================================================

    public List<ShopRow> loadShops() throws SQLException
    {
        List<ShopRow> shops = new ArrayList<>();
        try (Connection con = _sql.GetConnection();
             PreparedStatement shopPs = con.prepareStatement("SELECT id, name, display_name, settings FROM gs_shops");
             PreparedStatement listingPs = con.prepareStatement("SELECT id, page, slot, item, amount, data FROM gs_listings WHERE shop_id=?"))
        {
            try (ResultSet rs = shopPs.executeQuery())
            {
                while (rs.next())
                    shops.add(new ShopRow(UUID.fromString(rs.getString(1)), rs.getString(2), rs.getString(3), rs.getString(4), new ArrayList<>()));
            }
            for (ShopRow shop : shops)
            {
                listingPs.setString(1, shop.id().toString());
                try (ResultSet rs = listingPs.executeQuery())
                {
                    while (rs.next())
                        shop.listings().add(new ListingRow(UUID.fromString(rs.getString(1)), rs.getInt(2), rs.getInt(3), rs.getString(4), rs.getInt(5), rs.getString(6)));
                }
            }
        }
        return shops;
    }

    public List<MaterialRow> loadMaterials() throws SQLException
    {
        List<MaterialRow> rows = new ArrayList<>();
        try (Connection con = _sql.GetConnection();
             PreparedStatement ps = con.prepareStatement("SELECT material, price, smart_multiplier, overflow_softcap, overflow_drop, overflow_min, tags, limit_group FROM gs_materials");
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                Double smart = rs.getObject(3) == null ? null : rs.getDouble(3);
                Overflow overflow = rs.getObject(4) == null ? null : new Overflow(rs.getInt(4), rs.getDouble(5), rs.getDouble(6));
                Set<String> tags = new LinkedHashSet<>();
                for (String tag : rs.getString(7).split(","))
                {
                    if (!tag.isBlank())
                        tags.add(tag.trim());
                }
                rows.add(new MaterialRow(rs.getString(1), rs.getDouble(2), smart, overflow, tags, rs.getString(8)));
            }
        }
        return rows;
    }

    public Map<String, SellLimit> loadSellLimits() throws SQLException
    {
        Map<String, SellLimit> limits = new LinkedHashMap<>();
        try (Connection con = _sql.GetConnection();
             PreparedStatement ps = con.prepareStatement("SELECT grp, allowance, refill_per_hour, min_fraction FROM gs_sell_limits");
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
                limits.put(rs.getString(1), new SellLimit(rs.getDouble(2), rs.getDouble(3), rs.getDouble(4)));
        }
        return limits;
    }

    /**
     * Saved flash deals: shop id -> deal JSON, see {@link FlashDealCodec}.
     */
    public Map<UUID, String> loadFlashDeals() throws SQLException
    {
        Map<UUID, String> deals = new LinkedHashMap<>();
        try (Connection con = _sql.GetConnection();
             PreparedStatement ps = con.prepareStatement("SELECT shop_id, data FROM gs_flash_deals");
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
                deals.put(UUID.fromString(rs.getString(1)), rs.getString(2));
        }
        return deals;
    }

    public List<SellPressure.Entry> loadSellPressure() throws SQLException
    {
        List<SellPressure.Entry> entries = new ArrayList<>();
        try (Connection con = _sql.GetConnection();
             PreparedStatement ps = con.prepareStatement("SELECT shop_id, grp, pressure, updated FROM gs_sell_pressure");
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
                entries.add(new SellPressure.Entry(UUID.fromString(rs.getString(1)), rs.getString(2), rs.getDouble(3), rs.getLong(4)));
        }
        return entries;
    }

    public void loadEnchants(EnchantPrices enchants) throws SQLException
    {
        try (Connection con = _sql.GetConnection())
        {
            try (PreparedStatement ps = con.prepareStatement("SELECT enchant, max_level, min_price, max_price, book_multiplier FROM gs_enchants");
                 ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    NamespacedKey key = NamespacedKey.fromString(rs.getString(1));
                    if (key != null)
                        enchants.getValues().put(key, new EnchantPrices.EnchantValue(rs.getInt(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5)));
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT name, value FROM gs_enchant_settings");
                 ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    String name = rs.getString(1);
                    if (name.equals("enchant_count_percent"))
                        enchants.setEnchantCountPercent(rs.getDouble(2));
                    else if (name.startsWith("gear_"))
                    {
                        try
                        {
                            enchants.setGearMultiplier(GearTier.valueOf(name.substring(5)), rs.getDouble(2));
                        } catch (IllegalArgumentException e)
                        {
                            _logger.warning("Unknown gear tier in gs_enchant_settings: " + name);
                        }
                    }
                }
            }
        }
    }

    public List<UniqueRow> loadUniques() throws SQLException
    {
        List<UniqueRow> rows = new ArrayList<>();
        try (Connection con = _sql.GetConnection();
             PreparedStatement ps = con.prepareStatement("SELECT item, price FROM gs_uniques");
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
                rows.add(new UniqueRow(rs.getString(1), rs.getDouble(2)));
        }
        return rows;
    }

    // Writing ========================================================================================================

    /**
     * Replaces everything stored of the shop, in one transaction.
     */
    /**
     * @param onFailure run on the database thread when the save fails, to try again later
     */
    public void saveShop(ShopRow shop, Runnable onFailure)
    {
        write("save shop " + shop.name(), onFailure, con ->
        {
            try (PreparedStatement deleteListings = con.prepareStatement("DELETE FROM gs_listings WHERE shop_id=?");
                 PreparedStatement upsertShop = con.prepareStatement("REPLACE INTO gs_shops (id, name, display_name, settings) VALUES (?,?,?,?)");
                 PreparedStatement insertListing = con.prepareStatement("INSERT INTO gs_listings (id, shop_id, page, slot, item, amount, data) VALUES (?,?,?,?,?,?,?)"))
            {
                deleteListings.setString(1, shop.id().toString());
                deleteListings.executeUpdate();

                upsertShop.setString(1, shop.id().toString());
                upsertShop.setString(2, shop.name());
                upsertShop.setString(3, shop.displayName());
                upsertShop.setString(4, shop.settings());
                upsertShop.executeUpdate();

                for (ListingRow listing : shop.listings())
                {
                    insertListing.setString(1, listing.id().toString());
                    insertListing.setString(2, shop.id().toString());
                    insertListing.setInt(3, listing.page());
                    insertListing.setInt(4, listing.slot());
                    insertListing.setString(5, listing.item());
                    insertListing.setInt(6, listing.amount());
                    insertListing.setString(7, listing.data());
                    insertListing.addBatch();
                }
                insertListing.executeBatch();
            }
        });
    }

    public void deleteShop(UUID shopId)
    {
        write("delete shop " + shopId, con ->
        {
            try (PreparedStatement listings = con.prepareStatement("DELETE FROM gs_listings WHERE shop_id=?");
                 PreparedStatement shop = con.prepareStatement("DELETE FROM gs_shops WHERE id=?"))
            {
                listings.setString(1, shopId.toString());
                listings.executeUpdate();
                shop.setString(1, shopId.toString());
                shop.executeUpdate();
            }
        });
    }

    public void saveMaterials(List<MaterialRow> rows)
    {
        if (rows.isEmpty())
            return;
        write("save material prices", con ->
        {
            try (PreparedStatement ps = con.prepareStatement("REPLACE INTO gs_materials (material, price, smart_multiplier, overflow_softcap, overflow_drop, overflow_min, tags, limit_group) VALUES (?,?,?,?,?,?,?,?)"))
            {
                for (MaterialRow row : rows)
                {
                    ps.setString(1, row.material());
                    ps.setDouble(2, row.price());
                    ps.setObject(3, row.smartMultiplier());
                    ps.setObject(4, row.overflow() == null ? null : row.overflow().softCap());
                    ps.setObject(5, row.overflow() == null ? null : row.overflow().dropFactor());
                    ps.setObject(6, row.overflow() == null ? null : row.overflow().minPrice());
                    ps.setString(7, String.join(",", row.tags()));
                    ps.setString(8, row.limitGroup());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        });
    }

    public static MaterialRow materialRow(Material material, MaterialPrices.Entry entry)
    {
        return new MaterialRow(material.name(), entry.getPrice(), entry.getSmartMultiplier(), entry.getOverflow(), new LinkedHashSet<>(entry.getTags()), entry.getLimitGroup());
    }

    /**
     * Replaces every sell limit with these.
     */
    public void saveSellLimits(Map<String, SellLimit> limits)
    {
        Map<String, SellLimit> copy = new LinkedHashMap<>(limits);
        write("save sell limits", con ->
        {
            try (Statement st = con.createStatement())
            {
                st.executeUpdate("DELETE FROM gs_sell_limits");
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO gs_sell_limits (grp, allowance, refill_per_hour, min_fraction) VALUES (?,?,?,?)"))
            {
                for (Map.Entry<String, SellLimit> entry : copy.entrySet())
                {
                    ps.setString(1, entry.getKey());
                    ps.setDouble(2, entry.getValue().allowance());
                    ps.setDouble(3, entry.getValue().refillPerHour());
                    ps.setDouble(4, entry.getValue().minFraction());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        });
    }

    /**
     * Replaces every saved flash deal with these: shop id -> deal JSON.
     */
    public void saveFlashDeals(Map<UUID, String> deals, Runnable onFailure)
    {
        Map<UUID, String> copy = new LinkedHashMap<>(deals);
        write("save flash deals", onFailure, con ->
        {
            try (Statement st = con.createStatement())
            {
                st.executeUpdate("DELETE FROM gs_flash_deals");
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO gs_flash_deals (shop_id, data) VALUES (?,?)"))
            {
                for (Map.Entry<UUID, String> entry : copy.entrySet())
                {
                    ps.setString(1, entry.getKey().toString());
                    ps.setString(2, entry.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        });
    }

    /**
     * Replaces the saved sell pressure with this snapshot.
     */
    public void saveSellPressure(List<SellPressure.Entry> entries, Runnable onFailure)
    {
        write("save sell pressure", onFailure, con ->
        {
            try (Statement st = con.createStatement())
            {
                st.executeUpdate("DELETE FROM gs_sell_pressure");
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO gs_sell_pressure (shop_id, grp, pressure, updated) VALUES (?,?,?,?)"))
            {
                for (SellPressure.Entry entry : entries)
                {
                    ps.setString(1, entry.owner().toString());
                    ps.setString(2, truncate(entry.group(), 64));
                    ps.setDouble(3, entry.pressure());
                    ps.setLong(4, entry.updatedMillis());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        });
    }

    public void saveEnchants(List<EnchantRow> rows)
    {
        write("save enchant prices", con ->
        {
            try (PreparedStatement ps = con.prepareStatement("REPLACE INTO gs_enchants (enchant, max_level, min_price, max_price, book_multiplier) VALUES (?,?,?,?,?)"))
            {
                for (EnchantRow row : rows)
                {
                    ps.setString(1, row.enchant());
                    ps.setInt(2, row.value().maxLevel());
                    ps.setDouble(3, row.value().minPrice());
                    ps.setDouble(4, row.value().maxPrice());
                    ps.setDouble(5, row.value().bookMultiplier());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        });
    }

    /**
     * @param name gear_DIAMOND... or enchant_count_percent
     */
    public void saveEnchantSetting(String name, double value)
    {
        write("save enchant setting " + name, con ->
        {
            try (PreparedStatement ps = con.prepareStatement("REPLACE INTO gs_enchant_settings (name, value) VALUES (?,?)"))
            {
                ps.setString(1, name);
                ps.setDouble(2, value);
                ps.executeUpdate();
            }
        });
    }

    public void saveUnique(ItemKey item, double price)
    {
        write("save unique", con ->
        {
            try (PreparedStatement ps = con.prepareStatement("REPLACE INTO gs_uniques (item, item_hash, price) VALUES (?,?,?)"))
            {
                ps.setString(1, item.id());
                ps.setString(2, hash(item.id()));
                ps.setDouble(3, price);
                ps.executeUpdate();
            }
        });
    }

    public void deleteUnique(ItemKey item)
    {
        write("delete unique", con ->
        {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM gs_uniques WHERE item_hash=?"))
            {
                ps.setString(1, hash(item.id()));
                ps.executeUpdate();
            }
        });
    }

    public void logTrade(Trade trade, String shopName)
    {
        write("log trade", con ->
        {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO gs_trades (type, shop_id, shop_name, player_id, player_name, item, amount, price) VALUES (?,?,?,?,?,?,?,?)"))
            {
                ps.setString(1, trade.type().name());
                ps.setString(2, trade.shopId().toString());
                ps.setString(3, truncate(shopName, 100));
                ps.setString(4, trade.customerId().toString());
                ps.setString(5, truncate(trade.customerName(), 16));
                ps.setString(6, trade.item().id());
                ps.setInt(7, trade.amount());
                ps.setDouble(8, trade.price());
                ps.executeUpdate();
            }
        });
    }

    // Writer =========================================================================================================

    private interface Write
    {
        void run(Connection con) throws SQLException;
    }

    private void write(String what, Write write)
    {
        write(what, () -> {}, write);
    }

    private void write(String what, Runnable onFailure, Write write)
    {
        if (_writer.isShutdown())
        {
            _logger.severe("Database is closed, couldn't " + what);
            return;
        }
        _writer.execute(() ->
        {
            try (Connection con = _sql.GetConnection())
            {
                con.setAutoCommit(false);
                try
                {
                    write.run(con);
                    con.commit();
                } catch (SQLException e)
                {
                    con.rollback();
                    throw e;
                }
            } catch (Exception e)
            {
                _logger.log(Level.SEVERE, "Couldn't " + what + ", the database keeps its previous state", e);
                onFailure.run();
            }
        });
    }

    /**
     * Waits for every queued write. Call it last in onDisable.
     */
    public void close()
    {
        _writer.shutdown();
        try
        {
            if (!_writer.awaitTermination(30, TimeUnit.SECONDS))
                _logger.severe("Database writes didn't finish in 30 seconds");
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    private static String truncate(String text, int max)
    {
        return text == null || text.length() <= max ? text : text.substring(0, max);
    }

    private static String hash(String text)
    {
        try
        {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest)
                hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e)
        {
            throw new IllegalStateException(e);
        }
    }

    static List<String> split(String text)
    {
        return text == null || text.isBlank() ? List.of() : Arrays.asList(text.split(","));
    }
}
