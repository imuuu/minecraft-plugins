package imu.UCI.INVs;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import imu.UCI.Other.CustomSet;
import imu.UCI.main.Main;
import net.md_5.bungee.api.ChatColor;

public class SetMenuINV extends CustomInvLayout
{
	CustomSet _customSet = null;
	public SetMenuINV(Main main, Player player, String name, int size, HashMap<String, Object> dataContainer) 
	{
		super(main, player, name, size, dataContainer);
		
	
		setCustomSet();
		setButtons();
	}
	
	enum Button
	{
		GO_BACK_MAINMENU(0),
		SET_NAME(1),
		SET_SIZE(2),
		SET_SOCKETS(3),
		SETITEM(4);
				
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
	
	
	void setCustomSet()
	{
		String name = (String) _dataContainer.getOrDefault("name", "No name");
		int piece_size = (Integer) _dataContainer.getOrDefault("piecesize", 0);
		int sockets_size = (Integer) _dataContainer.getOrDefault("socketssize", 0);
		_customSet = new CustomSet(name, piece_size, sockets_size);
	}
	void setButtons()
	{
		setSwitch(Button.GO_BACK_MAINMENU.getType(), Material.RED_STAINED_GLASS_PANE, ChatColor.AQUA + "GO BACK", 0);
		setSwitch(Button.SET_NAME.getType(), Material.WHITE_BANNER, "Set Name", 1);
		setSwitch(Button.SET_SIZE.getType(), Material.WHITE_BANNER, "Set size of set", 2);
		setSwitch(Button.SET_SOCKETS.getType(), Material.WHITE_BANNER, "SET Sockets", 3);
		
		
		setSwitch(Button.SETITEM.getType(),makeSetDisplay(), 4);
	}
	
	ItemStack makeSetDisplay()
	{
		ItemStack setItem = new ItemStack(Material.PAPER);
		_itemM.setDisplayName(setItem, "CUSTOM SET");
		_itemM.addLore(setItem, "Name: "+ChatColor.RESET+_customSet.get_name() , true);
		_itemM.addLore(setItem, "Pieces: "+ChatColor.RESET+_customSet.get_pieceSize() , true);
		_itemM.addLore(setItem, "Sockets: "+ChatColor.RESET+_customSet.get_socketsSize() , true);
		return setItem;
	}
	
	void buttonTree(Button button)
	{
		
		switch(button)
		{
			case GO_BACK_MAINMENU:
				_main.getMainMenuManager().openNewInv(_player);
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
