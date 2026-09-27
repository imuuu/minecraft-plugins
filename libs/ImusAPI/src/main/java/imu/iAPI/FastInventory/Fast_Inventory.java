package imu.iAPI.FastInventory;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Fast_Inventory
{
	private String ID = "";
	private String InvName;
	public ArrayList<ItemStack> _stacks;
	public Fast_Inventory(String id, String invName, ArrayList<ItemStack> stacks)
	{
		ID = id;
		InvName = invName;
		
		if(stacks == null)
		{
			_stacks = new ArrayList<>();
			return;
		}
		_stacks = stacks;
	}
	
	public String GetID()
	{
		return ID;
	}
	
	public void OpenInv(Player player)
	{
		ArrayList<ItemStack> clones = new ArrayList<>();
		
		for(ItemStack stack : _stacks)
		{
			clones.add(stack);
		}
				
		new Click_Get_Inv(player, InvName, clones).openThis();
	}
	
	public void AddStack(ItemStack stack)
	{
		_stacks.add(stack);
	}
	
	public Fast_Inventory Register()
	{
		Manager_FastInventories.Instance.RegisterFastInventory(this);
		return this;
	}
	
}
