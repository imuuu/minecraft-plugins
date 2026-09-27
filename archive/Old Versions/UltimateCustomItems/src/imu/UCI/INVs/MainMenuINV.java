package imu.UCI.INVs;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import imu.UCI.main.Main;
import net.md_5.bungee.api.ChatColor;

public class MainMenuINV extends CustomInvLayout
{

	public MainMenuINV(Main main, Player player, String name, int size,HashMap<String, Object> dataContainer) 
	{
		super(main, player, name, size, dataContainer);
		
		setButtons();
	}
	
	
	enum Button
	{
		GO_SETMENU(0);
				
		int type;
		
		Button(int i)
		{
			this.type = i;
		}
		public int getType()
		{
			return type;
		}		
	}
	
	void setButtons()
	{
		setSwitch(Button.GO_SETMENU.getType(), Material.DIAMOND, ChatColor.AQUA + "Set Menu", 1);
	}
	
	void buttonTree(Button button)
	{
		
		switch(button)
		{
			case GO_SETMENU:
				_main.getSetMenuManager().openNewInv(_player);
				break;
			default:
				System.out.println("No button");
		}
	}

	
	@EventHandler
	void onInvClick(InventoryClickEvent e)
	{
		if(isThisInv(e))
		{
			e.setCancelled(true);
			
			ItemStack stack = e.getCurrentItem();
			Integer bSwitch = getButtonSwitch(stack);
			if(bSwitch != null)
			{
				buttonTree(Button.values()[bSwitch]);
			}
			
			
		}
	}
	
	
}
