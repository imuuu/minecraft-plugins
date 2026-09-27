package imu.AC.Main;

import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import imu.AC.Other.CmdHelper;
import imu.AC.Xray.Managers.XrayManager;
import imu.iAPI.Handelers.CommandHandler;

import imu.iAPI.Other.ImusTabCompleter;
import imu.iAPI.Other.MySQL;

public class Main extends JavaPlugin
{

	MySQL _SQL;
	ImusTabCompleter _tab_cmd1;
	CmdHelper _cmdHelper;
	ProtocolManager _pManager;
	
	XrayManager _xRayManager;
	@Override
	public void onEnable() 
	{
		_pManager = ProtocolLibrary.getProtocolManager();
		ConnectDataBase();
		
		_xRayManager = new XrayManager(this);
		_cmdHelper = new CmdHelper(this);
		
		
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN +" [imusAntiCheat] has been activated!");
		
		registerCommands();
		
		
	}
	
	public ProtocolManager GetProtocolManager()
	{
		return _pManager;
	}
	
	@Override
	 public void onDisable()
	{
				
		if(_SQL != null)
			_SQL.Disconnect();
		
		
	}
	
	void ConnectDataBase()
	{
		_SQL = new MySQL(this, "imusAntiCheat");
		try {
			_SQL.Connect();
			Bukkit.getLogger().info(ChatColor.GREEN +"[imusAntiCheat] Database Connected!");
		} 
		catch (ClassNotFoundException | SQLException e) {

			Bukkit.getLogger().info(ChatColor.RED +"[imusAntiCheat] Database not connected");
		}
	}
	
	public void registerCommands() 
	{
		HashMap<String, String[]> cmd1AndArguments = new HashMap<>();
		CommandHandler handler = new CommandHandler(this);
//		String cmd1="gs";
//	    handler.registerCmd(cmd1, new Cmd(this));
//	     	     
//	    String cmd1_sub1 = "create";
//	    String full_sub1 = cmd1+" "+cmd1_sub1;
//	    _cmdHelper.setCmd(full_sub1, "Create Shop", full_sub1 + " [ShopName]");
//	    handler.registerSubCmd(cmd1, cmd1_sub1, new SubShopCreateCMD(this, _cmdHelper.getCmdData(full_sub1)));
	    
	    
	    
	     
	    
	    //cmd1AndArguments.put(cmd1, new String[] {"create","open","add", "modify","assign","setprice"});
	  
//	    getCommand(cmd1).setExecutor(handler);
//	    _tab_cmd1 = new ImusTabCompleter(cmd1, cmd1AndArguments);
//	    getCommand(cmd1).setTabCompleter(_tab_cmd1);
	  
	}	
		
	
	
	public MySQL GetSQL()
	{
		return _SQL;
	}

	public ImusTabCompleter get_tab_cmd1() {
		return _tab_cmd1;
	}
	
//	boolean setupImusApi()
//	{
//		if(Bukkit.getPluginManager().getPlugin("imusAPI") != null)
//		{
//			_imusAPI = (ImusAPI) Bukkit.getPluginManager().getPlugin("imusAPI");
//			return true;
//		}
//		return false;
//	}
	
//	public ImusAPI getImusAPI()
//	{
//		return _imusAPI;
//	}

	
}
