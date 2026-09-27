package imu.iWaystones.Main;

import java.util.HashMap;

import imu.iWaystones.SubCmds.SubWaystoneReloadCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import imu.iAPI.Handelers.CommandHandler;
import imu.iAPI.Other.ImusTabCompleter;
import imu.iAPI.Other.MySQL;
import imu.iWaystones.Managers.WaystoneManager;
import imu.iWaystones.Managers.WaystoneManagerSQL;
import imu.iWaystones.Other.CmdHelper;
import imu.iWaystones.SubCmds.CMD;
import imu.iWaystones.SubCmds.SubCMDWaystoneList;
import imu.iWaystones.SubCmds.SubWaystoneConfirmationCmd;
import imus.iWaystones.Events.WaystoneEvents;
import org.bukkit.scheduler.BukkitRunnable;

public class ImusWaystones extends JavaPlugin
{
	public static ImusWaystones _instance;
	MySQL _SQL;
	ImusTabCompleter _tab_cmd1;
	CmdHelper _cmdHelper;
	final private String _pluginName = "[imusWaystones]";
	
	private WaystoneManager _waystoneManagers;
	final public Permission perm_buildIngnore = new Permission("iw.build.ingnore");
	final public Permission perm_reload = new Permission("iw.reload");
	@Override
	public void onEnable() 
	{
		_instance = this;	
		if(!ConnectDataBase())
		{
			getServer().getConsoleSender().sendMessage(ChatColor.RED +_pluginName+" has been disabled due to unable to connect to database!");
			getServer().getConsoleSender().sendMessage(ChatColor.RED +_pluginName+" Plugin folder you can adjust database settings");
			return;
		}
		
		_waystoneManagers = new WaystoneManager();
		
		_cmdHelper = new CmdHelper();
		
		
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN +_pluginName+" has been activated!");
		
		registerCommands();
		
		new WaystoneEvents();
		
		_waystoneManagers.Init();

	}
	
	void RegisterPermissions()
	{
		Bukkit.getPluginManager().addPermission(perm_buildIngnore);
	}
	@Override
	 public void onDisable()
	{
		if(_waystoneManagers != null) _waystoneManagers.OnDisable();	
		
		//if(_SQL != null) _SQL.Disconnect();
			
		
		
	}
	
	boolean ConnectDataBase()
	{
		_SQL = new MySQL(this, 10,"imusWaystones");		
		return true;
	}
	
	public void registerCommands() 
	{
		HashMap<String, String[]> cmd1AndArguments = new HashMap<>();
		CommandHandler handler = new CommandHandler(this);
		String cmd1="iw";
	    handler.registerCmd(cmd1, new CMD());
	    
	    String cmd1_sub2 = "list"; 
		String full_sub2 = cmd1 + " " + cmd1_sub2;
		_cmdHelper.setCmd(full_sub2, "Open waystone list", full_sub2);
		handler.registerSubCmd(cmd1, cmd1_sub2, new SubCMDWaystoneList(_cmdHelper.getCmdData(full_sub2)));
		handler.setPermissionOnLastCmd("iw.list");

	    String cmd1_sub1 = "confirm";
	    String full_sub1 = cmd1+" "+cmd1_sub1;
	    _cmdHelper.setCmd(full_sub1, "Confirm waystone", full_sub1);
	    handler.registerSubCmd(cmd1, cmd1_sub1, new SubWaystoneConfirmationCmd(_cmdHelper.getCmdData(full_sub1)));

		String cmd1_sub3 = "reload";
		String full_sub3 = cmd1 + " " + cmd1_sub3;
		_cmdHelper.setCmd(full_sub3, "Reload plugin", full_sub3);
		handler.registerSubCmd(cmd1, cmd1_sub3, new SubWaystoneReloadCommand(_cmdHelper.getCmdData(full_sub3)));
		handler.setPermissionOnLastCmd("iw.reload");
	    
	    getCommand(cmd1).setExecutor(handler);
	        
	    cmd1AndArguments.put(cmd1, new String[] { "list", "reload"});

		//cmd1AndArguments.put("inv", new String[] {"test123"});

		// register cmds
		

		// register tabcompleters
		_tab_cmd1 = new ImusTabCompleter(cmd1, cmd1AndArguments, "iw.tabcompleter");

		getCommand(cmd1).setTabCompleter(_tab_cmd1);
	}	

	public WaystoneManager GetWaystoneManager()
	{
		return _waystoneManagers;
	}
	
	public WaystoneManagerSQL GetWaystoneManagerSQL()
	{
		return _waystoneManagers.GetWaystoneManagerSQL();
	}
	
	public MySQL GetSQL()
	{
		return _SQL;
	}

	public ImusTabCompleter get_tab_cmd1() {
		return _tab_cmd1;
	}
	
}
