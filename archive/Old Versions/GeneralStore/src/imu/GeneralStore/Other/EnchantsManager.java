package imu.GeneralStore.Other;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.GeneralStore.main.Main;
import net.md_5.bungee.api.ChatColor;

public class EnchantsManager 
{
	Main _main = null;
	ItemMetods _itemM = null;
	HashMap<String, Double[]> enchPrices = new HashMap<>();  // {minlvl,maxlvl,minPrice,maxPrice
	ArrayList<ItemStack> ench_items = new ArrayList<>();
	
	Material ench_item_mat = Material.ENCHANTED_BOOK;
	
	public String pd_ench = "gs.emEnc";
	public String pd_modify = "gs.emModify";
	public String pd_name = "gs.emEncName";
	
	public EnchantsManager(Main main) 
	{
		_main = main;
		_itemM = _main.getItemM();
	}
	
	public void openEnchantINV(Player player)
	{
		EnchantINV ei = new EnchantINV(_main, player,ChatColor.DARK_PURPLE +"======= Enchantments =======");
		ei.openThis();
	}
	
	public void openEnchantINVmodify(Player player, ItemStack stack)
	{
		EnchantInvModify eim = new EnchantInvModify(_main, player, ChatColor.DARK_AQUA + "========== Modify ==========", stack);
		eim.openThis();
	}
	
	HashMap<String, Double[]> getEnchPrices() 
	{
		return enchPrices;
	}
	
	public boolean isEnchPriced(Enchantment ench)
	{
		return enchPrices.containsKey(ench.toString());
	}
	
	public void addNewEnchant(Enchantment enc, Double[] price, boolean setToConfig)
	{
		enchPrices.put(enc.toString(), price);
		ItemStack encItem = makeEnchItem(enc, price);
		removeModifyData(encItem);
		int contain = isContaining(encItem);
		
		if(contain != -1)
		{
			ench_items.set(contain, encItem);
		}else
		{
			ench_items.add(encItem);
		}
		
		if(setToConfig)
		{
			setEnchantToConfig(enc, price);
		}
	}
	
	void removeEnchant(Enchantment enc)
	{
		enchPrices.remove(enc.toString());
	}
	
	public void clearEnchPrices()
	{
		enchPrices.clear();
	}
	
	public ItemStack makeEnchItem(Enchantment enc, Double[] data)
	{
		ItemStack newEnc = new ItemStack(ench_item_mat);
		String display_name = WordUtils.capitalize( enc.getKey().getKey().replace("_", " "));
		_itemM.setDisplayName(newEnc, display_name);
		setEnchNameData(newEnc, enc);		
		setEnchData(newEnc, data);
		
		return newEnc;
	}
	
	public Enchantment getEnchantByName(String name)
	{
		return Enchantment.getByKey(NamespacedKey.minecraft(name));
	}
	
	public Enchantment getEnchantFromStack(ItemStack stack)
	{
		return getEnchantByName(getEnchNameData(stack));
	}
	
	public int isContaining(ItemStack stack)
	{
		ItemStack test,copy;
		test = stack.clone();
		removeEnchPriceData(test);
		removeModifyData(test);
		
		for(int i = 0; i < ench_items.size(); ++i)
		{
			copy = ench_items.get(i).clone();
			removeEnchPriceData(copy);
			removeModifyData(copy);			
			if(test.isSimilar(copy))
			{
				return i;
			}		
		}		
		return -1;
	}
	
	public void setEnchantToConfig(Enchantment ench, double minLvl, double maxLvl, double minPrice, double maxPrice)
	{
		ConfigMaker cm = new ConfigMaker(_main, "enchant_prices.yml");
		FileConfiguration config = cm.getConfig();
		config.set(ench.getKey().toString().split(":")[1]+".minLevel", minLvl);
		config.set(ench.getKey().toString().split(":")[1]+".maxLevel", maxLvl);
		config.set(ench.getKey().toString().split(":")[1]+".minPrice", minPrice);
		config.set(ench.getKey().toString().split(":")[1]+".maxPrice", maxPrice);
		cm.saveConfig();
	}
	public void setEnchantToConfig(Enchantment ench,Double[] data)
	{
		setEnchantToConfig(ench, data[0], data[1], data[2], data[3]);
	}
	public void setEnchantToConfig(ItemStack enchItem)
	{
		Double[] data = getEnchPriceData(enchItem);
		setEnchantToConfig(getEnchantFromStack(enchItem),data);
	}
	public boolean isEnchantPriceValid(Double[] data)
	{
		if (  (data.length < 4) || (data.length > 4))
		{
			return false;
		}
		
		if((data[0] > data[1]) || (data[2] > data[3])) // can be chanced in future
		{
			return false;
		}
		
		if(data[0] < 1 || data[1] < 1 || data[2] < 0 || data[3] < 0)
		{
			return false;
		}
		
		
		return true;
	}
	
	public void setEnchNameData(ItemStack stack, Enchantment enc)
	{
		_itemM.setPersistenData(stack, pd_name, PersistentDataType.STRING, enc.getKey().getKey());
	}
	
	public String getEnchNameData(ItemStack stack)
	{
		return _itemM.getPersistenData(stack, pd_name, PersistentDataType.STRING);
	}
	public void removeEnchNameData(ItemStack stack)
	{
		_itemM.removePersistenData(stack, pd_name);
	}
	
	public void setEnchData(ItemStack stack, Double[] price)
	{
		String str = price[0] + ":" + price[1] + ":" + price[2] + ":" + price[3];
		_itemM.setPersistenData(stack, pd_ench, PersistentDataType.STRING, str);
	}
	public Double[] getEnchPriceData(ItemStack stack)
	{

		String[] strs = _itemM.getPersistenData(stack, pd_ench,  PersistentDataType.STRING).split(":");
		Double[] data = {Double.parseDouble(strs[0]),Double.parseDouble(strs[1]),Double.parseDouble(strs[2]),Double.parseDouble(strs[3])};
		return data;
	}
	
	public void removeEnchPriceData(ItemStack stack)
	{
		_itemM.removePersistenData(stack, pd_ench);
	}
	public Double[] getEnchPrice(Enchantment ench)
	{
		return enchPrices.get(ench.toString());
	}
	
	public void setModifyData(ItemStack stack)
	{
		_itemM.setPersistenData(stack, pd_modify, PersistentDataType.INTEGER, 1);
	}
	public void removeModifyData(ItemStack stack)
	{
		_itemM.removePersistenData(stack, pd_modify);
	}
	public boolean isModify(ItemStack stack)
	{
		Integer x = _itemM.getPersistenData(stack, pd_modify, PersistentDataType.INTEGER);
		if(x != null)
		{
			return true;
		}
		return false;
	}
	
	
}
