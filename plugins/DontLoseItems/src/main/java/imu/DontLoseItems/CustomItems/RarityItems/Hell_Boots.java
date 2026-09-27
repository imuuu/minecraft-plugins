package imu.DontLoseItems.CustomItems.RarityItems;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import imu.DontLoseItems.Enums.ITEM_RARITY;
import imu.DontLoseItems.other.RarityItem;
import imu.iAPI.Other.Metods;

public class Hell_Boots extends RarityItem
{

	public Hell_Boots(ITEM_RARITY rarity)
	{
		super(GetBaseItemStack(rarity), "noName", rarity, new double[] {});
	}
	
	private static ItemStack GetBaseItemStack(ITEM_RARITY rarity)
	{
		switch (rarity)
		{
			case Common: 	return new ItemStack(Material.LEATHER_BOOTS);
			case Uncommon: 	return new ItemStack(Material.GOLDEN_BOOTS);
			case Rare: 		return new ItemStack(Material.CHAINMAIL_BOOTS);
			case Epic: 		return new ItemStack(Material.IRON_BOOTS);
			case Mythic: 	return new ItemStack(Material.DIAMOND_BOOTS);
			case Legendary: return new ItemStack(Material.NETHERITE_BOOTS);
			case Void: 		return new ItemStack(Material.NETHERITE_BOOTS);
		case NONE:
			break;
		default:
			break;
		}
		
		return new ItemStack(Material.STONE);
	}
	
	@Override
	public ItemStack GetItemStack()
	{
		ItemStack stack = super.GetItemStack();
		
		if(Rarity == ITEM_RARITY.Void)
		{
			Metods.setDisplayName(stack, "&0"+Rarity.toString()+"&4 Boots");
		}
		else
		{
			Metods.setDisplayName(stack, "&4"+Rarity.toString()+"&4 Boots");
		}
		
		return stack;
		
	}

}
