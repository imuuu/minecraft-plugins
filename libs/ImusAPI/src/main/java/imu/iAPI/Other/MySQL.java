package imu.iAPI.Other;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import imu.iAPI.Main.ImusAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.*;
import java.util.Arrays;

public class MySQL
{
    private Plugin _plugin;
    private String _host = "localhost";
    private Integer _port = 3306;
    private String _dataBase = "";
    private String _username = "root";
    private String _password = "";

    private Connection _connection;
    private Cooldowns _cds;

    private int _poolSize = 1;
    private int _minimumIdle = -1;
    private HikariDataSource dataSource;

    @SuppressWarnings("unused")
    private BukkitTask RunnableAsyncTask;

    public MySQL(Plugin plugin, int poolSize, String dataBaseName)
    {
        _plugin = plugin;
        _dataBase = dataBaseName;
        _cds = new Cooldowns();
        _poolSize = poolSize;
        LoadConfig();
        setupDataSource();
        RunnableAsync();
    }

    public MySQL(Plugin plugin, int poolSize, int minimumIdle, String dataBaseName)
    {
        _plugin = plugin;
        _dataBase = dataBaseName;
        _cds = new Cooldowns();
        _poolSize = poolSize;
        _minimumIdle = minimumIdle;
        LoadConfig();
        setupDataSource();
        RunnableAsync();

    }



   /* private void setupDataSource()
    {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + _host + ":" + _port + "/" + _dataBase);
        config.setUsername(_username);
        config.setPassword(_password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(_poolSize); // Increase pool size
        config.setConnectionTimeout(30000); // Connection timeout in milliseconds

        this.dataSource = new HikariDataSource(config);
    }*/

    private void setupDataSource()
    {
        try {
            // Connect to MySQL without specifying a database
            String jdbcUrlWithoutDatabase = "jdbc:mysql://" + _host + ":" + _port;
            HikariConfig tempConfig = new HikariConfig();
            tempConfig.setJdbcUrl(jdbcUrlWithoutDatabase);
            tempConfig.setUsername(_username);
            tempConfig.setPassword(_password);

            try (HikariDataSource tempDataSource = new HikariDataSource(tempConfig);
                 Connection conn = tempDataSource.getConnection();
                 Statement stmt = conn.createStatement())
            {
                // Check if database exists and create it if not
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + _dataBase);
            }

            // Now set up the data source with the database
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + _host + ":" + _port + "/" + _dataBase);
            config.setUsername(_username);
            config.setPassword(_password);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.setMaximumPoolSize(_poolSize);
            config.setConnectionTimeout(30000);

            if(_minimumIdle != -1)
            {
            	config.setMinimumIdle(_minimumIdle);
            }

            this.dataSource = new HikariDataSource(config);
            ImusAPI._instance.RegisterSQL(this);

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle errors here
        }
    }


    // In ImusAPI's MySQL class
    public HikariDataSource getDataSource() {
        return this.dataSource;
    }

    public void CloseDataSource()
    {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }


    public Connection GetConnection() throws SQLException
    {
        return dataSource.getConnection();
    }

    //private ConnectionSource _connectionSource;

    /*public synchronized ConnectionSource GetConnectionSource() throws  SQLException
    {
        if (_connectionSource == null) {
            _connectionSource = new DataSourceConnectionSource(dataSource, dataSource.getJdbcUrl());
        }
        return _connectionSource;
    }

    public ConnectionSource GetConnectionSourceNew() throws  SQLException
    {
       return new DataSourceConnectionSource(dataSource, dataSource.getJdbcUrl());
    }*/

    public boolean ExecuteStatements(Iterable<String> statements)
    {
        try (Connection con = GetConnection();
             Statement stmt = con.createStatement())
        {
            for (String sm : statements)
            {
                stmt.addBatch(sm);
            }
            stmt.executeBatch();
            return true;
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public void ExecuteStatementsAsync(Iterable<String> statements)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                ExecuteStatements(statements);
            }
        }.runTaskAsynchronously(_plugin);
    }

    public void ExecuteStatementsAsync(String... statements)
    {
        ExecuteStatementsAsync(Arrays.asList(statements));
    }


    void RunnableAsync()
    {

        final String syntax = "[" + _dataBase + "]";
        RunnableAsyncTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (_cds.isCooldownReady("SQL_CONNECTION_CHECK"))
                {
                    _cds.setCooldownInSeconds("SQL_CONNECTION_CHECK", 60 * 60);


                    if (CheckConnection())
                    {
                        //Bukkit.getLogger().info(syntax+"Checking SQL connection and its TRUE");
                    }
                    else
                    {
                        Bukkit.getLogger().info(syntax + "Checking SQL connection and its FALSE");
                    }
                }

            }
        }.runTaskTimerAsynchronously(_plugin, 20 * 60, 20 * 60 * 5);


    }

    public boolean CheckConnection()
    {
        boolean connected = true;
        try
        {
            Connection con = GetConnection();
            PreparedStatement ps = con.prepareStatement("SHOW PROCESSLIST");
            ps.executeQuery();

            con.close();
            ps.close();
        } catch (Exception e)
        {
            //System.out.println("[imusGS] Tried to check SQL connection but failed");
            //System.out.println(e);
            connected = false;
        }
        //RemovePriceValue(shopItemSellerUUID);

        return connected;


    }

    void LoadConfig()
    {
        ConfigMaker cm = new ConfigMaker(_plugin, "DataBaseSettings.yml");
        if (!cm.isExists())
        {
            Bukkit.getLogger().info("DataBaseSettings.yml created!");
            cm.saveConfig();
        }
        try
        {
            _host = cm.addDefault("Host", _host, "Host of Database");
            _port = cm.addDefault("Port", _port, "Port of Database");
            _dataBase = cm.addDefault("Database", _dataBase, "Database");
            _username = cm.addDefault("Username", _username, "User for database");
            _password = cm.addDefault("Password", _password, "User password for database");
            cm.addComments();
        } catch (Exception e)
        {
            //e.printStackTrace();
            _plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "WARNING: Something went wrong loading " + cm.getFileName());
        }


    }
}
