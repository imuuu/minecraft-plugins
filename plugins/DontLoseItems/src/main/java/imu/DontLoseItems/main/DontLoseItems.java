package imu.DontLoseItems.main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import imu.DontLoseItems.CustomEnd.EndEvents;
import imu.DontLoseItems.Events.ChestLootEvents;
import imu.DontLoseItems.Events.DotEvents;
import imu.DontLoseItems.Events.ElytraGenerationEvents;
import imu.DontLoseItems.Events.EndChestLootEvents;
import imu.DontLoseItems.Events.MainEvents;
import imu.DontLoseItems.Events.NetherEvents;
import imu.DontLoseItems.Events.VoidTotemEvents;
import imu.DontLoseItems.Managers.Manager_Difficult;
import imu.DontLoseItems.Managers.Manager_HellArmor;
import imu.DontLoseItems.Managers.Manager_HellTools;
import imu.DontLoseItems.Managers.Manager_IronArmor;
import imu.DontLoseItems.Managers.Manager_LegendaryUpgrades;
import imu.DontLoseItems.Managers.Manager_VoidStones;
import imu.DontLoseItems.other.AntiAfk;
import imu.iAPI.CmdUtil.CmdHelper;
import imu.iAPI.Other.ImusTabCompleter;

public class DontLoseItems extends JavaPlugin
{
	public static DontLoseItems Instance;
	final private String _pluginName = "[DontLoseItems]";

	@SuppressWarnings("unused")
	private Manager_LegendaryUpgrades _manager_leg_upgrades;

	private CmdHelper _cmdHelper;
	private ImusTabCompleter _tab_cmd1;

	@Override
	public void onEnable()
	{
		Instance = this;
		_manager_leg_upgrades = new Manager_LegendaryUpgrades();
		RegisterCommands();
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + " Dont lose your items has been activated!");
		getServer().getPluginManager().registerEvents(new MainEvents(this), this);
		getServer().getPluginManager().registerEvents(new DotEvents(), this);
		getServer().getPluginManager().registerEvents(new NetherEvents(), this);
		getServer().getPluginManager().registerEvents(new EndEvents(), this);
		
		getServer().getPluginManager().registerEvents(new Manager_VoidStones(), this);
		getServer().getPluginManager().registerEvents(new Manager_HellArmor(), this);
		getServer().getPluginManager().registerEvents(new Manager_HellTools(), this);
		getServer().getPluginManager().registerEvents(new ChestLootEvents(), this);
		getServer().getPluginManager().registerEvents(new EndChestLootEvents(), this);
		getServer().getPluginManager().registerEvents(new ElytraGenerationEvents(), this);
		getServer().getPluginManager().registerEvents(new VoidTotemEvents(), this);
		getServer().getPluginManager().registerEvents(new AntiAfk(), this);
		getServer().getPluginManager().registerEvents(new Manager_IronArmor(), this);
		getServer().getPluginManager().registerEvents(new Manager_Difficult(), this);
		
		// getServer().getPluginManager().registerEvents(new FishingEvent(this), this);
		
		_manager_leg_upgrades.SetTestItems();
		Manager_HellArmor.Instance.SetArmorTestItems();
		Manager_VoidStones.Instance.SetTestItems();
	}

	@Override
	public void onDisable()
	{
		NetherEvents.Instance.OnDisabled();
		Manager_HellTools.Instance.OnDisable();
		EndEvents.Instance.OnDisabled();
	}

	public void RegisterCommands()
	{
//			_cmdHelper = new CmdHelper(_pluginName);
//			
//			HashMap<String, String[]> cmd1AndArguments = new HashMap<>();
//			CommandHandler handler = new CommandHandler(this);
//			String cmd1 = "dl";
//			handler.registerCmd(cmd1, new ExampleCmd());
//
//			String cmd1_sub1 = "inv";
//			String full_sub1 = cmd1 + " " + cmd1_sub1;
//			_cmdHelper.setCmd(full_sub1, "Open Custom Items", full_sub1);
//			handler.registerSubCmd(cmd1, cmd1_sub1, new SubOpenCustomItems_InvCmd(_cmdHelper.getCmdData(full_sub1)));
//			handler.setPermissionOnLastCmd("dl.inv");
//
//			cmd1AndArguments.put(cmd1, new String[] { "inv" });
//			// cmd1AndArguments.put("create", new String[] {"card"});
//
//			// register cmds
//			getCommand(cmd1).setExecutor(handler);
//
//			// register tabcompleters
//			_tab_cmd1 = new ImusTabCompleter(cmd1, cmd1AndArguments, "dl.tabcompleter");
//			getCommand(cmd1).setTabCompleter(_tab_cmd1);

	}
	
	
	public static boolean HasServerImusEnchants()
	{
		 Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("ImusEnchants");
		 
		 return (plugin != null && plugin.isEnabled());
	}
	
	@SuppressWarnings("unused")
	public static boolean IsEnd(World world)
	{
		if (world == null)
			return false;

		return world.getEnvironment() == Environment.THE_END;
	}

	@SuppressWarnings("unused")
	public static boolean IsEnd(Entity entity)
	{
		if (entity == null)
			return false;

		return IsEnd(entity.getWorld());
	}

	@SuppressWarnings("unused")
	public static boolean IsEnd(Block block)
	{
		if (block == null)
			return false;

		return IsEnd(block.getWorld());
	}

	@SuppressWarnings("unused")
	public static boolean IsEnd(Location loc)
	{
		if (loc == null)
			return false;

		return IsEnd(loc.getWorld());
	}

}
