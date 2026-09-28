package me.imu.imusenchants.CustomEnchants.Enchants.Armor;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.EnchantEffects;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class JumpBoostEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "jump_boost"; }
	@Override public String GetName() { return "Jump Boost"; }
	@Override public int GetMaxLevel() { return 2; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.ARMOR; }
	@Override public Set<ItemTarget> GetTargets() { return ItemTarget.SetOf(ItemTarget.BOOTS); }

	@Override
	public String GetDescription(int level)
	{
		return "Jump Boost " + ToRoman(Math.min(level, 2));
	}

	@Override
	public void OnTick(Player player, ItemStack item, int level, long seconds)
	{
		EnchantEffects.KeepEffect(player, PotionEffectType.JUMP, Math.min(level, 2) - 1, 60);
	}
}
