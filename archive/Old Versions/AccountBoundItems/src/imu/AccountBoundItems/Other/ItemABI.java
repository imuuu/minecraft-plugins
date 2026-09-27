package imu.AccountBoundItems.Other;

import java.util.ArrayList;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ItemABI extends ItemPersistentDataMethods
{
	
	public String boundStr;
	public String brokenStr;
		
	public ItemABI() 
	{
		boundStr = main.loreNames.get("bound");
		brokenStr = main.loreNames.get("broken");
	}
	
	/**
	 * 
	 * adds item bound lore, tag:bound,uuid
	 * @return
	 */
	
	public boolean setBind(ItemStack stack, Player player, boolean forceIt)
    {
		if(hasDurability(stack))
		{
			if(!hasLore(stack, boundStr))
	    	{ 		
	    		player.sendMessage("You have bind the item");
	    		addLore(stack, boundStr+ChatColor.AQUA+player.getName(), false);
	    		
	    		setNameData(stack, player.getName());
	    		setUUIDData(stack,  player.getUniqueId().toString());
	    		setWaitData(stack, 0);
	    		setOnUseWaitData(stack, 0);
	    		setBoundData(stack, 1);
	    		return true;
	    	}
	    	else
	    	{
	    		if(forceIt)
	    		{
	    			removeLore(stack, boundStr);
	    			setBind(stack, player, false);
	    		}
	    		else 
	    		{
	    			player.sendMessage("You have bind on item already");
	    		}
	    	  		
	    	}
		}else
		{
			player.sendMessage(ChatColor.RED + "You can't bind that item");			
		}
		return false;
    	
    }
	
	/**
	 * 
	 * adds item bound lore, tag:bound,price,uuid
	 * @return
	 */
	
	public boolean setBind(ItemStack stack, Player player, double price, boolean forceIt,boolean overridePrice)
    {
		if(hasDurability(stack))
		{
			if(!hasLore(stack, boundStr))
	    	{ 		
	    		player.sendMessage("You have bind the item");
	    		addLore(stack, boundStr+ChatColor.AQUA+player.getName(), false);
	    		
	    		setNameData(stack, player.getName());
	    		setUUIDData(stack,  player.getUniqueId().toString());
	    		setPriceData(stack, price,overridePrice);
	    		setWaitData(stack, 0);
	    		setOnUseWaitData(stack, 0);
	    		setBoundData(stack, 1);
	    		return true;
	    		
	    	}
	    	else
	    	{
	    		if(forceIt)
	    		{
	    			removeLore(stack, boundStr);
	    			setBind(stack, player, false);
	    		}
	    		else 
	    		{
	    			player.sendMessage("You have bind on item already");
	    		}
	    	  		
	    	}
		}
    	
    	return false;
    }
	
	public void setPriceOverride(ItemStack stack, double price)
	{
		setPriceData(stack, price, true);
	}
	
	public boolean setWait(ItemStack stack)
	{
		if(hasDurability(stack))
		{
			if(!hasLore(stack, boundStr))
	    	{ 		
	    		addLore(stack, boundStr+ChatColor.MAGIC+"WAITING BIND", false);
	    		
	    		setWaitData(stack, 1);
	    		return true;
	    		
	    	}
	    	else
	    	{	    		
				removeLore(stack, boundStr);
				setWait(stack);

	    	}
	    	
		}
		return false;
	}
	
	public boolean setOnUseWait(ItemStack stack)
	{
		if(hasDurability(stack))
		{
			if(!hasLore(stack, boundStr))
	    	{ 		
	    		addLore(stack, boundStr+ChatColor.DARK_PURPLE+"BIND ON USE", false);
	    		
	    		setOnUseWaitData(stack, 1);
	    		return true;
	    		
	    	}
	    	else
	    	{	    		
				removeLore(stack, boundStr);
				setOnUseWait(stack);

	    	}
	    	
		}
		return false;
	}
	
	/**
	 * 
	 * adds item broken lore and tag
	 * @return
	 */
	public void setBroken(ItemStack stack, boolean forceIt)
    {
    	if(!isBroken(stack))
    	{
    		//player.sendMessage(ChatColor.RED + "Your item: "+stack.getType().toString()+ " has broken!");
    		//addLore(stack, brokenStr, false);
    		removeDisplayName(stack, brokenStr);
    		//removeDisplayName(stack, ChatColor.RED+"");
    		addDisplayName(stack, brokenStr, true);
    		setBrokenData(stack, 1);
    		
    	}else
    	{
    		if(forceIt)
    		{
    			//removeLore(stack, brokenStr);
    			removeDisplayName(stack, brokenStr);
    			//removeDisplayName(stack, ChatColor.RED+"");
    			setBroken(stack, false);
    		}
    		
    	}
    		
    }
		
	public boolean isBound(ItemStack stack)
	{
		Integer value = getBoundData(stack);
		if(value != null)
		{
			if(value > 0)
			{
				return true;
			}
		}
		
		return false;
	}
	public boolean isBroken(ItemStack stack)
	{
		Integer value = getBrokenData(stack);
		if(value != null)
		{
			if(value > 0)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isWaiting(ItemStack stack)
	{
		Integer value = getWaitData(stack);
		if(value != null)
		{
			if(value > 0)
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	public boolean isOnUse(ItemStack stack)
	{
		Integer value = getOnUseWaitData(stack);
		if(value != null)
		{
			if(value > 0)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public double getItemCost(ItemStack stack,boolean includeDefaultPrice)
	{
		Double cost = 0.0;
		ArrayList<String> lores = getLores(stack);
		if(!isOverridePrice(stack))
		{
			if(stack != null && stack.getType() != Material.AIR)
	    	{
				ItemMeta meta = stack.getItemMeta();
				if(lores.size() > 0)
				{
					for(String lore : lores)
					{
						int firstL=getStringFirstUpperLetter(lore);
						if(firstL > -1)
						{
							String pureName=lore.substring(firstL, lore.length()).toLowerCase();
							if(main.lorePrices.containsKey(pureName))
							{
								cost += main.lorePrices.get(pureName);
							}
						}
						
					}
				}
							
	    		for(Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet())
	    		{
	    			if(!main.enchExPrices.containsKey(entry.getKey()))
	    			{
	    				String enchName = entry.getKey().getKey().toString().split(":")[1]+" "+entry.getValue();
	        			if(main.enchPrices.containsKey(enchName))
	        			{
	        				cost += main.enchPrices.get(enchName);
	        			}
	    			}
	    			else
	    			{
	    				Double[] values=main.enchExPrices.get(entry.getKey()); // minlvl, maxlvl, minprice, maxprice
	    				cost += priceCalculation(entry.getValue(), values[1], values[2], values[3]);
	    				
	    				
	    			}
	    			
	    		}
	    	
				
				if(main.materialPrices.containsKey(stack.getType()))
				{
					cost += main.materialPrices.get(stack.getType());
				}
	    	}
		}
		else
		{
			cost = getPriceData(stack);
			if(cost == null)
			{
				setPriceData(stack, 0, false);
				System.out.println("REPAIR COST NULL. Item has override status 1 but not money set!");
			}
		}
		
		if(includeDefaultPrice && cost <= 0)
		{
			cost = main.defaultPrice;
		}
		
		return cost;
	}
	
	public double priceCalculation(double levelNow, double maxLevel, double minPrice, double maxPrice)
	{
		double price = 0;
		double maxDmin = maxPrice / minPrice;
		double top = minPrice;
		double lower = Math.pow(maxDmin, 1/(maxLevel-1));
		double end = Math.pow(Math.pow(maxDmin, 1/(maxLevel-1)), levelNow);
		price = (top/lower) * end;		
		return price;
	}
	
	public void repairAll(ItemStack[] stacks)
	{
		for(ItemStack stack : stacks)
		{
			repair(stack);
		}
	}
	
	public void repair(ItemStack stack)
	{
		if(isBroken(stack))
		{
			removeDisplayName(stack, brokenStr);
			setBrokenData(stack, 0);
		}
	}
	
	public ItemStack getMoneyItem(double price)
	{
		ItemStack stack = new ItemStack(Material.PAPER);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA +"CHECK worth of "+ChatColor.GOLD +(int)price);
		stack.setItemMeta(meta);
		addLore(stack, ChatColor.DARK_PURPLE + "Redeem your reward from bank!", false);
		setPriceData(stack, price, true);
		setPersistenData(stack, main.keyNames.get("check"), PersistentDataType.INTEGER, 1);
		return stack;
	}
	
	public boolean withdrawPlayerHasMoney(Player player, double price)
	{
		double balance = econ.getBalance(player);
		if(balance > price)
		{
			econ.withdrawPlayer(player, price);
			return true;
		}
		player.sendMessage(ChatColor.RED + "You don't have enough money!");
		return false;
	}
	
	public boolean repair(ItemStack stack, Player player,double price)
	{
		if(isBroken(stack) && withdrawPlayerHasMoney(player, price))
		{
			removeDisplayName(stack, brokenStr);
			setBrokenData(stack, 0);
			return true;
		}
		return false;
	}
	
	public void unBind(ItemStack stack)
	{
		if(hasLore(stack, boundStr))
		{
			removeLore(stack, boundStr);
			setNameData(stack, "");
    		setUUIDData(stack, "");
    		setBoundData(stack, 0);
    		setWaitData(stack, 0);
    		setOnUseWaitData(stack, 0);
		}
	}
	
		

	/**
	 * 
	 * returns name who bound the item as string
	 * @return
	 */	
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
	
	/**
	 * 
	 * Drops item to ground where player is gives text and adds it to dropevent
	 * @return
	 */
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
	
	/**
	 * 
	 * Put item to player inventory if there is free space.. if not drop it on ground.. can include hotbar
	 * @return
	 */
	public void moveItemFirstFreeSpaceInv(ItemStack stack, Player player, boolean includeHotbar, boolean includeText)
	{
		if(stack == null || stack.getType() == Material.AIR)
		{
			return;
		}
		ItemStack copy = new ItemStack(stack);
		stack.setAmount(0);
		
		PlayerInventory inv = player.getInventory();
		
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
			if(includeText)
			{
				player.sendMessage(ChatColor.RED + "You can't use broken: "+ ChatColor.AQUA + copy.getType().toString());
			}
			
			inv.setItem(invSlot, copy);
		}else
		{
			player.sendMessage(ChatColor.RED + "You don't have space!");
			dropItem(copy,player,true);
			
			
		}
		
	}
}
