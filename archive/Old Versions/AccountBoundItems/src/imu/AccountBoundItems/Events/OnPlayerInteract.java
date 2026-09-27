package imu.AccountBoundItems.Events;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import imu.AccountBoundItems.Other.ItemABI;

public class OnPlayerInteract implements Listener
{
	
	ItemABI itemAbi = new ItemABI();
	//ItemMetods itemM = new ItemMetods();
	
	@EventHandler
	public void onDragInv(InventoryDragEvent e)
	{		
		if(e.getWhoClicked() instanceof Player)
		{
			ItemStack inMouse_stack = e.getOldCursor();
			if(itemAbi.hasDurability(inMouse_stack))
			{
				
				if(itemAbi.isWaiting(inMouse_stack))
				{
					itemAbi.setBind(inMouse_stack, (Player)e.getWhoClicked(), true);
				}
				
				{
					e.setCancelled(true);
				}
			}
			
		}
		
		
	}
	
	@EventHandler
	public void onSwapPlayer(PlayerSwapHandItemsEvent e)
	{
		ItemStack main_hand = e.getOffHandItem();
		
		if(itemAbi.isWaiting(main_hand))
		{
			itemAbi.setBind(main_hand, e.getPlayer(), true);
		}
		
		
		if(itemAbi.isBroken(main_hand))
		{
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInvClose(InventoryCloseEvent e)
	{
		Player player = (Player) e.getPlayer();
		for(ItemStack stack : player.getInventory().getContents())
		{
			if(itemAbi.isBound(stack) && !itemAbi.getNameData(stack).equalsIgnoreCase(player.getName()))
			{
				itemAbi.dropItem(stack, player, false);
				player.sendMessage(ChatColor.RED + "Some item(s) didn't belong to you!");
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onInv(InventoryClickEvent e)
	{
		ItemStack hover_stack = e.getCurrentItem();
		ItemStack inMouse_stack = e.getCursor();

		Player player = null;
		player = (Player)e.getWhoClicked();
		
		if(itemAbi.isWaiting(hover_stack))
		{
			itemAbi.setBind(hover_stack, player, true);
		}
		
		if(itemAbi.isBound(hover_stack))
		{
			if(player != null)
			{
				
				if(!itemAbi.getNameData(hover_stack).equalsIgnoreCase(player.getName()))
				{
					e.setCancelled(true);
					player.closeInventory();
					player.sendMessage(ChatColor.RED + "This item doesn't belong to you!");
				}
				
			}
		}
				
		if(player != null && (player.getOpenInventory().getType() == InventoryType.CRAFTING || player.getOpenInventory().getType() == InventoryType.ANVIL))
		{
			if(itemAbi.hasDurability(inMouse_stack) || itemAbi.hasDurability(hover_stack))
			{
				if(itemAbi.isBroken(inMouse_stack) || itemAbi.isBroken(hover_stack))
				{
					
					SlotType slot_type = e.getSlotType();
						
					if(e.getRawSlot() != e.getSlot() && e.getSlot() > 8 )
					{
						if(slot_type == SlotType.ARMOR || slot_type == SlotType.QUICKBAR)
						{

							if(!itemAbi.isInArmorSlots(hover_stack, player) && !itemAbi.isInShieldSlot(hover_stack, player))
							{
								e.setCancelled(true);
								player.closeInventory();
								sendWarningMessage(player);
							}
							
							if(e.getAction() == InventoryAction.SWAP_WITH_CURSOR)
							{
								e.setCancelled(true);
								player.closeInventory();
								sendWarningMessage(player);
							}
											
						}
					}
					
					
					//System.out.println("CLICK TYPE: " + e.getClick());
					if(!itemAbi.isTool(hover_stack) )
					{
						if(e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT)
						{
							e.setCancelled(true);
							player.closeInventory();
							sendWarningMessage(player);

						}
					}
					
					if(player.getOpenInventory().getType() == InventoryType.ANVIL)
					{
						e.setCancelled(true);
						player.closeInventory();
						sendWarningMessage(player);
					}
					
				}
						
			}
			
		}
		
		
		
	}
	
	void sendWarningMessage(Player player)
	{
		player.sendMessage(ChatColor.RED + "You can't use broken item! Broken item is fragile and in use it might drop on the ground!"+ ChatColor.DARK_RED +" BE CAREFUL" );
	}
	

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e)
	{
		
		Player player = (Player) e.getPlayer();
		ItemStack main_stack = player.getInventory().getItemInMainHand();
		ItemStack off_stack = player.getInventory().getItemInOffHand();
		if(itemAbi.hasDurability(main_stack))
		{
			if(itemAbi.isWaiting(main_stack))
			{
				itemAbi.setBind(main_stack, player, true);
			}
			
			if(itemAbi.isBroken(main_stack) || itemAbi.isBroken(off_stack))
			{
				e.setCancelled(true);
				itemAbi.moveItemFirstFreeSpaceInv(main_stack, player, false,true);
				itemAbi.moveItemFirstFreeSpaceInv(off_stack, player, false,true);
			}			
		}
		
		
		
		
	}

	
	
}
