package me.imu.imusenchants.CustomEnchants;

import me.imu.imusenchants.ImusEnchants;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

// Logs placed by players, kept in their chunk's data so Timber can tell builds from trees.
// A stale mark (the log burned or was pushed away) only makes Timber stop earlier, never fell more.
public class PlacedLogs
{
	public static boolean IsLog(Block block)
	{
		return Tag.LOGS.isTagged(block.getType());
	}

	public static boolean IsPlaced(Block block)
	{
		int packed = Pack(block);
		for (int value : Read(block))
		{
			if (value == packed)
				return true;
		}
		return false;
	}

	public static void Mark(Block block)
	{
		if (IsPlaced(block))
			return;

		int[] values = Read(block);
		int[] updated = Arrays.copyOf(values, values.length + 1);
		updated[values.length] = Pack(block);
		block.getChunk().getPersistentDataContainer().set(Key(), PersistentDataType.INTEGER_ARRAY, updated);
	}

	public static void Unmark(Block block)
	{
		int packed = Pack(block);
		int[] values = Read(block);
		int[] updated = Arrays.stream(values).filter(value -> value != packed).toArray();
		if (updated.length == values.length)
			return;

		PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
		if (updated.length == 0)
			pdc.remove(Key());
		else
			pdc.set(Key(), PersistentDataType.INTEGER_ARRAY, updated);
	}

	private static int[] Read(Block block)
	{
		int[] values = block.getChunk().getPersistentDataContainer().get(Key(), PersistentDataType.INTEGER_ARRAY);
		return values == null ? new int[0] : values;
	}

	// Position inside the chunk: y in the upper bits, then x and z (0-15)
	private static int Pack(Block block)
	{
		return ((block.getY() & 0xFFFF) << 8) | ((block.getX() & 15) << 4) | (block.getZ() & 15);
	}

	private static NamespacedKey Key()
	{
		return new NamespacedKey(ImusEnchants.Instance, "ie_placed_logs");
	}
}
