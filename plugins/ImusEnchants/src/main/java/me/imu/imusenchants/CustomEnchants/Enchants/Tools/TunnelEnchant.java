package me.imu.imusenchants.CustomEnchants.Enchants.Tools;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.MultiBreak;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// Mines extra blocks around the mined one, on the plane facing the player.
// I: 1x2 (player sized tunnel), II: 2x2, III: 3x3
public class TunnelEnchant extends CustomEnchant
{
	@Override
	public String GetKey()
	{
		return "tunnel";
	}

	@Override
	public String GetName()
	{
		return "Tunnel";
	}

	@Override
	public int GetMaxLevel()
	{
		return 3;
	}

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.SetOf(ItemTarget.PICKAXE, ItemTarget.SHOVEL);
	}

	@Override
	public String GetDescription(int level)
	{
		switch (Math.min(level, GetMaxLevel()))
		{
			case 1: return "Mines a 1x2 tunnel";
			case 2: return "Mines a 2x2 area";
			default: return "Mines a 3x3 area";
		}
	}

	@Override
	public ITEM_CATEGORY GetShopCategory()
	{
		return ITEM_CATEGORY.TOOL;
	}

	@Override
	public boolean OnBlockBreak(BlockBreakEvent event, ItemStack tool, int level)
	{
		Player player = event.getPlayer();
		if (MultiBreak.IsBreaking(player))
			return false;

		Block origin = event.getBlock();

		List<Block> blocks = new ArrayList<>();
		for (Block block : GetArea(player, origin, Math.min(level, GetMaxLevel())))
		{
			if (MultiBreak.CanBreakExtra(origin, block, tool))
				blocks.add(block);
		}

		MultiBreak.BreakBlocks(player, blocks);
		return true;
	}

	private List<Block> GetArea(Player player, Block origin, int level)
	{
		List<Block> area = new ArrayList<>();
		BlockFace face = GetHitFace(player, origin);

		if (face == BlockFace.UP || face == BlockFace.DOWN)
		{
			// Mining the floor or ceiling
			BlockFace depth = face.getOppositeFace();
			BlockFace forward = player.getFacing();
			BlockFace right = RightOf(forward);

			switch (level)
			{
				case 1:
					area.add(origin.getRelative(depth));
					break;
				case 2:
					area.add(Offset(origin, right, 1, forward, 0));
					area.add(Offset(origin, right, 0, forward, 1));
					area.add(Offset(origin, right, 1, forward, 1));
					break;
				default:
					AddSquare(area, origin, right, forward);
					break;
			}
			return area;
		}

		// Mining a wall
		BlockFace right = RightOf(face.getOppositeFace());
		BlockFace vertical = origin.getY() >= player.getEyeLocation().getBlockY() ? BlockFace.DOWN : BlockFace.UP;

		switch (level)
		{
			case 1:
				area.add(origin.getRelative(vertical));
				break;
			case 2:
				area.add(origin.getRelative(vertical));
				area.add(Offset(origin, right, 1, vertical, 0));
				area.add(Offset(origin, right, 1, vertical, 1));
				break;
			default:
				AddSquare(area, origin, right, BlockFace.UP);
				break;
		}
		return area;
	}

	private static void AddSquare(List<Block> area, Block origin, BlockFace axisA, BlockFace axisB)
	{
		for (int a = -1; a <= 1; a++)
		{
			for (int b = -1; b <= 1; b++)
			{
				if (a == 0 && b == 0)
					continue;

				area.add(Offset(origin, axisA, a, axisB, b));
			}
		}
	}

	private static Block Offset(Block origin, BlockFace axisA, int a, BlockFace axisB, int b)
	{
		return origin.getRelative(
				axisA.getModX() * a + axisB.getModX() * b,
				axisA.getModY() * a + axisB.getModY() * b,
				axisA.getModZ() * a + axisB.getModZ() * b);
	}

	private static BlockFace GetHitFace(Player player, Block origin)
	{
		RayTraceResult result = player.rayTraceBlocks(8.0, FluidCollisionMode.NEVER);
		if (result != null && origin.equals(result.getHitBlock()) && result.getHitBlockFace() != null)
			return result.getHitBlockFace();

		float pitch = player.getLocation().getPitch();
		if (pitch > 60)
			return BlockFace.UP;
		if (pitch < -60)
			return BlockFace.DOWN;

		return player.getFacing().getOppositeFace();
	}

	private static BlockFace RightOf(BlockFace facing)
	{
		switch (facing)
		{
			case NORTH: return BlockFace.EAST;
			case EAST: return BlockFace.SOUTH;
			case SOUTH: return BlockFace.WEST;
			default: return BlockFace.NORTH;
		}
	}
}
