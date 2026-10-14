package imu.waystones;

import imu.iAPI.Database.Database;
import imu.iAPI.Utilities.ItemUtils;
import imu.waystones.engine.Panel;
import imu.waystones.engine.UpgradeKind;
import imu.waystones.engine.Visibility;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * The waystones in the database, in the old imusWaystones' tables: waystones, waystone_owners, upgrades and
 * discovered. Every write goes through one thread, so they happen in order.
 */
final class WaystoneDb
{
    /** Everything in the database */
    record Loaded(List<Waystone> waystones, Map<UUID, Set<UUID>> discovered)
    {
    }

    /** A waystone's row, copied on the main thread */
    private record Row(UUID id, String name, String world, int x, int y, int z, String icon, String visibility,
                       boolean maxedOut, boolean unbreakable, boolean enabled, UUID owner, String ownerName)
    {
    }

    private final ImusWaystones _plugin;
    private final Database _db;
    private boolean _copyTried;
    private final ExecutorService _thread = Executors.newSingleThreadExecutor(r -> new Thread(r, "imusWaystones-db"));

    WaystoneDb(ImusWaystones plugin, Database db)
    {
        _plugin = plugin;
        _db = db;
    }

    /**
     * Makes the tables right and loads everything; then runs whenLoaded on the main thread.
     */
    void load(Consumer<Loaded> whenLoaded)
    {
        _thread.execute(() ->
        {
            try (Connection con = _db.getConnection())
            {
                setUp(con, _db.isSqlite());
                // Once, after the tables are made and before anything is read
                if (!_copyTried)
                {
                    _copyTried = true;
                    _db.copyFromIfAsked();
                }
                Loaded loaded = read(con);
                Bukkit.getScheduler().runTask(_plugin, () -> whenLoaded.accept(loaded));
            } catch (Exception e)
            {
                _plugin.getLogger().severe("Couldn't load the waystones: " + e.getMessage()
                        + ". Check the database section of imusAPI's config.yml.");
            }
        });
    }

    private static void setUp(Connection con, boolean sqlite) throws SQLException
    {
        try (Statement st = con.createStatement())
        {
            if (sqlite)
            {
                // SQLite has no ENUM and its own auto increment; the rest is the same
                st.executeUpdate("CREATE TABLE IF NOT EXISTS waystones (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid CHAR(36) NOT NULL UNIQUE, "
                        + "name VARCHAR(100), loc_world VARCHAR(64), loc_x INT NOT NULL, loc_y INT NOT NULL, loc_z INT NOT NULL, "
                        + "display_item TEXT, visibility_type VARCHAR(20) DEFAULT 'BY_TOUCH', "
                        + "max_out BOOLEAN DEFAULT FALSE, unbreakable BOOLEAN DEFAULT FALSE, `enable` BOOLEAN DEFAULT TRUE)");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS discovered (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid_player CHAR(36) NOT NULL, "
                        + "uuid_ws CHAR(36) NOT NULL)");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS waystone_owners (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid_player CHAR(36) NOT NULL, "
                        + "player_name VARCHAR(50) NOT NULL, uuid_ws CHAR(36) NOT NULL)");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS upgrades (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid_ws CHAR(36) NOT NULL, "
                        + "uuid_player CHAR(36) NOT NULL, upgrade_name VARCHAR(20) NOT NULL, tier INT NOT NULL)");
                return;
            }

            st.executeUpdate("CREATE TABLE IF NOT EXISTS waystones (id INT AUTO_INCREMENT, uuid CHAR(36) NOT NULL UNIQUE, "
                    + "name VARCHAR(100), loc_world VARCHAR(64), loc_x INT NOT NULL, loc_y INT NOT NULL, loc_z INT NOT NULL, "
                    + "display_item TEXT, visibility_type ENUM('BY_TOUCH','TO_ALL','ONE_WAY','TO_ALL_ONE_WAY') DEFAULT 'BY_TOUCH', "
                    + "max_out BOOLEAN DEFAULT FALSE, unbreakable BOOLEAN DEFAULT FALSE, `enable` BOOLEAN DEFAULT TRUE, PRIMARY KEY(id))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS discovered (id INT NOT NULL AUTO_INCREMENT, uuid_player CHAR(36) NOT NULL, "
                    + "uuid_ws CHAR(36) NOT NULL, PRIMARY KEY(id))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS waystone_owners (id INT NOT NULL AUTO_INCREMENT, uuid_player CHAR(36) NOT NULL, "
                    + "player_name VARCHAR(50) NOT NULL, uuid_ws CHAR(36) NOT NULL, PRIMARY KEY(id))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS upgrades (id INT NOT NULL AUTO_INCREMENT, uuid_ws CHAR(36) NOT NULL, "
                    + "uuid_player CHAR(36) NOT NULL, upgrade_name VARCHAR(20) NOT NULL, tier INT NOT NULL, PRIMARY KEY(id))");

            // Tables made by older versions: the old one never added these columns on a new database
            addColumnIfMissing(con, st, "waystones", "max_out", "BOOLEAN DEFAULT FALSE");
            addColumnIfMissing(con, st, "waystones", "unbreakable", "BOOLEAN DEFAULT FALSE");
            addColumnIfMissing(con, st, "waystones", "enable", "BOOLEAN DEFAULT TRUE");
            st.executeUpdate("ALTER TABLE waystones MODIFY COLUMN visibility_type "
                    + "ENUM('BY_TOUCH','TO_ALL','ONE_WAY','TO_ALL_ONE_WAY') DEFAULT 'BY_TOUCH'");
            st.executeUpdate("ALTER TABLE waystones MODIFY COLUMN loc_world VARCHAR(64)");
            // Names can have any character
            for (String table : List.of("waystones", "waystone_owners"))
                useUtf8mb4(con, st, table);
        }
    }

    private static void addColumnIfMissing(Connection con, Statement st, String table, String column, String definition) throws SQLException
    {
        try (ResultSet rs = con.getMetaData().getColumns(con.getCatalog(), null, table, column))
        {
            if (rs.next())
                return;
        }
        st.executeUpdate("ALTER TABLE " + table + " ADD COLUMN `" + column + "` " + definition);
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

    private Loaded read(Connection con) throws SQLException
    {
        Map<UUID, Waystone> waystones = new LinkedHashMap<>();
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM waystones ORDER BY id"))
        {
            while (rs.next())
            {
                UUID id = uuid(rs.getString("uuid"));
                String world = rs.getString("loc_world");
                if (id == null || world == null)
                    continue;
                Waystone ws = new Waystone(id, world, rs.getInt("loc_x"), rs.getInt("loc_y"), rs.getInt("loc_z"));
                ws.name = rs.getString("name") == null ? "&6Waystone" : rs.getString("name");
                ws.icon = icon(rs.getString("display_item"));
                ws.visibility = Visibility.parse(rs.getString("visibility_type"));
                ws.maxedOut = rs.getBoolean("max_out");
                ws.unbreakable = rs.getBoolean("unbreakable");
                ws.enabled = rs.getBoolean("enable");
                waystones.put(id, ws);
            }
        }
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM waystone_owners ORDER BY id"))
        {
            while (rs.next())
            {
                Waystone ws = waystones.get(uuid(rs.getString("uuid_ws")));
                UUID owner = uuid(rs.getString("uuid_player"));
                if (ws != null && owner != null)
                {
                    ws.owner = owner;
                    ws.ownerName = rs.getString("player_name");
                }
            }
        }
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM upgrades"))
        {
            while (rs.next())
            {
                Waystone ws = waystones.get(uuid(rs.getString("uuid_ws")));
                UUID player = uuid(rs.getString("uuid_player"));
                UpgradeKind kind = UpgradeKind.parse(rs.getString("upgrade_name"));
                if (ws != null && player != null && kind != null)
                    ws.panel(player).set(kind, rs.getInt("tier"));
            }
        }
        Map<UUID, Set<UUID>> discovered = new HashMap<>();
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT uuid_player, uuid_ws FROM discovered"))
        {
            while (rs.next())
            {
                UUID player = uuid(rs.getString(1));
                UUID ws = uuid(rs.getString(2));
                if (player != null && waystones.containsKey(ws))
                    discovered.computeIfAbsent(player, p -> new HashSet<>()).add(ws);
            }
        }
        List<Waystone> list = new ArrayList<>();
        for (Waystone ws : waystones.values())
        {
            // An owner row went missing: the waystone belongs to nobody, but works
            if (ws.owner == null)
            {
                ws.owner = new UUID(0, 0);
                ws.ownerName = "?";
            }
            list.add(ws);
        }
        return new Loaded(list, discovered);
    }

    private static UUID uuid(String text)
    {
        try
        {
            return text == null ? null : UUID.fromString(text);
        } catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    private static ItemStack icon(String encoded)
    {
        try
        {
            ItemStack item = encoded == null ? null : ItemUtils.DecodeItemStack(encoded);
            return item == null ? new ItemStack(Material.BLACKSTONE_WALL) : item;
        } catch (Exception e)
        {
            return new ItemStack(Material.BLACKSTONE_WALL);
        }
    }

    /**
     * Saves the waystone, its owner and everyone's upgrades on it.
     */
    void save(Waystone ws)
    {
        Row row = new Row(ws.id, ws.name, ws.world, ws.x, ws.y, ws.z, ItemUtils.EncodeItemStack(ws.icon), ws.visibility.name(),
                ws.maxedOut, ws.unbreakable, ws.enabled, ws.owner, ws.ownerName);
        Map<UUID, Map<UpgradeKind, Integer>> panels = new HashMap<>();
        ws.panels.forEach((player, panel) -> panels.put(player, panel.tiers()));
        run(con ->
        {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO waystones (uuid, name, loc_world, loc_x, loc_y, loc_z, "
                    + "display_item, visibility_type, max_out, unbreakable, `enable`) VALUES (?,?,?,?,?,?,?,?,?,?,?) " + upsert()))
            {
                int i = 1;
                ps.setString(i++, row.id().toString());
                ps.setString(i++, row.name());
                ps.setString(i++, row.world());
                ps.setInt(i++, row.x());
                ps.setInt(i++, row.y());
                ps.setInt(i++, row.z());
                ps.setString(i++, row.icon());
                ps.setString(i++, row.visibility());
                ps.setBoolean(i++, row.maxedOut());
                ps.setBoolean(i++, row.unbreakable());
                ps.setBoolean(i, row.enabled());
                ps.executeUpdate();
            }
            delete(con, "waystone_owners", row.id());
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO waystone_owners (uuid_player, player_name, uuid_ws) VALUES (?,?,?)"))
            {
                ps.setString(1, row.owner().toString());
                ps.setString(2, row.ownerName() == null ? "?" : row.ownerName());
                ps.setString(3, row.id().toString());
                ps.executeUpdate();
            }
            for (Map.Entry<UUID, Map<UpgradeKind, Integer>> panel : panels.entrySet())
                writePanel(con, row.id(), panel.getKey(), panel.getValue());
        });
    }

    // A waystone saved again keeps its row and id
    private String upsert()
    {
        return _db.isSqlite()
                ? "ON CONFLICT(uuid) DO UPDATE SET name = excluded.name, display_item = excluded.display_item, "
                + "visibility_type = excluded.visibility_type, max_out = excluded.max_out, unbreakable = excluded.unbreakable, "
                + "`enable` = excluded.`enable`"
                : "ON DUPLICATE KEY UPDATE name = VALUES(name), display_item = VALUES(display_item), "
                + "visibility_type = VALUES(visibility_type), max_out = VALUES(max_out), unbreakable = VALUES(unbreakable), "
                + "`enable` = VALUES(`enable`)";
    }

    /**
     * Saves one player's upgrades on the waystone.
     */
    void savePanel(Waystone ws, UUID player)
    {
        UUID id = ws.id;
        Map<UpgradeKind, Integer> tiers = ws.panel(player).tiers();
        run(con -> writePanel(con, id, player, tiers));
    }

    private static void writePanel(Connection con, UUID waystone, UUID player, Map<UpgradeKind, Integer> tiers) throws SQLException
    {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM upgrades WHERE uuid_ws = ? AND uuid_player = ?"))
        {
            ps.setString(1, waystone.toString());
            ps.setString(2, player.toString());
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO upgrades (uuid_ws, uuid_player, upgrade_name, tier) VALUES (?,?,?,?)"))
        {
            for (Map.Entry<UpgradeKind, Integer> tier : tiers.entrySet())
            {
                ps.setString(1, waystone.toString());
                ps.setString(2, player.toString());
                ps.setString(3, tier.getKey().name());
                ps.setInt(4, tier.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    void delete(UUID waystone)
    {
        run(con ->
        {
            delete(con, "waystones", "uuid", waystone);
            delete(con, "waystone_owners", waystone);
            delete(con, "upgrades", waystone);
            delete(con, "discovered", waystone);
        });
    }

    private static void delete(Connection con, String table, UUID waystone) throws SQLException
    {
        delete(con, table, "uuid_ws", waystone);
    }

    private static void delete(Connection con, String table, String column, UUID waystone) throws SQLException
    {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM " + table + " WHERE " + column + " = ?"))
        {
            ps.setString(1, waystone.toString());
            ps.executeUpdate();
        }
    }

    void discover(UUID player, UUID waystone)
    {
        run(con ->
        {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO discovered (uuid_player, uuid_ws) VALUES (?,?)"))
            {
                ps.setString(1, player.toString());
                ps.setString(2, waystone.toString());
                ps.executeUpdate();
            }
        });
    }

    private interface Work
    {
        void run(Connection con) throws SQLException;
    }

    private void run(Work work)
    {
        _thread.execute(() ->
        {
            try (Connection con = _db.getConnection())
            {
                work.run(con);
            } catch (Exception e)
            {
                _plugin.getLogger().warning("Database write failed: " + e.getMessage());
            }
        });
    }

    /**
     * Finishes the writes still waiting, then closes the connections.
     */
    void close()
    {
        _thread.shutdown();
        try
        {
            if (!_thread.awaitTermination(10, TimeUnit.SECONDS))
                _plugin.getLogger().warning("Some waystone saves didn't finish in time");
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
        _db.close();
    }
}
