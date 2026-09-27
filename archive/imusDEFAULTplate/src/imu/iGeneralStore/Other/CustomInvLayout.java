package imu.iGeneralStore.Other;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import imu.iGeneralStore.Main.Main;


public class CustomInvLayout 
{
	protected Main _main = null;
	protected ItemMetods _itemM = null;
	String _name="";
	protected int _size = 0;
	
	protected Inventory _inv = null;
	protected Player _player = null;
	

	
	public CustomInvLayout(Main main,Player player, String name, int size)
	{
		_main = main;
		_name = name;
		_size = size;
		_player = player;		
		_inv =  _main.getServer().createInventory(null, _size, _name);
		
		_itemM = _main.get_itemM();
	}
	
	
	public boolean isThisInv(InventoryEvent e) 
	{
		InventoryView view = e.getView();

		if(view.getTitle().equalsIgnoreCase(_name) && view.getPlayer() == _player)
		{
			return true;
		}
		return false;
	}
		
	public void onDisable()
	{
		_player.closeInventory();
	}
	
	
	public void openThis() 
	{
		_player.openInventory(_inv);		
	}
	
	
	
}
