package imu.iGeneralStore.Main;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import imu.iGeneralStore.CMDs.Cmd;
import imu.iGeneralStore.Handlers.CommandHandler;
import imu.iGeneralStore.Managers.ShopManager;
import imu.iGeneralStore.Other.CmdHelper;
import imu.iGeneralStore.Other.ImusAPI;
import imu.iGeneralStore.SubCmds.subShopCreateCMD;
import imu.iGeneralStore.SubCmds.subShopOpenCMD;
import imu.iGeneralStore.TabCompletes.Cmd1_tab;
import net.milkbowl.vault.economy.Economy;


public class Main extends JavaPlugin
{
	ShopManager _shopManager;
	

	CmdHelper _cmdHelper;
	Economy _econ = null;
	ImusAPI _ia;
	
	Cmd1_tab _tab_cmd1;
	

	@Override
	public void onEnable() 
	{
		setupEconomy();
		_cmdHelper = new CmdHelper(this);
		_ia = new ImusAPI(this);
		
		// MANAGERS
		_shopManager = new ShopManager(this);
		_shopManager.loadShopsAsync();
		 
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN +" [imusGeneralStore] has been activated!");
		
		registerCommands();
		_shopManager.updateTabCompliters();
		
	}
	
	@Override
	 public void onDisable()
	{
		_shopManager.onDisabled();
	}
	
	public void registerCommands() 
	{
		HashMap<String, String[]> cmd1AndArguments = new HashMap<>();
		CommandHandler handler = new CommandHandler(this);
		String cmd1="igs";
	    handler.registerCmd(cmd1, new Cmd(this));
	     	     
	    String cmd1_sub1 = "create";
	    String full_sub1 = cmd1+" "+cmd1_sub1;
	    _cmdHelper.setCmd(full_sub1, "Create Shop", full_sub1 + " [ShopName]");
	    handler.registerSubCmd(cmd1, cmd1_sub1, new subShopCreateCMD(this, _cmdHelper.getCmdData(full_sub1)));
	    
	    String cmd1_sub2 = "open shop";
	    String full_sub2 = cmd1+" "+cmd1_sub2;
	    _cmdHelper.setCmd(full_sub2, "Open the Shop", full_sub2 + " [ShopName]");
	    handler.registerSubCmd(cmd1, cmd1_sub2, new subShopOpenCMD(this, _cmdHelper.getCmdData(full_sub2)));
	     
	    
	    cmd1AndArguments.put(cmd1, new String[] {"create","open"});
	    cmd1AndArguments.put("open", new String[] {"shop"});
	    cmd1AndArguments.put("create", new String[] {"shop"});
	    
	    getCommand(cmd1).setExecutor(handler);
	    _tab_cmd1 = new Cmd1_tab(this, cmd1, cmd1AndArguments);
	    getCommand(cmd1).setTabCompleter(_tab_cmd1);
	    

	}	
	
	public Cmd1_tab get_tab_cmd1() {
		return _tab_cmd1;
	}
	
	public Economy get_econ() {
			return _econ;
		}

	public ImusAPI get_ia() 
	{
		return _ia;
	}
	
	public ShopManager get_shopManager() {
		return _shopManager;
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
