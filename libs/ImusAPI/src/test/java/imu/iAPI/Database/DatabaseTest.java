package imu.iAPI.Database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SQLite databases for real, in a temporary folder. MySQL is tried by the bot tests on the test server.
 */
class DatabaseTest
{
    private static final Logger LOGGER = Logger.getLogger("test");

    @TempDir
    Path folder;

    private Database sqlite(String file)
    {
        return Database.sqlite(folder.resolve(file).toFile(), "test", LOGGER);
    }

    private static void execute(Database db, String... statements) throws SQLException
    {
        try (Connection con = db.getConnection(); Statement st = con.createStatement())
        {
            for (String sql : statements)
                st.executeUpdate(sql);
        }
    }

    private static int count(Database db, String table) throws SQLException
    {
        try (Connection con = db.getConnection(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table))
        {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    private static final String[] SCHEMA = {
            "CREATE TABLE IF NOT EXISTS shops (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, `enable` BOOLEAN DEFAULT TRUE)",
            "CREATE TABLE IF NOT EXISTS trades (id INTEGER PRIMARY KEY AUTOINCREMENT, time TEXT NOT NULL DEFAULT (datetime('now','localtime')), price DOUBLE)"
    };

    @Test
    void aSqliteFileIsMadeWithItsFolder() throws SQLException
    {
        File file = folder.resolve("plugin/data.db").toFile();
        try (Database db = Database.sqlite(file, "test", LOGGER))
        {
            execute(db, SCHEMA);
            execute(db, "INSERT INTO shops (name) VALUES ('Smith')");
            assertEquals(1, count(db, "shops"));
            assertTrue(db.isSqlite());
            assertTrue(db.describe().endsWith("data.db"));
        }
        assertTrue(file.isFile());
    }

    @Test
    void everythingIsCopiedWithItsIds() throws SQLException
    {
        try (Database from = sqlite("from.db"); Database to = sqlite("to.db"))
        {
            execute(from, SCHEMA);
            execute(from, "INSERT INTO shops (id, name, `enable`) VALUES (7, 'Smith', 0), (9, 'Baker', 1)",
                    "INSERT INTO trades (time, price) VALUES ('2026-10-01 12:00:00', 50.5)");
            execute(to, SCHEMA);

            DatabaseCopy.Result result = DatabaseCopy.copy(from, to);
            assertTrue(result.copied());
            assertEquals(2, count(to, "shops"));
            assertTrue(result.report().stream().anyMatch(line -> line.startsWith("Copied 2 rows of shops")), result.report().toString());

            try (Connection con = to.getConnection(); Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, name, `enable` FROM shops ORDER BY id"))
            {
                assertTrue(rs.next());
                assertEquals(7, rs.getInt(1));
                assertFalse(rs.getBoolean(3));
                assertTrue(rs.next());
                assertEquals("Baker", rs.getString(2));
            }
            // New rows continue after the copied ids
            execute(to, "INSERT INTO shops (name) VALUES ('New')");
            try (Connection con = to.getConnection(); Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id FROM shops WHERE name = 'New'"))
            {
                assertTrue(rs.next());
                assertEquals(10, rs.getInt(1));
            }
        }
    }

    @Test
    void nothingIsCopiedIntoADatabaseWithData() throws SQLException
    {
        try (Database from = sqlite("from.db"); Database to = sqlite("to.db"))
        {
            execute(from, SCHEMA);
            execute(from, "INSERT INTO shops (name) VALUES ('Smith')");
            execute(to, SCHEMA);
            execute(to, "INSERT INTO trades (price) VALUES (1)");

            DatabaseCopy.Result result = DatabaseCopy.copy(from, to);
            assertFalse(result.copied());
            assertEquals(0, count(to, "shops"));
            assertTrue(result.report().getFirst().contains("has data already"));
        }
    }

    @Test
    void onlyTablesAndColumnsBothHaveAreCopied() throws SQLException
    {
        try (Database from = sqlite("from.db"); Database to = sqlite("to.db"))
        {
            execute(from, "CREATE TABLE shops (id INTEGER PRIMARY KEY, name TEXT, old_column TEXT)",
                    "CREATE TABLE gone (id INTEGER)",
                    "INSERT INTO shops VALUES (1, 'Smith', 'x')", "INSERT INTO gone VALUES (1)");
            execute(to, "CREATE TABLE shops (id INTEGER PRIMARY KEY, name TEXT, new_column TEXT DEFAULT 'yes')");

            assertTrue(DatabaseCopy.copy(from, to).copied());
            try (Connection con = to.getConnection(); Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT name, new_column FROM shops"))
            {
                assertTrue(rs.next());
                assertEquals("Smith", rs.getString(1));
                assertEquals("yes", rs.getString(2), "a new column keeps its default");
            }
        }
    }

    @DatabaseTable(tableName = "visits")
    static class Visit
    {
        @DatabaseField(generatedId = true)
        int id;
        @DatabaseField
        String player;
        @DatabaseField
        Date time;
    }

    @Test
    void ormliteWorksOnSqliteWithDates() throws Exception
    {
        try (Database db = sqlite("orm.db"))
        {
            TableUtils.createTableIfNotExists(db.connectionSource(), Visit.class);
            Dao<Visit, Integer> dao = DaoManager.createDao(db.connectionSource(), Visit.class);
            Visit visit = new Visit();
            visit.player = "Alex";
            visit.time = new Date(1_791_000_000_000L);
            dao.create(visit);

            Visit read = dao.queryForAll().getFirst();
            assertEquals("Alex", read.player);
            assertEquals(visit.time.getTime() / 1000, read.time.getTime() / 1000, "the time comes back to the second");
        }
    }

    @Test
    void settingsAreRead()
    {
        YamlConfiguration config = new YamlConfiguration();
        assertEquals(DatabaseType.SQLITE, DatabaseSettings.read(null).type(), "SQLite when there is nothing");

        config.set("database.type", "MariaDB");
        config.set("database.mysql.host", "db.example");
        config.set("database.mysql.databases.imusGS", "shops");
        config.set("database.copy-from", "sqlite");
        DatabaseSettings settings = DatabaseSettings.read(config.getConfigurationSection("database"));
        assertEquals(DatabaseType.MYSQL, settings.type());
        assertEquals("db.example", settings.host());
        assertEquals("shops", settings.databaseName("imusGS"));
        assertEquals("imusWaystones", settings.databaseName("imusWaystones"));
        assertEquals(DatabaseType.SQLITE, settings.copyFrom());

        config.set("database.copy-from", "mysql");
        assertNull(DatabaseSettings.read(config.getConfigurationSection("database")).copyFrom(), "copying from itself is nothing");

        config.set("database.type", "postgres");
        assertThrows(IllegalArgumentException.class, () -> DatabaseSettings.read(config.getConfigurationSection("database")));
    }

    @Test
    void oldDatabaseSettingsFilesBecomeTheMysqlSettings() throws Exception
    {
        Path plugins = folder.resolve("plugins");
        Files.createDirectories(plugins.resolve("imusGS"));
        Files.writeString(plugins.resolve("imusGS/DataBaseSettings.yml"),
                "Host: gs-host\nPort: 3307\nDatabase: shops\nUsername: gs\nPassword: secret\n");
        Map<String, String> names = Map.of("imusAPI", "imusAPI", "imusGS", "imusGS");
        assertNull(DatabaseSettings.fromOldFiles(folder.resolve("empty").toFile(), names), "none: SQLite stays");

        DatabaseSettings settings = DatabaseSettings.fromOldFiles(plugins.toFile(), names);
        assertEquals(DatabaseType.MYSQL, settings.type());
        assertEquals("gs-host", settings.host());
        assertEquals(3307, settings.port());
        assertEquals("shops", settings.databaseName("imusGS"), "a renamed database is kept");

        Files.createDirectories(plugins.resolve("imusAPI"));
        Files.writeString(plugins.resolve("imusAPI/DataBaseSettings.yml"), "Host: api-host\nPort: 3306\nDatabase: imusAPI\nUsername: root\nPassword: ''\n");
        assertEquals("api-host", DatabaseSettings.fromOldFiles(plugins.toFile(), names).host(), "imusAPI's own file first");

        YamlConfiguration config = new YamlConfiguration();
        settings.write(config.createSection("database"));
        assertEquals(settings, DatabaseSettings.read(config.getConfigurationSection("database")), "written and read back the same");
    }
}
