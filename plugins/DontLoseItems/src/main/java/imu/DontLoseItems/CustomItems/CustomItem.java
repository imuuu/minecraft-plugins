package imu.DontLoseItems.CustomItems;

import org.bukkit.inventory.ItemStack;

import imu.iAPI.Other.Metods;

public abstract class CustomItem
{
	private ItemStack _stack;
	private String _displayName;
	
	public CustomItem(ItemStack stack, String displayName)
	{
		this._stack = stack;
		this._displayName = displayName;
	}
	
	public ItemStack GetItemStack()
	{
		ItemStack stack = _stack.clone();
		Metods.setDisplayName(stack, _displayName);
		return stack;
	}
	
	public ItemStack Get_stack()
	{
		return _stack;
	}

	public void Set_stack(ItemStack _stack)
	{
		this._stack = _stack;
	}

	public String Get_displayName()
	{
		return _displayName;
	}

	public void Set_displayName(String _displayName)
	{
		this._displayName = _displayName;
	}
}
