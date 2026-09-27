package imu.DontLoseItems.other;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.DontLoseItems.CustomItems.CustomItem;
import imu.DontLoseItems.Enums.CATEGORY;
import imu.DontLoseItems.Enums.ITEM_RARITY;
import imu.iAPI.FastInventory.Fast_Inventory;
import imu.iAPI.FastInventory.Manager_FastInventories;
import imu.iAPI.Other.Metods;

public class RarityItem extends CustomItem
{
	
	public ITEM_RARITY Rarity;
	
	public double[] Values;
	private final static String _PD_HELL_TIER = "HELL_TIER";
	
	public RarityItem(ItemStack stack, String name, ITEM_RARITY rarity, double[] values)
	{
		super(stack, name);
		
		Rarity = rarity;
		Values = values;
	}
	
	@Override
	public ItemStack GetItemStack()
	{
		ItemStack stack = Get_stack().clone();
		Metods.setDisplayName(stack, GetColor(Rarity)+" "+Get_displayName());
		Metods._ins.setPersistenData(stack, _PD_HELL_TIER, PersistentDataType.STRING, Rarity.toString());
		return stack;
	}
	

	public RarityItem AddToTestInventory(CATEGORY category)
	{
		boolean hasCat = Manager_FastInventories.Instance.HasFastInv(category.toString());
		
		if(hasCat)
		{
			Manager_FastInventories.Instance.AddItemStack(category.toString(), GetItemStack());
		}else
		{
			Fast_Inventory fastInv = new Fast_Inventory(category.toString(), "&4"+category.toString(), new ArrayList<>());
			fastInv.AddStack(GetItemStack());
			Manager_FastInventories.Instance.RegisterFastInventory(fastInv);
		}
		
		return this;
	}
	//private abstract ItemStack GetItemStackBase;
	
	public static ITEM_RARITY GetRarity(ItemStack stack)
	{
		String data  = Metods._ins.getPersistenData(stack, _PD_HELL_TIER, PersistentDataType.STRING);
		
		if(data == null) return ITEM_RARITY.Common;
		
		return ITEM_RARITY.valueOf(data);
	}
	private String GetColor(ITEM_RARITY rarity) 
	{
	    switch (rarity) {
	        case Common:
	            return ChatColor.GRAY + rarity.toString();
	        case Uncommon:
	            return ChatColor.GREEN + rarity.toString();
	        case Rare:
	            return ChatColor.YELLOW + rarity.toString();
	        case Epic:
	            return ChatColor.LIGHT_PURPLE + rarity.toString();
	        case Mythic:
	            return ChatColor.AQUA + rarity.toString();
	        case Legendary:
	            return ChatColor.GOLD + rarity.toString();
	        case Void:
	            return ChatColor.BLACK + rarity.toString().toUpperCase();
	            
	        default:
	            return ChatColor.WHITE + rarity.toString();
	    }
	}
	
	
}
