package imu.hometele.engine;

/**
 * A place in a world, without Bukkit: where a home is or where a player stood.
 */
public record Spot(String world, double x, double y, double z, float yaw, float pitch)
{
    /**
     * Distance to the other spot, or infinity in another world.
     */
    public double distance(Spot other)
    {
        if (!world.equals(other.world))
            return Double.POSITIVE_INFINITY;
        double dx = x - other.x, dy = y - other.y, dz = z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
