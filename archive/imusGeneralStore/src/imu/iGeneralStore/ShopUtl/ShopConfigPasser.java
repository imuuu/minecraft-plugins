package imu.iGeneralStore.ShopUtl;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iGeneralStore.Main.Main;
import imu.iGeneralStore.Other.ConfigMaker;

public class ShopConfigPasser 
{
	Main _main;
	Shop _shop;
	
	String yml_path = "Shops";
	public ShopConfigPasser(Main main, Shop shop)
	{
		_shop = shop;
		_main = main;
	}
	
	public void saveShopAsync()
	{
		new BukkitRunnable() 
		{
			
			@Override
			public void run() 
			{
				saveShop();				
			}
		}.runTaskAsynchronously(_main);
	}
	public void saveShop()
	{
		ConfigMaker cm = new ConfigMaker(_main, yml_path+File.separator + _shop.get_name()+".yml");
		FileConfiguration config = cm.getConfig();
		
		config.set("Name", _shop.get_name().toString());
		config.set("Sell_Multiplier", _shop.get_sellM());
		config.set("Buy_Multiplier", _shop.get_buyM());
		config.set("Expire_percent", _shop.get_expire_percent());
		config.set("Expire_Cooldown", _shop.get_expire_cooldown_m());
		cm.saveSTACKSconfig(_shop.get_items().toArray(new ItemStack[_shop.get_items().size()]));
		cm.saveConfig();
	}
	
	public void loadShopAsync()
	{
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				ConfigMaker cm = new ConfigMaker(_main, yml_path+File.separator + _shop.get_name()+".yml");
				FileConfiguration config = cm.getConfig();
				
				_shop.set_name(config.getString("Name"));
				_shop.set_sellM(config.getDouble("Sell_Multiplier"));
				_shop.set_buyM(config.getDouble("Buy_Multiplier"));
				_shop.set_expire_percent(config.getDouble("Expire_percent"));
				_shop.set_expire_cooldown_m(config.getDouble("Expire_Cooldown"));
//				ArrayList<ItemStack> its = new ArrayList<>();
//				Collections.addAll(its, cm.getSavedInvFromConfig());
//				_shop.set_items(its);
			}
		}.runTaskAsynchronously(_main);
		
	}
}
