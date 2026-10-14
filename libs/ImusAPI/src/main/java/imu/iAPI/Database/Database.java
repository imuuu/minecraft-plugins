package imu.iAPI.Database;

import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.Plugin;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * A plugin's database, SQLite or MySQL as imusAPI's config.yml says. SQLite keeps a data.db in the plugin's folder;
 * MySQL gives each plugin a database of its own name on the server. The plugin sees a connection pool either way,
 * and {@link #type()} for the few statements that differ.
 *
 * <pre>
 * Database db = Database.open(this, "imusGS", 10);   // throws DatabaseException with the reason
 * createTables(db);
 * db.copyFromIfAsked();                              // before the plugin reads anything
 * </pre>
 */
public final class Database implements AutoCloseable
{
    private static final String SQLITE_FILE = "data.db";
    // SQLite has one writer at a time; a few connections let reads go on while another thread writes
    private static final int SQLITE_POOL = 4;

    private static volatile DatabaseSettings _settings = DatabaseSettings.sqlite();

    private final DatabaseType _type;
    private final String _name;
    private final String _where;
    private final HikariDataSource _pool;
    private final Logger _logger;
    private final File _folder;

    private Database(DatabaseType type, String name, String where, HikariDataSource pool, Logger logger, File folder)
    {
        _type = type;
        _name = name;
        _where = where;
        _pool = pool;
        _logger = logger;
        _folder = folder;
    }

    /**
     * Sets the settings every plugin's database uses. imusAPI does this from its config.yml on enable.
     */
    public static void configure(DatabaseSettings settings)
    {
        _settings = settings;
    }

    public static DatabaseSettings settings()
    {
        return _settings;
    }

    /**
     * Opens the plugin's database as imusAPI's config.yml says.
     *
     * @param name     the plugin's database, e.g. "imusGS": the MySQL database's name
     * @param poolSize connections for MySQL; SQLite uses a few
     * @throws DatabaseException when it can't be opened, with the reason for the console
     */
    public static Database open(Plugin plugin, String name, int poolSize)
    {
        DatabaseSettings settings = _settings;
        return settings.type() == DatabaseType.SQLITE
                ? sqlite(new File(plugin.getDataFolder(), SQLITE_FILE), name, plugin.getLogger())
                : mysql(settings, name, poolSize, true, plugin.getLogger(), plugin.getDataFolder());
    }

    /**
     * A SQLite file; the folder is made when missing.
     */
    public static Database sqlite(File file, String name, Logger logger)
    {
        try
        {
            File folder = file.getAbsoluteFile().getParentFile();
            if (folder != null && !folder.isDirectory() && !folder.mkdirs())
                throw new SQLException("Couldn't make the folder " + folder);

            HikariConfig config = new HikariConfig();
            config.setPoolName(name + "-sqlite");
            config.setDriverClassName("org.sqlite.JDBC");
            config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
            config.setMaximumPoolSize(SQLITE_POOL);
            config.setConnectionTimeout(30_000);
            // Readers don't wait for the writer, and a writer waits its turn instead of failing
            config.addDataSourceProperty("journal_mode", "WAL");
            config.addDataSourceProperty("busy_timeout", "10000");
            config.addDataSourceProperty("synchronous", "NORMAL");
            // Dates as text, like MySQL shows them, so a copy between the two keeps them as they are
            config.addDataSourceProperty("date_class", "TEXT");
            config.addDataSourceProperty("date_string_format", "yyyy-MM-dd HH:mm:ss");
            HikariDataSource pool = new HikariDataSource(config);
            return new Database(DatabaseType.SQLITE, name, "SQLite " + file.getPath(), pool, logger, folder);
        } catch (Exception e)
        {
            throw new DatabaseException("Couldn't open the SQLite database " + file.getPath() + ": " + e.getMessage(), e);
        }
    }

    /**
     * The plugin's database on the MySQL server.
     *
     * @param create make the database when it isn't there
     */
    public static Database mysql(DatabaseSettings settings, String name, int poolSize, boolean create, Logger logger, File folder)
    {
        String database = settings.databaseName(name);
        String server = settings.host() + ":" + settings.port();
        try
        {
            if (create)
            {
                HikariConfig first = mysqlConfig(settings, "jdbc:mysql://" + server, name + "-setup", 1);
                try (HikariDataSource setup = new HikariDataSource(first);
                     Connection con = setup.getConnection();
                     Statement st = con.createStatement())
                {
                    st.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + database.replace("`", "") + "`");
                }
            }
            HikariConfig config = mysqlConfig(settings, "jdbc:mysql://" + server + "/" + database, name + "-mysql", Math.max(1, poolSize));
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            HikariDataSource pool = new HikariDataSource(config);
            return new Database(DatabaseType.MYSQL, name, "MySQL " + server + "/" + database, pool, logger, folder);
        } catch (Exception e)
        {
            throw new DatabaseException("Couldn't connect to MySQL at " + server + " (database " + database + ", user "
                    + settings.username() + "): " + e.getMessage()
                    + ". Check the database section of plugins/imusAPI/config.yml and that the server is running.", e);
        }
    }

    private static HikariConfig mysqlConfig(DatabaseSettings settings, String url, String poolName, int poolSize)
    {
        HikariConfig config = new HikariConfig();
        config.setPoolName(poolName);
        config.setJdbcUrl(url);
        config.setUsername(settings.username());
        config.setPassword(settings.password());
        config.setMaximumPoolSize(poolSize);
        config.setConnectionTimeout(10_000);
        // Fail at once at startup when the server isn't there, instead of retrying in the background
        config.setInitializationFailTimeout(1);
        return config;
    }

    public Connection getConnection() throws SQLException
    {
        return _pool.getConnection();
    }

    public DataSource dataSource()
    {
        return _pool;
    }

    /**
     * For ORMLite: it picks SQLite's or MySQL's SQL from the address.
     */
    public ConnectionSource connectionSource() throws SQLException
    {
        return new DataSourceConnectionSource(_pool, _pool.getJdbcUrl());
    }

    public DatabaseType type()
    {
        return _type;
    }

    public boolean isSqlite()
    {
        return _type == DatabaseType.SQLITE;
    }

    public String name()
    {
        return _name;
    }

    /**
     * Where the data is, e.g. "SQLite plugins/imusGS/data.db", for the console.
     */
    public String describe()
    {
        return _where;
    }

    /**
     * When imusAPI's config.yml has copy-from, copies everything from that other database into this one. Call it
     * after making the tables and before reading anything. Nothing is copied into a database that has data
     * already, and the other database isn't changed.
     */
    public void copyFromIfAsked()
    {
        DatabaseType from = _settings.copyFrom();
        if (from == null || from == _type)
            return;

        Database source;
        try
        {
            if (from == DatabaseType.SQLITE)
            {
                File file = new File(_folder, SQLITE_FILE);
                if (!file.isFile())
                {
                    _logger.warning("copy-from sqlite: there is no " + file.getPath() + "; nothing copied");
                    return;
                }
                source = sqlite(file, _name, _logger);
            } else
                source = mysql(_settings, _name, 2, false, _logger, _folder);
        } catch (DatabaseException e)
        {
            _logger.warning("copy-from " + from.key() + ": " + e.getMessage() + "; nothing copied");
            return;
        }

        try (source)
        {
            DatabaseCopy.Result result = DatabaseCopy.copy(source, this);
            for (String line : result.report())
                _logger.info(line);
            if (result.copied())
                _logger.warning("Copied the data from " + source.describe() + " to " + describe()
                        + ". Remove copy-from from imusAPI's config.yml so it isn't tried again.");
        } catch (SQLException e)
        {
            _logger.severe("Copying from " + source.describe() + " failed, so this database may be partly filled: " + e.getMessage()
                    + ". Empty it before trying again.");
        }
    }

    @Override
    public void close()
    {
        if (!_pool.isClosed())
            _pool.close();
    }
}
