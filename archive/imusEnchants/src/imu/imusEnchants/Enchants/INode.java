package imu.imusEnchants.Enchants;

import org.bukkit.inventory.ItemStack;

import imu.imusEnchants.Enums.TOUCH_TYPE;

public interface INode
{
	public int GetX();
	public int GetY();
	
	public void SetPosition(int x, int y);
	
	public INode[] GetNeighbors();
	
	public void SetNeighbors(INode[] nodes);
	
	public boolean IsLocked();
	
	public boolean IsFrozen();
	
	public void SetFrozen(boolean frozen);
	public void SetLock(boolean lock);
	
	public String Serialize();
	public void Deserialize(String data);
	
	public int GetFlatIndex();
	
	//public ItemStack GetItemStack();
	
	//Manangerii
	public boolean IsValidGUIitem(TOUCH_TYPE touchType, EnchantedItem enchantedItem, ItemStack stack);
	
	//public ItemStack GetGUIitemSet(EnchantedItem enchantedItem);
	public ItemStack GetGUIitemLoad(EnchantedItem enchantedItem);
	public ItemStack GetGUIitemUnLoad(EnchantedItem enchantedItem, ItemStack stack);
	
	public void Activate(EnchantedItem enchantedItem);
}
