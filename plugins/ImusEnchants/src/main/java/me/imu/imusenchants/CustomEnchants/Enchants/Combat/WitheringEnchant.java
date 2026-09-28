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

// wither the target
public class WitheringEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "withering"; }
	@Override public String GetName() { return "Withering"; }
	@Override public int GetMaxLevel() { return 2; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.WEAPON; }

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.SetOf(ItemTarget.SWORD);
	}

	private static double GetChance(int level)
	{
		return Scale(level, 0.10, 0.15);
	}

	@Override
	public String GetDescription(int level)
	{
		return Percent(GetChance(level)) + " chance to wither the target";
	}

	@Override
	public void OnAttack(EntityDamageByEntityEvent event, Player attacker, LivingEntity victim, ItemStack weapon, int level)
	{
		if (Roll(GetChance(level)))
			EnchantEffects.AddEffect(victim, PotionEffectType.WITHER, 0, 60 + 20 * level);
	}
}
