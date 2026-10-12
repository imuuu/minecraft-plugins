package imu.iAPI.Utilities;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;


public class InvUtil
{
	/**
     * Adds an ItemStack to the player's inventory. If the inventory is full, it drops the overflow items on the ground.
     * @param player The player to add the ItemStack to.
     * @param item The ItemStack to add.
     */
    public static void AddItemToInventoryOrDrop(Player player, ItemStack item) 
    {
        PlayerInventory inventory = player.getInventory();
        HashMap<Integer, ItemStack> overflow = inventory.addItem(item);

        if (!overflow.isEmpty()) 
        {
            Location location = player.getLocation();
            World world = player.getWorld();
            for (ItemStack overflowItem : overflow.values()) 
            {
                world.dropItemNaturally(location, overflowItem);
                player.sendMessage(TextUtils.Color("&9Dropped: &e" + overflowItem.getType() + " &9x &e" + overflowItem.getAmount()));
            }
        }
    }

	// Moved from the old Methods class ================================================================================

	public static Integer[] AddItemOrDropGetSlots(ItemStack stack, Player player)
	{
		//System.out.println("===========>adding item to inventory: "+stack.getType() + " stack amount: "+stack.getAmount());
		ArrayList<Integer> slots = new ArrayList<>();
		if(stack == null || stack.getType() == Material.AIR)
		{
			return slots.toArray(new Integer[slots.size()]);
		}
		
		PlayerInventory inv = player.getInventory();
		
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
					DropItem(clone.clone(), player, true);
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

	public static Integer[] AddItemOrDropGetSlots(ItemStack stack, Player player, int amount)
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
			
			Integer[] slot = AddItemOrDropGetSlots(newStack, player);
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
		Integer[] slot = AddItemOrDropGetSlots(newStack, player);
		if(slot.length == 0)
		{
			return slots.toArray(new Integer[slots.size()]);
		}
		for(int l = 0; l < slot.length; l++) {slots.add(slot[l]);}
		
		return slots.toArray(new Integer[slots.size()]);

	}

	public static void DropItem(ItemStack stack, Player player, boolean putText)
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
}
