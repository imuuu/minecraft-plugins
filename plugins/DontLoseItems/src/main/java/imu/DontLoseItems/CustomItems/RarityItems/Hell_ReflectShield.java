package imu.DontLoseItems.CustomItems.RarityItems;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.inventory.ItemStack;

import imu.DontLoseItems.Enums.ITEM_RARITY;
import imu.DontLoseItems.other.RarityItem;
import imu.iAPI.Other.Metods;

public class Hell_ReflectShield extends RarityItem
{
	public boolean Enable_PVP = true;
	public int PVP_CooldownSeconds = 120;
	public Hell_ReflectShield( ITEM_RARITY rarity, double[] values)
	{
		super(GetBaseItemStack(rarity), ChatColor.DARK_RED + "Hell Reflect Shield", rarity, values);
		
	}
	
	private static ItemStack GetBaseItemStack(ITEM_RARITY rarity)
	{
		
		return new ItemStack(Material.SHIELD);
	}
	
	@Override
	public ItemStack GetItemStack()
	{
		ItemStack stack = super.GetItemStack();
		
		if(Rarity == ITEM_RARITY.Void)
		{
			Metods.setDisplayName(stack, "&0"+Rarity.toString()+"&4 Double Shield");
		}
		
		return stack;
		
	}
	
	public Material GetMaterial()
	{
		return GetBaseItemStack(Rarity).getType();
	}
	
	public int GetUseDurabilityLost()
	{
		switch (Rarity)
		{
		case Epic: 		return 1;
		case Mythic: 	return 1;
		case Legendary: return 1;
		case Void: 		return 1;
		default: 		return 1;
		
		}
	}
	
	
	
	
}




