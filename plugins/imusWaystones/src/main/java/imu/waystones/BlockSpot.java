package imu.waystones;

import org.bukkit.block.Block;

/**
 * A block's spot, also in a world that isn't loaded.
 */
record BlockSpot(String world, int x, int y, int z)
{
    static BlockSpot of(Block block)
    {
        return new BlockSpot(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }
}
