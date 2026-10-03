package me.imu.imusminiquests.Managers;

import me.imu.imusminiquests.CONSTANTS;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

/**
 * Remembers blocks that players placed, in the chunk's persistent data, so breaking them again
 * doesn't count as progress or roll a drop. It survives restarts and is gone with the chunk.
 * Only blocks that matter (quest or drop targets) are marked, to keep chunk data small.
 * Blocks moved by pistons or removed by explosions keep or lose their mark at the old spot;
 * that is accepted.
 */
public class ManagerPlacedBlocks
{
    private final Plugin _plugin;

    public ManagerPlacedBlocks(Plugin plugin)
    {
        _plugin = plugin;
    }

    private NamespacedKey keyOf(Block block)
    {
        return new NamespacedKey(_plugin, CONSTANTS.KEY_PLACED_BLOCK_PREFIX
                + (block.getX() & 15) + "_" + block.getY() + "_" + (block.getZ() & 15));
    }

    public void mark(Block block)
    {
        block.getChunk().getPersistentDataContainer().set(keyOf(block), PersistentDataType.BYTE, (byte) 1);
    }

    public void unmark(Block block)
    {
        block.getChunk().getPersistentDataContainer().remove(keyOf(block));
    }

    public boolean isPlacedByPlayer(Block block)
    {
        return block.getChunk().getPersistentDataContainer().has(keyOf(block), PersistentDataType.BYTE);
    }
}
