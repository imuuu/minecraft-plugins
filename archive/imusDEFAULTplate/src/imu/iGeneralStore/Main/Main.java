package imu.iGeneralStore.Main;

import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import imu.iGeneralStore.CMDs.Cmd;
import imu.iGeneralStore.Handlers.CommandHandler;
import imu.iGeneralStore.Other.ItemMetods;
import net.milkbowl.vault.economy.Economy;


public class Main extends JavaPlugin
{
	
	Economy _econ = null;
	ItemMetods _itemM;
	

	@Override
	public void onEnable() 
	{
		setupEconomy();
		_itemM = new ItemMetods(this);
			 
		 
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN +" [imusGeneralStore] has been activated!");
		registerCommands();
		
		
	}
	
	@Override
	 public void onDisable()
	{
		
	}
	
	public void registerCommands() 
	{
		 
		 CommandHandler handler = new CommandHandler(this);
		 String cmd1="igs";
	     handler.registerCmd(cmd1, new Cmd(this));
	     getCommand(cmd1).setExecutor(handler);

	}
	 
	
	

	public Economy get_econ() {
			return _econ;
		}

	public ItemMetods get_itemM() 
	{
		return _itemM;
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
}
