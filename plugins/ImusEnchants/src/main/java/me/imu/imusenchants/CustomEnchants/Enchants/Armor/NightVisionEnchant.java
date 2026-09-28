package me.imu.imusenchants.CustomEnchants.Enchants.Armor;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.EnchantEffects;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class NightVisionEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "night_vision"; }
	@Override public String GetName() { return "Night Vision"; }
	@Override public int GetMaxLevel() { return 1; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.ARMOR; }
	@Override public Set<ItemTarget> GetTargets() { return ItemTarget.SetOf(ItemTarget.HELMET); }

	@Override
	public String GetDescription(int level)
	{
		return "See in the dark";
	}

	@Override
	public void OnTick(Player player, ItemStack item, int level, long seconds)
	{
		// Long enough that the screen never starts to flicker
		EnchantEffects.KeepEffect(player, PotionEffectType.NIGHT_VISION, 0, 300);
	}
}
