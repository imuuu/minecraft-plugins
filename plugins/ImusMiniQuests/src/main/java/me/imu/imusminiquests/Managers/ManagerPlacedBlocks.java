package me.imu.imusminiquests.Managers;

import me.imu.imusminiquests.CONSTANTS;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

/**
 * Remembers blocks that players placed, in the chunk's persistent data, so breaking them again
 * doesn't count as progress or roll a drop. It survives restarts and is gone with the chunk.
 * <p>
 * Each 16x16x16 section of a chunk gets one bit per block (512 bytes), created when the first
 * tracked block is placed in it and removed when its last one is broken, so the data stays small
 * however much players build. Only blocks that matter (quest or drop targets) are marked.
 * Blocks moved by pistons or removed by explosions keep or lose their mark at the old spot;
 * that is accepted.
 */
public class ManagerPlacedBlocks
{
    private static final int SECTION_BYTES = 16 * 16 * 16 / 8;

    private final Plugin _plugin;

    public ManagerPlacedBlocks(Plugin plugin)
    {
        _plugin = plugin;
    }

    private NamespacedKey sectionKey(Block block)
    {
        // y >> 4 rounds down, so -64..-49 is section -4
        return new NamespacedKey(_plugin, CONSTANTS.KEY_PLACED_BLOCK_PREFIX + "s" + (block.getY() >> 4));
    }

    private static int bitIndex(Block block)
    {
        return ((block.getY() & 15) << 8) | ((block.getZ() & 15) << 4) | (block.getX() & 15);
    }

    public void mark(Block block)
    {
        PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        NamespacedKey key = sectionKey(block);
        byte[] bits = pdc.get(key, PersistentDataType.BYTE_ARRAY);
        if (bits == null || bits.length != SECTION_BYTES) bits = new byte[SECTION_BYTES];

        int index = bitIndex(block);
        bits[index >> 3] |= (byte) (1 << (index & 7));
        pdc.set(key, PersistentDataType.BYTE_ARRAY, bits);
    }

    public void unmark(Block block)
    {
        PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        NamespacedKey key = sectionKey(block);
        byte[] bits = pdc.get(key, PersistentDataType.BYTE_ARRAY);
        if (bits == null || bits.length != SECTION_BYTES) return;

        int index = bitIndex(block);
        bits[index >> 3] &= (byte) ~(1 << (index & 7));

        for (byte b : bits)
        {
            if (b != 0)
            {
                pdc.set(key, PersistentDataType.BYTE_ARRAY, bits);
                return;
            }
        }
        pdc.remove(key);
    }

    public boolean isPlacedByPlayer(Block block)
    {
        byte[] bits = block.getChunk().getPersistentDataContainer().get(sectionKey(block), PersistentDataType.BYTE_ARRAY);
        if (bits == null || bits.length != SECTION_BYTES) return false;

        int index = bitIndex(block);
        return (bits[index >> 3] & (1 << (index & 7))) != 0;
    }
}
