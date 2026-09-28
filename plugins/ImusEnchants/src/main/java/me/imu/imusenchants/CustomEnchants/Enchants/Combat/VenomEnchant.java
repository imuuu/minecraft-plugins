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

// poison the target
public class VenomEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "venom"; }
	@Override public String GetName() { return "Venom"; }
	@Override public int GetMaxLevel() { return 3; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.WEAPON; }

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.SetOf(ItemTarget.SWORD);
	}

	private static double GetChance(int level)
	{
		return Scale(level, 0.10, 0.15, 0.20);
	}

	@Override
	public String GetDescription(int level)
	{
		return Percent(GetChance(level)) + " chance to poison the target";
	}

	@Override
	public void OnAttack(EntityDamageByEntityEvent event, Player attacker, LivingEntity victim, ItemStack weapon, int level)
	{
		if (Roll(GetChance(level)))
			EnchantEffects.AddEffect(victim, PotionEffectType.POISON, level >= 3 ? 1 : 0, 60 + 20 * level);
	}
}
