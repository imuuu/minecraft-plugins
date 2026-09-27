package imu.GeneralStore.Other;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.mojang.datafixers.util.Pair;

import imu.GeneralStore.Interfaces.DelaySendable;
import imu.GeneralStore.main.Main;
import net.md_5.bungee.api.ChatColor;

public class ShopManager implements DelaySendable
{
	Main _main = null;
	public HashMap<Material,Double[]> smart_prices = new HashMap<>();
	public HashMap<Material,Double[]> looked_prices = new HashMap<>();
	
	ArrayList<ItemStack> unique_items = new ArrayList<>();
	HashMap<Player, UniquesINV> uniqueInvs = new HashMap<>();
	HashMap<Player, UniquesINVmodify> uniqueInvModifies = new HashMap<>();
	
	
	HashMap<String,Shop> shops = new HashMap<>();
	HashMap<Player,Integer> openedSomeInv = new HashMap<>();
	
	ArrayList<Pair<Date, String> > selledItems = new ArrayList<>();
	
	String shopYAML ="shopNames.yml";
	String uniqueYAML ="UniqueItems.yml";
	
	boolean calculationReady = true;
	boolean lock_shops = false;
	boolean _logSoldShopsConsole = true;
	
	

	ItemMetods itemM = null;
	
	String pd_unique = "gs.unique";
	String pd_modify = "gs.uniqueModify";

	String str_invalid_price=ChatColor.RED +"The price is invalid, please enter correct price";
	
	public ShopManager(Main main)
	{
		_main = main;
		itemM = _main.getItemM();
		
		if(_main.isLoadSmartPricesUpFront())
		{
			calculateAllSmart();
		}		
		

	}
	
	final public void setupConfigs()
	{
		loadUniqueItemsConfig();
		makeShopsConfig();
	}
	
	public boolean isShopsLocked()
	{
		return lock_shops;
	}
	
	public void setLockShops(boolean lock, boolean saveData)
	{
		lock_shops = lock;
		if(saveData)
		{
			_main.setLocked(lock);
			_main.makeSettingsConfig(true);
		}
		
		if(lock)
		{
			closeShopsInvs();
		}
		
	}
	
	public boolean is_logSoldShopsConsole() {
		return _logSoldShopsConsole;
	}

	public void set_logSoldShopsConsole(boolean _logSoldShopsConsole) {
		this._logSoldShopsConsole = _logSoldShopsConsole;
	}
	public String get_pd_modify() 
	{
		return pd_modify;
	}
	public ArrayList<ItemStack> getUnique_items() 
	{
		return unique_items;
	}

	public String getStr_invalid_price() 
	{
		return str_invalid_price;
	}

	public HashMap<String,Shop> getShops()
	{
		return shops;
	}
	@Override
	public boolean isReady() 
	{
		return calculationReady;
	}
	
	
	public void addInv(Player player)
	{
		openedSomeInv.put(player, 1);
	}
	
	public void removeOpenedInv(Player player)
	{
		openedSomeInv.remove(player);
	}
	public void calculateAllSmart()
	{
		calculationReady = false;
		_main.getServer().getConsoleSender().sendMessage(ChatColor.DARK_PURPLE +" GeneralStore: Start calculating prices");		
		closeShopsInvs();
		clearSmartPrices();
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				Shop tempShop = new Shop(_main, "asddd",false, false);
				for(Material m : Material.values())
				{
					//System.out.println("GOING now: "+m.name());
					ItemStack stack = new ItemStack(m);
				
					Double[] values = tempShop.getSmartPrice(stack, false, 0);						
					if(values[0] != 0 && values[1] != 0 && values[2] != 0)
					{
						addSmartPrice(m, values.clone());											
					}						
				}
				calculationReady = true;
				_main.getServer().getConsoleSender().sendMessage(ChatColor.DARK_PURPLE +" GeneralStore: Calculating is done");
				looked_prices.clear();
			}
		}.runTaskAsynchronously(_main);
	
		
	}
	
	public void clearSmartPrices()
	{
		smart_prices.clear();
		looked_prices.clear();
	}
	
	public boolean isCalculationReady()
	{
		return calculationReady;
	}
	
	public boolean isPriceValid(Double[] price)
	{
		if (  (price.length < 3) || (price.length > 3))
		{
			return false;
		}

		if((price[0] > price[1]) || (price[0]< 0) || (price[2] > 100) || price[2] < 0)
		{
			return false;
		}
		
		return true;
	}
	
	public Shop addShop(String name, boolean onlySelling)
	{
		Shop shop = new Shop(_main, name,true, onlySelling);
		shops.put(name, shop);
		
		ConfigMaker cm = new ConfigMaker(_main, shopYAML);
		FileConfiguration config = cm.getConfig();
		config.set(name, onlySelling);
		cm.saveConfig();
		
		return shop;
	}
	public void setIsSellingOnly(String name, boolean isSellingOnly)
	{
		if(isExists(name))
		{
			Shop shop = getShop(name);
			shop.closeShopInvs();
			shop.setOnlySell(isSellingOnly);
			ConfigMaker cm = new ConfigMaker(_main, shopYAML);
			FileConfiguration config = cm.getConfig();
			config.set(shop.getDisplayName(), isSellingOnly);
			cm.saveConfig();
			
		}
	}
	public boolean isExists(String name)
	{
		if(getShop(name) != null)
		{
			return true;
		}
		return false;
	}
	public void removeShop(String name)
	{
		Shop shop = getShop(name);
		if(shop == null)
		{
			System.out.println("Try to remove shop but didnt find with that name: " + name);
			return;
		}
		shops.remove(shop._displayName);
		
		ConfigMaker cm = new ConfigMaker(_main, shopYAML);
		FileConfiguration config = cm.getConfig();
		config.set(name, null);
		cm.saveConfig();
		
		String[] fileNames = {shop._fileNameShopYML,shop._fileNameSelledMaterialCount};
		for(String fName : fileNames)
		{
			ConfigMaker cm2 = new ConfigMaker(_main, fName);
			cm2.removeConfig();
		}
		
		saveShopNames();
		
	}
	
	public void addSmartPrice(Material material, Double[] prices)
	{
		if(!smart_prices.containsKey(material))
		{
			smart_prices.put(material, prices);
		}
	}
	
	public void addLookedPrices(Material material, Double[] prices)
	{
		
		if(!looked_prices.containsKey(material))
		{
			looked_prices.put(material, prices);
		}
	}
	
	
	public Shop getShop(String name)
	{
		Shop shop = shops.get(name);
		
		if(shop == null)
		{
			name = name.replace(" ", "");
			for(Shop s : shops.values())
			{
				if(name.equalsIgnoreCase(s.getName().replace(" ", "")))
				{
					shop = s;
					break;
				}
			}
		}
		
		return shop;
	}

	
	public void openShop(Player player, String shopName)
	{
		if(!calculationReady)
		{
			return;
		}
		
		Shop shop = getShop(shopName);
		if(shop == null)
		{
			player.sendMessage(ChatColor.RED + "Shop wasn't found with that name");
			return;
		}
		
		shop.openShopInv(player);
	}
	
	public void saveShopsContent()
	{
		for(Shop shop : shops.values())
		{
			shop.configSaveContent();		
		}
	}
	public void closeShopsInvs()
	{
		for(Shop shop : shops.values())
		{
			shop.closeShopInvs();	
		}
		
	}
	
	public void closeOpenedInvs()
	{
		for(Player p : openedSomeInv.keySet())
		{
			p.closeInventory();
		}
	}
	
	public void saveShopNames()
	{
		ConfigMaker cm = new ConfigMaker(_main, shopYAML);
		FileConfiguration config = cm.getConfig();
		
		cm.clearConfig();
		for(Shop shop : shops.values())
		{
			config.set(shop.getDisplayName(), shop.getOnlySell());
		}
		
		cm.saveConfig();
	}
	void makeShopsConfig() 
	{
		ConfigMaker cm = new ConfigMaker(_main, shopYAML);
		FileConfiguration config = cm.getConfig();
		
		if(!cm.isExists())
		{
			config.set(ChatColor.DARK_RED + "General Store", true);
			cm.saveConfig();
		}
		
		for (String key : config.getConfigurationSection("").getKeys(false)) 
		{
			String shopName = key;
			addShop(shopName, config.getBoolean(key));
		}		
	}
	
	public void logAddSold(Pair<Date, String> value)
	{
		selledItems.add(value);
	}
	
	public void checkIfAbleToSaveData()
	{
		if(selledItems.size() <= 0 )
		{
			return;
		}
		
		for(Shop shop : shops.values())
		{
			if(shop.isShopsOpened())
			{
				return;
			}
		}
		ConfigMaker cm = new ConfigMaker(_main, "Sold_Log.yml");
		FileConfiguration config = cm.getConfig();
		
		if(!cm.isExists())
		{
			cm.saveConfig();
		}
		
		for(Pair<Date, String> values : selledItems)
		{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss z");
			String date = formatter.format(values.getFirst());
			String str = values.getSecond();
			if(config.contains(date))
			{
				int count =  Integer.parseInt(str.split(":")[3]) + Integer.parseInt(config.getString(date).split(":")[3]);
				str = str.substring(0, str.lastIndexOf(":"))+":"+count;
			}
			config.set(date, str);
		}
		cm.saveConfig();
		selledItems.clear();
		
	}
	
	void setUniquePDdata(ItemStack stack, Double[] price)
	{
		itemM.setPersistenData(stack, pd_unique, PersistentDataType.STRING, price[0]+":"+price[1]+":"+price[2]);
	}
	
	public Double[] getUniquePriceData(ItemStack stack)
	{
		String[] strs = itemM.getPersistenData(stack, pd_unique, PersistentDataType.STRING).split(":");
		Double[] ds = {Double.parseDouble(strs[0]),Double.parseDouble(strs[1]),Double.parseDouble(strs[2])};
		return ds;
	}
	
	void removeUniqueTag(ItemStack stack)
	{
		itemM.removePersistenData(stack, pd_unique);
	}
	
	void removeInModify(ItemStack stack)
	{
		itemM.removePersistenData(stack, pd_modify);
	}
	
	public void addUniqueItem(ItemStack stack, Double[] price, boolean removeCheck)
	{
		removeInModify(stack);
		if(itemM.isEveryThingThis(price, 0.0) && removeCheck)
		{
			unique_items.remove(stack);
			removeUniqueTag(stack);
		}else
		{
			setUniquePDdata(stack, price);
			if(isUnique(stack))
			{
				ItemStack stack_test = new ItemStack(stack);
				removeUniqueTag(stack_test);
				for(int i = 0; i < unique_items.size(); ++i)
				{
					ItemStack s_test = new ItemStack(unique_items.get(i));
					removeUniqueTag(s_test);
					if(stack_test.isSimilar(s_test))
					{
						unique_items.set(i, stack);
						break;
					}
				}
			}else
			{
				unique_items.add(stack);
			}
		}		
		saveUniqueItemsConfig();

	}
	
	void saveUniqueItemsConfig()
	{
		ConfigMaker cm = new ConfigMaker(_main, uniqueYAML);
		FileConfiguration config = cm.getConfig();
		cm.clearConfig();
		for(int i = 0; i < unique_items.size() ; ++i)
		{
			config.set(String.valueOf(i), unique_items.get(i));
		}		
		cm.saveConfig();
	}
	
	void loadUniqueItemsConfig()
	{
		ConfigMaker cm = new ConfigMaker(_main, uniqueYAML);
		FileConfiguration config = cm.getConfig();
		boolean found = false;
		unique_items.clear();
		for (String key : config.getConfigurationSection("").getKeys(false)) 
		{
			ItemStack stack =config.getItemStack(key);
			
			
			if(unique_items.stream().anyMatch(s -> stack.isSimilar(s)))
			{
				found = true;
				continue;				
			}					
			unique_items.add(config.getItemStack(key));
		}
		
		if(found)
		{
			saveShopsContent();
		}

	}
	
	public boolean isUnique(ItemStack stack)
	{
		ItemStack stack_test = new ItemStack(stack);
		removeUniqueTag(stack_test);
		removeInModify(stack_test);
		for(ItemStack s : unique_items)
		{
			ItemStack s_test = new ItemStack(s);
			removeUniqueTag(s_test);
			removeInModify(stack_test);
			if(stack_test.isSimilar(s_test))
			{
				return true;
			}
		}
		return false;
	}
	
	public Double[] getUniqueItemPrice(ItemStack stack)
	{
		ItemStack stack_test = new ItemStack(stack);
		removeUniqueTag(stack_test);
		removeInModify(stack_test);

		for(ItemStack s : unique_items)
		{
			ItemStack s_test = new ItemStack(s);
			removeUniqueTag(s_test);
			removeInModify(s_test);
			if(stack_test.isSimilar(s_test))
			{
				return getUniquePriceData(s);
			}
		}		
		return null;
		
	}
	
	public void openUniqueINV(Player player)
	{
		UniquesINV ui = null;
		if(uniqueInvs.containsKey(player))
		{
			ui = uniqueInvs.get(player);
		}else
		{
			ui = new UniquesINV(_main, player, ChatColor.DARK_PURPLE +"========== Uniques =========");
		}
		ui.openThis();
		
	}
	
	public void openUniqueINVmodify(Player player,ItemStack stack,boolean newItem)
	{
		UniquesINVmodify ui = null;
		if(uniqueInvModifies.containsKey(player))
		{
			ui = uniqueInvModifies.get(player);
		}
		else
		{
			ui = new UniquesINVmodify(_main, player, ChatColor.DARK_AQUA + "========== Modify ==========", uniqueInvs.get(player));
		}
		ui.INIT(stack,newItem);
		ui.openThis();
		
		
	}
	
	public void checkAndRemoveEliteMobSouldBound(ItemStack stack)
	{
		Plugin plugin = Bukkit.getPluginManager().getPlugin("EliteMobs");
		if( stack != null && plugin != null)
		{
			itemM.removePersistenDataOtherPlugin(plugin, stack, "soulbind");
			itemM.removeLore(stack, "Soulbound");
		}
	}


}
