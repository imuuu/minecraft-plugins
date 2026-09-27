package imu.AccountBoundItems.Events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import imu.AccountBoundItems.Other.ItemABI;
import imu.AccountBoundItems.main.Main;

public class OnDamage implements Listener
{
	
	ItemABI itemAbi = new ItemABI();
	Main main = Main.getInstance();
	HashMap<Player, ItemStack[]> playerInv = new HashMap<Player, ItemStack[]>();
	
	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent e)
	{
		if(e.getDamager() instanceof Player)
		{
			if(checkItemsForBroken((Player)e.getDamager()))
			{
				e.setDamage(0);
			}
			
			if(e.getEntity() instanceof Player)
			{
				if(e.getEntity() instanceof LivingEntity)
				{
					LivingEntity entity = (LivingEntity) e.getEntity();
					if(entity.getKiller() != null)
					{
						//Player victim = (Player)entity;
						
						//pvp happend here
					}
					
				}
			}
			
		}	
	}
	
	@EventHandler
	public void onDurUse(PlayerItemDamageEvent e)
	{
		ItemStack stack = e.getItem();
		if(itemAbi.isOnUse(stack) || itemAbi.isWaiting(stack))
		{
			itemAbi.setBind(stack, e.getPlayer(), true);
		}
		
		
		
	}
	
	public void saveInv(Player player)
	{
		ArrayList<ItemStack> saved = new ArrayList<ItemStack>();
				
		for(ItemStack stack : player.getInventory().getContents())
		{
			if(itemAbi.hasDurability(stack))
			{
				if(itemAbi.isWaiting(stack))
				{
					itemAbi.setBind(stack, player, true);
				}
				
				if(itemAbi.isBound(stack))
				{
					
					double moneyAmount =itemAbi.getItemCost(stack,true)*(main.deadDropPricePros/100);
					if(moneyAmount > 0 && !itemAbi.isBroken(stack))
					{
						ItemStack moneyDrop = itemAbi.getMoneyItem(moneyAmount);
						itemAbi.dropItem(moneyDrop, player, false);
					}		
					itemAbi.setBroken(stack, true);
					saved.add(new ItemStack(stack));
					stack.setAmount(0);
				}
			}
		}	
		ItemStack[] array = saved.toArray(new ItemStack[saved.size()]);
		playerInv.put(player, array);
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e)
	{
		if(playerInv.containsKey(e.getPlayer()))
		{
			Inventory inv = e.getPlayer().getInventory();
			inv.addItem(playerInv.get(e.getPlayer()));
			playerInv.remove(e.getPlayer());
		}
		
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e)
	{
		saveInv(e.getEntity());
		//System.out.println("DEATH EVETN");
		
	}
		
	boolean checkItemsForBroken(Player player)
	{
		boolean weaponHasBroken = false;
		PlayerInventory inv = player.getInventory();
		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		stacks.add(inv.getItemInMainHand());
		stacks.add(inv.getItemInOffHand());
		stacks.addAll(Arrays.asList(inv.getArmorContents()));
		
		for(int i= 0 ; i < stacks.size(); ++i)
		{
			ItemStack stack = stacks.get(i);
			if(itemAbi.isBroken(stack))
			{
				if(i == 0) // main
				{
					player.sendMessage(ChatColor.RED + "There is no use for broken item, go fix it!");
					weaponHasBroken = true;
					continue;
				}
				
				itemAbi.dropItem(stack, player,true);
			}
		}
		return weaponHasBroken;
	}
	
	
	
	
	
	
}
