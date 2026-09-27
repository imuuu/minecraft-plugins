package imu.AccountBoundItems.main;


import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import imu.AccountBoundItems.Commands.BoundCommand;
import imu.AccountBoundItems.Events.DropAndPickup;
import imu.AccountBoundItems.Events.OnDamage;
import imu.AccountBoundItems.Events.OnPlayerInteract;
import imu.AccountBoundItems.Handlers.CommandHandler;
import imu.AccountBoundItems.Other.ConfigMaker;
import imu.AccountBoundItems.Other.Cooldowns;
import imu.AccountBoundItems.SubCommands.subBindCostCmd;
import imu.AccountBoundItems.SubCommands.subBindOnUseWaitCmd;
import imu.AccountBoundItems.SubCommands.subBindWaitCmd;
import imu.AccountBoundItems.SubCommands.subBoundCmd;
import imu.AccountBoundItems.SubCommands.subBrokenCmd;
import imu.AccountBoundItems.SubCommands.subGetMoneyCmd;
import imu.AccountBoundItems.SubCommands.subRedeemCmd;
import imu.AccountBoundItems.SubCommands.subReloadCmd;
import imu.AccountBoundItems.SubCommands.subRepairAllCmd;
import imu.AccountBoundItems.SubCommands.subRepairCmd;
import imu.AccountBoundItems.SubCommands.subRepairCostCmd;
import imu.AccountBoundItems.SubCommands.subSetPriceCmd;
import imu.AccountBoundItems.SubCommands.subUnBoundCmd;
import net.milkbowl.vault.economy.Economy;


public class Main extends JavaPlugin
{
	public HashMap<String, String> loreNames = new HashMap<String, String>();
	public HashMap<String, String> keyNames = new HashMap<String, String>();
	
	public HashMap<String, Double> enchPrices = new HashMap<String, Double>();
	public HashMap<String, Double> lorePrices = new HashMap<String, Double>();
	public HashMap<Material, Double> materialPrices = new HashMap<Material, Double>();
	public HashMap<Enchantment,Double[]> enchExPrices = new HashMap<Enchantment, Double[]>();
	
	public HashMap<Player, Cooldowns> playerCds = new HashMap<Player, Cooldowns>();
	
	static Main instance;
	
	public double repairPricePros = 60;
	public double deadDropPricePros = 50;
	public double defaultPrice = 1000;
	
	
	static Economy econ = null;
	
    void registerCommands() 
    {
 
        CommandHandler handler = new CommandHandler();
        String cmd1 = "abi";
        handler.registerCmd(cmd1, new BoundCommand());       
        handler.setPermissionOnLastCmd("abi");
        handler.registerSubCmd(cmd1, "bind", new subBoundCmd());
        handler.registerSubCmd(cmd1, "reload", new subReloadCmd());
        handler.registerSubCmd(cmd1, "bind wait", new subBindWaitCmd());
        handler.registerSubCmd(cmd1, "bind use", new subBindOnUseWaitCmd());
        handler.registerSubCmd(cmd1, "broken", new subBrokenCmd());
        handler.registerSubCmd(cmd1, "unbind", new subUnBoundCmd());
        handler.registerSubCmd(cmd1, "repair", new subRepairCmd());
        handler.registerSubCmd(cmd1, "repair all", new subRepairAllCmd());
        handler.registerSubCmd(cmd1, "repair cost", new subRepairCostCmd());
        handler.registerSubCmd(cmd1, "bind cost", new subBindCostCmd());
        handler.registerSubCmd(cmd1, "setprice", new subSetPriceCmd());
        handler.registerSubCmd(cmd1, "money", new subGetMoneyCmd());
        handler.registerSubCmd(cmd1, "redeem", new subRedeemCmd());
        //handler.registerSubCmd(cmd1, "help", new subRedeemCmd());
        //handler.setPermissionOnLastCmd(cmd1+".bound");
        
        
        getCommand(cmd1).setExecutor(handler);
 
    }
	
   
    
	@Override
	public void onEnable() 
	{
		ConfigsSetup();
		instance = this;
		setupEconomy();
		
		registerLoreNames();
		registerkeyNames();
		registerCommands();
		
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN +" AccountBoundItems has been activated!");
		getServer().getPluginManager().registerEvents(new OnDamage(), this);
		getServer().getPluginManager().registerEvents(new DropAndPickup(), this);
		getServer().getPluginManager().registerEvents(new OnPlayerInteract(), this);
	}
	
	public static Main getInstance()
	{
		return instance;
	}
	
	public static Economy getEconomy() 
	{
        return econ;
    }
	
	private boolean setupEconomy() 
	{
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	    
    void registerkeyNames()
    {
    	keyNames.put("name","abi.Name");
    	keyNames.put("uuid","abi.Uuid");
    	keyNames.put("price","abi.Price");
    	keyNames.put("overrideprice","abi.Overrideprice");
    	keyNames.put("broken","abi.Broken");
    	keyNames.put("bound","abi.Bound");
    	keyNames.put("wait","abi.WaitBind");
    	keyNames.put("onuse","abi.OnUseBind");
    	keyNames.put("check","abi.Check");
    }
    
    void registerLoreNames()
    {
    	loreNames.put("bound", ChatColor.GRAY + "BOUND: ");
    	loreNames.put("broken",ChatColor.RED + "BROKEN ");
    	//loreNames.put("broken","BROKEN ");
    }

	
	public void ConfigsSetup()
	{
		
		makeEnchantConfig();
		makeLoreConfig();
		makeMaterialConfig();
		makeEnchantExponentConfig();
		makeSettingsConfig();
	}
	
	
	void makeSettingsConfig()
	{
		String dp = "defaultPrice";
		String ddp="defaultDropProsent";
		String drp="defaultRepairProsent";
		ConfigMaker cm = new ConfigMaker(this, "settings.yml");
		FileConfiguration config = cm.getConfig();
		if(!cm.isExists())
		{
			
			config.set(dp, defaultPrice);
			config.set(ddp,deadDropPricePros);
			config.set(drp,repairPricePros);
			cm.saveConfig();
		}
		else
		{
			if(!config.contains(ddp))
			{
				System.out.println("not contain dead");
				config.set(ddp,deadDropPricePros);
			}
			
			if(!config.contains(drp))
			{
				System.out.println("not contain repair");
				config.set(drp,repairPricePros);
			}
			
			if(!config.contains(dp))
			{
				config.set(dp, defaultPrice);
			}
			cm.saveConfig();
			
			defaultPrice = config.getDouble(dp);
			deadDropPricePros = config.getDouble(ddp);
			repairPricePros = config.getDouble(drp);
			
		
			
		}
	}
	
	void makeMaterialConfig()
	{
		ConfigMaker cm = new ConfigMaker(this, "material_prices.yml");
		FileConfiguration config = cm.getConfig();
		if(!cm.isExists())
		{
			
			for(Material m : Material.values())
			{
				config.set(m.name(), 0);
			}
			cm.saveConfig();
		}
		else
		{
			materialPrices.clear();
			for (String key : config.getConfigurationSection("").getKeys(false)) 
			{
				Material material = Material.getMaterial(key);
				double value = config.getDouble(key);
				if(value != 0)
				{
					materialPrices.put(material,value);
				}
				
			}
			
		}
	}
	
	void makeLoreConfig()
	{
		ConfigMaker cm = new ConfigMaker(this, "lore_prices.yml");
		FileConfiguration config = cm.getConfig();
		if(!cm.isExists())
		{
			config.options().header("Remember start with UPPERCASE and Exactly named as in game(No need colors)");
			config.set("lore",0);
			cm.saveConfig();
		}
		else
		{
			lorePrices.clear();
			for (String key : config.getConfigurationSection("").getKeys(false)) 
			{
				String lore = key;
				double value = config.getDouble(key);
				lorePrices.put(lore.toLowerCase(),value);
			}
			
		}
	}
	
	
	void makeEnchantExponentConfig()
	{
		ConfigMaker cm = new ConfigMaker(this, "enchantExpo_prices.yml");
		FileConfiguration config = cm.getConfig();
		if(!cm.isExists())
		{
			config.options().header("MinLevel doesnt effect anything yet.. always calculate 1-maxLevel");
			for(Enchantment ench : Enchantment.values())
			{

				config.set(ench.getKey().toString().split(":")[1]+".minLevel", ench.getStartLevel());
				config.set(ench.getKey().toString().split(":")[1]+".maxLevel", ench.getMaxLevel());
				config.set(ench.getKey().toString().split(":")[1]+".minPrice", 0);
				config.set(ench.getKey().toString().split(":")[1]+".maxPrice",0);
				
			}
			cm.saveConfig();
		}
		else
		{
			for (String key : config.getConfigurationSection("").getKeys(false)) 
			{
				Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(key));
				double minlvl = config.getDouble(key+".minLevel");
				double maxlvl = config.getDouble(key+".maxLevel");
				double minPrice = config.getDouble(key+".minPrice");
				double maxPrice = config.getDouble(key+".maxPrice");
				
				if(minPrice != 0 || maxPrice != 0)
				{
					Double[] array= {minlvl,maxlvl,minPrice,maxPrice};
					enchExPrices.put(ench, array);
				}
				
			}
			
			
		}
	}
	void makeEnchantConfig()
	{
		ConfigMaker cm = new ConfigMaker(this, "enchant_prices.yml");
		FileConfiguration config = cm.getConfig();
		if(!cm.isExists())
		{
			
			for(Enchantment ench : Enchantment.values())
			{
				int minlvl = ench.getStartLevel();
				int maxlvl = ench.getMaxLevel();
				for(int i = minlvl ; i <maxlvl+1; ++i)
				{
					config.set(ench.getKey().toString().split(":")[1]+" "+i, 0);
				}
				
				
			}
			cm.saveConfig();
		}
		else
		{
			enchPrices.clear();
			for (String key : config.getConfigurationSection("").getKeys(false)) 
			{
				String encName = key.replaceAll("\\s{2,}", " ").trim();
				double value = config.getDouble(key);
				if(value != 0)
				{
					enchPrices.put(encName.toLowerCase(),value);
				}
				
			}
			
			
		}
	}

}
