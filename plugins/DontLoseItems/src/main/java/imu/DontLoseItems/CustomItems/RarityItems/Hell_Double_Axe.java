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

public class Hell_Double_Axe extends RarityItem
{
	public boolean Enable_PVP = true;
	public int PVP_CooldownSeconds = 120;
	public Hell_Double_Axe( ITEM_RARITY rarity, double[] values)
	{
		super(GetBaseItemStack(rarity), ChatColor.DARK_RED + "Hell Double Axe", rarity, values);
		
	}
	
	@SuppressWarnings("incomplete-switch")
	private static ItemStack GetBaseItemStack(ITEM_RARITY rarity)
	{
		switch (rarity)
		{
			case Common: 	return new ItemStack(Material.WOODEN_AXE);
			case Uncommon: 	return new ItemStack(Material.WOODEN_AXE);
			case Rare: 		return new ItemStack(Material.WOODEN_AXE);
			case Epic: 		return new ItemStack(Material.IRON_AXE);
			case Mythic: 	return new ItemStack(Material.DIAMOND_AXE);
			case Legendary: return new ItemStack(Material.NETHERITE_AXE);
			case Void: 		return new ItemStack(Material.NETHERITE_AXE);

		}
		
		return new ItemStack(Material.STONE);
	}
	
	@Override
	public ItemStack GetItemStack()
	{
		ItemStack stack = super.GetItemStack();
		
		if(Rarity == ITEM_RARITY.Void)
		{
			Metods.setDisplayName(stack, "&0"+Rarity.toString()+"&4 Double Axe");
		}
		
		return stack;
		
	}
	
	public Material GetMaterial()
	{
		return GetBaseItemStack(Rarity).getType();
	}
	
	public double GetUseCooldown()
	{
		switch (Rarity)
		{
		case Epic: 		return 1.5;
		case Mythic: 	return 0.8;
		case Legendary: return 0.33;
		case Void: 		return 0.29;
		default: 		return 1;
		
		}
	}
	
	public double GetDamageIncrease()
	{
		switch (Rarity)
		{
		case Epic: 		return 1;
		case Mythic: 	return 1;
		case Legendary: return 1;
		case Void: 		return 2;
		default: return 1;
		
		}
	}
	
	public double GetDamageBase()
	{
		switch (Rarity)
		{
		case Epic: 		return 0.5;
		case Mythic: 	return 0.8;
		case Legendary: return 1.5;
		case Void: 		return 1.5;
		default: return 1;
		
		}
	}

	
	public void SpawnDamageParticle(Location loc, double damage)
	{
		Particle.DustOptions dustOptions  = null;

		if(damage > 9) 
		{
			dustOptions = new DustOptions(Color.RED, 1.5f);

			loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone(), 1, dustOptions);
			if(Rarity != ITEM_RARITY.Void) loc.getWorld().spawnParticle(Particle.DRIP_LAVA, loc.clone(), 5, 0.1,0.1,0.1);
			else loc.getWorld().spawnParticle(Particle.FALLING_OBSIDIAN_TEAR, loc.clone(), 5, 0.1,0.1,0.1);
			return;
		}
		if(damage > 8) 
		{
			dustOptions = new DustOptions(Color.YELLOW, 1.3f);

			loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone(), 1, dustOptions);
			return;
		}
		if(damage > 7) 
		{
			dustOptions = new DustOptions(Color.GREEN, 1.1f);

			loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone(), 1, dustOptions);
			return;
		}
		if(damage > 6) 
		{
			dustOptions = new DustOptions(Color.BLUE, 1.0f);

			loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone(), 1, dustOptions);
			return;
		}
		if(damage > 5) 
		{
			dustOptions = new DustOptions(Color.PURPLE, 0.9f);

			loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone(), 1, dustOptions);
			return;
		}
		if(damage > 4) 
		{
			dustOptions = new DustOptions(Color.AQUA, 0.8f);

			loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone(), 1, dustOptions);
			return;
		}
		if(damage > 3) 
		{
			dustOptions = new DustOptions(Color.PURPLE, 0.6f);

			loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone(), 1, dustOptions);
			return;
		}
		if(damage > 2)
		{
			dustOptions = new DustOptions(Color.TEAL, 0.5f);

			loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone(), 1, dustOptions);
			return;
		}
		if(damage > 1)
		{
			dustOptions = new DustOptions(Color.GRAY, 0.4f);
			loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone(), 1, dustOptions);
			return;
		}
		
		if(damage > 0)
		{
			dustOptions = new DustOptions(Color.BLACK, 0.3f);
			loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone(), 1, dustOptions);
			return;
		}
	
		
	}
	
	
	public double GetThrowForce()
	{
		switch (Rarity)
		{
		case Epic: 		return 0.4;
		case Mythic: 	return 0.6;
		case Legendary: return 0.9;
		case Void: 		return 1.2;
		default: return 0;
		
		}
	}
	
	
	
}




