package imu.iAPI.Utilities;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

/**
 * Things placed in the world.
 */
public final class WorldUtils
{
	private WorldUtils() {}

	public static ArmorStand CreateHologram(String str, Location loc)
	{
		ArmorStand hologram = (ArmorStand)loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		hologram.setVisible(false);
		hologram.setCustomNameVisible(true);
		hologram.setCustomName(TextUtils.Color(str));
		hologram.setGravity(false);
		return hologram;
	}
}
