package me.imu.imusenchants.CustomEnchants.Enchants.Bow;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

// Faster arrows that fly further. Vanilla arrow damage grows with speed, so the base damage is
// lowered by the same factor: Sniper is range, not a damage buff on top of Power.
public class SniperEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "sniper"; }
	@Override public String GetName() { return "Sniper"; }
	@Override public int GetMaxLevel() { return 3; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.WEAPON; }

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.RANGED;
	}

	private static double GetSpeed(int level) { return Scale(level, 1.2, 1.35, 1.5); }

	@Override
	public String GetDescription(int level)
	{
		return "Arrows fly " + Percent(GetSpeed(level) - 1) + " faster and further";
	}

	@Override
	public void OnShoot(EntityShootBowEvent event, Player shooter, ItemStack bow, int level)
	{
		double speed = GetSpeed(level);
		Entity projectile = event.getProjectile();
		projectile.setVelocity(projectile.getVelocity().multiply(speed));

		if (projectile instanceof AbstractArrow)
		{
			AbstractArrow arrow = (AbstractArrow) projectile;
			arrow.setDamage(arrow.getDamage() / speed);
		}
	}
}
