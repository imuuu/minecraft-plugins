package imu.UCI.INVs;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.UCI.Other.ItemMetods;
import imu.UCI.main.Main;


public abstract class CustomInvLayout implements Listener 
{
	Main _main = null;
	ItemMetods _itemM = null;
	String _name="";
	int _size = 0;
	
	Inventory _inv = null;
	Player _player = null;
	String pd_switch = "gs.buttonSwitch";
	
	HashMap<String, Object> _dataContainer = new HashMap<String, Object>();
	
	public CustomInvLayout(Main main, Player player, String name, int size, HashMap<String, Object> dataContainer)
	{
		_main = main;
		_name = name;
		_size = size;
		_player = player;		
		_inv =  _main.getServer().createInventory(null, _size, _name);
		_itemM = _main.getItemM();
		_main.getServer().getPluginManager().registerEvents(this, _main);
		setDataContainer(dataContainer);
	}
	
	public void setDataContainer(HashMap<String, Object> dataContainer)
	{
		if(dataContainer != null)
		{
			_dataContainer = dataContainer;
		}
		
	}
	
	void setSwitch(int SwitchID, Material material, String displayName,int itemSlot)
	{
		ItemStack sbutton = new ItemStack(material);
		_itemM.setDisplayName(sbutton, displayName);
		setButtonSwitch(sbutton, SwitchID);
		_inv.setItem(itemSlot, sbutton);
	}
	void setSwitch(int SwitchID, ItemStack sbutton, int itemSlot)
	{
		setButtonSwitch(sbutton, SwitchID);
		_inv.setItem(itemSlot, sbutton);
	}
	
	public boolean isThisInv(InventoryEvent e) 
	{
		InventoryView view = e.getView();

		if( view.getPlayer() == _player && view.getTitle().equalsIgnoreCase(_name))
		{
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onInvCloseEvent(InventoryCloseEvent e)
	{
		if(isThisInv(e))
		{
			System.out.println("inv closed: "+_name);
			HandlerList.unregisterAll(this);
		}
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
		_itemM.setPersistenData(stack, pd_switch, PersistentDataType.INTEGER, i);
	}
	
	Integer getButtonSwitch(ItemStack stack)
	{
		return _itemM.getPersistenData(stack, pd_switch, PersistentDataType.INTEGER);
	}
	
}
