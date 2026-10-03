package me.imu.imusenchants;

import java.util.HashMap;

import me.imu.imusenchants.Commands.ImusEnchantsCmd;
import me.imu.imusenchants.CustomEnchants.CustomEnchantRegistry;
import me.imu.imusenchants.CustomEnchants.EnchantSettings;
import me.imu.imusenchants.Events.AnvilEvents;
import me.imu.imusenchants.Events.CustomEnchantEvents;
import me.imu.imusenchants.Events.BetterStructuresEvents;
import me.imu.imusenchants.Events.Events;
import me.imu.imusenchants.Events.SlotCoreEvents;
import me.imu.imusenchants.Events.VanillaEnchantFilter;
import me.imu.imusenchants.Events.VillagerEvents;
import me.imu.imusenchants.Managers.ManagerEnchants;
import me.imu.imusenchants.SubCommands.SubOpenEnchant_InvCmd;
import me.imu.imusenchants.Inventories.InventoryEnchanting;
import me.imu.imusenchants.CustomEnchants.TemporaryBlocks;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import imu.iAPI.CmdUtil.CmdHelper;
import imu.iAPI.Commands.ExampleCmd;
import imu.iAPI.Handelers.CommandHandler;
import imu.iAPI.Other.ImusTabCompleter;

public class ImusEnchants extends JavaPlugin
{
    public static ImusEnchants Instance;

    public ManagerEnchants _managerEnchants;

    final private String _pluginName = "[imusEnchants]";

    private CustomEnchantEvents _customEnchantEvents;

    private CmdHelper _cmdHelper;
    private ImusTabCompleter _tab_cmd1;
    @Override
    public void onEnable()
    {
        Instance = this;
        _managerEnchants = new ManagerEnchants();
        CustomEnchantRegistry.RegisterDefaults();
        EnchantSettings.Load(this);

        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + _pluginName+" is Activated");

        getServer().getPluginManager().registerEvents(new Events(), this);
        getServer().getPluginManager().registerEvents(new VillagerEvents(), this);
        getServer().getPluginManager().registerEvents(new AnvilEvents(), this);
        getServer().getPluginManager().registerEvents(new SlotCoreEvents(), this);
        _customEnchantEvents = new CustomEnchantEvents(this);
        getServer().getPluginManager().registerEvents(_customEnchantEvents, this);
        // Chunks loaded before the plugin (spawn chunks) don't fire ChunkLoadEvent for it
        TemporaryBlocks.RestoreLeftoversInLoadedChunks();
        getServer().getPluginManager().registerEvents(new VanillaEnchantFilter(), this);

        if (getServer().getPluginManager().isPluginEnabled("BetterStructures"))
        {
            getServer().getPluginManager().registerEvents(new BetterStructuresEvents(), this);
            getServer().getConsoleSender().sendMessage(ChatColor.GREEN + _pluginName + " BetterStructures support enabled");
        }
        RegisterCommands();
    }

    @Override
    public void onDisable()
    {
        InventoryEnchanting.CloseAll();
        if (_customEnchantEvents != null)
            _customEnchantEvents.OnDisable();
    }

    public void RegisterCommands()
    {
        ImusEnchantsCmd cmd = new ImusEnchantsCmd();
        getCommand("imusenchants").setExecutor(cmd);
        getCommand("imusenchants").setTabCompleter(cmd);

        /*_cmdHelper = new CmdHelper(_pluginName);

        HashMap<String, String[]> cmd1AndArguments = new HashMap<>();
        CommandHandler handler = new CommandHandler(this);
        String cmd1 = "imusenchants";
        handler.registerCmd(cmd1, new ExampleCmd());

        String cmd1_sub1 = "inv";
        String full_sub1 = cmd1 + " " + cmd1_sub1;
        _cmdHelper.setCmd(full_sub1, "Open Enchant Inv", full_sub1);
        handler.registerSubCmd(cmd1, cmd1_sub1, new SubOpenEnchant_InvCmd(_cmdHelper.getCmdData(full_sub1)));
        handler.setPermissionOnLastCmd("ie.inv");

        cmd1AndArguments.put(cmd1, new String[] { "inv" });
        // cmd1AndArguments.put("create", new String[] {"card"});

        // register cmds
        getCommand(cmd1).setExecutor(handler);

        // register tabcompleters
        _tab_cmd1 = new ImusTabCompleter(cmd1, cmd1AndArguments, "it.tabcompleter");
        getCommand(cmd1).setTabCompleter(_tab_cmd1);*/

    }

}
