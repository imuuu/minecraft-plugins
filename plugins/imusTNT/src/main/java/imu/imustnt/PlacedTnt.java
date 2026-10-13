package imu.imustnt;

import imu.imustnt.engine.TntType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

/**
 * Placed special TNT blocks, kept in their chunk's persistent data so they survive restarts. The key is
 * imustnt:tnt_x_y_z, as the old imusTNT wrote it. A stored type only counts while the block is still TNT, so
 * an old entry can't turn a later vanilla TNT into a special one.
 */
class PlacedTnt
{
    private final Plugin _plugin;

    PlacedTnt(Plugin plugin)
    {
        _plugin = plugin;
    }

    private NamespacedKey key(Block block)
    {
        return new NamespacedKey(_plugin, "tnt_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
    }

    TntType get(Block block)
    {
        if (block.getType() != Material.TNT)
            return null;
        return TntType.parse(block.getChunk().getPersistentDataContainer().get(key(block), PersistentDataType.STRING));
    }

    void set(Block block, TntType type)
    {
        block.getChunk().getPersistentDataContainer().set(key(block), PersistentDataType.STRING, type.name());
    }

    void remove(Block block)
    {
        block.getChunk().getPersistentDataContainer().remove(key(block));
    }
}
