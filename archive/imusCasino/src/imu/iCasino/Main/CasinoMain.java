package imu.iCasino.Main;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import imu.iAPI.Handelers.CommandHandler;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.ImusTabCompleter;
import imu.iAPI.Other.Metods;
import imu.iCasino.Commands.IcCMD;
import imu.iCasino.Commands.Sub.SubCreateTable;
import net.milkbowl.vault.economy.Economy;


public class CasinoMain extends JavaPlugin
{
	private Economy _econ = null;
	private ImusAPI _imusAPI;
	private Metods _metods;
	
	@Override
	public void onEnable() 
	{
		setupEconomy();
		setupImusApi();
		_metods = new Metods(this);

		getServer().getConsoleSender().sendMessage(ChatColor.GREEN +" imusCasino has been activated!");
		registerCommands();
		
	}
	
	@Override
	 public void onDisable()
	{
		
	}
	
	public void registerCommands() 
	{
		 
		 CommandHandler handler = new CommandHandler(this);
		 
		 String cmd1 ="ic";
	     handler.registerCmd(cmd1, new IcCMD(this));
	     handler.setPermissionOnLastCmd("ic");
	     handler.registerSubCmd(cmd1, "create", new SubCreateTable(this));
	     getCommand(cmd1).setExecutor(handler);
	     
	     
	     //tab completer cmd1
	     HashMap<String, String[]> tabCmd1 = new HashMap<>();
	     tabCmd1.put(cmd1,new String[] {"create"});
	     getCommand(cmd1).setTabCompleter(new ImusTabCompleter(cmd1, tabCmd1));
	     
	 }
	 
	public Economy get_econ() {
			return _econ;
		}


	boolean setupEconomy() 
	{
        if (getServer().getPluginManager().getPlugin("Vault") == null) 
        {
        	System.out.println("Vault not found");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        
        if (rsp == null) {
            return false;
        }
        _econ = rsp.getProvider();
        return _econ != null;
    }
	
	boolean setupImusApi()
	{
		if(Bukkit.getPluginManager().getPlugin("imusAPI") != null)
		{
			_imusAPI = (ImusAPI) Bukkit.getPluginManager().getPlugin("imusAPI");
			return true;
		}
		return false;
	}
	
	public ImusAPI getImusAPI()
	{
		return _imusAPI;
	}
	public Metods getMetods()
	{
		return _metods;
	}
}
