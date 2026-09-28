package me.imu.imusenchants.CustomEnchants.Enchants.Tools;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.EnchantEffects;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class HasteEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "haste"; }
	@Override public String GetName() { return "Haste"; }
	@Override public int GetMaxLevel() { return 2; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.TOOL; }

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.SetOf(ItemTarget.PICKAXE, ItemTarget.SHOVEL, ItemTarget.AXE);
	}

	@Override
	public String GetDescription(int level)
	{
		return "Haste " + ToRoman(Math.min(level, 2)) + " while held";
	}

	@Override
	public void OnTick(Player player, ItemStack item, int level, long seconds)
	{
		EnchantEffects.KeepEffect(player, PotionEffectType.FAST_DIGGING, Math.min(level, 2) - 1, 60);
	}
}
