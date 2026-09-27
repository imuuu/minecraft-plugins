package imu.def.Other;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.def.main.Main;


public class CustomInvLayout 
{
	Main _main = null;
	ItemMetods itemM = null;
	String _name="";
	int _size = 0;
	
	Inventory _inv = null;
	Player _player = null;
	String pd_switch = "gs.buttonSwitch";
	
	public CustomInvLayout(Main main,Player player, String name, int size)
	{
		_main = main;
		_name = name;
		_size = size;
		_player = player;		
		_inv =  _main.getServer().createInventory(null, _size, _name);
		
		//itemM
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
	
	void setButtonSwitch(ItemStack stack, int i)
	{
		itemM.setPersistenData(stack, pd_switch, PersistentDataType.INTEGER, i);
	}
	
	Integer getButtonSwitch(ItemStack stack)
	{
		return itemM.getPersistenData(stack, pd_switch, PersistentDataType.INTEGER);
	}
	
}
