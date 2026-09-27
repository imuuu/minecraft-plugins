package me.imu.imusenchants.Enchants;

import me.imu.imusenchants.Enums.TOUCH_TYPE;
import org.bukkit.inventory.ItemStack;


public interface INode
{
	int GetX();
	int GetY();
	
	void SetPosition(int x, int y);
	
	INode[] GetNeighbors();
	
	void SetNeighbors(INode[] nodes);
	
	boolean IsLocked();
	
	boolean IsFrozen();
	
	void SetFrozen(boolean frozen);
	void SetLock(boolean lock);
	
	String Serialize();
	void Deserialize(String data);
	
	int GetFlatIndex();
	
	//public ItemStack GetItemStack();
	
	//Manangerii
	boolean IsValidGUIitem(TOUCH_TYPE touchType, EnchantedItem enchantedItem, ItemStack stack);
	
	//public ItemStack GetGUIitemSet(EnchantedItem enchantedItem);
	ItemStack GetGUIitemLoad(EnchantedItem enchantedItem);
	ItemStack GetGUIitemUnLoad(EnchantedItem enchantedItem, ItemStack stack);
	
	void Activate(EnchantedItem enchantedItem);
}
