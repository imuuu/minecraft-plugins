package imu.iMiniGames.Other;

import org.bukkit.inventory.ItemStack;

public class CombatConsumable 
{
	private ItemStack _stack;
	private int _amount = 0;
	
	public CombatConsumable(ItemStack stack) 
	{
		_stack = stack;
		_amount = stack.getAmount();
	}
	
	public int getAmount()	
	{
		return _amount;
	}
	
	public void addAmount(int add)
	{
		_amount += add;
		if(_amount < 0)
			_amount = 0;
	}
	
	public ItemStack getStack()
	{
		return _stack;
	}
}
