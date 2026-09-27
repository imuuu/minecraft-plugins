package imu.DontLoseItems.CustomItems.RarityItems;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import imu.DontLoseItems.Enums.ITEM_RARITY;
import imu.DontLoseItems.other.RarityItem;
import imu.iAPI.Other.Metods;

public class Hell_Triple_Sword extends RarityItem
{
	public boolean Enable_PVP = true;
	public int PVP_CooldownSeconds = 120;
	public Hell_Triple_Sword( ITEM_RARITY rarity, double[] values)
	{
		super(GetBaseItemStack(rarity), ChatColor.DARK_RED + "Hell Triple Sword", rarity, values);
		
	}
	
	private static ItemStack GetBaseItemStack(ITEM_RARITY rarity)
	{
		switch (rarity)
		{
			case Common: 	return new ItemStack(Material.WOODEN_SWORD);
			case Uncommon: 	return new ItemStack(Material.WOODEN_SWORD);
			case Rare: 		return new ItemStack(Material.WOODEN_SWORD);
			case Epic: 		return new ItemStack(Material.IRON_SWORD);
			case Mythic: 	return new ItemStack(Material.DIAMOND_SWORD);
			case Legendary: return new ItemStack(Material.NETHERITE_SWORD);
			case Void: 		return new ItemStack(Material.NETHERITE_SWORD);
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
			Metods.setDisplayName(stack, "&0"+Rarity.toString()+"&4 Triple Sword");
		}
		
		return stack;
		
	}
	
	public Material GetMaterial()
	{
		return GetBaseItemStack(Rarity).getType();
	}
	
	public double GetUseCooldown()
	{
		return 7;
	}
	
	public double GetDotTimeMultiplier()
	{
		switch (Rarity)
		{
		case Epic: 		return 1;
		case Mythic: 	return 2;
		case Legendary: return 4;
		case Void: 		return 7;
		default: 		return 0;
		
		}
	}
	public int GetThrowDistance()
	{
		switch (Rarity)
		{
		case Epic: 		return 5;
		case Mythic: 	return 10;
		case Legendary: return 20;
		case Void: 		return 40;
		default: return 0;
		
		}
	}
	
	public boolean HasDotDamageEntityMultiplier()
	{
		switch (Rarity)
		{
		case Epic: 		return false;
		case Mythic: 	return false;
		case Legendary: return false;
		case Void: 		return true;
		default: 		return false;
		
		}
	}
	
	public boolean HasBringEntityClose()
	{
		switch (Rarity)
		{
		case Epic: 		return false;
		case Mythic: 	return false;
		case Legendary: return false;
		case Void: 		return true;
		default: return false;
		
		}
	}
	
	
}




