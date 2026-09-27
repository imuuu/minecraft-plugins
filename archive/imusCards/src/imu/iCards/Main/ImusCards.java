package imu.iCards.Main;

import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import imu.iAPI.Handelers.CommandHandler;
import imu.iAPI.Other.ImusTabCompleter;
import imu.iAPI.Other.MySQL;
import imu.iCards.Invs.Inv_CreateCard;
import imu.iCards.Managers.CardManager;
import imu.iCards.Other.CmdHelper;
import imu.iCards.SubCmds.CMD;
import imu.iCards.SubCmds.SubCreateCardCmd;


public class ImusCards extends JavaPlugin
{
	public static ImusCards _instance;
	private MySQL _SQL;
	
	private CmdHelper _cmdHelper;
	final private String _pluginName = "[imusCards]";

	//final public Permission perm_buildIngnore = new Permission("ic.build.ingnore");
	
	//MANAGERS
	private CardManager _cardManager;
	
	private ImusTabCompleter _tab_cmd1;
	@Override
	public void onEnable()
	{
		_instance = this;
		if (!ConnectDataBase())
		{
			getServer().getConsoleSender().sendMessage(
					ChatColor.RED + _pluginName + " has been disabled due to unable to connect to database!");
			getServer().getConsoleSender()
					.sendMessage(ChatColor.RED + _pluginName + " Plugin folder you can adjust database settings");
			return;
		}

		_cmdHelper = new CmdHelper();

		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + _pluginName + " has been activated!");

		RegisterCommands();
		
		_cardManager = new CardManager();

	}

	void RegisterPermissions()
	{
		//Bukkit.getPluginManager().addPermission(perm_buildIngnore);
	}

	@Override
	public void onDisable()
	{

		if (_SQL != null)
			_SQL.Disconnect();

	}

	boolean ConnectDataBase()
	{
		_SQL = new MySQL(this, "imusWaystones");
		try
		{
			_SQL.Connect();
			Bukkit.getLogger().info(ChatColor.GREEN + _pluginName + " Database Connected!");
			return true;
		} catch (ClassNotFoundException | SQLException e)
		{

			Bukkit.getLogger().info(ChatColor.GREEN + _pluginName + " Database not connected");
		}

		return false;
	}

	public void RegisterCommands()
	{
		HashMap<String, String[]> cmd1AndArguments = new HashMap<>();
		CommandHandler handler = new CommandHandler(this);
		String cmd1 = "ic";
		handler.registerCmd(cmd1, new CMD());

		String cmd1_sub1 = "create card";
		String full_sub1 = cmd1 + " " + cmd1_sub1;
		_cmdHelper.setCmd(full_sub1, "Creat Card", full_sub1);		
		handler.registerSubCmd(cmd1, cmd1_sub1, new SubCreateCardCmd(_cmdHelper.getCmdData(full_sub1)));
		handler.setPermissionOnLastCmd("ic.create.card");
		
		
		cmd1AndArguments.put(cmd1, new String[] {"create"});
		cmd1AndArguments.put("create", new String[] {"card"});
		
		//register cmds
		getCommand(cmd1).setExecutor(handler);
		
		
		//register tabcompleters
		_tab_cmd1 = new ImusTabCompleter(cmd1, cmd1AndArguments, "ic.tabcompleter");
		getCommand(cmd1).setTabCompleter(_tab_cmd1);
		
	}

	public MySQL GetSQL()
	{
		return _SQL;
	}

	public ImusTabCompleter Get_tab_cmd()
	{
		return _tab_cmd1;
	}

}
