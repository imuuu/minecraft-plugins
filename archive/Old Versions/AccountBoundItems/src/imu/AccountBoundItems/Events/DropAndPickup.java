package imu.AccountBoundItems.Events;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import imu.AccountBoundItems.Other.ItemABI;

public class DropAndPickup implements Listener
{
	
	ItemABI itemAbi = new ItemABI();
	//ItemMetods itemM = new ItemMetods();
	
	int pickUpDelay = 8; // seconds
		
	@EventHandler
	public void onDrop(PlayerDropItemEvent e)
	{
		ItemStack stack = e.getItemDrop().getItemStack();

		
		
		if(itemAbi.hasDurability(stack))
		{
			boolean bound = itemAbi.isBound(stack);
			boolean broken = itemAbi.isBroken(stack);
			
			if(itemAbi.isWaiting(stack))
			{
				itemAbi.setBind(stack, e.getPlayer(), true);
			}
			
			if(bound || broken)
			{
				e.getItemDrop().setPickupDelay(20*pickUpDelay);
				e.getItemDrop().setGlowing(true);
				
			}
		}	
	}
	
	@EventHandler
	public void onPickUp(EntityPickupItemEvent e)
	{
		if(e.getEntity() instanceof Player)
		{
			Player player = (Player) e.getEntity();
			ItemStack stack = e.getItem().getItemStack();
			
			if(itemAbi.hasDurability(stack))
			{
				if(itemAbi.isWaiting(stack))
				{
					itemAbi.setBind(stack, player, true);
				}
				
				if(itemAbi.isBound(stack))
				{
					if(!player.getName().equalsIgnoreCase(itemAbi.getNameData(stack)) && !player.isOp())
					{
						player.sendMessage(ChatColor.RED + "This item doesn't belong to you!");
						e.getItem().setPickupDelay(20 * (int)(pickUpDelay*0.5));
						e.setCancelled(true);
						return;
					}
									
				}
			}
			
			
			
		}
	}
	
	
	
	
	
}
