package me.imu.imusenchants.CustomEnchants;

import me.imu.imusenchants.ImusEnchants;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Blocks an enchant changes for a while (Magma Walker). They turn back by themselves, can't be
// mined, pushed or blown up (CustomEnchantEvents) and are all restored when the plugin stops.
// Each one is also written into its chunk's data together with the original block, so if the
// server dies before they turn back, they are restored the next time the chunk loads.
public class TemporaryBlocks
{
	private static final Map<Location, Material> _original = new HashMap<>();

	public static boolean IsTemporary(Block block)
	{
		return _original.containsKey(block.getLocation());
	}

	public static void Place(Plugin plugin, Block block, Material temporary, long ticks)
	{
		Location location = block.getLocation();
		if (!_original.containsKey(location))
		{
			_original.put(location, block.getType());
			AddToChunk(block, block.getType());
		}

		block.setType(temporary, false);
		ScheduleRevert(plugin, location, ticks);
	}

	private static void ScheduleRevert(Plugin plugin, Location location, long ticks)
	{
		Bukkit.getScheduler().runTaskLater(plugin, () ->
		{
			if (!_original.containsKey(location))
				return;

			// Don't pull the floor out from under someone, try again a bit later
			for (Player player : location.getWorld().getPlayers())
			{
				Location feet = player.getLocation();
				if (feet.getBlockX() == location.getBlockX() && feet.getBlockZ() == location.getBlockZ()
						&& Math.abs(feet.getY() - (location.getBlockY() + 1)) < 1.5)
				{
					ScheduleRevert(plugin, location, 20);
					return;
				}
			}
			Revert(location);
		}, ticks);
	}

	private static void Revert(Location location)
	{
		Material original = _original.remove(location);
		if (original == null || location.getWorld() == null)
			return;

		Block block = location.getBlock();
		block.setType(original, false);
		RemoveFromChunk(block);
	}

	public static void RevertAll()
	{
		for (Location location : new HashMap<>(_original).keySet())
			Revert(location);
	}

	// Restores blocks left over from a crash: ones in the chunk's data that nothing is going to
	// turn back. Called when a chunk loads and, for chunks already loaded, when the plugin starts.
	public static void RestoreLeftovers(Chunk chunk)
	{
		PersistentDataContainer pdc = chunk.getPersistentDataContainer();
		String stored = pdc.get(Key(), PersistentDataType.STRING);
		if (stored == null)
			return;

		List<String> pending = new ArrayList<>();
		for (String entry : stored.split(";"))
		{
			String[] parts = entry.split(",");
			if (parts.length != 4)
				continue;

			Block block = chunk.getWorld().getBlockAt(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
			if (_original.containsKey(block.getLocation()))
			{
				pending.add(entry);
				continue;
			}

			Material original = Material.matchMaterial(parts[3]);
			if (original != null)
				block.setType(original, false);
		}
		Write(pdc, pending);
	}

	public static void RestoreLeftoversInLoadedChunks()
	{
		for (World world : Bukkit.getWorlds())
		{
			for (Chunk chunk : world.getLoadedChunks())
				RestoreLeftovers(chunk);
		}
	}

	private static NamespacedKey Key()
	{
		return new NamespacedKey(ImusEnchants.Instance, "ie_temporary_blocks");
	}

	private static String Entry(Block block, Material original)
	{
		return block.getX() + "," + block.getY() + "," + block.getZ() + "," + original.name();
	}

	private static List<String> Read(PersistentDataContainer pdc)
	{
		List<String> entries = new ArrayList<>();
		String stored = pdc.get(Key(), PersistentDataType.STRING);
		if (stored != null && !stored.isEmpty())
			entries.addAll(List.of(stored.split(";")));
		return entries;
	}

	private static void Write(PersistentDataContainer pdc, List<String> entries)
	{
		if (entries.isEmpty())
			pdc.remove(Key());
		else
			pdc.set(Key(), PersistentDataType.STRING, String.join(";", entries));
	}

	private static void AddToChunk(Block block, Material original)
	{
		PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
		List<String> entries = Read(pdc);
		entries.add(Entry(block, original));
		Write(pdc, entries);
	}

	private static void RemoveFromChunk(Block block)
	{
		PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
		String prefix = block.getX() + "," + block.getY() + "," + block.getZ() + ",";
		List<String> entries = Read(pdc);
		if (entries.removeIf(entry -> entry.startsWith(prefix)))
			Write(pdc, entries);
	}
}
