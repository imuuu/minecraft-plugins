package me.imu.imusenchants.CustomEnchants.Enchants.Tools;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import me.imu.imusenchants.CustomEnchants.MultiBreak;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// Harvesting a grown crop also harvests the grown crops around it. Works with Replanter,
// the extra crops go through the same drop event.
public class HarvestEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "harvest"; }
	@Override public String GetName() { return "Harvest"; }
	@Override public int GetMaxLevel() { return 2; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.TOOL; }
	@Override public int GetPriority() { return 5; }

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.SetOf(ItemTarget.HOE);
	}

	@Override
	public String GetDescription(int level)
	{
		int size = GetRadius(level) * 2 + 1;
		return "Harvests grown crops in a " + size + "x" + size + " area";
	}

	@Override
	public boolean OnBlockBreak(BlockBreakEvent event, ItemStack tool, int level)
	{
		Player player = event.getPlayer();
		if (MultiBreak.IsBreaking(player))
			return false;

		Block origin = event.getBlock();
		if (!CropUtil.IsMatureCrop(origin))
			return false;

		int radius = GetRadius(level);
		List<Block> crops = new ArrayList<>();
		for (int x = -radius; x <= radius; x++)
		{
			for (int z = -radius; z <= radius; z++)
			{
				Block block = origin.getRelative(x, 0, z);
				if (!block.equals(origin) && CropUtil.IsMatureCrop(block))
					crops.add(block);
			}
		}

		MultiBreak.BreakBlocks(player, origin, crops);
		return true;
	}

	private static int GetRadius(int level)
	{
		return Math.max(1, Math.min(level, 2));
	}
}
