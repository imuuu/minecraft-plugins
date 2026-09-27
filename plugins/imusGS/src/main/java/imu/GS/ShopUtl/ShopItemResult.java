package imu.GS.ShopUtl;

import org.bukkit.inventory.ItemStack;

public class ShopItemResult 
{
	public ItemStack _stack;
	public int _amount;
	
	public ShopItemResult(ItemStack stack, int amount) 
	{
		_stack = stack;
		_amount = amount;
	}
	public void AddAmount(int amount)
	{
		_amount += amount;
	}
}
