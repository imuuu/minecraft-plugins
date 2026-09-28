package me.imu.imusenchants.CustomEnchants;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

// Blocks an enchant changes for a while (Magma Walker). They turn back by themselves, can't be
// mined, pushed or blown up (CustomEnchantEvents) and are all restored when the plugin stops.
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
			_original.put(location, block.getType());

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
		if (original != null && location.getWorld() != null)
			location.getBlock().setType(original, false);
	}

	public static void RevertAll()
	{
		for (Location location : new HashMap<>(_original).keySet())
			Revert(location);
	}
}
