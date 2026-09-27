package imu.iAPI.InvUtil;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class InventoryReaderStack 
{
	public class ItemInfo
	{
		public ItemStack _clonedStack;
		public int _totalCount = 0;
		public ArrayList<ItemStack> _refs = new ArrayList<>();
		
		public ItemInfo(ItemStack stack)
		{
			_clonedStack = stack.clone();
		}
		public ItemInfo Add(ItemStack stack)
		{
			_refs.add(stack);
			_totalCount+= stack.getAmount();
			//System.out.println("adding: "+stack+ " total amount: "+_totalCount);
			return this;
		}
		
		public boolean HasEnough(int amount)
		{
			return _totalCount  >= amount;
		}
		
		public boolean Reduce(int amount)
		{
			if(_totalCount < amount) return false;
		
			//System.out.println("start reducing");
			int left = amount;
			for(int i = _refs.size()-1; i >= 0; i--)
			{
				ItemStack s = _refs.get(i);
				int num = s.getAmount() - left;
				if(num <= 0)
				{
					s.setAmount(0);
					_refs.remove(i);
					left = Math.abs(num);
					
					if(left == 0)
					{
						_totalCount -= amount;
						return true;
					}				
				}
				else
				{
					s.setAmount(num);
					_totalCount -= amount;
					return true;
				}		
			}
			
			return false;
		}
	}
	
	HashMap<ItemStack, ItemInfo> _info = new HashMap<>();
	public InventoryReaderStack(Inventory inv)
	{
		LoadInv(inv);
		//Metods._ins.printHashMap(_info);
	}
	
	public boolean Reduce(ItemStack stack, int amount)
	{
		if(!HasStack(stack)) return false;
		
		return GetInfo(stack).Reduce(amount);
	}
	
	ItemInfo GetInfo(ItemStack stack)
	{
		ItemStack test = stack.clone();
		test.setAmount(1);
		//System.out.println("asking: "+test +" AND : "+_info.get(test)._totalCount+" ");
		return  _info.get(test);
	}
	
	public HashMap<ItemStack, ItemInfo> GetAllData()
	{
		return _info;
	}
	
	public boolean HasStack(ItemStack stack)
	{				
		return GetInfo(stack) == null ? false : true;
	}
	
	public boolean HasEnough(ItemStack stack, int amount)
	{
		if(!HasStack(stack)) return false;
		return GetInfo(stack).HasEnough(amount);
	}
	
	void LoadInv(Inventory inv)
	{
		for(ItemStack stack : inv.getContents())
		{
			if(stack == null || stack.getType() == Material.AIR) continue;
			
			AddToInfo(stack);
		}
	}
	
	void AddToInfo(ItemStack stack)
	{
		ItemStack test = stack.clone();
		test.setAmount(1);
		if(!_info.containsKey(test))
		{
			_info.put(test, new ItemInfo(stack));
		}
		_info.get(test).Add(stack);
	}
}
