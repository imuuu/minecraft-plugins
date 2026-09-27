package me.imu.imuschallenges;

import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.zaxxer.hikari.HikariDataSource;
import imu.iAPI.CmdUtil.CmdHelper;
import imu.iAPI.Handelers.CommandHandler;
import imu.iAPI.Other.ImusTabCompleter;
import imu.iAPI.Other.MySQL;
import me.imu.imuschallenges.Commands.ExampleCmd;
import me.imu.imuschallenges.Managers.*;
import me.imu.imuschallenges.SubCommands.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

public class ImusChallenges extends JavaPlugin
{

    private static ImusChallenges _instance;

    public static ImusChallenges getInstance() {return _instance;}

    private MySQL _SQL;
    private ImusTabCompleter _tab_cmd1;
    private CmdHelper _cmdHelper;

    //Managers
    private ManagerChallenges _managerChallenges;
    private ManagerCCollectMaterial _managerCCollectMaterial;
    private ManagerPlayers _managerPlayers;
    private ManagerPointType _managerPointType;
    private ManagerPlayerPoints _managerPlayerPoints;
    private ManagerChallengeShop _managerChallengeShop;
    private ManagerAdvancement _managerAdvancement;

    @Override
    public void onEnable()
    {
        _instance = this;
        connectDataBase();
        registerCommands();
        System.out.println("ImusChallenges has been enabled!");
        registerPermissions();


        _managerPlayers = new ManagerPlayers();
        _managerChallenges = new ManagerChallenges();
        _managerCCollectMaterial = new ManagerCCollectMaterial(this);
        _managerPointType = new ManagerPointType(this);
        _managerPlayerPoints = new ManagerPlayerPoints(this);
        _managerChallengeShop = new ManagerChallengeShop();
        _managerAdvancement = new ManagerAdvancement();
        getServer().getPluginManager().registerEvents(_managerCCollectMaterial, this);
        getServer().getPluginManager().registerEvents(new ManagerAchievementChallenges(), this);


    }

    @Override
    public void onDisable()
    {
        System.out.println("ImusChallenges has been disabled!");

    }

    private void registerPermissions()
    {
        String permissionNode = CONSTANTS.PERM_SERVER_WIDE_COLLECTION_CHALLENGE;
        Permission permission = new Permission(permissionNode, "Allows compete server wide research materials competition", PermissionDefault.NOT_OP);
        Bukkit.getPluginManager().addPermission(permission);

        permissionNode = CONSTANTS.PERM_SERVER_WIDE_COLLECTION_CHALLENGE_BROADCAST;
        permission = new Permission(permissionNode, "Allows hear other research findings", PermissionDefault.NOT_OP);
        Bukkit.getPluginManager().addPermission(permission);

        permissionNode = CONSTANTS.PERM_SERVER_WIDE_ACHIEVEMENT_CHALLENGE;
        permission = new Permission(permissionNode, "Allows compete server wide achievement competition", PermissionDefault.FALSE);
        Bukkit.getPluginManager().addPermission(permission);

        permissionNode = CONSTANTS.PERM_SERVER_WIDE_ACHIEVEMENT_CHALLENGE_BROADCAST;
        permission = new Permission(permissionNode, "Allows hear other achievement findings", PermissionDefault.FALSE);
        Bukkit.getPluginManager().addPermission(permission);

        permissionNode = CONSTANTS.PERM_BROADCAST_CHALLENGE_SHOP_UPDATE;
        permission = new Permission(permissionNode, "Allows broadcast challenge shop update", PermissionDefault.OP);
        Bukkit.getPluginManager().addPermission(permission);
    }
    private boolean connectDataBase()
    {
        Bukkit.getLogger().info(ChatColor.GREEN + "[imusChallenges] Connecting to database...");
        _SQL = new MySQL(this, 10,"ImusChallenges");
        return true;
    }

    public MySQL getSQL()
    {
        return _SQL;
    }

    public ConnectionSource getSource() throws SQLException
    {
        HikariDataSource dataSource = getSQL().getDataSource();
        return new DataSourceConnectionSource(dataSource, dataSource.getJdbcUrl());
    }


    public void registerCommands()
    {
        String _pluginName = "[ImusChallenges]";
        _cmdHelper = new CmdHelper(_pluginName);

        HashMap<String, String[]> cmd1AndArguments = new HashMap<>();
        CommandHandler handler = new CommandHandler(this);
        String cmd1 = "imuschallenges";
        handler.registerCmd(cmd1, new ExampleCmd());

        String cmd1_sub1 = "view advancements";
        String full_sub1 = cmd1 + " " + cmd1_sub1;
        _cmdHelper.setCmd(full_sub1, "Open Enchant Inv", full_sub1);
        handler.registerSubCmd(cmd1, cmd1_sub1, new SubStatsAdvancements(_cmdHelper.getCmdData(full_sub1)));
        handler.setPermissionOnLastCmd("ic.view.advancements");

        String cmd1_sub2 = "view materials";
        String full_sub2 = cmd1 + " " + cmd1_sub2;
        _cmdHelper.setCmd(full_sub2, "Open collected challenge inv", full_sub2);
        handler.registerSubCmd(cmd1, cmd1_sub2, new SubOpenInvCollectionMaterial(_cmdHelper.getCmdData(full_sub2)));
        handler.setPermissionOnLastCmd("ic.view.materials");

        String cmd1_sub3 = "shop";
        String full_sub3 = cmd1 + " " + cmd1_sub3;
        _cmdHelper.setCmd(full_sub3, "Open Challenge Shop", full_sub3);
        handler.registerSubCmd(cmd1, cmd1_sub3, new SubOpenShopCmd());
        handler.setPermissionOnLastCmd("ic.shop");

        String cmd1_sub4 = "add points";
        handler.registerSubCmd(cmd1, cmd1_sub4, new SubAddPointsCmd());
        handler.setPermissionOnLastCmd("ic.add.points");

        String cmd1_sub6 = "view points";
        handler.registerSubCmd(cmd1, cmd1_sub6, new SubGetPointsCmd());
        handler.setPermissionOnLastCmd("ic.view.points");

        cmd1AndArguments.put(cmd1, new String[] { "view", "shop", "add" });
        cmd1AndArguments.put("view", new String[] { "points","materials","advancements"});
        cmd1AndArguments.put("add", new String[] { "points" });

        // register cmds
        getCommand(cmd1).setExecutor(handler);

        // register tabcompleters
        _tab_cmd1 = new ImusTabCompleter(cmd1, cmd1AndArguments, "ic.tabcompleter");
        getCommand(cmd1).setTabCompleter(_tab_cmd1);

    }

    public void UpdateTapCompleterRules()
    {
       _tab_cmd1.SetRule("/ic add points", 3, _managerPointType.getPointTypeNames());
       _tab_cmd1.SetRule("/ic add points", 4, Arrays.asList("1", "5", "10", "20", "50", "100"));
    }


}
