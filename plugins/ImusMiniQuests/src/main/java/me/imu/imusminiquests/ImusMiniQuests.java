package me.imu.imusminiquests;

import imu.iAPI.CmdUtil.CmdHelper;
import imu.iAPI.Config.ConfigMenu;
import imu.iAPI.Handelers.CommandHandler;
import imu.iAPI.Other.ImusTabCompleter;
import imu.iAPI.Other.Metods;
import me.imu.imusminiquests.Commands.RootCmd;
import me.imu.imusminiquests.Managers.*;
import me.imu.imusminiquests.Quests.QuestItem;
import me.imu.imusminiquests.SubCommands.*;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;

public class ImusMiniQuests extends JavaPlugin
{
    private static ImusMiniQuests _instance;

    public static ImusMiniQuests getInstance() {return _instance;}

    private ImusTabCompleter _tab_cmd1;
    private CmdHelper _cmdHelper;
    private ConfigMenu _configMenu;

    //Managers
    private ManagerQuests _managerQuests;
    private ManagerPlacedBlocks _managerPlacedBlocks;
    private ManagerQuestDrops _managerQuestDrops;
    private ManagerQuestProgress _managerQuestProgress;

    @Override
    public void onEnable()
    {
        _instance = this;
        saveDefaultConfig();
        QuestItem.init(this);

        _managerQuests = new ManagerQuests(this);
        _managerQuests.load();
        _managerPlacedBlocks = new ManagerPlacedBlocks(this);
        _managerQuestDrops = new ManagerQuestDrops(this, _managerQuests);
        _managerQuestProgress = new ManagerQuestProgress(this, _managerQuests, _managerPlacedBlocks, _managerQuestDrops);

        getServer().getPluginManager().registerEvents(_managerQuestProgress, this);
        getServer().getPluginManager().registerEvents(new ManagerQuestItemGuard(this, _managerQuests), this);
        getServer().getPluginManager().registerEvents(_managerQuestDrops, this);
        if (getServer().getPluginManager().isPluginEnabled("BetterStructures"))
            getServer().getPluginManager().registerEvents(new ManagerBetterStructuresLoot(_managerQuestDrops), this);

        _configMenu = createConfigMenu();
        _configMenu.register();
        registerCommands();
        getLogger().info("Loaded " + _managerQuests.getQuests().size() + " mini quests");
    }

    @Override
    public void onDisable()
    {
        if (_configMenu != null)
            _configMenu.unregister();
    }

    /**
     * Re-reads config.yml and quests.yml. Quest items already in inventories keep their progress
     * and pick up the new name, lore and amount the next time they move forward.
     */
    public void reloadSettings()
    {
        reloadConfig();
        _managerQuests.load();
        _managerQuestDrops.reload();
        updateTabCompleterRules();
    }

    /**
     * A message from the messages section of config.yml with & colours translated.
     */
    public String getMessage(String key)
    {
        return Metods.msgC(getConfig().getString("messages." + key, key));
    }

    public ManagerQuests getQuests() {return _managerQuests;}

    public ManagerQuestProgress getQuestProgress() {return _managerQuestProgress;}

    private ConfigMenu createConfigMenu()
    {
        ConfigMenu menu = new ConfigMenu(this, "ImusMiniQuests", CONSTANTS.PERM_CONFIG, this::reloadSettings);

        menu.addChoice("progress.mode", "Progress mode", Material.COMPASS, List.of("FIRST", "ALL"))
                .description("FIRST = only the first matching quest panel moves forward")
                .description("ALL = every matching quest panel moves forward")
                .note("now");
        menu.addBoolean("progress.action-bar", "Progress on action bar", Material.NAME_TAG)
                .note("now");
        menu.addBoolean("progress.count-spawner-mobs", "Spawner mobs count", Material.SPAWNER)
                .note("now");
        menu.addBoolean("progress.ignore-player-placed-blocks", "Ignore player placed blocks", Material.OAK_LOG)
                .description("Stops place-and-break farming of BREAK_BLOCK quests")
                .note("now");

        menu.addBoolean("drops.enabled", "Quest panels can be found", Material.CHEST)
                .description("Off = only /imq give hands them out")
                .note("now");
        menu.addDouble("drops.chest-chance", "Loot chest chance", Material.CHEST_MINECART, 0, 1)
                .description("Chance a vanilla loot chest has a quest panel, 0.15 = 15%")
                .note("chests opened from now on");
        menu.addDouble("drops.betterstructures-chest-chance", "BetterStructures chest chance", Material.BARREL, 0, 1)
                .note("chests filled from now on");
        menu.addDouble("drops.mob-kill-chance", "Mob kill drop chance", Material.ZOMBIE_HEAD, 0, 1)
                .description("0.01 = 1%")
                .note("now");
        menu.addBoolean("drops.only-hostile-mobs", "Only hostile mobs drop", Material.SKELETON_SKULL)
                .note("now");
        menu.addDouble("drops.block-break-chance", "Block break drop chance", Material.IRON_PICKAXE, 0, 1)
                .description("0.001 = 0.1%")
                .note("now");
        return menu;
    }

    private void registerCommands()
    {
        String _pluginName = "[ImusMiniQuests]";
        _cmdHelper = new CmdHelper(_pluginName);

        HashMap<String, String[]> cmd1AndArguments = new HashMap<>();
        CommandHandler handler = new CommandHandler(this);
        String cmd1 = "imusminiquests";
        handler.registerCmd(cmd1, new RootCmd());

        String cmd1_sub1 = "give";
        String full_sub1 = cmd1 + " " + cmd1_sub1;
        _cmdHelper.setCmd(full_sub1, "Give a quest panel", "/imq give <player> <quest> [amount]");
        handler.registerSubCmd(cmd1, cmd1_sub1, new SubGiveCmd(_cmdHelper.getCmdData(full_sub1)));
        handler.setPermissionOnLastCmd(CONSTANTS.PERM_GIVE);

        String cmd1_sub2 = "list";
        handler.registerSubCmd(cmd1, cmd1_sub2, new SubListCmd());
        handler.setPermissionOnLastCmd(CONSTANTS.PERM_LIST);

        String cmd1_sub3 = "complete";
        handler.registerSubCmd(cmd1, cmd1_sub3, new SubCompleteCmd());
        handler.setPermissionOnLastCmd(CONSTANTS.PERM_COMPLETE);

        String cmd1_sub4 = "reload";
        handler.registerSubCmd(cmd1, cmd1_sub4, new SubReloadCmd());
        handler.setPermissionOnLastCmd(CONSTANTS.PERM_RELOAD);

        String cmd1_sub5 = "config";
        handler.registerSubCmd(cmd1, cmd1_sub5, new SubConfigCmd(_configMenu));
        handler.setPermissionOnLastCmd(CONSTANTS.PERM_CONFIG);

        String cmd1_sub6 = "menu";
        handler.registerSubCmd(cmd1, cmd1_sub6, new SubMenuCmd());
        handler.setPermissionOnLastCmd(CONSTANTS.PERM_MENU);

        cmd1AndArguments.put(cmd1, new String[] { "menu", "give", "list", "complete", "reload", "config" });

        getCommand(cmd1).setExecutor(handler);

        _tab_cmd1 = new ImusTabCompleter(cmd1, cmd1AndArguments, CONSTANTS.PERM_TAB_COMPLETER);
        getCommand(cmd1).setTabCompleter(_tab_cmd1);
        updateTabCompleterRules();
    }

    private void updateTabCompleterRules()
    {
        if (_tab_cmd1 == null) return;

        List<String> questIds = List.copyOf(_managerQuests.getQuestIds());
        List<String> amounts = List.of("1", "5", "10");
        for (String label : new String[] { "/imq give", "/imusminiquests give" })
        {
            _tab_cmd1.SetRule(label, 3, questIds);
            _tab_cmd1.SetRule(label, 4, amounts);
        }
    }
}
