package imu.GS.live;

import org.bukkit.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Blocks players placed lately, so breaking them again doesn't count as gathering. Only the newest
 * {@code capacity} are remembered, and nothing survives a restart; that is close enough for a rare chance.
 */
final class PlacedBlocks
{
    private record Position(UUID world, int x, int y, int z) {}

    private final Map<Position, Boolean> _positions;

    PlacedBlocks(int capacity)
    {
        _positions = new LinkedHashMap<>(1024, 0.75f, false)
        {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Position, Boolean> eldest)
            {
                return size() > capacity;
            }
        };
    }

    private static Position of(Block block)
    {
        return new Position(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
    }

    void add(Block block)
    {
        _positions.put(of(block), Boolean.TRUE);
    }

    /**
     * Forgets the block; true when a player had placed it.
     */
    boolean remove(Block block)
    {
        return _positions.remove(of(block)) != null;
    }
}
