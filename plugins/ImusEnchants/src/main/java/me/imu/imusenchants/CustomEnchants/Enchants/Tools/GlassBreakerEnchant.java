package me.imu.imusenchants.CustomEnchants.Enchants.Tools;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class GlassBreakerEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "glass_breaker"; }
	@Override public String GetName() { return "Glass Breaker"; }
	@Override public int GetMaxLevel() { return 1; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.TOOL; }

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.MINING_TOOLS;
	}

	@Override
	public String GetDescription(int level)
	{
		return "Breaks glass instantly";
	}

	@Override
	public void OnBlockDamage(BlockDamageEvent event, ItemStack tool, int level)
	{
		if (event.getBlock().getType().name().contains("GLASS"))
			event.setInstaBreak(true);
	}
}
