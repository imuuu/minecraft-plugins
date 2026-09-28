package me.imu.imusenchants.CustomEnchants.Enchants.Armor;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.EnchantEffects;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Set;

// Chance to hit a melee attacker back with part of the damage
public class ReflectEnchant extends CustomEnchant
{
	private static final double SHARE = 0.3;

	@Override public String GetKey() { return "reflect"; }
	@Override public String GetName() { return "Reflect"; }
	@Override public int GetMaxLevel() { return 3; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.ARMOR; }
	@Override public Set<ItemTarget> GetTargets() { return ItemTarget.SetOf(ItemTarget.CHESTPLATE); }
	@Override public Set<Enchantment> GetVanillaConflicts() { return Collections.singleton(Enchantment.THORNS); }

	private static double GetChance(int level) { return Scale(level, 0.10, 0.15, 0.20); }

	@Override
	public String GetDescription(int level)
	{
		return Percent(GetChance(level)) + " chance to return " + Percent(SHARE) + " of melee damage";
	}

	@Override
	public void OnDamaged(EntityDamageEvent event, Player victim, ItemStack armor, int level)
	{
		if (!(event instanceof EntityDamageByEntityEvent) || event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK)
			return;

		if (!(((EntityDamageByEntityEvent) event).getDamager() instanceof LivingEntity) || !Roll(GetChance(level)))
			return;

		LivingEntity attacker = (LivingEntity) ((EntityDamageByEntityEvent) event).getDamager();
		EnchantEffects.DealExtraDamage(attacker, event.getFinalDamage() * SHARE, victim);
	}
}
