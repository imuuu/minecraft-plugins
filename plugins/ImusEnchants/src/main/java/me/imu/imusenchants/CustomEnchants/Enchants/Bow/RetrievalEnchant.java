package me.imu.imusenchants.CustomEnchants.Enchants.Bow;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

// Chance for a shot arrow to come straight back to the inventory when it hits
public class RetrievalEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "retrieval"; }
	@Override public String GetName() { return "Retrieval"; }
	@Override public int GetMaxLevel() { return 3; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.WEAPON; }

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.RANGED;
	}

	@Override
	public Set<Enchantment> GetVanillaConflicts()
	{
		return Collections.singleton(Enchantment.ARROW_INFINITE);
	}

	private static double GetChance(int level) { return Scale(level, 0.20, 0.35, 0.50); }

	@Override
	public String GetDescription(int level)
	{
		return Percent(GetChance(level)) + " chance to get the arrow back";
	}

	@Override
	public void OnProjectileHit(ProjectileHitEvent event, Player shooter, Projectile projectile, int level)
	{
		if (!(projectile instanceof AbstractArrow))
			return;

		// Multishot copies and creative arrows can't be picked up, don't give them either
		AbstractArrow arrow = (AbstractArrow) projectile;
		if (arrow.getPickupStatus() != AbstractArrow.PickupStatus.ALLOWED || !Roll(GetChance(level)))
			return;

		ItemStack item;
		if (arrow instanceof SpectralArrow)
			item = new ItemStack(Material.SPECTRAL_ARROW);
		else if (arrow instanceof Arrow && !((Arrow) arrow).hasCustomEffects())
			item = new ItemStack(Material.ARROW);
		else
			return;

		HashMap<Integer, ItemStack> leftover = shooter.getInventory().addItem(item);
		if (leftover.isEmpty())
			arrow.remove();
	}
}
