package imu.GeneralStore.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import imu.GeneralStore.Other.Shop;
import imu.GeneralStore.main.Main;
import net.md_5.bungee.api.ChatColor;

public class SomeSmallEventsClass implements Listener
{
	Main _main;
	Boolean enable_soulbound_nerf = true;
	
	Shop _tempShop = null;

	public SomeSmallEventsClass(Main main) 
	{
		_main = main;
		_tempShop = new Shop(main, "temp_events" , false, true);
	}
	
	public Boolean getEnable_soulbound_nerf() {
		return enable_soulbound_nerf;
	}

	public void setEnable_soulbound_nerf(Boolean enable_soulbound_nerf) {
		this.enable_soulbound_nerf = enable_soulbound_nerf;
	}
	
	@EventHandler
	public void onInvClic(PrepareAnvilEvent event)
	{
		if(enable_soulbound_nerf && event.getView().getPlayer() instanceof Player)
		{
			Player player = (Player) event.getView().getPlayer();
			AnvilInventory anvil_inv = (AnvilInventory) event.getInventory();
			ItemStack[] content = anvil_inv.getContents();

			if((_main.getItemM().findLoreIndex( content[0], "Soulbound") > -1 && _main.getItemM().findLoreIndex( content[1], "Soulbound") < 0) || 
			   (_main.getItemM().findLoreIndex( content[0], "Soulbound") < 0 && _main.getItemM().findLoreIndex( content[1], "Soulbound") > -1) 	)
			{
				if(content[0] == null || content[1] == null)
				{
					event.setResult(null);
					return;
				}
				event.setResult(null);
				player.sendMessage(ChatColor.RED + "You can't mix Soulbound item and normal item!");
			}
		}
		
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChangeWorld(PlayerChangedWorldEvent event)
	{
		
		Player p = event.getPlayer();
		boolean remove_item = false;
		for(ItemStack s : p.getInventory().getContents())
		{
			if(_tempShop.getPDCustomWorlds(s) != null)
			{
				if(!_tempShop.isWorldInCustomWorlds(s, p.getWorld().getName()))
				{
					s.setAmount(0);
					remove_item = true;
				}
			}
			
		}
		if(remove_item)
		{
			p.sendMessage(ChatColor.RED + "Items in your inventory that doesn't belong this world has been removed!");
			p.updateInventory();
		}
		
	}
	
}
