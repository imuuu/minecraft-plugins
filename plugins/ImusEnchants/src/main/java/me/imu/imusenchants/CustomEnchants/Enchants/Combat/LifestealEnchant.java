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

public class LifestealEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "lifesteal"; }
	@Override public String GetName() { return "Lifesteal"; }
	@Override public int GetMaxLevel() { return 3; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.WEAPON; }
	@Override public int GetPriority() { return -10; } // after damage changes like Execute

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.MELEE;
	}

	private static double GetChance(int level) { return Scale(level, 0.10, 0.15, 0.20); }
	private static double GetHeal(int level) { return Scale(level, 0.15, 0.25, 0.35); }

	@Override
	public String GetDescription(int level)
	{
		return Percent(GetChance(level)) + " chance to heal " + Percent(GetHeal(level)) + " of the damage dealt";
	}

	@Override
	public void OnAttack(EntityDamageByEntityEvent event, Player attacker, LivingEntity victim, ItemStack weapon, int level)
	{
		if (Roll(GetChance(level)))
			EnchantEffects.Heal(attacker, event.getFinalDamage() * GetHeal(level));
	}
}
