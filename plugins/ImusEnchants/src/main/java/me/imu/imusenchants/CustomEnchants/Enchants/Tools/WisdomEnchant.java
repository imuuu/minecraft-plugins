package me.imu.imusenchants.CustomEnchants.Enchants.Tools;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class WisdomEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "wisdom"; }
	@Override public String GetName() { return "Wisdom"; }
	@Override public int GetMaxLevel() { return 3; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.TOOL; }
	@Override public int GetPriority() { return 300; } // before Telekinesis collects the XP

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.Union(ItemTarget.MINING_TOOLS, ItemTarget.SetOf(ItemTarget.SWORD));
	}

	private static double GetBonus(int level)
	{
		return Scale(level, 0.25, 0.5, 0.75);
	}

	@Override
	public String GetDescription(int level)
	{
		return "+" + Percent(GetBonus(level)) + " XP from blocks and mobs";
	}

	@Override
	public boolean OnBlockBreak(BlockBreakEvent event, ItemStack tool, int level)
	{
		if (event.getExpToDrop() > 0)
			event.setExpToDrop((int) Math.round(event.getExpToDrop() * (1 + GetBonus(level))));
		return false;
	}

	@Override
	public void OnKill(EntityDeathEvent event, Player killer, ItemStack weapon, int level)
	{
		if (event.getDroppedExp() > 0)
			event.setDroppedExp((int) Math.round(event.getDroppedExp() * (1 + GetBonus(level))));
	}
}
