package imu.iGeneralStore.Other;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iGeneralStore.Interfaces.DelaySendable;
import imu.iGeneralStore.Main.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_16_R3.ItemArmor;
import net.minecraft.server.v1_16_R3.ItemElytra;
import net.minecraft.server.v1_16_R3.ItemShield;

public class ImusAPI 
{
	Main _main = null;
	public ImusAPI(Main main) 
	{
		_main = main;
	}
	
	Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
	
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
	
	public ItemStack addLore(ItemStack stack, String[] lores, boolean addLast)
	{

    	if(stack != null && stack.getType() != Material.AIR)
    	{
			ItemMeta meta = stack.getItemMeta();
    		ArrayList<String> read_lores = new ArrayList<String>();
    		if(meta.hasLore())
    		{
    			read_lores.addAll(meta.getLore());
    		}
    		
    		if(addLast)
    		{
    			Collections.addAll(read_lores, lores);
    		}else
    		{
    			int idx = 0;
    			for(String l : lores)
    			{
    				read_lores.add(idx++, l);
    			}
    			
    		}
    		
    		meta.setLore(read_lores);
    		stack.setItemMeta(meta);

			
    	}
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
	
	public ItemStack reSetLore(ItemStack stack, String lore, int index)
	{
		ItemMeta meta = stack.getItemMeta();
		ArrayList<String> lores = new ArrayList<String>();	
		lores.addAll(meta.getLore());
		lores.set(index, lore);
		meta.setLore(lores);
		stack.setItemMeta(meta);
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
	
	public ItemStack setDisplayName(ItemStack stack, String name)
	{
		if(stack != null && stack.getType() != Material.AIR)
		{
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(name);
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
		if(stack.getType() == Material.AIR)
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
	
	public void moveItemFirstFreeSpaceInv(ItemStack stack, Player player, boolean includeHotbar)
	{
		if(stack == null || stack.getType() == Material.AIR)
		{
			return;
		}
		ItemStack copy = new ItemStack(stack);
		stack.setAmount(0);
		int dropAmount = 1;
		if(copy.getAmount() > copy.getMaxStackSize())
		{
			dropAmount = copy.getAmount();
			copy.setAmount(1);
		}
		PlayerInventory inv = player.getInventory();
		
		for(int i = 0; i < dropAmount ; ++i)
		{
			int invSlot;
			if(includeHotbar)
			{
				invSlot = inv.firstEmpty();
			}else
			{
				invSlot = getFirstEmpty(inv.getContents());
			}

			if( invSlot != -1)
			{

				inv.addItem(copy);
			}else
			{
				player.sendMessage(ChatColor.RED + "You don't have space!");
				dropItem(copy,player,true);			
			}
		}
		
		
	}
	
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
			player.sendMessage(ChatColor.RED + "You have dropped your: "+ ChatColor.AQUA +copy.getType().toString());
		}
		
		Item dropped = player.getWorld().dropItemNaturally(player.getLocation(), copy);
		PlayerDropItemEvent event = new PlayerDropItemEvent(player, dropped);
		Bukkit.getPluginManager().callEvent(event);
	}
	
	public <K,V> void printHashMap(HashMap<K, V> map)
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
	
	public String addColor(String str) {
        String coloredString = str;
        coloredString = coloredString.replace("{{BLACK}}", ChatColor.BLACK.toString());
        coloredString = coloredString.replace("{{DARK_BLUE}}", ChatColor.DARK_BLUE.toString());
        coloredString = coloredString.replace("{{DARK_GREEN}}", ChatColor.DARK_GREEN.toString());
        coloredString = coloredString.replace("{{GREEN}}", ChatColor.GREEN.toString());
        coloredString = coloredString.replace("{{DARK_CYAN}}", ChatColor.DARK_AQUA.toString());
        coloredString = coloredString.replace("{{DARK_RED}}", ChatColor.DARK_RED.toString());
        coloredString = coloredString.replace("{{PURPLE}}", ChatColor.DARK_PURPLE.toString());
        coloredString = coloredString.replace("{{GOLD}}", ChatColor.GOLD.toString());
        coloredString = coloredString.replace("{{GRAY}}", ChatColor.GRAY.toString());
        coloredString = coloredString.replace("{{DARK_GRAY}}", ChatColor.DARK_GRAY.toString());
        coloredString = coloredString.replace("{{BLUE}}", ChatColor.BLUE.toString());
        coloredString = coloredString.replace("{{BRIGHT_GREEN}}", ChatColor.GREEN.toString());
        coloredString = coloredString.replace("{{CYAN}}", ChatColor.AQUA.toString());
        coloredString = coloredString.replace("{{RED}}", ChatColor.RED.toString());
        coloredString = coloredString.replace("{{PINK}}", ChatColor.LIGHT_PURPLE.toString());
        coloredString = coloredString.replace("{{YELLOW}}", ChatColor.YELLOW.toString());
        coloredString = coloredString.replace("{{WHITE}}", ChatColor.WHITE.toString());
        coloredString = coloredString.replace("{{OBFUSCATED}}", ChatColor.MAGIC.toString());
        coloredString = coloredString.replace("{{BOLD}}", ChatColor.BOLD.toString());
        coloredString = coloredString.replace("{{STRIKETHROUGH}}", ChatColor.STRIKETHROUGH.toString());
        coloredString = coloredString.replace("{{UNDERLINE}}", ChatColor.UNDERLINE.toString());
        coloredString = coloredString.replace("{{ITALIC}}", ChatColor.ITALIC.toString());
        coloredString = coloredString.replace("{{RESET}}", ChatColor.RESET.toString());
        coloredString = ChatColor.translateAlternateColorCodes('&', coloredString);
        return coloredString;
    }
	
	public boolean giveDamage(ItemStack stack,int dmg, boolean destroyItem)
	{
		if(stack != null && stack.getType() != Material.AIR && stack.getType().getMaxDurability() > 0)
		{
			if(stack.getItemMeta() instanceof Damageable)
			{
				Damageable meta = (Damageable) stack.getItemMeta();
				int maxDur=stack.getType().getMaxDurability();
				int givenDamage = meta.getDamage()+ dmg;
					
				if(givenDamage >= maxDur)
				{
					System.out.println("RESET");
					meta.setDamage(maxDur);
					if(destroyItem)
					{
						stack.setAmount(0);
					}
				}else
				{
					System.out.println("DAMAGE SET: "+givenDamage);
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
		msgYes.setColor(ChatColor.GREEN);
		msgYes.setBold(true);
		msgYes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, yesCommandStr));
		
		TextComponent msgSlash = new TextComponent(" / ");
		msgSlash.setColor(ChatColor.DARK_GREEN);
		
		TextComponent msgNo = new TextComponent("NO");
		msgNo.setColor(ChatColor.RED);
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
	public void SendMessageCommands(Player player, HashMap<String, String> table, String seperator)
	{
		TextComponent main_msg = new TextComponent();
		for (Map.Entry<String, String> entry : table.entrySet()) 
		{
		    String text = entry.getKey();
		    String cmd_str = entry.getValue();
		    
		    TextComponent msg = new TextComponent(text);
		    msg.setBold(true);
		    msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd_str));
		    
		    TextComponent msgSlash = new TextComponent(" "+seperator+" ");
			msgSlash.setColor(ChatColor.DARK_GREEN);
		    
		    main_msg.addExtra(msg);
		    main_msg.addExtra(msgSlash);
		    
		}
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
	
	@SuppressWarnings("deprecation")
	public ItemStack getPlayerHead(Player p) 
	{
		boolean isNewVersion = Arrays.stream(Material.values()).map(Material::name).collect(Collectors.toList()).contains("PLAYER_HEAD");
		Material type = Material.matchMaterial(isNewVersion ? "PLAYER_HEAD" : "SKULL_ITEM");
		ItemStack item = new ItemStack(type,1);
		
		if(!isNewVersion)
			item.setDurability((short) 3);
		
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		meta.setOwner(p.getName());
		item.setItemMeta(meta);
		return item;
	}
	
	public String msgC(String s)
	{
		return ChatColor.translateAlternateColorCodes('&', s);
	}
	
	
}
