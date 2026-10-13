package imu.iAPI.FastInventory;

import java.util.ArrayList;

import imu.iAPI.Menus.Paper.PaperMenus;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Named lists of items admins can take copies of, opened with /ia inv &lt;name&gt;.
 */
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
		PaperMenus.open(player, new ItemGalleryMenu(InvName, _stacks));
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
