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
    private ManagerEconomy _managerEconomy;
    private ManagerQuestPoints _managerQuestPoints;
    private ManagerRewardPools _managerRewardPools;
    private ManagerUnlocks _managerUnlocks;
    private ManagerRarities _managerRarities;

    @Override
    public void onEnable()
    {
        _instance = this;
        saveDefaultConfig();
        addMissingSettings();
        QuestItem.init(this);
        _managerEconomy = new ManagerEconomy(this);
        _managerQuestPoints = new ManagerQuestPoints(this);
        _managerRarities = new ManagerRarities(this);
        _managerRarities.load();
        _managerUnlocks = new ManagerUnlocks(this);
        _managerUnlocks.load();
        _managerRewardPools = new ManagerRewardPools(this);
        _managerRewardPools.load();

        _managerQuests = new ManagerQuests(this);
        _managerQuests.load();
        _managerPlacedBlocks = new ManagerPlacedBlocks(this);
        _managerQuestDrops = new ManagerQuestDrops(this, _managerQuests);
        _managerQuestProgress = new ManagerQuestProgress(this, _managerQuests, _managerPlacedBlocks, _managerQuestDrops);

        getServer().getPluginManager().registerEvents(_managerQuestProgress, this);
        getServer().getPluginManager().registerEvents(new ManagerQuestItemGuard(this, _managerQuests), this);
        getServer().getPluginManager().registerEvents(_managerQuestDrops, this);
        getServer().getPluginManager().registerEvents(_managerUnlocks, this);
        if (getServer().getPluginManager().isPluginEnabled("BetterStructures"))
            getServer().getPluginManager().registerEvents(new ManagerBetterStructuresLoot(_managerQuestDrops), this);

        _configMenu = createConfigMenu();
        _configMenu.register();
        registerCommands();
        _managerEconomy.start();
        getLogger().info("Loaded " + _managerQuests.getQuests().size() + " mini quests");
    }

    @Override
    public void onDisable()
    {
        if (_configMenu != null)
            _configMenu.unregister();
        if (_managerEconomy != null)
            _managerEconomy.stop();
        if (_managerUnlocks != null)
            _managerUnlocks.stop();
    }

    /**
     * Re-reads config.yml, rewards.yml and quests.yml. Quest items already in inventories keep their progress
     * and pick up the new name, lore and amount the next time they move forward.
     */
    public void reloadSettings()
    {
        reloadConfig();
        addMissingSettings();
        _managerRarities.load();
        _managerUnlocks.load();
        _managerRewardPools.load();
        _managerQuests.load();
        _managerQuestDrops.reload();
        _managerEconomy.start();
        updateTabCompleterRules();
    }

    /**
     * Writes settings added in an update (rarities, unlocks...) into an existing config.yml.
     * Without this, Bukkit hands an empty section for a section the file doesn't have, instead
     * of the one in the jar's config.yml, so whole features would be silently off.
     */
    private void addMissingSettings()
    {
        getConfig().options().copyDefaults(true);
        saveConfig();
        // Read it back, so the added sections are real sections and not empty placeholders
        reloadConfig();
    }

    /**
     * A message from the messages section of config.yml with & colours translated.
     */
    public String getMessage(String key)
    {
        // getString(path) falls back to the jar's config.yml for messages added in an update
        String message = getConfig().getString("messages." + key);
        return Metods.msgC(message != null ? message : key);
    }

    public ManagerQuests getQuests() {return _managerQuests;}

    public ManagerQuestProgress getQuestProgress() {return _managerQuestProgress;}

    public ManagerEconomy getEconomy() {return _managerEconomy;}

    public ManagerQuestPoints getQuestPoints() {return _managerQuestPoints;}

    public ManagerRewardPools getRewardPools() {return _managerRewardPools;}

    public ManagerUnlocks getUnlocks() {return _managerUnlocks;}

    public ManagerRarities getRarities() {return _managerRarities;}

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
        menu.addDouble("drops.chest-chance", "Loot chest chance", Material.CHEST_MINECART, 0, 3)
                .description("Chance a vanilla loot chest has a quest panel, 0.25 = 25%")
                .description("Above 1 can give more than one panel")
                .note("chests opened from now on");
        menu.addDouble("drops.betterstructures-chest-chance", "BetterStructures chest chance", Material.BARREL, 0, 3)
                .note("chests filled from now on");
        menu.addDouble("drops.world-multiplier.overworld", "Overworld drop multiplier", Material.GRASS_BLOCK, 0, 10)
                .description("Every drop chance in the overworld is multiplied by this")
                .note("now");
        menu.addDouble("drops.world-multiplier.nether", "Nether drop multiplier", Material.NETHERRACK, 0, 10)
                .description("2 = panels twice as likely in the Nether")
                .note("now");
        menu.addDouble("drops.world-multiplier.end", "End drop multiplier", Material.END_STONE, 0, 10)
                .description("4 = panels four times as likely in the End")
                .note("now");
        menu.addDouble("drops.mob-kill-chance", "Mob kill drop chance", Material.ZOMBIE_HEAD, 0, 1)
                .description("0.01 = 1%")
                .note("now");
        menu.addBoolean("drops.only-hostile-mobs", "Only hostile mobs drop", Material.SKELETON_SKULL)
                .note("now");
        menu.addDouble("drops.block-break-chance", "Block break drop chance", Material.IRON_PICKAXE, 0, 1)
                .description("0.001 = 0.1%")
                .note("now");

        menu.addBoolean("quest-points.enabled", "Quest points and luck", Material.EXPERIENCE_BOTTLE)
                .description("Every claimed quest gives a quest point, and points")
                .description("make the player's later rewards better")
                .note("now");
        menu.addDouble("quest-points.luck-per-point", "Luck per quest point", Material.RABBIT_FOOT, 0, 1)
                .description("0.01 = +1% luck for every quest claimed")
                .note("now");
        menu.addDouble("quest-points.max-luck", "Most luck", Material.GOLDEN_CARROT, 0, 1)
                .description("0.5 = halfway to every reward being equally likely")
                .note("now");

        menu.addInt("random-rewards.rolls-per-quest", "Random rewards per quest", Material.ENDER_CHEST, 0, 10)
                .description("Rewards from the shared rarity tiers in rewards.yml,")
                .description("on top of each quest's own rewards. A quest's own")
                .description("rewards.random-rolls overrides this")
                .note("now");

        menu.addBoolean("imuschallenges.enabled", "ImusChallenges points", Material.NETHER_STAR)
                .description("Claiming a quest gives ImusChallenges challenge points")
                .description("Needs ImusChallenges on the server")
                .note("now");
        menu.addInt("imuschallenges.points-per-quest", "Challenge points per quest", Material.GOLD_NUGGET, 0, 1000)
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
        _cmdHelper.setCmd(full_sub1, "Give a quest panel", "/imq give <player> <quest> [amount] [rarity]");
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

        String cmd1_sub7 = "economy";
        handler.registerSubCmd(cmd1, cmd1_sub7, new SubEconomyCmd());
        handler.setPermissionOnLastCmd(CONSTANTS.PERM_ECONOMY);

        String cmd1_sub8 = "points";
        handler.registerSubCmd(cmd1, cmd1_sub8, new SubPointsCmd());
        handler.setPermissionOnLastCmd(CONSTANTS.PERM_POINTS);

        String cmd1_sub9 = "unlocks";
        handler.registerSubCmd(cmd1, cmd1_sub9, new SubUnlocksCmd());
        handler.setPermissionOnLastCmd(CONSTANTS.PERM_UNLOCKS);

        cmd1AndArguments.put(cmd1, new String[] { "points", "unlocks", "menu", "give", "list", "complete", "economy", "reload", "config" });
        cmd1AndArguments.put("economy", new String[] { "refresh" });

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
            _tab_cmd1.SetRule(label, 5, List.copyOf(_managerRarities.getNames()));
        }
    }
}
