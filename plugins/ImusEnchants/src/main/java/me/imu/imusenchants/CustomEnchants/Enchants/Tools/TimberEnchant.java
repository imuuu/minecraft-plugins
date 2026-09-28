package me.imu.imusenchants.CustomEnchants.Enchants.Tools;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import me.imu.imusenchants.CustomEnchants.MultiBreak;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

// Chopping a log fells the whole tree. Only natural trees: the logs must have natural leaves
// (or nether wart blocks) around them, so log houses and builds are safe.
public class TimberEnchant extends CustomEnchant
{
	private static final int[] MAX_LOGS = {32, 64, 128};
	private static final int MIN_FOLIAGE = 4;

	@Override public String GetKey() { return "timber"; }
	@Override public String GetName() { return "Timber"; }
	@Override public int GetMaxLevel() { return MAX_LOGS.length; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.TOOL; }
	@Override public int GetPriority() { return 5; }

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.SetOf(ItemTarget.AXE);
	}

	@Override
	public String GetDescription(int level)
	{
		return "Fells whole trees, up to " + GetMaxLogs(level) + " logs";
	}

	@Override
	public boolean OnBlockBreak(BlockBreakEvent event, ItemStack tool, int level)
	{
		Player player = event.getPlayer();
		if (MultiBreak.IsBreaking(player))
			return false;

		Block origin = event.getBlock();
		if (!IsLog(origin.getType()))
			return false;

		int maxLogs = GetMaxLogs(level);
		List<Block> logs = new ArrayList<>();
		Set<Block> visited = new HashSet<>();
		int foliage = 0;

		Queue<Block> queue = new ArrayDeque<>();
		queue.add(origin);
		visited.add(origin);

		while (!queue.isEmpty() && logs.size() < maxLogs)
		{
			Block current = queue.poll();
			for (int x = -1; x <= 1; x++)
			{
				for (int y = -1; y <= 1; y++)
				{
					for (int z = -1; z <= 1; z++)
					{
						Block neighbor = current.getRelative(x, y, z);
						if (!visited.add(neighbor))
							continue;

						if (IsNaturalFoliage(neighbor))
						{
							foliage++;
							continue;
						}

						if (!IsLog(neighbor.getType()) || logs.size() >= maxLogs)
							continue;

						logs.add(neighbor);
						queue.add(neighbor);
					}
				}
			}
		}

		if (foliage < MIN_FOLIAGE || logs.isEmpty())
			return false;

		List<Block> breakable = new ArrayList<>();
		for (Block log : logs)
		{
			if (MultiBreak.CanBreakExtra(origin, log, tool))
				breakable.add(log);
		}

		MultiBreak.BreakBlocks(player, breakable);
		return true;
	}

	private static int GetMaxLogs(int level)
	{
		return MAX_LOGS[Math.max(1, Math.min(level, MAX_LOGS.length)) - 1];
	}

	private static boolean IsLog(Material material)
	{
		return Tag.LOGS.isTagged(material) && !material.name().startsWith("STRIPPED_");
	}

	private static boolean IsNaturalFoliage(Block block)
	{
		Material type = block.getType();
		if (type == Material.NETHER_WART_BLOCK || type == Material.WARPED_WART_BLOCK || type == Material.SHROOMLIGHT)
			return true;

		return block.getBlockData() instanceof Leaves && !((Leaves) block.getBlockData()).isPersistent();
	}
}
