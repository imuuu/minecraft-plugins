package imu.waystones.engine;

import java.util.UUID;

/**
 * What the rules need to know about a waystone.
 *
 * @param name the name without colour codes, for sorting
 */
public record Stone(UUID id, UUID owner, String name, String world, Dimension dimension, int x, int y, int z,
                    Visibility visibility, boolean enabled)
{
    public double distance(String otherWorld, int ox, int oy, int oz)
    {
        if (!world.equals(otherWorld))
            return Double.MAX_VALUE;
        double dx = x - ox, dy = y - oy, dz = z - oz;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
