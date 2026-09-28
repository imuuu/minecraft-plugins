package me.imu.imusenchants.CustomEnchants.Enchants.Combat;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.EnchantEffects;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

// Chance to slow the target down and frost it over
public class FrostEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "frost"; }
	@Override public String GetName() { return "Frost"; }
	@Override public int GetMaxLevel() { return 2; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.WEAPON; }

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.SetOf(ItemTarget.SWORD);
	}

	private static double GetChance(int level)
	{
		return Scale(level, 0.15, 0.25);
	}

	@Override
	public String GetDescription(int level)
	{
		return Percent(GetChance(level)) + " chance to freeze the target";
	}

	@Override
	public void OnAttack(EntityDamageByEntityEvent event, Player attacker, LivingEntity victim, ItemStack weapon, int level)
	{
		if (!Roll(GetChance(level)))
			return;

		int duration = 40 + 20 * Math.min(level, 2);
		EnchantEffects.AddEffect(victim, PotionEffectType.SLOW, 1, duration);
		// Frosted look only, freeze damage starts at 140 ticks
		victim.setFreezeTicks(Math.max(victim.getFreezeTicks(), 100));
	}
}
