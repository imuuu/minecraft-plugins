package imu.imusTNT.main;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import imu.iAPI.CmdUtil.CmdHelper;
import imu.iAPI.Commands.ExampleCmd;
import imu.iAPI.Handelers.CommandHandler;
import imu.iAPI.Other.ImusTabCompleter;
import imu.imusTNT.Events.imusTNT_events;
import imu.imusTNT.SubCommands.SubOpenTNT_InvCmd;
import imu.imusTNT.TNTs.TNT_Mananger;

public class ImusTNT extends JavaPlugin
{
	public static ImusTNT Instance;
	
	final private String _pluginName = "[imusCards]";

	private CmdHelper _cmdHelper;
	private ImusTabCompleter _tab_cmd1;
	private TNT_Mananger _managerTNT;
	@Override
	public void onEnable()
	{
		Instance = this;
		_managerTNT = new TNT_Mananger();
		
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + _pluginName+" is Activated");
		getServer().getPluginManager().registerEvents(new imusTNT_events(), this);
		
		RegisterCommands();
		// getServer().getPluginManager().registerEvents(new FishingEvent(this), this);
	}

	public void RegisterCommands()
	{
		_cmdHelper = new CmdHelper(_pluginName);
		
		HashMap<String, String[]> cmd1AndArguments = new HashMap<>();
		CommandHandler handler = new CommandHandler(this);
		String cmd1 = "it";
		handler.registerCmd(cmd1, new ExampleCmd());

		String cmd1_sub1 = "inv";
		String full_sub1 = cmd1 + " " + cmd1_sub1;
		_cmdHelper.setCmd(full_sub1, "Open TNT inv", full_sub1);
		handler.registerSubCmd(cmd1, cmd1_sub1, new SubOpenTNT_InvCmd(_cmdHelper.getCmdData(full_sub1)));
		handler.setPermissionOnLastCmd("it.inv");

		cmd1AndArguments.put(cmd1, new String[] { "inv" });
		// cmd1AndArguments.put("create", new String[] {"card"});

		// register cmds
		getCommand(cmd1).setExecutor(handler);

		// register tabcompleters
		_tab_cmd1 = new ImusTabCompleter(cmd1, cmd1AndArguments, "it.tabcompleter");
		getCommand(cmd1).setTabCompleter(_tab_cmd1);

	}

}
