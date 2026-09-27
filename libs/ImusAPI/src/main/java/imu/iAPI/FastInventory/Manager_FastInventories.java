package imu.iAPI.FastInventory;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.iAPI.Main.ImusAPI;

public class Manager_FastInventories
{
	public static Manager_FastInventories Instance;
	private String base_cmd_ID = "";
	private HashMap<String, Fast_Inventory> _fastInventories;
	public Manager_FastInventories()
	{
		Instance = this;
		_fastInventories = new HashMap<>();
	}
	public void SetBaseID(String cmd_id)
	{
		base_cmd_ID = cmd_id;
		
	}
	public void OpenFastInv(String fastInv_ID, Player player)
	{
		Bukkit.getLogger().info("Opened fast inv named: "+fastInv_ID);
		if(!_fastInventories.containsKey(fastInv_ID.toLowerCase()))
		{
			Bukkit.getLogger().info("Couldnt find inv with named as for open:" +fastInv_ID);
			return;	
		}
		
		_fastInventories.get(fastInv_ID.toLowerCase()).OpenInv(player);
	}
	
	public void RegisterFastInventory(Fast_Inventory fastInv)
	{
		String id = fastInv.GetID();
		ImusAPI._instance.GetCMD1_TabCompleter().AddArgument(base_cmd_ID, id);
		
		_fastInventories.put(id.toLowerCase(), fastInv);
	}
	
	public boolean HasFastInv(String fastInv_ID)
	{
		if(!_fastInventories.containsKey(fastInv_ID.toLowerCase()))
		{		
			return false;
			
		}
		return true;
	}
	
	public void AddItemStack(String fastInv_ID,ItemStack stack)
	{
		if(!_fastInventories.containsKey(fastInv_ID.toLowerCase()))
		{
			Bukkit.getLogger().info("Couldnt find inv with named as:" +fastInv_ID);
			return;
			
		}
		
		_fastInventories.get(fastInv_ID.toLowerCase()).AddStack(stack);
	}
	
	public void TryToAdd(String fastInv_ID, String invName,ItemStack stack)
	{
		boolean hasCat = HasFastInv(fastInv_ID);
		
		if(hasCat)
		{
			AddItemStack(fastInv_ID, stack);
		}else
		{
			Fast_Inventory fastInv = new Fast_Inventory(fastInv_ID, invName, new ArrayList<>());
			fastInv.AddStack(stack);
			RegisterFastInventory(fastInv);

		}
	}
	
	public void TryToAdd(String fastInv_ID,ItemStack stack)
	{
		TryToAdd(fastInv_ID, fastInv_ID,stack);
	}
	
}
