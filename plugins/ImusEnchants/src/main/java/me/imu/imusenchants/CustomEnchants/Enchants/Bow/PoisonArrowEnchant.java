package me.imu.imusenchants.CustomEnchants.Enchants.Bow;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.EnchantEffects;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class PoisonArrowEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "poison_arrow"; }
	@Override public String GetName() { return "Poison Arrow"; }
	@Override public int GetMaxLevel() { return 3; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.WEAPON; }

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.RANGED;
	}

	private static double GetChance(int level) { return Scale(level, 0.30, 0.45, 0.60); }

	@Override
	public String GetDescription(int level)
	{
		return Percent(GetChance(level)) + " chance for arrows to poison";
	}

	@Override
	public void OnProjectileDamage(EntityDamageByEntityEvent event, Player shooter, Projectile projectile, LivingEntity victim, int level)
	{
		if (Roll(GetChance(level)))
			EnchantEffects.AddEffect(victim, PotionEffectType.POISON, level >= 3 ? 1 : 0, 60 + 20 * level);
	}
}
