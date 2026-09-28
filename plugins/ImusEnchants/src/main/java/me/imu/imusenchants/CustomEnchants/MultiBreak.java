package me.imu.imusenchants.CustomEnchants;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// Breaks extra blocks for mining enchants. Blocks are broken with Player#breakBlock, so it
// behaves like the player broke them: protection plugins can cancel it, drops use
// Fortune/Silk Touch, the tool takes durability (Unbreaking works) and XP drops.
public class MultiBreak
{
	// Blocks up to this hardness can always be broken, harder ones only if the mined block is as hard.
	// Keeps a stone tunnel from eating obsidian but lets it take deepslate and ores.
	private static final float FREE_HARDNESS = 5.0f;

	// Tool is left with at least this much durability so the original break doesn't destroy it
	private static final int DURABILITY_MARGIN = 2;

	private static final Set<UUID> _breaking = new HashSet<>();

	// True while the player's extra blocks are being broken. BlockBreakEvents fired by those
	// breaks must not trigger mining enchants again.
	public static boolean IsBreaking(Player player)
	{
		return _breaking.contains(player.getUniqueId());
	}

	public static boolean CanBreakExtra(Block origin, Block extra, ItemStack tool)
	{
		if (extra == null || extra.equals(origin))
			return false;

		Material type = extra.getType();
		if (type.isAir() || !type.isSolid())
			return false;

		float hardness = type.getHardness();
		if (hardness < 0)
			return false;

		if (hardness > FREE_HARDNESS && hardness > origin.getType().getHardness())
			return false;

		if (!extra.isPreferredTool(tool))
			return false;

		// Spawners, chests, furnaces...
		return !(extra.getState() instanceof TileState);
	}

	// Returns how many blocks were broken
	public static int BreakBlocks(Player player, List<Block> blocks)
	{
		int broken = 0;
		_breaking.add(player.getUniqueId());
		try
		{
			for (Block block : blocks)
			{
				if (!HasDurabilityLeft(player.getInventory().getItemInMainHand()))
					break;

				if (player.breakBlock(block))
					broken++;
			}
		}
		finally
		{
			_breaking.remove(player.getUniqueId());
		}
		return broken;
	}

	private static boolean HasDurabilityLeft(ItemStack tool)
	{
		if (tool == null || tool.getType().isAir())
			return false;

		int maxDurability = tool.getType().getMaxDurability();
		if (maxDurability <= 0)
			return true;

		ItemMeta meta = tool.getItemMeta();
		if (meta == null || meta.isUnbreakable() || !(meta instanceof Damageable))
			return true;

		return maxDurability - ((Damageable) meta).getDamage() > DURABILITY_MARGIN;
	}
}
