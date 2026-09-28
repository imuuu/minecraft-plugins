package me.imu.imusenchants.CustomEnchants.Enchants.Armor;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.EnchantEffects;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import java.util.Set;

// Less knockback. Uses Paper's knockback event, on Spigot the velocity is scaled a tick later.
public class StoppingForceEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "stopping_force"; }
	@Override public String GetName() { return "Stopping Force"; }
	@Override public int GetMaxLevel() { return 3; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.ARMOR; }
	@Override public Set<ItemTarget> GetTargets() { return ItemTarget.SetOf(ItemTarget.LEGGINGS); }

	private static double GetReduction(int level) { return Scale(level, 0.20, 0.40, 0.60); }

	@Override
	public String GetDescription(int level)
	{
		return "-" + Percent(GetReduction(level)) + " knockback";
	}

	@Override
	public double GetKnockbackMultiplier(int level)
	{
		return 1.0 - GetReduction(level);
	}
}
