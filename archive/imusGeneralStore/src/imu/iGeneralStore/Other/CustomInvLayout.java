package imu.iGeneralStore.Other;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;

import imu.iGeneralStore.Main.Main;


public class CustomInvLayout 
{
	protected Main _main = null;
	protected ImusAPI _ia = null;
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
		
		_ia = _main.get_ia();
	}
	
	public void closeInventory()
	{
		_player.closeInventory();
	}
	
	public boolean isThisInv(InventoryEvent e) 
	{
		if(e.getInventory().equals(_inv)) //  && view.getPlayer() == _player
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
