package me.imu.imusenchants.CustomEnchants.Enchants.Combat;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.EnchantEffects;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class ExecuteEnchant extends CustomEnchant
{
	private static final double LOW_HEALTH = 0.3;

	@Override public String GetKey() { return "execute"; }
	@Override public String GetName() { return "Execute"; }
	@Override public int GetMaxLevel() { return 3; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.WEAPON; }
	@Override public int GetPriority() { return 100; } // damage changes go before effects that read the damage

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.MELEE;
	}

	private static double GetBonus(int level) { return Scale(level, 0.10, 0.20, 0.30); }

	@Override
	public String GetDescription(int level)
	{
		return "+" + Percent(GetBonus(level)) + " damage to targets below " + Percent(LOW_HEALTH) + " health";
	}

	@Override
	public void OnAttack(EntityDamageByEntityEvent event, Player attacker, LivingEntity victim, ItemStack weapon, int level)
	{
		if (victim.getHealth() / EnchantEffects.GetMaxHealth(victim) < LOW_HEALTH)
			event.setDamage(event.getDamage() * (1 + GetBonus(level)));
	}
}
