package me.imu.imusenchants.CustomEnchants.Enchants.Combat;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.EnchantEffects;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

// Full strength axe hits also hurt everything close to the target, players included
public class CleaveEnchant extends CustomEnchant
{
	private static final double RADIUS = 2.0;

	@Override public String GetKey() { return "cleave"; }
	@Override public String GetName() { return "Cleave"; }
	@Override public int GetMaxLevel() { return 3; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.WEAPON; }
	@Override public int GetPriority() { return -10; }

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.SetOf(ItemTarget.AXE);
	}

	private static double GetShare(int level) { return Scale(level, 0.20, 0.30, 0.40); }

	@Override
	public String GetDescription(int level)
	{
		return "Nearby enemies take " + Percent(GetShare(level)) + " of the damage";
	}

	@Override
	public void OnAttack(EntityDamageByEntityEvent event, Player attacker, LivingEntity victim, ItemStack weapon, int level)
	{
		if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK || attacker.getAttackCooldown() < 0.9f)
			return;

		double damage = event.getFinalDamage() * GetShare(level);
		for (Entity nearby : victim.getNearbyEntities(RADIUS, RADIUS, RADIUS))
		{
			if (!(nearby instanceof LivingEntity) || nearby == attacker || nearby instanceof ArmorStand)
				continue;

			// Leave the attacker's own pets alone
			if (nearby instanceof Tameable && attacker.equals(((Tameable) nearby).getOwner()))
				continue;

			EnchantEffects.DealExtraDamage((LivingEntity) nearby, damage, attacker);
		}
	}
}
