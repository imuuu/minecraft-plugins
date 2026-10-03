package me.imu.imusenchants.CustomEnchants.Enchants.Tools;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.MultiBreak;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

// Sneak + mine an ore: connected ores of the same kind are mined too (deepslate variants count).
public class VeinMinerEnchant extends CustomEnchant
{
	// Total blocks per level, including the mined one
	private static final int[] MAX_BLOCKS = {8, 16, 32};

	private static final Map<Material, Set<Material>> ORE_FAMILIES = new HashMap<>();

	static
	{
		AddFamily(Tag.COAL_ORES.getValues());
		AddFamily(Tag.IRON_ORES.getValues());
		AddFamily(Tag.COPPER_ORES.getValues());
		AddFamily(Tag.GOLD_ORES.getValues());
		AddFamily(Tag.REDSTONE_ORES.getValues());
		AddFamily(Tag.LAPIS_ORES.getValues());
		AddFamily(Tag.DIAMOND_ORES.getValues());
		AddFamily(Tag.EMERALD_ORES.getValues());
		AddFamily(Collections.singleton(Material.NETHER_QUARTZ_ORE));
		AddFamily(Collections.singleton(Material.ANCIENT_DEBRIS));
	}

	private static void AddFamily(Set<Material> materials)
	{
		Set<Material> family = EnumSet.copyOf(materials);
		for (Material material : family)
			ORE_FAMILIES.put(material, family);
	}

	@Override
	public String GetKey()
	{
		return "vein_miner";
	}

	@Override
	public String GetName()
	{
		return "Vein Miner";
	}

	@Override
	public int GetMaxLevel()
	{
		return MAX_BLOCKS.length;
	}

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.SetOf(ItemTarget.PICKAXE);
	}

	@Override
	public String GetDescription(int level)
	{
		return "Sneak while mining an ore to mine up to " + GetMaxBlocks(level) + " connected ores";
	}

	@Override
	public ITEM_CATEGORY GetShopCategory()
	{
		return ITEM_CATEGORY.TOOL;
	}

	// Goes before Tunnel: sneaking on an ore mines the vein, everything else is left to Tunnel
	@Override
	public int GetPriority()
	{
		return 10;
	}

	@Override
	public boolean OnBlockBreak(BlockBreakEvent event, ItemStack tool, int level)
	{
		Player player = event.getPlayer();
		if (!player.isSneaking() || MultiBreak.IsBreaking(player))
			return false;

		Block origin = event.getBlock();
		Set<Material> family = ORE_FAMILIES.get(origin.getType());
		if (family == null)
			return false;

		if (!origin.isPreferredTool(tool))
			return false;

		MultiBreak.BreakBlocks(player, origin, FindVein(origin, family, GetMaxBlocks(level) - 1));
		return true;
	}

	private static List<Block> FindVein(Block origin, Set<Material> family, int max)
	{
		List<Block> vein = new ArrayList<>();
		Set<Block> visited = new HashSet<>(Arrays.asList(origin));
		Queue<Block> queue = new ArrayDeque<>();
		queue.add(origin);

		while (!queue.isEmpty() && vein.size() < max)
		{
			Block current = queue.poll();
			for (int x = -1; x <= 1 && vein.size() < max; x++)
			{
				for (int y = -1; y <= 1 && vein.size() < max; y++)
				{
					for (int z = -1; z <= 1 && vein.size() < max; z++)
					{
						Block neighbor = current.getRelative(x, y, z);
						if (!visited.add(neighbor) || !family.contains(neighbor.getType()))
							continue;

						vein.add(neighbor);
						queue.add(neighbor);
					}
				}
			}
		}
		return vein;
	}

	private int GetMaxBlocks(int level)
	{
		int index = Math.max(1, Math.min(level, MAX_BLOCKS.length)) - 1;
		return MAX_BLOCKS[index];
	}
}
