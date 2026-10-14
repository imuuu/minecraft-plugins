package imu.iAPI.Main;

import com.j256.ormlite.support.ConnectionSource;
import imu.iAPI.CmdUtil.CmdHelper;
import imu.iAPI.Commands.ExampleCmd;
import imu.iAPI.FastInventory.Manager_FastInventories;
import imu.iAPI.Handlers.CommandHandler;
import imu.iAPI.Database.Database;
import imu.iAPI.Database.DatabaseException;
import imu.iAPI.Database.DatabaseSettings;
import imu.iAPI.Interfaces.IHasSqlSource;
import imu.iAPI.LootTables.Manager_ImusLootTable;
import imu.iAPI.Managers.Manager_CommandSender;
import imu.iAPI.Managers.Manager_Database;
import imu.iAPI.Managers.Manager_Vault;
import imu.iAPI.Menus.Paper.PaperMenus;
import imu.iAPI.Other.*;
import imu.iAPI.Config.ConfigMenu;
import imu.iAPI.Npc.NpcManager;
import imu.iAPI.SubCommands.Sub_Cmd_CreateNpc;
import imu.iAPI.SubCommands.Sub_Cmd_OpenConfigMenus;
import imu.iAPI.SubCommands.Sub_Cmd_OpenLootTablesInvs;
import imu.iAPI.SubCommands.Sub_Cmd_OpenNamedInvs;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.*;

public class ImusAPI extends JavaPlugin implements IHasSqlSource
{
    public static ImusAPI _instance;
    public static MySQLHelper _sqlHelper;
    private Manager_FastInventories _fastInvs;
    private Manager_ImusLootTable _lootTableManager;
    private Manager_Vault _vaultManager;
    private Manager_Database _databaseManager;
    private Manager_CommandSender _commandSenderManager;
    private NpcManager _npcManager;
    private ConfigMenu _configMenu;

    private Database _database;

    public static HashSet<Material> AirHashSet;
    public static HashSet<Material> Ores;

    public static HashSet<Material> FortuneOres;
    public static HashSet<EntityType> EntitiesNoBosses;
    private List<EntityType> _excludeEntityTypes = Arrays.asList(EntityType.WANDERING_TRADER, EntityType.WARDEN,
            EntityType.ARMOR_STAND, EntityType.GIANT, EntityType.ENDER_DRAGON, EntityType.WITHER,
            EntityType.ELDER_GUARDIAN

    );

    final private String _pluginName = "[imusAPI]";
    private CmdHelper _cmdHelper;
    private ImusTabCompleter _tab_cmd1;

    private static Map<String, Boolean> _pluginCheckCache = new HashMap<>();

    @Override
    public void onEnable()
    {
        _instance = this;
        saveDefaultConfig();
        configureDatabase();
        // Settings added in an update go into an existing config.yml, with their explanations
        getConfig().options().copyDefaults(true);
        copyDefaultComments();
        saveConfig();
        connectDataBase();
        InitilizeDataBases();

        _vaultManager = new Manager_Vault(this);
        _commandSenderManager = new Manager_CommandSender();
        Manager_Vault.setupEconomy();

        _sqlHelper = new MySQLHelper();
        _fastInvs = new Manager_FastInventories();
        // The loot tables live in the database; without it they are off
        if (_databaseManager != null)
            _lootTableManager = new Manager_ImusLootTable(this, _databaseManager);

        _npcManager = new NpcManager(this);
        getServer().getPluginManager().registerEvents(_npcManager, this);
        _npcManager.loadSettings();

        PaperMenus.enable(this);
        RegisterCommands();
        _configMenu = createConfigMenu();
        _configMenu.register();
        AirHashSet = new HashSet<>();
        AirHashSet.add(Material.AIR);
        AirHashSet.add(Material.VOID_AIR);
        AirHashSet.add(Material.CAVE_AIR);

        InitOres();
        InitFortuneOres();
        InitEntities();


    }

    @Override
    public void onDisable()
    {
        if (_configMenu != null)
            _configMenu.unregister();
        PaperMenus.disable();

        if (_database != null)
            _database.close();
    }

    private void InitilizeDataBases()
    {
        if (_database == null)
            return;
        try
        {
            _databaseManager = new Manager_Database(this);
            _database.copyFromIfAsked();
        }
        catch (Exception e)
        {
            getLogger().log(java.util.logging.Level.SEVERE, "Couldn't make the loot table tables in " + _database.describe(), e);
        }
    }

    private void copyDefaultComments()
    {
        Configuration defaults = getConfig().getDefaults();
        if (defaults == null)
            return;
        for (String path : defaults.getKeys(true))
        {
            if (getConfig().getComments(path).isEmpty() && !defaults.getComments(path).isEmpty())
                getConfig().setComments(path, defaults.getComments(path));
        }
    }

    // Plugin folder to the database name it asks for, for reading older DataBaseSettings.yml files
    private static final Map<String, String> OLD_DATABASE_FILES = Map.of("imusAPI", "imusAPI", "imusGS", "imusGS",
            "imusWaystones", "imusWaystones", "ImusChallenges", "ImusChallenges");

    /**
     * Reads the database section of config.yml for every plugin. A server that had DataBaseSettings.yml files (older
     * versions) keeps its MySQL: the first start writes their settings into config.yml.
     */
    private void configureDatabase()
    {
        if (!getConfig().isSet("database.type"))
        {
            DatabaseSettings old = DatabaseSettings.fromOldFiles(getDataFolder().getParentFile(), OLD_DATABASE_FILES);
            if (old != null)
            {
                old.write(getConfig().createSection("database"));
                saveConfig();
                getLogger().warning("Moved the MySQL settings of the DataBaseSettings.yml files into the database section of "
                        + "config.yml; those files aren't read any more");
            }
        }
        try
        {
            Database.configure(DatabaseSettings.read(getConfig().getConfigurationSection("database")));
        } catch (IllegalArgumentException e)
        {
            getLogger().severe(e.getMessage() + " in config.yml; using SQLite");
            Database.configure(DatabaseSettings.sqlite());
        }
    }
    public void RegisterCommands()
    {
        _cmdHelper = new CmdHelper(_pluginName);

        HashMap<String, String[]> cmd1AndArguments = new HashMap<>();
        CommandHandler handler = new CommandHandler(this);
        String cmd1 = "ia";
        handler.registerCmd(cmd1, new ExampleCmd());

//		String cmd1_sub2 = "follow"; 
//		String full_sub2 = cmd1 + " " + cmd1_sub2;
//		_cmdHelper.setCmd(full_sub2, "Follow the player", full_sub2);
//		handler.registerSubCmd(cmd1, cmd1_sub2, new Sub_Cmd_FollowPlayer(_cmdHelper.getCmdData(full_sub2)));
//		handler.setPermissionOnLastCmd("ia.follow");

        String cmd1_sub1 = "inv";
        String full_sub1 = cmd1 + " " + cmd1_sub1;
        _cmdHelper.setCmd(full_sub1, "Open Custom Invs", full_sub1);
        handler.registerSubCmd(cmd1, cmd1_sub1, new Sub_Cmd_OpenNamedInvs(_cmdHelper.getCmdData(full_sub1)));
        handler.setPermissionOnLastCmd("ia.inv");
        _fastInvs.SetBaseID(cmd1_sub1);

        String cmd1_sub2 = "lootTables";
        String full_sub2 = cmd1 + " " + cmd1_sub2;
        _cmdHelper.setCmd(full_sub2, "Open LootTable", full_sub2);
        handler.registerSubCmd(cmd1, cmd1_sub2, new Sub_Cmd_OpenLootTablesInvs(_cmdHelper.getCmdData(full_sub2)));
        handler.setPermissionOnLastCmd("ia.lootTables");

        String cmd1_sub3 = "config";
        String full_sub3 = cmd1 + " " + cmd1_sub3;
        _cmdHelper.setCmd(full_sub3, "Open plugin settings", full_sub3);
        handler.registerSubCmd(cmd1, cmd1_sub3, new Sub_Cmd_OpenConfigMenus(_cmdHelper.getCmdData(full_sub3)));
        handler.setPermissionOnLastCmd("ia.config");
        handler.allowConsole(cmd1, cmd1_sub3);

        String cmd1_sub4 = "npc create";
        handler.registerSubCmd(cmd1, cmd1_sub4, new Sub_Cmd_CreateNpc(_npcManager));
        handler.setPermissionOnLastCmd("ia.npc.create");

        cmd1AndArguments.put(cmd1, new String[]{"inv", "lootTables", "config", "npc"});
        cmd1AndArguments.put("npc", new String[]{"create"});

        // register cmds
        getCommand(cmd1).setExecutor(handler);

        // register tabcompleters
        _tab_cmd1 = new ImusTabCompleter(cmd1, cmd1AndArguments, "ia.tabcompleter");

        getCommand(cmd1).setTabCompleter(_tab_cmd1);

    }

    private ConfigMenu createConfigMenu()
    {
        ConfigMenu menu = new ConfigMenu(this, "imusAPI", "ia.config", () ->
        {
            reloadConfig();
            _npcManager.loadSettings();
        });
        menu.addBoolean("npc.look-at-players", "NPCs look at players", Material.ENDER_EYE)
                .description("NPCs turn to the nearest player close by")
                .note("now");
        menu.addDouble("npc.look-range", "NPC look range", Material.SPYGLASS, 1, 32)
                .description("How close a player must be, in blocks")
                .note("now");
        return menu;
    }

    /**
     * NPCs (villagers or player-looking mannequins) that run an action when clicked. Plugins register
     * their own actions here.
     */
    public NpcManager GetNpcManager()
    {
        return _npcManager;
    }

    public ImusTabCompleter GetCMD1_TabCompleter()
    {
        return _tab_cmd1;
    }

    private void InitEntities()
    {
        EntitiesNoBosses = new HashSet<>();

        for (EntityType entityType : EntityType.values())
        {
            if (entityType.isAlive() && entityType.isSpawnable() && !_excludeEntityTypes.contains(entityType))
            {
                EntitiesNoBosses.add(entityType);
            }
        }
    }

    private void InitOres()
    {
        Ores = new HashSet<>();

        Ores.add(Material.COAL_ORE);
        Ores.add(Material.DIAMOND_ORE);
        Ores.add(Material.EMERALD_ORE);
        Ores.add(Material.GOLD_ORE);
        Ores.add(Material.IRON_ORE);
        Ores.add(Material.LAPIS_ORE);
        Ores.add(Material.NETHER_QUARTZ_ORE);
        Ores.add(Material.REDSTONE_ORE);
        Ores.add(Material.ANCIENT_DEBRIS);
        Ores.add(Material.GLOWSTONE);
        Ores.add(Material.NETHER_GOLD_ORE);
        Ores.add(Material.COPPER_ORE);

        Ores.add(Material.DEEPSLATE_COAL_ORE);
        Ores.add(Material.DEEPSLATE_DIAMOND_ORE);
        Ores.add(Material.DEEPSLATE_EMERALD_ORE);
        Ores.add(Material.DEEPSLATE_GOLD_ORE);
        Ores.add(Material.DEEPSLATE_IRON_ORE);
        Ores.add(Material.DEEPSLATE_LAPIS_ORE);
        Ores.add(Material.DEEPSLATE_REDSTONE_ORE);
    }

    private void InitFortuneOres()
    {
        FortuneOres = new HashSet<>();

        for (Material ore : Ores)
        {
            FortuneOres.add(ore);
        }

        FortuneOres.remove(Material.ANCIENT_DEBRIS);
        FortuneOres.remove(Material.GLOWSTONE);

    }

    public static boolean isPluginEnabled(String pluginName)
    {
        if (_pluginCheckCache.containsKey(pluginName))
        {
            return _pluginCheckCache.get(pluginName);
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        boolean isEnabled = plugin != null && plugin.isEnabled();
        _pluginCheckCache.put(pluginName, isEnabled);
        return isEnabled;
    }


    private void connectDataBase()
    {
        try
        {
            _database = Database.open(this, "imusAPI", 10);
            getLogger().info("Data in " + _database.describe());
        }
        catch (DatabaseException e)
        {
            // The loot tables need it; everything else works without
            getLogger().severe(e.getMessage() + " The loot tables are off until a restart.");
            _database = null;
        }
    }

    /**
     * imusAPI's own database, or null when it couldn't be opened.
     */
    public Database getDatabase()
    {
        return _database;
    }

    @Override
    public ConnectionSource getSource() throws SQLException
    {
        if (_database == null)
            throw new SQLException("imusAPI's database isn't open");
        return _database.connectionSource();
    }



}
