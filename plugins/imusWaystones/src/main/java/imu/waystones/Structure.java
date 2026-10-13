package imu.waystones;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * What a waystone is built of: a base block from the list, any wall on it and any slab on top.
 */
final class Structure
{
    private final Set<Material> _bases = EnumSet.noneOf(Material.class);

    Structure(Collection<String> baseBlocks, java.util.logging.Logger log)
    {
        for (String name : baseBlocks)
        {
            Material material = Material.matchMaterial(name);
            if (material == null || !material.isBlock())
                log.warning("structure.base-blocks: no block " + name);
            else
                _bases.add(material);
        }
    }

    boolean isWhole(Block base)
    {
        return base != null && _bases.contains(base.getType()) && Tag.WALLS.isTagged(base.getRelative(BlockFace.UP).getType())
                && Tag.SLABS.isTagged(base.getRelative(BlockFace.UP, 2).getType());
    }

    boolean isWhole(Waystone waystone)
    {
        return isWhole(waystone.base());
    }

    /**
     * The base block of the waystone the placed block finishes, or null.
     */
    Block finishedBy(Block placed)
    {
        Material type = placed.getType();
        Block base;
        if (Tag.SLABS.isTagged(type))
            base = placed.getRelative(BlockFace.DOWN, 2);
        else if (Tag.WALLS.isTagged(type))
            base = placed.getRelative(BlockFace.DOWN);
        else if (_bases.contains(type))
            base = placed;
        else
            return null;
        return isWhole(base) ? base : null;
    }
}
