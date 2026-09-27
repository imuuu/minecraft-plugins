package imu.DontLoseItems.CustomItems.RarityItems;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import imu.DontLoseItems.Enums.ITEM_RARITY;
import imu.DontLoseItems.other.RarityItem;
import imu.iAPI.Other.Metods;

public class Hell_Pickaxe extends RarityItem
{

	public Hell_Pickaxe( ITEM_RARITY rarity, double[] values)
	{
		super(GetBaseItemStack(rarity), ChatColor.DARK_RED + "Hell Pickaxe", rarity, values);
		
	}

	private static ItemStack GetBaseItemStack(ITEM_RARITY rarity)
	{
		switch (rarity)
		{
			case Common: 	return new ItemStack(Material.WOODEN_AXE);
			case Uncommon: 	return new ItemStack(Material.WOODEN_AXE);
			case Rare: 		return new ItemStack(Material.WOODEN_AXE);
			case Epic: 		return new ItemStack(Material.IRON_PICKAXE);
			case Mythic: 	return new ItemStack(Material.DIAMOND_PICKAXE);
			case Legendary: return new ItemStack(Material.NETHERITE_PICKAXE);
			case Void: 		return new ItemStack(Material.NETHERITE_PICKAXE);
		default:
			break;
		}
		
		return new ItemStack(Material.STONE);
	}
	public int GetDurabilityLost()
	{
		switch (Rarity)
		{
		case Epic: 		return 3;
		case Mythic: 	return 2;
		case Legendary: return 2;
		case Void: 		return 0;
		default: return 0;
		
		}
	}
	
	public int GetDurabilityHealedByLava()
	{
		switch (Rarity)
		{
		case Epic: 		return 1;
		case Mythic: 	return 2;
		case Legendary: return 2;
		case Void: 		return 6;
		default: return 0;
		
		}
	}
	@Override
	public ItemStack GetItemStack()
	{
		ItemStack stack = super.GetItemStack();
		
		if(Rarity == ITEM_RARITY.Void)
		{
			Metods.setDisplayName(stack, "&0"+Rarity.toString()+"&4 Pickaxe");
		}
		
		return stack;
		
	}
	
	public Material GetMaterial()
	{
		return GetBaseItemStack(Rarity).getType();
	}
	
	

	
	
	
	
	
	
}




