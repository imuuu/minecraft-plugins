package imu.DontLoseItems.CustomItems.RarityItems;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import imu.DontLoseItems.Enums.ITEM_RARITY;
import imu.DontLoseItems.other.RarityItem;
import imu.iAPI.Other.Metods;
import net.minecraft.world.entity.animal.EntityTropicalFish.Base;

public class Hell_Hoe extends RarityItem
{

	public Hell_Hoe( ITEM_RARITY rarity, double[] values)
	{
		super(GetBaseItemStack(rarity), ChatColor.DARK_RED + "Hell Hoe", rarity, values);
		
	}
	
	private static ItemStack GetBaseItemStack(ITEM_RARITY rarity)
	{
		switch (rarity)
		{
			case Common: 	return new ItemStack(Material.WOODEN_HOE);
			case Uncommon: 	return new ItemStack(Material.WOODEN_HOE);
			case Rare: 		return new ItemStack(Material.WOODEN_HOE);
			case Epic: 		return new ItemStack(Material.IRON_HOE);
			case Mythic: 	return new ItemStack(Material.DIAMOND_HOE);
			case Legendary: return new ItemStack(Material.NETHERITE_HOE);
			case Void: 		return new ItemStack(Material.NETHERITE_HOE);
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
			Metods.setDisplayName(stack, "&0"+Rarity.toString()+"&4 Hoe");
		}
		
		return stack;
		
	}
	
	public int GetSeedUsage()
	{
		switch (Rarity)
		{
		case Epic: 		return 3;
		case Mythic: 	return 2;
		case Legendary: return 1;
		case Void: 		return 1;
		default: return 0;
		
		}
	}
	
	public int GetAreaRadius()
	{
		switch (Rarity)
		{
		case Epic: 		return 1;
		case Mythic: 	return 1;
		case Legendary: return 1;
		case Void: 		return 2;
		default: return 0;
		
		}
	}
	
	public Material GetMaterial()
	{
		return GetBaseItemStack(Rarity).getType();
	}
	

	
	
	
	
	
	
}




