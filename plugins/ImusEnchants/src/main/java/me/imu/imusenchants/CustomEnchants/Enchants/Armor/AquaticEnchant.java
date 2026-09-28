package me.imu.imusenchants.CustomEnchants.Enchants.Armor;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.EnchantEffects;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class AquaticEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "aquatic"; }
	@Override public String GetName() { return "Aquatic"; }
	@Override public int GetMaxLevel() { return 1; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.ARMOR; }
	@Override public Set<ItemTarget> GetTargets() { return ItemTarget.SetOf(ItemTarget.HELMET); }

	@Override
	public String GetDescription(int level)
	{
		return "Breathe underwater";
	}

	@Override
	public void OnTick(Player player, ItemStack item, int level, long seconds)
	{
		if (player.isInWater())
			EnchantEffects.KeepEffect(player, PotionEffectType.WATER_BREATHING, 0, 100);
	}
}
