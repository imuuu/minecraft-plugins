package imu.iGeneralStore.Managers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import imu.iGeneralStore.Invs.ShopUI;
import imu.iGeneralStore.Main.Main;
import imu.iGeneralStore.ShopUtl.Shop;
import imu.iGeneralStore.TabCompletes.Cmd1_tab;

public class ShopManager 
{
	Main _main;
	
	ArrayList<Shop> _shops = new ArrayList<>();
	HashMap<UUID, ShopUI> _openShopUIs = new HashMap<>();
	public ShopManager(Main main)
	{
		_main = main;
	}
	
	public boolean openShop(Player p,String name)
	{
		Shop shop = getShop(name);
		if(shop != null)
		{			
			shop.openUI(p);
			return true;
		}
		return false;
	}
	
	public void onDisabled()
	{	
		closeShopInvs();
	}
	
	void closeShopInvs()
	{
		for(Entry<UUID, ShopUI> entry : _openShopUIs.entrySet())
		{
			entry.getValue().closeInventory();
		}
		_openShopUIs.clear();
	}
	
	public void createShop(String name)
	{
		Shop shop = new Shop(_main, name);
		_shops.add(shop);
		updateTabCompliters();
	}
	
	public void removeShop(String name)
	{
		Shop s = getShop(name);
		_shops.remove(s);
	}
	
	public Shop getShop(String name)
	{
		for(Shop s : _shops)
		{
			if(s.get_name().toLowerCase().contains(name.toLowerCase()))
				return s;
		}
		return null;
	}
	
	public void saveShop(String name,boolean async)
	{
		Shop s = getShop(name);
		if(s != null)
			s.saveShop(async);
	}
	
	public void updateTabCompliters()
	{
		
		String[] shopNames = new String[_shops.size()];
		for(int i = 0; i < _shops.size(); ++i)
		{
			shopNames[i] = _shops.get(i).get_name();
		}
		Cmd1_tab tab = _main.get_tab_cmd1();
		tab.setArgumenrs("shop", shopNames);
	}
	
	public void loadShopsAsync()
	{
		for(File file : new File(_main.getDataFolder().getAbsoluteFile()+File.separator + "Shops").listFiles())
		{
			//ConfigMaker cm = new ConfigMaker(_main, file.getName());
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			
			Shop shop = new Shop(_main, "");
			shop.set_name(config.getString("Name"));
			shop.set_sellM(config.getDouble("Sell_Multiplier"));
			shop.set_buyM(config.getDouble("Buy_Multiplier"));
			shop.set_expire_percent(config.getDouble("Expire_percent"));
			shop.set_expire_cooldown_m(config.getDouble("Expire_Cooldown"));
			
//			ArrayList<ItemStack> its = new ArrayList<>();
//			Collections.addAll(its, cm.getSavedInvFromConfig());
//			shop.set_items(its);
			
			_shops.add(shop);
			System.out.println("Shop added: "+shop.get_name());
		}
	}
}
