package imu.spawners.engine;

/**
 * A block's spot.
 */
public record Spot(String world, int x, int y, int z)
{
    /**
     * The old imusSpawners' key, "world;x;y;z", or null when it isn't one.
     */
    public static Spot parse(String key)
    {
        if (key == null)
            return null;
        String[] parts = key.split(";");
        if (parts.length != 4 || parts[0].isEmpty())
            return null;
        try
        {
            return new Spot(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        } catch (NumberFormatException e)
        {
            return null;
        }
    }

    public String key()
    {
        return world + ";" + x + ";" + y + ";" + z;
    }
}
