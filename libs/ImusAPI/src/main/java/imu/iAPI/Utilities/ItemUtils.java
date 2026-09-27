package imu.iAPI.Utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;



public class ItemUtils
{
	private static ImusAPI _main = ImusAPI._instance;
	
	private static final int SIMILARITY_THRESHOLD = 50; // Percentage
	
	private static Random _random = new Random();
	
	
	public static boolean IsValid(ItemStack stack) 
	{ 
		return stack != null && stack.getType() != Material.AIR;
	}

	public static boolean IsShulkerBox(ItemStack stack)
	{
		if (!IsValid(stack))  return false;

		if(stack.getItemMeta() instanceof BlockStateMeta )
		{
			BlockStateMeta im = (BlockStateMeta)stack.getItemMeta();
            if(im.getBlockState() instanceof ShulkerBox){
            	return true;
            }
			
		}
		return false;
	}

	public static boolean IsArmor(ItemStack stack) 
	{   
		return ItemUtilToolsArmors.IsArmor(stack);
	}
	
	public static boolean IsTool(ItemStack stack) 
	{
		return ItemUtilToolsArmors.IsTool(stack);
		
	}
	
	private static int CalculateSimilarity(String x, String y) 
	{
        int maxLength = Math.max(x.length(), y.length());
        if (maxLength == 0) return 100; // Both strings are empty
        return (int) ((1 - ((double) ImusUtilities.LevenshteinDistance(x, y) / maxLength)) * 100);
    }

	public static ItemStack AddOrReplaceLore(ItemStack stack, String newLore)
	{
		return AddOrReplaceLore(stack, newLore, SIMILARITY_THRESHOLD);
	}
	public static ItemStack AddOrReplaceLore(ItemStack stack, String newLore, int similarityPercent)
    {
        if (!IsValid(stack)) return stack;

		if(similarityPercent > 100) similarityPercent = 100;
		if(similarityPercent < 0) similarityPercent = 0;

        ItemMeta meta = stack.getItemMeta();
        List<String> lores = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        boolean loreExists = false;
        for (int i = 0; i < lores.size(); i++) 
        {
        	String existingLore = lores.get(i);
            if (CalculateSimilarity(existingLore, Metods.msgC(newLore)) >= similarityPercent)
            {
                lores.set(i, Metods.msgC(newLore)); // Replace similar lore
                loreExists = true;
                break;
            }
        }

        if (!loreExists) 
        {
            lores.add(Metods.msgC(newLore)); 
        }

        meta.setLore(lores);
        stack.setItemMeta(meta);
        return stack;
    }
	
	public static  ItemStack AddLore(ItemStack stack, String lore, boolean addLast)
	{
		if (!IsValid(stack))  return stack;
		
		lore = Metods.msgC(lore);
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
		
    	return stack;
	}
	
	public static ItemStack RemoveSimilarLore(ItemStack stack, String loreToRemove) 
	{
        if (!IsValid(stack)) return stack;

        ItemMeta meta = stack.getItemMeta();
        List<String> lores = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        for (int i = 0; i < lores.size(); i++) 
        {
            String existingLore = lores.get(i);
            if (CalculateSimilarity(existingLore, loreToRemove) >= SIMILARITY_THRESHOLD) 
            {
                lores.remove(i); 
                break;
            }
        }
        meta.setLore(lores);
        stack.setItemMeta(meta);
        return stack;
    }
	
	public static  ItemStack RemoveLores(ItemStack stack)
	{
		if (!IsValid(stack))  return stack;
		
		ItemMeta meta = stack.getItemMeta();
		meta.setLore(new ArrayList<>());
		stack.setItemMeta(meta);
		
    	return stack;
	}
	
	public static ItemStack AddLore(ItemStack stack, String[] lores)
	{
		return AddLore(stack, Arrays.asList(lores));
	}
	
	public static  ItemStack AddLore(ItemStack stack, Iterable<String> lores)
	{
		if (!IsValid(stack))  return stack;
		
		ItemMeta meta = stack.getItemMeta();
		ArrayList<String> read_lores = new ArrayList<String>();
		if(meta.hasLore())
		{
			read_lores.addAll(meta.getLore());
		}
		
		int idx = 0;
		for(String l : lores)
		{
			read_lores.add(idx++, Metods.msgC(l));
		}
		
		meta.setLore(read_lores);
		stack.setItemMeta(meta);
    	
		return stack;
	}
	
	public static ItemStack SetLore(ItemStack stack, String lore, boolean removeEmpty)
	{
		return SetLores(stack, Arrays.asList(lore), removeEmpty);
	}
	
	public static  ItemStack SetLores(ItemStack stack, String[] lores, boolean removeEmpty)
	{
		return SetLores(stack, Arrays.asList(lores), removeEmpty);
	}
	
	public static ItemStack SetLores(ItemStack stack, Iterable<String> lores, boolean removeEmpty)
	{
		if (!IsValid(stack))  return stack;
		
		ItemMeta meta = stack.getItemMeta();
		ArrayList<String> metaLores = new ArrayList<>();

		for(String lore : lores)
		{
			metaLores.add(Metods.msgC(lore));
		}
		if(removeEmpty)
		{
			//System.out.println("Removing empty");
			for(int i = metaLores.size()-1; i >= 0 ; i--)
			{
				//System.out.println("==> empty? "+metaLores.get(i)+ "is it? "+Strings.isNullOrEmpty(metaLores.get(i)));
				if(Metods.IsStringNullOrEmpty(metaLores.get(i)))
				{
					metaLores.remove(i);
				}
			}
		}
		
		meta.setLore(metaLores);
		
		stack.setItemMeta(meta);
    	
		return stack;
	}
	public static ItemStack setLore(ItemStack stack,int index, String lore)
	{
		if (!IsValid(stack))  return stack;

		if(stack.getItemMeta() == null) return stack;

		ItemMeta meta = stack.getItemMeta();
		ArrayList<String> metaLores = new ArrayList<>();
		if(meta.hasLore())
		{
			metaLores.addAll(meta.getLore());
		}

		if(metaLores.size() <= index)
		{
			while(metaLores.size() < index+1)
			{
				metaLores.add(" ");
			}
		}
		metaLores.set(index, Metods.msgC(lore));
		meta.setLore(metaLores);

		stack.setItemMeta(meta);

		return stack;
	}

	public static ItemStack reSetLore(ItemStack stack, String lore, int index)
	{

		ItemMeta meta = stack.getItemMeta();
		if(!meta.hasLore())
		{
			meta.setLore(new ArrayList<>());			
		}
		
		ArrayList<String> metaLores = (ArrayList<String>)meta.getLore();
		if(metaLores.size() <= index)
		{
			while(metaLores.size() < index+1)
			{
				metaLores.add("");
			}
		}
		metaLores.set(index, lore);
		meta.setLore(metaLores);
		
		stack.setItemMeta(meta);

		return stack;
	}
	
	public static String GetDisplayName(ItemStack stack)
	{
		if (!IsValid(stack))  return "";
		
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
	
	///DISPLAY NAME
	public static ItemStack RemoveDisplayName(ItemStack stack, String name)
	{
		if (!IsValid(stack))  return stack;
		
		ItemMeta meta = stack.getItemMeta();
		String dName = GetDisplayName(stack);
		dName = dName.replace(name, "");
		meta.setDisplayName(dName);
		stack.setItemMeta(meta);
		return stack;
	}
	
	public static ItemStack SetDisplayName(ItemStack stack, String name)
	{
		if (!IsValid(stack))  return stack;
		
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(Metods.msgC(name));
		stack.setItemMeta(meta);
		return stack;
	}

	public static ItemStack setDisplayName(ItemStack stack, String name)
	{
		return SetDisplayName(stack, name);
	}
	
	public static ItemStack SetDisplayNameEmpty(ItemStack stack)
	{
		return SetDisplayName(stack, " ");
	}

	public static ItemStack setDisplayNameEmpty(ItemStack stack)
	{
		return SetDisplayNameEmpty(stack);
	}
	
	public enum DisplayNamePosition 
	{
	    FRONT,
	    BACK
	}
	
	public static ItemStack AddTextToDisplayName(ItemStack stack, String text, DisplayNamePosition position) 
	{
        if (!IsValid(stack)) return stack;

        ItemMeta meta = stack.getItemMeta();
        String dName = GetDisplayName(stack);
        
        switch (position) {
            case FRONT:
                dName = text + dName;
                break;
            case BACK:
                dName = dName + text;
                break;
        }

        SetDisplayName(stack, dName);
        return stack;
    }
	
	public static boolean GiveDamage(ItemStack stack,int dmg, boolean destroyItem)
	{
		if(stack != null && stack.getType() != Material.AIR && stack.getType().getMaxDurability() > 0)
		{
			if(stack.getItemMeta() instanceof Damageable)
			{
				Damageable meta = (Damageable) stack.getItemMeta();
				int maxDur=stack.getType().getMaxDurability();
				int givenDamage = meta.getDamage() + dmg;
					
				if(givenDamage >= maxDur)
				{
					meta.setDamage(maxDur);
					if(destroyItem)
					{
						stack.setAmount(0);
					}
				}else
				{
					meta.setDamage(givenDamage);
				}
				
				stack.setItemMeta((ItemMeta)meta);
				return true;
			}
			
		}
		return false;
	}
	
	public static double GetDurabilityProsent(ItemStack stack)
	{
		if(stack != null && stack.getType() != Material.AIR && stack.getType().getMaxDurability() > 0)
		{
			if(stack.getItemMeta() instanceof Damageable)
			{
				Damageable meta = (Damageable) stack.getItemMeta();
				double maxDur = stack.getType().getMaxDurability();
				double getDmg = meta.getDamage();
				double prosent = 1-getDmg/maxDur;
				
				if(prosent > 1.0)
				{
					prosent = 1.0;
				}
				return prosent;
			}
			
		}
		return 1.0;
	}
	
	public static void SetDamage(ItemStack stack,int dmg)
	{
		if(stack != null && stack.getType() != Material.AIR)
		{
			Damageable meta = (Damageable) stack.getItemMeta();
			meta.setDamage(dmg);								
			stack.setItemMeta((ItemMeta)meta);
			
		}
	}
	
	public static String EncodeItemStack(ItemStack stack)
	{
		String encodedObj= null;
		
		try
		{
			ByteArrayOutputStream io = new ByteArrayOutputStream();
			BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);
			os.writeObject(stack);
			os.flush();
			byte[] serializedObj = io.toByteArray();			
			encodedObj = java.util.Base64.getEncoder().encodeToString(serializedObj);
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
		
		return encodedObj;
	}
	
	public static ItemStack DecodeItemStack(String endcodedSTR)
	{
		byte[] serializedObj = java.util.Base64.getDecoder().decode(endcodedSTR);
		ItemStack stack = null;
		ByteArrayInputStream in = new ByteArrayInputStream(serializedObj);
		try 
		{
			BukkitObjectInputStream is = new BukkitObjectInputStream(in);
			stack = (ItemStack) is.readObject();
		} 
		catch (IOException | ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
		return stack;
	}
	
	
	
	public static ItemStack HideFlag(ItemStack stack, ItemFlag flag )
	{
    	ItemMeta meta = stack.getItemMeta();
        meta.addItemFlags(flag);
        stack.setItemMeta(meta);
        return stack;
	}
	
	///ENCHANT
	public static Set<Enchantment> GetEnchants(ItemStack stack)
	{
		if (!IsValid(stack))  return new HashSet<>();
		
		if(stack.getType() != Material.ENCHANTED_BOOK) return stack.getEnchantments().keySet();
		
		Set<Enchantment> enchs = new HashSet<>();
		enchs.addAll(stack.getEnchantments().keySet());
		enchs.addAll(((EnchantmentStorageMeta)stack.getItemMeta()).getStoredEnchants().keySet());
		

		return enchs;
	}
	
	public static boolean HasEnchant(ItemStack stack, Enchantment ench)
	{
		if (!IsValid(stack)) return false;
		
		return GetEnchants(stack).contains(ench);

	}
	
	public static int GetEnchantLevel(ItemStack stack, Enchantment ench)
	{
		if (!IsValid(stack)) return 0;
		
		if(!stack.hasItemMeta()) return 0;
		
		ItemMeta meta = stack.getItemMeta();
		
		if(meta.hasEnchant(ench)) 
		{
			return meta.getEnchantLevel(ench);
		}
		
		return 0;
	}
	
	@SuppressWarnings("unused")
	public static HashMap<Enchantment, Integer> GetEnchantsWithLevels(ItemStack stack)
	{
		if (!IsValid(stack)) return new HashMap<>();
		
		HashMap<Enchantment, Integer> map = new HashMap<>();
		for(Enchantment ench : GetEnchants(stack))
		{
			if(stack.getType() != Material.ENCHANTED_BOOK)
			{
				map.put(ench, stack.getEnchantmentLevel(ench));
				continue;
			}
			
			Integer bookLvl = ((EnchantmentStorageMeta)stack.getItemMeta()).getStoredEnchantLevel(ench);
			Integer stackLvl = stack.getEnchantmentLevel(ench);
			if(bookLvl == null) 
				bookLvl = 0;
			if(stackLvl == null) 
				stackLvl = 0;
			
			if(bookLvl > stackLvl)
			{
				map.put(ench, bookLvl);
				continue;
			}
			map.put(ench, stackLvl);
				
		}
		return map;
	}
	
	public static void CloneEnchantments(ItemStack item1, ItemStack item2) 
	{
	    item2.getEnchantments().keySet().forEach(enchantment -> item2.removeEnchantment(enchantment));
	    item1.getEnchantments().forEach((enchantment, level) -> item2.addUnsafeEnchantment(enchantment, level));
	}
	public static void RemoveEnchantments(ItemStack stack) {

		stack.getEnchantments().keySet().forEach(enchantment -> stack.removeEnchantment(enchantment));
	}
	
	public static ItemStack AddGlow(ItemStack stack)
	{
		ItemMeta meta = stack.getItemMeta();
		
		if(stack.getType() == Material.BOW)
		{
			meta.addEnchant(Enchantment.WATER_WORKER, 1, true);
		}else
		{
			meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
		}		
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		stack.setItemMeta(meta);
		return stack;
	}
	
	public static ItemStack[] GetShulkerBoxContent(ItemStack stack)
	{
		if(!IsShulkerBox(stack)) return null;
		BlockStateMeta bsm = (BlockStateMeta)stack.getItemMeta();
		if(bsm.getBlockState() instanceof ShulkerBox)
		{
			return ((ShulkerBox)bsm.getBlockState()).getInventory().getContents();
		}
		return null;
	}
	
	public static boolean SetShulkerBoxContent(ItemStack stack, ItemStack[] content)
	{
		if(!IsShulkerBox(stack)) return false;
		BlockStateMeta bsm = (BlockStateMeta)stack.getItemMeta();
		if(bsm.getBlockState() instanceof ShulkerBox)
		{
			ShulkerBox shulker = (ShulkerBox)bsm.getBlockState();
			shulker.getInventory().setContents(content);
			bsm.setBlockState(shulker);
			stack.setItemMeta(bsm);
			return true;
		}
		return false;
	}
	
	public static HashMap<Integer, ItemStack> AddItemShulkerBoxContent(ItemStack stack, ItemStack addedItem)
	{
		if(!IsShulkerBox(stack)) return null;

		BlockStateMeta bsm = (BlockStateMeta)stack.getItemMeta();
		if(bsm.getBlockState() instanceof ShulkerBox)
		{
			ShulkerBox shulker = (ShulkerBox)bsm.getBlockState();
			HashMap<Integer, ItemStack> hash = shulker.getInventory().addItem(addedItem);
			bsm.setBlockState(shulker);
			stack.setItemMeta(bsm);
			return hash;
		}
		return null;
	}
	
	///>>>> PERSISTENT DATA
	public static <T> ItemStack SetPersistenData(ItemStack stack, String keyName, PersistentDataType<T, T> type, T data)
	{
		if (!IsValid(stack))  return stack;
		
		NamespacedKey key = new NamespacedKey(_main, keyName);
		ItemMeta meta = stack.getItemMeta();
		meta.getPersistentDataContainer().set(key, type, data);
		stack.setItemMeta(meta);
		return stack;
		
	}
	
	public static <T> T GetPersistenData(ItemStack stack, String keyName, PersistentDataType<T, T> type)
	{
		T value = null;
		if (!IsValid(stack))  return value;
		
		NamespacedKey key = new NamespacedKey(_main, keyName);
		ItemMeta meta = stack.getItemMeta();
		PersistentDataContainer container = meta.getPersistentDataContainer();
		if(container.has(key, type))
		{
			value = container.get(key, type);
			return value;
		}
		
		return value;
	}
	
	public static ItemStack RemovePersistenData(ItemStack stack, String keyName)
	{
		if (!IsValid(stack))  return stack;

		NamespacedKey key = new NamespacedKey(_main, keyName);
		ItemMeta meta = stack.getItemMeta();
		meta.getPersistentDataContainer().remove(key);
		stack.setItemMeta(meta);

		return stack;
	}
	
	private static final String tagID = "pd_tg_";
	public static ItemStack SetTag(ItemStack stack, String tag)
	{
		if (!IsValid(stack))  return stack;
		
		SetPersistenData(stack, tagID+tag, PersistentDataType.INTEGER, 1);
		return stack;
	}
	
	public static boolean HasTag(ItemStack stack, String tag) 
	{
	    if (!IsValid(stack)) return false;
	    
	    return GetPersistenData(stack, tagID + tag, PersistentDataType.INTEGER) != null;
	}
	
	public static ItemStack RemoveTag(ItemStack stack, String tag) 
	{
	    if (!IsValid(stack)) return stack;
	    
	    return RemovePersistenData(stack, "pd_tg_" + tag);
	}
	
	public static List<String> GetTags(ItemStack stack) 
	{
	    List<String> tags = new ArrayList<>();
	    if (!IsValid(stack)) return tags;
	    
	    ItemMeta meta = stack.getItemMeta();
	    PersistentDataContainer container = meta.getPersistentDataContainer();
	    for (NamespacedKey key : container.getKeys()) 
	    {
	        if (key.getKey().startsWith(tagID)) 
	        {
	            tags.add(key.getKey().substring(6)); // Assuming "pd_tg_" is 6 characters long
	        }
	    }
	    return tags;
	}



	///<<<< PERSISTENT DATA
	
	
}
