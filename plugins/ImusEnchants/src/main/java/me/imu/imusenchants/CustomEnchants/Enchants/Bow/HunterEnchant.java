package me.imu.imusenchants.CustomEnchants.Enchants.Bow;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Set;

public class HunterEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "hunter"; }
	@Override public String GetName() { return "Hunter"; }
	@Override public int GetMaxLevel() { return 3; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.WEAPON; }
	@Override public int GetPriority() { return 100; }

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.RANGED;
	}

	private static double GetBonus(int level) { return Scale(level, 0.15, 0.30, 0.45); }

	@Override
	public String GetDescription(int level)
	{
		return "+" + Percent(GetBonus(level)) + " arrow damage to animals";
	}

	@Override
	public void OnProjectileDamage(EntityDamageByEntityEvent event, Player shooter, Projectile projectile, LivingEntity victim, int level)
	{
		if (victim instanceof Animals)
			event.setDamage(event.getDamage() * (1 + GetBonus(level)));
	}
}
