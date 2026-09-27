package imu.iCasino.Games.MainMenu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;
import imu.iCasino.Games.Crash.CreateTableCrashUI;
import net.md_5.bungee.api.ChatColor;

public class CreatingMainMenuUI extends CustomInvLayout
{
	
	public CreatingMainMenuUI(Plugin main, Metods metods, Player player) {
		super(main, metods, player, "Creating Menu", 6*9);
		
		setupButtons();
	}
	
	enum BUTTON implements IButton
	{
		NONE,
		GAME_CRASH;
	}
	
	public BUTTON getButton(ItemStack stack)
	{
		String name = getButtonName(stack);
		if(name != null)
			return BUTTON.valueOf(name);
		
		return BUTTON.NONE;
	}
	
	@Override
	public void invClosed(InventoryCloseEvent e) {
		
	}

	@Override
	public void setupButtons() 
	{
		ItemStack displayItem;
		displayItem = setupButton(BUTTON.GAME_CRASH, Material.DIAMOND, ChatColor.GOLD + "CRASH", null);
		_metods.addLore(displayItem, ChatColor.DARK_BLUE + "Press and start making table", true);
		_inv.setItem(1, displayItem);
		
	}


	@Override
	public void onClickInsideInv(InventoryClickEvent e) 
	{
		e.setCancelled(true);
		
		BUTTON button = getButton(e.getCurrentItem());
		
		switch (button) 
		{
		case NONE:			
			break;
		case GAME_CRASH:
			new CreateTableCrashUI(_main,_metods,_player).INIT(null);
			break;
		
		}
	}
}

