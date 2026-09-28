package me.imu.imusenchants.CustomEnchants.Enchants.Armor;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.EnchantEffects;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class RegrowthEnchant extends CustomEnchant
{
	private static final int INTERVAL_SECONDS = 5;

	@Override public String GetKey() { return "regrowth"; }
	@Override public String GetName() { return "Regrowth"; }
	@Override public int GetMaxLevel() { return 3; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.ARMOR; }
	@Override public Set<ItemTarget> GetTargets() { return ItemTarget.SetOf(ItemTarget.CHESTPLATE); }

	private static double GetHeal(int level) { return Scale(level, 1.0, 2.0, 3.0); }

	@Override
	public String GetDescription(int level)
	{
		return "Heals " + (GetHeal(level) / 2) + " hearts every " + INTERVAL_SECONDS + " seconds";
	}

	@Override
	public void OnTick(Player player, ItemStack item, int level, long seconds)
	{
		if (seconds % INTERVAL_SECONDS == 0)
			EnchantEffects.Heal(player, GetHeal(level));
	}
}
