package imu.iAPI.InvUtil;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryReaderMaterial 
{
	class ItemInfo
	{
		public int _totalCount = 0;
		public ArrayList<ItemStack> _refs = new ArrayList<>();
		
		public ItemInfo Add(ItemStack stack)
		{
			_refs.add(stack);
			_totalCount+= stack.getAmount();
			//System.out.println("adding: "+stack.getType()+ " total amount: "+_totalCount);
			return this;
		}
		
		public boolean HasEnough(int amount)
		{
			return _totalCount  >= amount;
		}
		
		public boolean Reduce(int amount)
		{
			if(_totalCount < amount) return false;
			
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
	
	HashMap<Material, ItemInfo> _info = new HashMap<>();
	public InventoryReaderMaterial(Inventory inv)
	{
		LoadInv(inv);
	}
	
	public boolean Reduce(Material mat, int amount)
	{
		if(!HasMaterial(mat)) return false;
		
		return _info.get(mat).Reduce(amount);
	}
	
	public boolean HasMaterial(Material mat)
	{
		return _info.containsKey(mat);
	}
	
	public boolean HasEnough(Material mat, int amount)
	{
		if(!HasMaterial(mat)) return false;
		return _info.get(mat).HasEnough(amount);
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
		if(!_info.containsKey(stack.getType()))
		{
			_info.put(stack.getType(), new ItemInfo());
			System.out.println("new info: "+stack.getType());
		}
		_info.get(stack.getType()).Add(stack);
	}
}
