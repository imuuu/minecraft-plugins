package me.imu.imuschallenges;

import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.zaxxer.hikari.HikariDataSource;
import imu.iAPI.CmdUtil.CmdHelper;
import imu.iAPI.Config.ConfigMenu;
import imu.iAPI.Handelers.CommandHandler;
import imu.iAPI.Other.ImusTabCompleter;
import imu.iAPI.Other.MySQL;
import me.imu.imuschallenges.Commands.ExampleCmd;
import me.imu.imuschallenges.Managers.*;
import me.imu.imuschallenges.SubCommands.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

    private ConfigMenu _configMenu;

    @Override
    public void onEnable()
    {
        _instance = this;
        saveDefaultConfig();
        connectDataBase();
        _configMenu = createConfigMenu();
        registerCommands();
        System.out.println("ImusChallenges has been enabled!");
        registerPermissions();


        _managerPlayers = new ManagerPlayers();
        _managerChallenges = new ManagerChallenges();
        _managerCCollectMaterial = new ManagerCCollectMaterial(this);
        _managerPointType = new ManagerPointType(this);
        _managerPlayerPoints = new ManagerPlayerPoints(this);
        new ManagerChallengeLeader();
        _managerChallengeShop = new ManagerChallengeShop();
        _managerAdvancement = new ManagerAdvancement();
        getServer().getPluginManager().registerEvents(_managerCCollectMaterial, this);
        getServer().getPluginManager().registerEvents(new ManagerAchievementChallenges(), this);
        getServer().getPluginManager().registerEvents(new ManagerPointsScoreboard(), this);
        _configMenu.register();


    }

    @Override
    public void onDisable()
    {
        if (_configMenu != null)
            _configMenu.unregister();
        if (_managerChallengeShop != null)
            _managerChallengeShop.shutdown();
        if (_managerPlayerPoints != null)
            _managerPlayerPoints.shutdown();
        System.out.println("ImusChallenges has been disabled!");

    }

    /**
     * Re-reads config.yml and applies it to everything that keeps settings, without a restart.
     */
    public void reloadSettings()
    {
        reloadConfig();
        _managerChallengeShop.reload();
        ManagerPointsScoreboard.getInstance().applyConfig();
    }

    /**
     * The values of config.yml that can be changed in game with /ic config or /ia config.
     */
    private ConfigMenu createConfigMenu()
    {
        ConfigMenu menu = new ConfigMenu(this, "ImusChallenges", CONSTANTS.PERM_CONFIG, this::reloadSettings);

        menu.addInt("points.first-material", "Points: first to find a material", Material.GRASS_BLOCK, 0, 1000)
                .description("For being the first on the server to pick up a material")
                .note("now");
        menu.addInt("points.advancement-task", "Points: task advancement", Material.PAPER, 0, 1000)
                .note("now");
        menu.addInt("points.advancement-goal", "Points: goal advancement", Material.MAP, 0, 1000)
                .note("now");
        menu.addInt("points.advancement-challenge", "Points: challenge advancement", Material.FILLED_MAP, 0, 1000)
                .note("now");
        menu.addInt("points.first-advancement-bonus", "Points: first advancement bonus", Material.NETHER_STAR, 0, 1000)
                .description("Extra for being the first on the server to complete an advancement")
                .note("now");
        menu.addChoice("scoreboard.display", "Points on scoreboard", Material.OAK_SIGN, ManagerPointsScoreboard.DISPLAY_OPTIONS)
                .description("Where lifetime challenge points are shown")
                .description("PLAYER_LIST = after the name in tab")
                .note("now");

        menu.addInt("shop.reminder-interval-minutes", "Unspent points reminder (min)", Material.BELL, 0, 1440)
                .description("How often players who can afford something are reminded")
                .description("0 turns the reminder off")
                .note("now");

        menu.addBoolean("leader-broadcast.enabled", "Leader broadcast", Material.GOAT_HORN)
                .description("Tell everyone when a player takes the lead in lifetime challenge points")
                .note("now");
        menu.addInt("leader-broadcast.min-points", "Leader broadcast: min points", Material.EXPERIENCE_BOTTLE, 0, 100000)
                .description("A new leader with fewer points is not announced")
                .note("now");
        menu.addInt("leader-broadcast.cooldown-minutes", "Leader broadcast: cooldown (min)", Material.CLOCK, 0, 1440)
                .description("At most one announcement per this many minutes, so two players")
                .description("passing each other don't flood the chat. 0 = no limit")
                .note("now");

        addShopTierEntries(menu, "normal", "Normal", CONSTANTS.NORMAL_SLOT_COLUMNS * CONSTANTS.NORMAL_SLOT_ROWS);
        addShopTierEntries(menu, "special", "Special", CONSTANTS.SPECIAL_SLOTS);
        return menu;
    }

    private void addShopTierEntries(ConfigMenu menu, String tier, String name, int slotCount)
    {
        String path = "shop." + tier + ".";
        menu.addInt(path + "refresh-minutes", name + ": refresh time (min)", Material.CLOCK, 1, 10080)
                .description("How long one set of " + name.toLowerCase() + " items stays in the shop")
                .note("next rotation, a shorter time also ends the current one");
        menu.addInt(path + "default-slots", name + ": free slots", Material.CHEST, 0, slotCount)
                .description("Slots open without buying them")
                .note("players who have never opened the shop");
        menu.addInt(path + "item-cost-min", name + ": item cost min", Material.IRON_NUGGET, 0, 100000)
                .description("Lowest challenge point price an item can roll")
                .note("next rotation");
        menu.addInt(path + "item-cost-max", name + ": item cost max", Material.GOLD_NUGGET, 0, 100000)
                .description("Highest challenge point price an item can roll")
                .note("next rotation");
        menu.addDouble(path + "slot-price-first", name + ": first slot price ($)", Material.GOLD_INGOT, 0, 1_000_000_000_000d)
                .description("Money price of the first extra slot")
                .note("now");
        menu.addDouble(path + "slot-price-multiplier", name + ": slot price multiplier", Material.EXPERIENCE_BOTTLE, 1, 100)
                .description("Each further slot costs this many times the previous one")
                .note("now");
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

        permissionNode = CONSTANTS.PERM_BROADCAST_LEADER;
        permission = new Permission(permissionNode, "Hear when someone takes the lead in challenge points", PermissionDefault.TRUE);
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
        handler.setPermissionOnLastCmd(CONSTANTS.PERM_CHALLENGE_SHOP);

        String cmd1_sub4 = "add points";
        handler.registerSubCmd(cmd1, cmd1_sub4, new SubAddPointsCmd());
        handler.setPermissionOnLastCmd("ic.add.points");

        String cmd1_sub10 = "set points";
        handler.registerSubCmd(cmd1, cmd1_sub10, new SubSetPointsCmd(false));
        handler.setPermissionOnLastCmd("ic.set.points");

        String cmd1_sub11 = "set lifetime";
        handler.registerSubCmd(cmd1, cmd1_sub11, new SubSetPointsCmd(true));
        handler.setPermissionOnLastCmd("ic.set.points");

        String cmd1_sub9 = "reset points";
        handler.registerSubCmd(cmd1, cmd1_sub9, new SubResetPointsCmd());
        handler.setPermissionOnLastCmd("ic.reset.points");

        String cmd1_sub7 = "reload";
        handler.registerSubCmd(cmd1, cmd1_sub7, new SubReloadCmd());
        handler.setPermissionOnLastCmd("ic.reload");

        String cmd1_sub8 = "config";
        handler.registerSubCmd(cmd1, cmd1_sub8, new SubConfigCmd(_configMenu));
        handler.setPermissionOnLastCmd(CONSTANTS.PERM_CONFIG);

        String cmd1_sub6 = "view points";
        handler.registerSubCmd(cmd1, cmd1_sub6, new SubGetPointsCmd());
        handler.setPermissionOnLastCmd("ic.view.points");

        cmd1AndArguments.put(cmd1, new String[] { "view", "shop", "add", "set", "reset", "reload", "config" });
        cmd1AndArguments.put("set", new String[] { "points", "lifetime" });
        cmd1AndArguments.put("reset", new String[] { "points" });
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
