package me.imu.imusenchants.CustomEnchants.Enchants.Armor;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.EnchantEffects;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class SaturationEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "saturation"; }
	@Override public String GetName() { return "Saturation"; }
	@Override public int GetMaxLevel() { return 3; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.ARMOR; }
	@Override public Set<ItemTarget> GetTargets() { return ItemTarget.SetOf(ItemTarget.HELMET); }

	private static double GetChance(int level) { return Scale(level, 0.25, 0.40, 0.55); }

	@Override
	public String GetDescription(int level)
	{
		return Percent(GetChance(level)) + " chance to not lose hunger";
	}

	@Override
	public void OnHunger(FoodLevelChangeEvent event, Player player, ItemStack armor, int level)
	{
		if (event.getFoodLevel() < player.getFoodLevel() && Roll(GetChance(level)))
			event.setCancelled(true);
	}
}
