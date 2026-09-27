package imu.iAPI.Main;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.zaxxer.hikari.HikariDataSource;
import imu.iAPI.CmdUtil.CmdHelper;
import imu.iAPI.Commands.ExampleCmd;
import imu.iAPI.FastInventory.Manager_FastInventories;
import imu.iAPI.Handelers.CommandHandler;
import imu.iAPI.Interfaces.ICustomInventory;
import imu.iAPI.Interfaces.IHasSql;
import imu.iAPI.Interfaces.IHasSqlSource;
import imu.iAPI.LootTables.Manager_ImusLootTable;
import imu.iAPI.Managers.Manager_CommandSender;
import imu.iAPI.Managers.Manager_Database;
import imu.iAPI.Managers.Manager_Vault;
import imu.iAPI.Other.*;
import imu.iAPI.SubCommands.Sub_Cmd_OpenLootTablesInvs;
import imu.iAPI.SubCommands.Sub_Cmd_OpenNamedInvs;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.*;

public class ImusAPI extends JavaPlugin implements IHasSql, IHasSqlSource
{
    public static ImusAPI _instance;
    public static Metods _metods;
    public static MySQLHelper _sqlHelper;
    private Manager_FastInventories _fastInvs;
    private Manager_ImusLootTable _lootTableManager;
    private Manager_Vault _vaultManager;
    private Manager_Database _databaseManager;
    private Manager_CommandSender _commandSenderManager;

    private MySQL _SQL;

    //depricated
    private HashMap<UUID, CustomInvLayout> _openedInvs = new HashMap<>();
    private HashMap<UUID, ICustomInventory> _openedCustomInventories = new HashMap<>();

    private ProtocolManager _protocolManager;
    private ProtocolLibUtil _protocolLibUtil;

    private List<MySQL> _sqls = new ArrayList<>();
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
        connectDataBase();
        InitilizeDataBases();

        _vaultManager = new Manager_Vault(this);
        _commandSenderManager = new Manager_CommandSender();
        Manager_Vault.setupEconomy();

        _metods = new Metods(this);
        _sqlHelper = new MySQLHelper();
        _fastInvs = new Manager_FastInventories();
        _lootTableManager = new Manager_ImusLootTable(this, _databaseManager);

        InitiliazeProtocolLib();

        RegisterCommands();
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
        for (CustomInvLayout inv : _openedInvs.values())
        {
            inv.GetPlayer().closeInventory();
        }

        for (ICustomInventory inv : _openedCustomInventories.values())
        {
            inv.getPlayer().closeInventory();
        }

        _openedCustomInventories.clear();
        _openedInvs.clear();

        for (MySQL sql : _sqls)
        {
            sql.CloseDataSource();
        }
    }

    public void InitiliazeProtocolLib()
    {
        _protocolManager = ProtocolLibrary.getProtocolManager();
        _protocolLibUtil = new ProtocolLibUtil();

        //_protocolManager.addPacketListener(new ChatPacketListener(this));

    }

    private void InitilizeDataBases()
    {
        try
        {
            _databaseManager = new Manager_Database(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
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

        cmd1AndArguments.put(cmd1, new String[]{"inv", "lootTables"});

        // register cmds
        getCommand(cmd1).setExecutor(handler);

        // register tabcompleters
        _tab_cmd1 = new ImusTabCompleter(cmd1, cmd1AndArguments, "ia.tabcompleter");

        getCommand(cmd1).setTabCompleter(_tab_cmd1);

    }

    public ImusTabCompleter GetCMD1_TabCompleter()
    {
        return _tab_cmd1;
    }

    public void RegisterSQL(MySQL sql)
    {
        _sqls.add(sql);
    }

    public void RegisterInvOpen(CustomInvLayout inv)
    {
        _openedInvs.put(inv.GetPlayer().getUniqueId(), inv);
    }

    public void UnregisterInv(CustomInvLayout inv)
    {
        _openedInvs.remove(inv.GetPlayer().getUniqueId());
    }

    public void RegisterCustomInventory(ICustomInventory inv)
    {
        _openedCustomInventories.put(inv.getPlayer().getUniqueId(), inv);
    }

    public void UnregisterCustomInventory(ICustomInventory inv)
    {
        _openedCustomInventories.remove(inv.getPlayer().getUniqueId());
    }

    public CustomInvLayout GetOpenedInv(Player player)
    {
        return _openedInvs.get(player.getUniqueId());
    }

    public ProtocolManager GetProtocolManager()
    {
        return _protocolManager;
    }

    public ProtocolLibUtil GetProtocolLibUtil()
    {
        return _protocolLibUtil;
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


    @Override
    public boolean connectDataBase()
    {
        Bukkit.getLogger().info(ChatColor.DARK_PURPLE + "[imusAPI] Connecting to database...");
        try
        {
            _SQL = new MySQL(this, 20, 2, "imusAPI");
        }
        catch (Exception e)
        {
            Bukkit.getLogger().info(ChatColor.DARK_PURPLE + "[imusAPI] Connecting to database FAILED");
            Bukkit.getLogger().info("Pls check your config.yml and restart the server");
            e.printStackTrace();
            _SQL = null;
            return false;
        }
        return true;
    }

    @Override
    public MySQL getSQL()
    {
        return _SQL;
    }

    @Override
    public ConnectionSource getSource() throws SQLException
    {
        HikariDataSource dataSource = getSQL().getDataSource();
        return new DataSourceConnectionSource(dataSource, dataSource.getJdbcUrl());
    }

//	boolean setup()
//	{
//		if(Bukkit.getPluginManager().getPlugin("imusAPI") != null)
//		{
//			ChestCleaner cc = (ChestCleaner) Bukkit.getPluginManager().getPlugin("ChestCleaner");
//			System.out.println("LOADING CHESTCLEANER");
//			
//			
//			return true;
//		}
//		return false;
//	}

//	boolean setupImusApi()
//	{
//		if(Bukkit.getPluginManager().getPlugin("imusAPI") != null)
//		{
//			_imusAPI = (ImusAPI) Bukkit.getPluginManager().getPlugin("imusAPI");
//			return true;
//		}
//		return false;
//	}

}
