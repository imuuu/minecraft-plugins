package imu.AccountBoundItems.Other;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import imu.AccountBoundItems.main.Main;
import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_16_R3.ItemArmor;
import net.minecraft.server.v1_16_R3.ItemElytra;
import net.minecraft.server.v1_16_R3.ItemShield;



public class ItemMetods 
{
	
	Main main = Main.getInstance();
	Economy econ = Main.getEconomy();
	private Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
	
	public boolean isDigit(String str)
	{
		if(str == null)
			return false;
		
		return pattern.matcher(str).matches();
	}
	
	public ItemStack addLore(ItemStack stack, String lore, boolean addLast)
	{

    	if(stack != null && stack.getType() != Material.AIR)
    	{
			ItemMeta meta = stack.getItemMeta();
    		ArrayList<String> lores = new ArrayList<String>();
    		if(meta.hasLore())
    		{
    			lores.addAll(meta.getLore());
    		}
    		
    		if(addLast)
    		{
    			lores.add(lore);
    		}else
    		{
    			lores.add(0, lore);
    		}
    		
    		meta.setLore(lores);
    		stack.setItemMeta(meta);

			
    	}
    	return stack;
	}
	
	public void printLores(ItemStack stack)
	{
		if(stack != null && stack.getType() != Material.AIR)
    	{
			ItemMeta meta = stack.getItemMeta();
    		if(meta.hasLore())
    		{			
    			for(String lore : meta.getLore())
    			{
    				System.out.println("Lore: "+lore);
    			}  			
    		}			
    	}
	}
	
	public void printEnchants(ItemStack stack)
	{
		if(stack != null && stack.getType() != Material.AIR)
    	{
			ItemMeta meta = stack.getItemMeta();
    		for(Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet())
    		{
    			Enchantment ench = entry.getKey();
    			System.out.println("ench: " + ench.getKey());
    			System.out.println("Enchant: " + entry.getKey() + " Level: "+entry.getValue());
    		}
    	}
	}
	
	public ArrayList<String> getLores(ItemStack stack)
	{
		ArrayList<String> lores = new ArrayList<String>();
		
		if(stack != null && stack.getType() != Material.AIR)
    	{
			ItemMeta meta = stack.getItemMeta();
    		if(meta.hasLore())
    		{			
    			lores.addAll(meta.getLore());
    		}			
    	}
		return lores;
	}
	
	public ArrayList<Enchantment> getEnchantsWithoutLvl(ItemStack stack)
	{
		ArrayList<Enchantment> enchs = new ArrayList<Enchantment>();
		if(stack != null && stack.getType() != Material.AIR)
    	{
			ItemMeta meta = stack.getItemMeta();
    		for(Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet())
    		{
    			enchs.add(entry.getKey());
    		}
    	}
		return enchs;
	}
	
	public ItemStack removeLore(ItemStack stack, String lore)
	{
		int idx = findLoreIndex(stack, lore);
		if(idx > -1)
		{
			ItemMeta meta = stack.getItemMeta();
			ArrayList<String> lores = new ArrayList<String>();
			lores.addAll(meta.getLore());
			lores.remove(idx);
			meta.setLore(lores);
			stack.setItemMeta(meta);
			
			
		}
		return stack;
	}
	public int findLoreIndex(ItemStack stack, String lore)
	{
		if(stack != null && stack.getType() != Material.AIR)
    	{
			if(stack.hasItemMeta())
			{
				ItemMeta meta = stack.getItemMeta();  		
	    		if(meta.hasLore())
	    		{
	    			ArrayList<String> lores = new ArrayList<String>();
	    			lores.addAll(meta.getLore());
	    			
	    			for(int i = 0 ; i < lores.size() ; ++i)
	    			{  				
	    				if(lores.get(i).contains(lore))
	    				{
	    					return i;
	    				}
	    			}
	    		}
			}
			
    	}
		return -1;
	}
	
	public int getStringFirstUpperLetter(String str)
	{
		int i = -1;
		
		for(int j = 0 ; j < str.length(); ++j)
		{
			char c = str.charAt(j);
			if(Character.isLetter(c))
			{
				if(Character.isUpperCase(c))
				{
					return j;
				}
				
				
			}
		}
		
		return i;
	}
	
	public boolean hasLore(ItemStack stack, String lore)
	{
		if(stack != null)
		{
			if(stack.hasItemMeta())
			{
				if(findLoreIndex(stack, lore) > -1)
				{
					return true;
				}
			}
		}
				
		return false;
	}
	
	void printItemStacks(ItemStack[] stacks)
	{
		System.out.println("===========================");
		for (ItemStack itemStack : stacks)
		{
			if(itemStack != null)
			{
				System.out.println("Item: " + itemStack + "Material:" +itemStack.getType());
			}else
			{
				System.out.println("Item: " + itemStack);
			}
				
		}
	}
	
	String getDisplayName(ItemStack stack)
	{
		if(stack != null && stack.getType() != Material.AIR)
		{
			ItemMeta meta = stack.getItemMeta();
			String dName="";
			if(meta.hasDisplayName())
			{
				dName = meta.getDisplayName();
			}
			else
			{
				String[] mNames = stack.getType().toString().split("_");
				for(String subName : mNames)
				{
					String sub = subName.substring(0,1).toUpperCase() + subName.substring(1).toLowerCase();
					if(dName == "")
					{
						dName = dName +sub;
					}else
					{
						dName = dName +" "+sub;
					}
					
				}
			}
			return dName;
			
		}
		return "";
	}
	public ItemStack removeDisplayName(ItemStack stack, String name)
	{
		if(stack != null && stack.getType() != Material.AIR)
		{
			ItemMeta meta = stack.getItemMeta();
			
			String dName=getDisplayName(stack);
			dName = dName.replace(name, "");
			meta.setDisplayName(dName);
			stack.setItemMeta(meta);
			
		}
		return stack;
	}
	
	public ItemStack addDisplayName(ItemStack stack,String name, boolean front)
	{
		if(stack != null && stack.getType() != Material.AIR)
		{
			ItemMeta meta = stack.getItemMeta();
			String dName = getDisplayName(stack);
			
			if(front)
			{
				dName = name + dName;
			}else
			{
				dName = dName + name;
			}
			meta.setDisplayName(dName);
			stack.setItemMeta(meta);
			
		}
		return stack;
	}
	public boolean isInArmorSlots(ItemStack stack,Player player)
	{

		if(stack != null && stack.getType() != Material.AIR)
		{
			for(ItemStack s : player.getInventory().getArmorContents())
			{
				if(s == null)
					continue;
				
				if(s.isSimilar(stack))
				{
					
					System.out.println("found match");
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean isInShieldSlot(ItemStack stack, Player player)
	{
		if(stack != null && stack.getType() != Material.AIR)
		{
			ItemStack offHand = player.getInventory().getItemInOffHand();
			if(offHand != null)
			{
				if(offHand.isSimilar(stack))
				{
					return true;
				}
			}
		}
		
		return false;
	}

	public boolean hasDurability(ItemStack stack)
	{
		if(stack != null && stack.getType() != Material.AIR )
		{
			if(stack.getType().getMaxDurability() != 0)
			{
				return true;
			}		
		}
		return false;
		
	}
	public boolean isArmor(ItemStack stack)
	{
		if(CraftItemStack.asNMSCopy(stack).getItem() instanceof ItemArmor || CraftItemStack.asNMSCopy(stack).getItem() instanceof ItemElytra)
		{
			return true;
		}
		return false;
	}
	
	public boolean isShield(ItemStack stack)
	{
		if(CraftItemStack.asNMSCopy(stack).getItem() instanceof ItemShield)
		{
			return true;
		}
		return false;
	}
	
	
	public boolean isTool(ItemStack stack) 
	{
		if(stack != null && stack.getType()!= Material.AIR)
		{
			switch(stack.getType()) 
			{
				case WOODEN_PICKAXE: return true;
				case WOODEN_SHOVEL: return true;
				case WOODEN_AXE: return true;
				case WOODEN_HOE: return true;
				case WOODEN_SWORD: return true;
				
				case STONE_PICKAXE: return true;
				case STONE_SHOVEL: return true;
				case STONE_AXE: return true;
				case STONE_HOE: return true;
				case STONE_SWORD: return true;
				
				case IRON_PICKAXE: return true;
				case IRON_SHOVEL: return true;
				case IRON_AXE: return true;
				case IRON_HOE: return true;
				case IRON_SWORD: return true;
				
				case GOLDEN_PICKAXE: return true;
				case GOLDEN_SHOVEL: return true;
				case GOLDEN_AXE: return true;
				case GOLDEN_HOE: return true;
				case GOLDEN_SWORD: return true;
				
				case DIAMOND_PICKAXE: return true;
				case DIAMOND_SHOVEL: return true;
				case DIAMOND_AXE: return true;
				case DIAMOND_HOE: return true;
				case DIAMOND_SWORD: return true;
				
				case SHIELD: return true;
				
				case BOW: return true;
				case CROSSBOW: return true;
				
				case TRIDENT: return true;
				
				case FISHING_ROD: return true;
				
				default: return false;
			}
		}
		return false;
		
	}
	
	public <T> ItemStack setPersistenData(ItemStack stack, String keyName, PersistentDataType<T, T> type, T data)
	{
		NamespacedKey key = new NamespacedKey(main, keyName);
		ItemMeta meta = stack.getItemMeta();
		meta.getPersistentDataContainer().set(key, type, data);
		stack.setItemMeta(meta);
		return stack;
		
	}
	
	public <T> T getPersistenData(ItemStack stack, String keyName, PersistentDataType<T, T> type)
	{
		T value = null;
		if(stack != null && stack.getType() != Material.AIR)
		{
			NamespacedKey key = new NamespacedKey(main, keyName);
			ItemMeta meta = stack.getItemMeta();
			PersistentDataContainer container = meta.getPersistentDataContainer();
			if(container.has(key, type))
			{
				value = container.get(key, type);
				return value;
			}
		}
		
				
		return value;
	}
	
	
	
	
}
