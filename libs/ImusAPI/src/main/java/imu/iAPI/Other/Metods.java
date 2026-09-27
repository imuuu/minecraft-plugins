package imu.iAPI.Other;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.TileState;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.Conversation.ConversationState;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.StringPrompt;
//import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
//import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;

//import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
//import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;

import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import imu.iAPI.Interfaces.DelaySendable;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Utilities.ItemUtilToolsArmors;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.level.block.entity.TileEntity;

public class Metods 
{
	public static Metods _ins;
	Plugin _main = null;
	
	public Metods(Plugin main) 
	{
		_ins = this;
		_main = main;
	}
	
	Pattern DIGIT_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");
	private final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf('&') + "[0-9A-FK-OR]");
	
	public boolean isDigit(String str)
	{
		if(str == null)
			return false;
		
		return DIGIT_PATTERN.matcher(str).matches();
	}
	
	public static final String FormatTime(long millis) 
	{
	    long secs = millis / 1000;
	    String str =String.format("&4%02dh &e: &3%02dm &e: &7%02ds", secs / 3600, (secs % 3600) / 60, secs % 60);
	    return msgC(str);
	}
	public ItemStack addLore(ItemStack stack, String lore, boolean addLast)
	{
		
    	if(stack != null && stack.getType() != Material.AIR)
    	{
    		lore = msgC(lore);
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
	
	public ItemStack addLore(ItemStack stack, String[] lores)
	{

    	if(stack != null && stack.getType() != Material.AIR)
    	{
			ItemMeta meta = stack.getItemMeta();
    		ArrayList<String> read_lores = new ArrayList<String>();
    		if(meta.hasLore())
    		{
    			read_lores.addAll(meta.getLore());
    		}
    		
    		int idx = 0;
			for(String l : lores)
			{
				read_lores.add(idx++, msgC(l));
			}
    		
    		meta.setLore(read_lores);
    		stack.setItemMeta(meta);

			
    	}
    	return stack;
	}
	
	public ItemStack RemoveLores(ItemStack stack)
	{
    	if(stack != null && stack.getType() != Material.AIR)
    	{
			ItemMeta meta = stack.getItemMeta();
    		meta.setLore(new ArrayList<>());
    		stack.setItemMeta(meta);
    	}
    	return stack;
	}
	
	public ItemStack addLore(ItemStack stack, Iterable<String> lores)
	{
    	if(stack != null && stack.getType() != Material.AIR)
    	{
			ItemMeta meta = stack.getItemMeta();
    		ArrayList<String> read_lores = new ArrayList<String>();
    		if(meta.hasLore())
    		{
    			read_lores.addAll(meta.getLore());
    		}
    		
    		int idx = 0;
			for(String l : lores)
			{
				read_lores.add(idx++, msgC(l));
			}
    		
    		meta.setLore(read_lores);
    		stack.setItemMeta(meta);

			
    	}
    	return stack;
	}
	public ItemStack SetLores(ItemStack stack, String[] lores, boolean removeEmpty)
	{
		return SetLores(stack, Arrays.asList(lores), removeEmpty);
	}
	public ItemStack SetLores(ItemStack stack, Iterable<String> lores, boolean removeEmpty)
	{

    	if(stack != null && stack.getType() != Material.AIR)
    	{
			ItemMeta meta = stack.getItemMeta();
			ArrayList<String> metaLores = new ArrayList<>();

    		for(String lore : lores)
    		{
    			metaLores.add(msgC(lore));
    		}
    		if(removeEmpty)
    		{
    			//System.out.println("Removing empty");
    			for(int i = metaLores.size()-1; i >= 0 ; i--)
    			{
    				//System.out.println("==> empty? "+metaLores.get(i)+ "is it? "+Strings.isNullOrEmpty(metaLores.get(i)));
    				if(IsStringNullOrEmpty(metaLores.get(i)))
    				{
    					metaLores.remove(i);
    				}
    			}
    		}
    		
    		meta.setLore(metaLores);
    		
    		stack.setItemMeta(meta);

    	}
    	return stack;
	}
	
	public ItemStack reSetLore(ItemStack stack, String lore, int index)
	{

		ItemMeta meta = stack.getItemMeta();
		if(!meta.hasLore())
		{
			meta.setLore(new ArrayList<>());			
		}
		
		ArrayList<String> metaLores = (ArrayList<String>)meta.getLore();
		int size = metaLores.size();
		if(size <= index)
		{
			//System.out.println("add more lines: "+(index-size));

			while(metaLores.size() < index+1)
			{
				metaLores.add("");
				//System.out.println("line => added");
			}
		}
		metaLores.set(index, lore);
		meta.setLore(metaLores);
		
		stack.setItemMeta(meta);

		return stack;
	}
	
	public ItemStack hideAttributes(ItemStack stack)
	{
		if(stack != null && stack.getType() != Material.AIR)
    	{
			ItemMeta meta =stack.getItemMeta();
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
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
	
	public Set<Enchantment> GetEnchants(ItemStack stack)
	{
		//if(stack.getType() == Material.ENCHANTED_BOOK) return ((EnchantmentStorageMeta)stack.getItemMeta()).getStoredEnchants().keySet();
		if(stack.getType() != Material.ENCHANTED_BOOK) return stack.getEnchantments().keySet();
		
		Set<Enchantment> enchs = new HashSet<>();
		enchs.addAll(stack.getEnchantments().keySet());
		enchs.addAll(((EnchantmentStorageMeta)stack.getItemMeta()).getStoredEnchants().keySet());
		

		return enchs;
	}
	
	public boolean HasEnchant(ItemStack stack, Enchantment ench)
	{
		if(stack == null || stack.getType() == Material.AIR) return false;
		
		if(!stack.hasItemMeta()) return false;
		
		ItemMeta meta = stack.getItemMeta();
		
		if(meta.hasEnchant(ench)) return true;
		
		return false;
	}
	
	public int GetEnchantLevel(ItemStack stack, Enchantment ench)
	{
		if(stack == null || stack.getType() == Material.AIR) return 0;
		
		if(!stack.hasItemMeta()) return 0;
		
		ItemMeta meta = stack.getItemMeta();
		
		if(meta.hasEnchant(ench)) 
		{
			return meta.getEnchantLevel(ench);
		}
		
		return 0;
	}
	
	@SuppressWarnings("unused")
	public HashMap<Enchantment, Integer> GetEnchantsWithLevels(ItemStack stack)
	{
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
	
	public static void CloneEnchantments(ItemStack item1, ItemStack item2) {

	    item2.getEnchantments().keySet().forEach(enchantment -> item2.removeEnchantment(enchantment));
	    item1.getEnchantments().forEach((enchantment, level) -> item2.addUnsafeEnchantment(enchantment, level));
	}
	public static void RemoveEnchantments(ItemStack stack) {

		stack.getEnchantments().keySet().forEach(enchantment -> stack.removeEnchantment(enchantment));
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
	    			lore = ChatColor.stripColor(lore);
	    			for(int i = 0 ; i < lores.size() ; ++i)
	    			{  	
	    				String lore1 = ChatColor.stripColor(lores.get(i));
	    				if(lore1.contains(lore))
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
	
	public static ItemStack setDisplayName(ItemStack stack, String name)
	{
		if(stack != null && stack.getType() != Material.AIR)
		{
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(msgC(name));
			stack.setItemMeta(meta);
			return stack;
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
			meta.setDisplayName(msgC(dName));
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
//	public boolean isArmor(ItemStack stack)
//	{
//		
//		if(CraftItemStack.asNMSCopy(stack).c() instanceof ItemArmor || CraftItemStack.asNMSCopy(stack).c() instanceof ItemElytra)
//		{
//			return true;
//		}
//		return false;
//	}
	
	public boolean isArmor(ItemStack stack) 
	{ 
		return ItemUtilToolsArmors.IsArmor(stack);
	}

	public boolean isShulkerBox(ItemStack stack)
	{
		if(stack == null) return false;
		
		if(stack.getItemMeta() instanceof BlockStateMeta )
		{
			BlockStateMeta im = (BlockStateMeta)stack.getItemMeta();
            if(im.getBlockState() instanceof ShulkerBox){
            	return true;
            }
			
		}
		return false;
	}
	
//	public boolean isShield(ItemStack stack)
//	{
//		if(CraftItemStack.asNMSCopy(stack).c() instanceof ItemShield)
//		{
//			return true;
//		}
//		return false;
//	}
//	
//	public boolean IsPotion(ItemStack stack)
//	{
//		if(CraftItemStack.asNMSCopy(stack).c() instanceof ItemPotion) return true;
//		return false;
//		
//	}
	
	
	
	public boolean isTool(ItemStack stack) 
	{
		return ItemUtilToolsArmors.IsTool(stack);	
	}
	
	
	
	public ItemStack removePersistenData(ItemStack stack, String keyName)
	{
		if(stack.getType() == Material.AIR)
			return stack;

		NamespacedKey key = new NamespacedKey(_main, keyName);
		ItemMeta meta = stack.getItemMeta();
		meta.getPersistentDataContainer().remove(key);
		stack.setItemMeta(meta);

		return stack;
	}
	
	public <T> ItemStack setPersistenData(ItemStack stack, String keyName, PersistentDataType<T, T> type, T data)
	{
		if(stack == null || stack.getType() == Material.AIR)
			return stack;
		
		NamespacedKey key = new NamespacedKey(_main, keyName);
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
			NamespacedKey key = new NamespacedKey(_main, keyName);
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
	
	//works only below things
	//    Banner, Barrel, Beacon, Bed, Beehive, Bell, BlastFurnace, BrewingStand, Campfire, Chest, CommandBlock, 
	//Comparator, Conduit, Container, CreatureSpawner, DaylightDetector, Dispenser, Dropper, EnchantingTable, EnderChest, EndGateway, EntityBlockStorage<T>, 
	//Furnace, Hopper, Jigsaw, Jukebox, Lectern, SculkSensor, ShulkerBox, Sign, Skull, Smoker, Structure
	public <T> Block setPersistenData(Block block, String keyName, PersistentDataType<T, T> type, T data)
	{
		if(block == null || !(block.getState() instanceof TileState)) return null;
		
		TileState tileState = (TileState)block.getState();
		NamespacedKey key = new NamespacedKey(_main, keyName);
		tileState.getPersistentDataContainer().set(key, type, data);
		tileState.update();
		//System.out.println( "persistent data set to block: "+block.getType());
		return block;
	}
	
	public <T> T getPersistenData(Block block, String keyName, PersistentDataType<T, T> type)
	{
		T value = null;
		if(block == null || !(block.getState() instanceof TileState)) return value;
		
		TileState tileState = (TileState)block.getState();
		NamespacedKey key = new NamespacedKey(_main, keyName);
		PersistentDataContainer container = tileState.getPersistentDataContainer();
		if(container.has(key, type))
		{
			return container.get(key, type);
		}
				
		return value;
	}
	
	public <T> Entity setPersistenData(Entity entity, String keyName, PersistentDataType<T, T> type, T data)
	{
		if(entity == null) return null;
		NamespacedKey key = new NamespacedKey(_main, keyName);
		entity.getPersistentDataContainer().set(key, type, data);

		return entity;
	}
	
	public <T> T getPersistenData(Entity entity, String keyName, PersistentDataType<T, T> type)
	{
		T value = null;
		if(entity == null) return value;
		
		NamespacedKey key = new NamespacedKey(_main, keyName);
		if(entity.getPersistentDataContainer().has(key, type)) return entity.getPersistentDataContainer().get(key, type);
		
		return value;
	}
	
	public <T> Block SetMetaData(Block block, String keyName, T data)
	{
		block.setMetadata(keyName, new FixedMetadataValue(ImusAPI._instance, data));
		return block;
	}
	
	public <T> Object GetMetaData(Block block, String keyName)
	{
		
		if(!block.hasMetadata(keyName)) return null;

		return block.getMetadata(keyName).get(0).value();
	}
	
	public boolean HasMetaData(Block block, String keyName)
	{
		
		return block.hasMetadata(keyName);
	}
	
	public void RemoveMetaData(Block block, String keyName)
	{
		block.removeMetadata(keyName,ImusAPI._instance);
	}
	
	public Integer[] InventoryAddItemOrDrop(ItemStack stack, Player player)
	{
		//System.out.println("===========>adding item to inventory: "+stack.getType() + " stack amount: "+stack.getAmount());
		ArrayList<Integer> slots = new ArrayList<>();
		if(stack == null || stack.getType() == Material.AIR)
		{
			return slots.toArray(new Integer[slots.size()]);
		}
		
		PlayerInventory inv = player.getInventory();
//
//		int invSlot = inv.firstEmpty();
//		if(invSlot < 0)
//		{
//			dropItem(stack, player, true);
//			return slots.toArray(new Integer[slots.size()]);
//		}
//		inv.setItem(invSlot, stack);
//		
		
		int amount = stack.getAmount();
		//System.out.println("max stack size: "+stack.getMaxStackSize());
		if(stack.getMaxStackSize() != 1)
		{
			for(int i = 0; i < inv.getContents().length-6; ++i)
			{
				ItemStack invItem = inv.getItem(i);
				if(invItem == null) continue;
				
				if(invItem.isSimilar(stack) && invItem.getAmount() < invItem.getMaxStackSize())
				{
					int adding = invItem.getMaxStackSize() - invItem.getAmount();
					if(adding > amount)
						adding = amount;
					
					invItem.setAmount(invItem.getAmount() + adding);
					amount -= adding;
					
					slots.add(i);
				}
				
				if(amount <= 0)
					break;
			}
			
			
		}
		
		if(amount > 0)
		{
			ItemStack clone = stack.clone();
			int rolls = 1;
			int extra = 0;
//			System.out.println("max stack: "+stack.getMaxStackSize());
//			System.out.println("amount "+amount);
//			System.out.println("extra "+(amount % stack.getMaxStackSize()));
//			System.out.println("rolls "+((amount - extra)/stack.getMaxStackSize() + (extra > 0 ? 1 : 0)));
//			
			if(amount > stack.getMaxStackSize())
			{
				extra = (amount % stack.getMaxStackSize());
				rolls = (amount - extra)/stack.getMaxStackSize() + (extra > 0 ? 1 : 0);
				clone.setAmount(stack.getMaxStackSize());
			}
			
//			if(stack.getMaxStackSize() == 1)
//			{
//				rolls = amount;
//				clone.setAmount(1);
//			}
			else
			{
				clone.setAmount(amount);
			}

			for(int i = 0; i < rolls; i++)
			{
				if(extra > 0 && i == rolls-1)
				{
					clone.setAmount(extra);
				}
				int invSlot = inv.firstEmpty();
				if(invSlot < 0)
				{
					dropItem(clone.clone(), player, true);
				}
				else
				{
					inv.setItem(invSlot, clone);
					slots.add(invSlot);
				}
			}
			
		}
		

		return slots.toArray(new Integer[slots.size()]);

	}
	public Integer[] InventoryAddItemOrDrop(ItemStack stack, Player player, int amount)
	{
		//System.out.println("==> adding item to inventory: "+stack.getType() + " custom stack amount: "+amount);
		ArrayList<Integer> slots = new ArrayList<>();
		int left = amount;
		
		int leftOver;
		if(left > 64)
		{
			leftOver = left % 64;
		}
		else
		{
			leftOver = left;
		}

		int full_stacks_amount = (left -leftOver) == 0 ? 0 : (left -leftOver) / 64;
		ItemStack newStack;

		for(int i = 0; i < full_stacks_amount; ++i)
		{
			newStack = stack.clone();
			newStack.setAmount(64);
			
			Integer[] slot = InventoryAddItemOrDrop(newStack, player);
			if(slot.length == 0)
			{
				continue;
			}
			for(int l = 0; l < slot.length; l++) {slots.add(slot[l]);}
		

		}

		if(leftOver == 0)
			return slots.toArray(new Integer[slots.size()]);
		

		newStack = stack.clone();
		newStack.setAmount(leftOver);
		Integer[] slot = InventoryAddItemOrDrop(newStack, player);
		if(slot.length == 0)
		{
			return slots.toArray(new Integer[slots.size()]);
		}
		for(int l = 0; l < slot.length; l++) {slots.add(slot[l]);}
		
		return slots.toArray(new Integer[slots.size()]);

	}
	
//	public void moveItemFirstFreeSpaceInv(ItemStack stack, Player player, boolean includeHotbar)
//	{
//		if(stack == null || stack.getType() == Material.AIR)
//		{
//			return;
//		}
//		ItemStack copy = new ItemStack(stack);
//		stack.setAmount(0);
//		int dropAmount = 1;
//		if(copy.getAmount() > copy.getMaxStackSize())
//		{
//			dropAmount = copy.getAmount();
//			copy.setAmount(1);
//		}
//		PlayerInventory inv = player.getInventory();
//		
//		for(int i = 0; i < dropAmount ; ++i)
//		{
//			int invSlot;
//			if(includeHotbar)
//			{
//				invSlot = inv.firstEmpty();
//			}else
//			{
//				invSlot = getFirstEmpty(inv.getContents()); // no need I think
//			}
//
//			if( invSlot != -1)
//			{
//
//				//inv.addItem(copy);
//				inv.setItem(invSlot, copy);
//				
//			}else
//			{
//				player.sendMessage(ChatColor.RED + "You don't have space!");
//				dropItem(copy,player,true);			
//			}
//		}
		
		
//	}
	
	public int getFirstEmpty(ItemStack[] itemStacks)
	{
		for(int i = itemStacks.length-6; i > 8; --i) // armors lots and shield = -6, hotbar = 8
		{
			if(itemStacks[i] == null)
			{
				return i;
			}			
		}
		return -1;
	}
	
	public void dropItem(ItemStack stack, Player player, boolean putText)
	{
		ItemStack copy = new ItemStack(stack);
		stack.setAmount(0);
		
		if(putText)
		{
			player.sendMessage(ChatColor.RED + "You have dropped your "+copy.getAmount()+": "+ ChatColor.AQUA +copy.getType().toString());

		}
		
		Item dropped = player.getWorld().dropItemNaturally(player.getLocation(), copy);
		PlayerDropItemEvent event = new PlayerDropItemEvent(player, dropped);
		Bukkit.getPluginManager().callEvent(event);
	}

	/*public <K,V> void printHashMap(HashMap<K, V> map)
	{
		System.out.println("========MAP========");
		for(Entry<K, V> entry : map.entrySet())
		{
			if(entry.getValue().getClass().isArray())
			{
				@SuppressWarnings("unchecked")
				V[] values =(V[]) entry.getValue();
				System.out.println("==KEY: "+entry.getKey());
				for(V v : values)
				{
					System.out.println("values: "+v);
				}
				System.out.println("==KEY: "+entry.getKey());
			}else
			{
				System.out.println("Key: "+entry.getKey()+" value: "+entry.getValue());
			}
		}
		System.out.println("========MAP========");
	}*/

	public void printHashMap(HashMap<?, ?> map) {
		StringBuilder sb = new StringBuilder("========MAP========\n");
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Object value = entry.getValue();
			// Check if the value is null
			if (value == null) {
				sb.append("Key: ").append(entry.getKey()).append(" value: null\n");
			} else if (value.getClass().isArray()) {
				sb.append("==KEY: ").append(entry.getKey()).append("\n");
				Object[] array = (Object[]) value;
				for (Object v : array) {
					sb.append("values: ").append(v).append("\n");
				}
			} else {
				sb.append("Key: ").append(entry.getKey()).append(" value: ").append(value).append("\n");
			}
		}
		sb.append("========MAP========");
		System.out.println(sb.toString());
	}



	public <T> void printArray(String id, T[] arr)
	{
		System.out.println("====================");
		for(int i = 0; i < arr.length ; ++i)
		{
			System.out.println(i+": "+id+" : "+ arr[i]);
		}
		System.out.println("====================");
	}
	
	public boolean isEveryThingThis(Double[] arr, double value )
	{
		
		for(double v : arr)
		{
			if(v != value)
			{
				return false;
			}
		}

		return true;
	}
	
	public <T> void setAllThisValue(T[] arr, T value)
	{		
		for(int i = 0; i < arr.length ; ++i)
		{
			arr[i] = value;
		}

	}
	
	public String getMaterialCategory(Material m)
	{
		String name = m.name();
		if(name.contains("_"))
		{
			String[] parts = name.split("_");
			name = parts[parts.length-1];
		}
		return name;
	}
	
//	public String addColor(String str) {
//        String coloredString = str;
//        coloredString = coloredString.replace("{{BLACK}}", ChatColor.BLACK.toString());
//        coloredString = coloredString.replace("{{DARK_BLUE}}", ChatColor.DARK_BLUE.toString());
//        coloredString = coloredString.replace("{{DARK_GREEN}}", ChatColor.DARK_GREEN.toString());
//        coloredString = coloredString.replace("{{GREEN}}", ChatColor.GREEN.toString());
//        coloredString = coloredString.replace("{{DARK_CYAN}}", ChatColor.DARK_AQUA.toString());
//        coloredString = coloredString.replace("{{DARK_RED}}", ChatColor.DARK_RED.toString());
//        coloredString = coloredString.replace("{{PURPLE}}", ChatColor.DARK_PURPLE.toString());
//        coloredString = coloredString.replace("{{GOLD}}", ChatColor.GOLD.toString());
//        coloredString = coloredString.replace("{{GRAY}}", ChatColor.GRAY.toString());
//        coloredString = coloredString.replace("{{DARK_GRAY}}", ChatColor.DARK_GRAY.toString());
//        coloredString = coloredString.replace("{{BLUE}}", ChatColor.BLUE.toString());
//        coloredString = coloredString.replace("{{BRIGHT_GREEN}}", ChatColor.GREEN.toString());
//        coloredString = coloredString.replace("{{CYAN}}", ChatColor.AQUA.toString());
//        coloredString = coloredString.replace("{{RED}}", ChatColor.RED.toString());
//        coloredString = coloredString.replace("{{PINK}}", ChatColor.LIGHT_PURPLE.toString());
//        coloredString = coloredString.replace("{{YELLOW}}", ChatColor.YELLOW.toString());
//        coloredString = coloredString.replace("{{WHITE}}", ChatColor.WHITE.toString());
//        coloredString = coloredString.replace("{{OBFUSCATED}}", ChatColor.MAGIC.toString());
//        coloredString = coloredString.replace("{{BOLD}}", ChatColor.BOLD.toString());
//        coloredString = coloredString.replace("{{STRIKETHROUGH}}", ChatColor.STRIKETHROUGH.toString());
//        coloredString = coloredString.replace("{{UNDERLINE}}", ChatColor.UNDERLINE.toString());
//        coloredString = coloredString.replace("{{ITALIC}}", ChatColor.ITALIC.toString());
//        coloredString = coloredString.replace("{{RESET}}", ChatColor.RESET.toString());
//        coloredString = ChatColor.translateAlternateColorCodes('&', coloredString);
//        return coloredString;
//    }
	
	public boolean giveDamage(ItemStack stack,int dmg, boolean destroyItem)
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
	
	public double getDurabilityProsent(ItemStack stack)
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
	
	public void setDamage(ItemStack stack,int dmg)
	{
		if(stack != null && stack.getType() != Material.AIR)
		{
			Damageable meta = (Damageable) stack.getItemMeta();
			meta.setDamage(dmg);								
			stack.setItemMeta((ItemMeta)meta);
			
		}
	}
	
	public void sendMessageLater(Player player, DelaySendable ds, String msg)
	{
		new BukkitRunnable() 
		{
			
			@Override
			public void run() 
			{
				if(ds.isReady() == true)
				{
					player.sendMessage(msg);
					cancel();
				}
				
			}
		}.runTaskTimerAsynchronously(_main, 0, 20);
		
	}
	
	public void sendYesNoConfirm(Player player, String yesCommandStr, String noCommandStr)
	{
		TextComponent msgYes = new TextComponent("         YES");
		msgYes.setColor( net.md_5.bungee.api.ChatColor.GREEN);
		msgYes.setBold(true);
		msgYes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, yesCommandStr));
		
		TextComponent msgSlash = new TextComponent(" / ");
		msgSlash.setColor(net.md_5.bungee.api.ChatColor.DARK_GREEN);
		
		TextComponent msgNo = new TextComponent("NO");
		msgNo.setColor(net.md_5.bungee.api.ChatColor.RED);
		msgNo.setBold(true);
		msgNo.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, noCommandStr));
		
		
		msgYes.addExtra(msgSlash);
		msgYes.addExtra(msgNo);
		player.spigot().sendMessage(msgYes);
	}
	
	
	
	
	/**
	 * 
	 * @param player
	 * @param table text is key and value would be command. Remember command: /command pal pla
	 * @param seperator
	 */
	public void SendMessageCommands(Player player, String frontText, HashMap<String, String> table, String backText ,String seperator)
	{
		TextComponent main_msg = new TextComponent();
		main_msg.addExtra(frontText);
		for (Map.Entry<String, String> entry : table.entrySet()) 
		{
		    String text = entry.getKey();
		    String cmd_str = entry.getValue();
		    
		    TextComponent msg = new TextComponent(text);
		    msg.setBold(true);
		    msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd_str));
		    
		    TextComponent msgSlash = new TextComponent(" "+seperator+" ");
			msgSlash.setColor(net.md_5.bungee.api.ChatColor.DARK_GREEN);
		    
		    main_msg.addExtra(msg);
		    main_msg.addExtra(msgSlash);
		    
		}
		main_msg.addExtra(backText);
		player.spigot().sendMessage(main_msg);
	}
	
	public boolean doesStrArrayCointainStr(String[] args, String option)
    {
    	for(String str : args)
    	{
    		if(str.equalsIgnoreCase(option))
    		{
    			return true;
    		}
    	}
    	return false;
    }
	

//	@SuppressWarnings("deprecation")
//	public ItemStack getPlayerHead(Player p) 
//	{
//		boolean isNewVersion = Arrays.stream(Material.values()).map(Material::name).collect(Collectors.toList()).contains("PLAYER_HEAD");
//		Material type = Material.matchMaterial(isNewVersion ? "PLAYER_HEAD" : "SKULL_ITEM");
//		ItemStack item = new ItemStack(type,1);
//		
//		if(!isNewVersion)
//			item.setDurability((short) 3);
//		
//		SkullMeta meta = (SkullMeta) item.getItemMeta();
//		meta.setOwner(p.getName());
//		item.setItemMeta(meta);
//		return item;
//	}
	
	public static String msgC(String s)
	{
		if(s == null) return s;
		return ChatColor.translateAlternateColorCodes('&', s);
	}
	
	public String StripColor(String str)
	{
		return str == null ? null : STRIP_COLOR_PATTERN.matcher(str).replaceAll("");
	}
	
	
	public String EncodeItemStack(ItemStack stack)
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
	
	public ItemStack DecodeItemStack(String endcodedSTR)
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
	
	public String GetItemDisplayName(ItemStack stack)
	{
		String displayName = stack.getType().name();
		if(stack.hasItemMeta())
		{
			if(!IsStringNullOrEmpty(stack.getItemMeta().getDisplayName()))
				displayName = stack.getItemMeta().getDisplayName();
		}
			
		return displayName;

	}
	
	public <T> String CombineArrayToOneString(T[] array, String seperator)
	{
		String str = "";
		for(int i = 0; i < array.length; i++)
		{
			str += array[i];
			if(i < array.length-1)
				str += seperator;
		}
		return str;
	}
	
	public <T> String CombineArrayToOneString(List<T> array, String seperator)
	{
		String str = "";
		for(int i = 0; i < array.size(); i++)
		{
			str += array.get(i);
			if(i < array.size()-1)
				str += seperator;
		}
		return str;
	}
	
//	ItemStack AddGlow(ItemStack stack)
//	{
//		//NOT WORKING
//		//org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack nmsStack = (CraftItemStack) getField(stack, "handle");
//		net.minecraft.world.item.ItemStack s = CraftItemStack.asNMSCopy(stack);
//		NBTTagCompound compound = s.getTag();
//        
//        // Initialize the compound if we need to
//        if (compound == null) {
//            compound = new NBTTagCompound();            
//        }
//     
//        // Empty enchanting compound
//
//        compound.set("Enchantments", new NBTTagCompound());
//        s.setTag(compound);
//        
//        //stack.setItemMeta(.getItemMeta());
//        return CraftItemStack.asBukkitCopy(s);
//	}
	
//	private Object getField(Object obj, String name) {
//  try {
//      Field field = obj.getClass().getDeclaredField(name);
//      field.setAccessible(true);
//
//      return field.get(obj);
//  } catch (Exception e) {
//      // We don't care
//      throw new RuntimeException("Unable to retrieve field content.", e);
//  }
//}

	
	public ItemStack AddGlow(ItemStack stack)
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
	
	public void ConversationWithPlayer(Player player, StringPrompt conv)
	{
		ConversationFactory cf = new ConversationFactory(_main);
		Conversation conversation = cf.withFirstPrompt(conv).withLocalEcho(true).buildConversation(player);
		conversation.begin();
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				if(conversation.getState() == ConversationState.STARTED)
				{
					if(player.isOnline())
						player.sendMessage(msgC("&3Took too long to anwser. Please try again!"));
					
					conversation.abandon();
				}
			}
		}.runTaskLaterAsynchronously(_main, 20 * 30);
	}
	public static double Round(double value)
	{
		return Math.round(value * 100.00) / 100.00;
	}
	public boolean copyBlock(Block copyBlock, Block toSetBlock)
	{
		Material mat_copy = copyBlock.getType();
		Material mat_set = toSetBlock.getType();
		
		if(mat_copy != mat_set)
		{
			toSetBlock.setType(mat_copy, false);
		}
		
		if(copyBlock.getState() != toSetBlock.getState())
		{			
			final BlockState bState = toSetBlock.getState();
			bState.setBlockData(copyBlock.getBlockData());
			bState.update(true);
		}
		
		CraftWorld cw1 = (CraftWorld)copyBlock.getWorld();
		CraftWorld cw2 = (CraftWorld)toSetBlock.getWorld();
		
		Location loc_copy = copyBlock.getLocation();
		//TileEntity targetEntity = cw1.getHandle().
		//cw1.getHandle().
		//net.minecraft.core.BlockPosition blockPos = new BlockPosition(loc_copy.getBlockX(),loc_copy.getBlockY(),loc_copy.getBlockZ());
		TileEntity targetEntity = cw1.getHandle().c_( new BlockPosition(loc_copy.getBlockX(),loc_copy.getBlockY(),loc_copy.getBlockZ()));
		//TileEntity targetEntity = cw1.getHandle().c
		if(targetEntity == null)
		{
			return false;
		}
		Location loc_set = toSetBlock.getLocation();
		TileEntity copyEntity = cw2.getHandle().c_(new BlockPosition(loc_set.getBlockX(),loc_set.getBlockY(),loc_set.getBlockZ())); //get tileentity
		if(copyEntity == null)
		{
			return false;
		}

		NBTTagCompound ntc = new NBTTagCompound();
		NBTTagCompound ntc2 = new NBTTagCompound();
		targetEntity.a(ntc); //save
		//targetEntity.a
		ntc2 = (NBTTagCompound) ntc.h(); //ntc.clone()katto
		ntc2.a("x", loc_set.getBlockX());
		ntc2.a("y", loc_set.getBlockY()); //setInt
		ntc2.a("z", loc_set.getBlockZ());
		copyEntity.a(ntc2); //load  ja targetEntity.Getblock()
		//copyEntity.
//		copyEntity.ae_(); //update
		//copyEntity.ar_(); //update
		copyEntity.at_();
		
		return true;
	}
	
	public ItemStack[] GetShulkerBoxContent(ItemStack stack)
	{
		if(!isShulkerBox(stack)) return null;
		BlockStateMeta bsm = (BlockStateMeta)stack.getItemMeta();
		if(bsm.getBlockState() instanceof ShulkerBox)
		{
			return ((ShulkerBox)bsm.getBlockState()).getInventory().getContents();
		}
		return null;
	}
	
	public boolean SetShulkerBoxContent(ItemStack stack, ItemStack[] content)
	{
		if(!isShulkerBox(stack)) return false;
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
	
	public HashMap<Integer, ItemStack> AddItemShulkerBoxContent(ItemStack stack, ItemStack addedItem)
	{
		if(!isShulkerBox(stack)) return null;

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
	
	public int GetItemStackEnchantCount(ItemStack stack, Enchantment searchEnch)
	{
		
		if(!HasEnchant(stack, searchEnch)) return 0;
		
		for(Entry<Enchantment, Integer> ench : stack.getEnchantments().entrySet())
		{
			if(ench.getKey().equals(searchEnch)) return ench.getValue();
		}
		return 0;
	}
	public int GetArmorSlotEnchantCount(Player player, Enchantment searchEnch)
	{
		int value = 0;
		ItemStack[] armors = player.getInventory().getArmorContents();
		
		ItemStack helmet = armors[3];
		ItemStack chestplate = armors[2];
		ItemStack leggings = armors[1];
		ItemStack boots = armors[0];
		
		value += GetItemStackEnchantCount(helmet, searchEnch);
		value += GetItemStackEnchantCount(chestplate, searchEnch);
		value += GetItemStackEnchantCount(leggings, searchEnch);
		value += GetItemStackEnchantCount(boots, searchEnch);
		value += GetItemStackEnchantCount(player.getInventory().getItemInOffHand(), searchEnch);

//		for(int i = player.getInventory().getContents().length - 6 ; i < player.getInventory().getContents().length; i++)
//		{
//			ItemStack stack = player.getInventory().getContents()[i];
//			//if(stack == null || stack.getType() == Material.AIR) continue;
//			
//			value += GetItemStackEnchantCount(stack, searchEnch);
//		}
		
		return value;
	}
	
	public ArmorStand CreateHologram(String str, Location loc)
	{
		ArmorStand hologram = (ArmorStand)loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		hologram.setVisible(false);
		hologram.setCustomNameVisible(true);
		hologram.setCustomName(msgC(str));
		hologram.setGravity(false);
		return hologram;
	}
	
	public ItemStack FortuneSimulation(ItemStack stack, int fortuneLevel) 
	{
		if(!ImusAPI.FortuneOres.contains(stack.getType())) return stack;

	    int x = fortuneLevel;
	    double chance = 1.0 - (2.0 / ((double)x + 2.0)); 

	    int xx = stack.getAmount();
	    
	    if(ThreadLocalRandom.current().nextInt(100) < (chance*100.0))
	    	xx *= ThreadLocalRandom.current().nextInt(2,x+1);
   
	    stack.setAmount(xx);
	    return stack;
	}
	
	public static boolean IsStringNullOrEmpty(String value) 
	{
	    return value == null || value.isEmpty();
	}
	
	public static void SetSmithingRecipe(String id, Material mat1, Material mat2, ItemStack resultStack)
	{
		NamespacedKey key = new NamespacedKey(ImusAPI._instance, id);
        RecipeChoice.MaterialChoice choice1 = new RecipeChoice.MaterialChoice(mat1);
        RecipeChoice.MaterialChoice choice2 = new RecipeChoice.MaterialChoice(mat2);
        
        
        Bukkit.addRecipe(new SmithingRecipe(
                key,
                resultStack,
                choice1,
                choice2));
	}
	public static void SetSmithingRecipe(String id, ItemStack stack1, ItemStack stack2, ItemStack resultStack)
	{
		NamespacedKey key = new NamespacedKey(ImusAPI._instance, id);
        
        
        RecipeChoice.ExactChoice choice1 = new RecipeChoice.ExactChoice(stack1);
        RecipeChoice.ExactChoice choice2 = new RecipeChoice.ExactChoice(stack2);
        
        Bukkit.getServer().addRecipe(new SmithingRecipe(
                key,
                resultStack,
                choice1,
                choice2));
//        System.out.println("");
//        System.out.println("Stack added: "+stack1.getItemMeta().getDisplayName());
//        System.out.println("with Stack added: "+stack2.getItemMeta().getDisplayName());
	}
	
	/**
	 * 
	 * @param stack
	 * @return Linkedlist with stacks and their ratio in recipe
	 */
	public LinkedList<Tuple<ItemStack,Double>> GetRecipe(ItemStack stack)
	{
		LinkedList<Tuple<ItemStack,Double>> list = new LinkedList<>();
		Iterable<ItemStack> ingredients = null;
		Recipe re = null;
		for(Recipe recipe : Bukkit.getServer().getRecipesFor(stack))
		{
			re = recipe;
			if(recipe instanceof ShapedRecipe)
			{
				ShapedRecipe shapedRescipe = (ShapedRecipe)recipe;
				ingredients = shapedRescipe.getIngredientMap().values();
				break;
			}
			
			if(recipe instanceof ShapelessRecipe)
			{
				ShapelessRecipe shapedRescipe = (ShapelessRecipe)recipe;
				ingredients = shapedRescipe.getIngredientList();	
				break;
			}	
			
		}
		if(ingredients != null)
		{
			for(ItemStack ingredient : ingredients)
			{
				if(ingredient == null) continue;
				
				list.add(new Tuple<ItemStack, Double>(ingredient, 1.0/re.getResult().getAmount()));
			}
		}
		
		return list;
	}
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
