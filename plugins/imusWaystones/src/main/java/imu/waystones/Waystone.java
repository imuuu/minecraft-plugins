package imu.waystones;

import imu.waystones.engine.Dimension;
import imu.waystones.engine.Panel;
import imu.waystones.engine.Stone;
import imu.waystones.engine.Visibility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * A waystone: a base block, a wall on it and a slab on top. x, y, z is the base block.
 */
final class Waystone
{
    final UUID id;
    final String world;
    final int x, y, z;
    UUID owner;
    String ownerName;
    /** With &amp; colour codes */
    String name;
    ItemStack icon;
    Visibility visibility = Visibility.BY_TOUCH;
    boolean enabled = true;
    boolean unbreakable;
    /** No cooldown, cast time or XP cost, set by an admin */
    boolean maxedOut;
    /** Each player's upgrades on this waystone */
    final Map<UUID, Panel> panels = new HashMap<>();

    Waystone(UUID id, String world, int x, int y, int z)
    {
        this.id = id;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    Panel panel(UUID player)
    {
        return panels.computeIfAbsent(player, p -> new Panel());
    }

    /**
     * The world, or null while it isn't loaded.
     */
    World bukkitWorld()
    {
        return Bukkit.getWorld(world);
    }

    Block base()
    {
        World w = bukkitWorld();
        return w == null ? null : w.getBlockAt(x, y, z);
    }

    /**
     * The middle of the waystone's base block.
     */
    Location center()
    {
        World w = bukkitWorld();
        return w == null ? null : new Location(w, x + 0.5, y + 0.5, z + 0.5);
    }

    /**
     * The base, the wall and the slab.
     */
    List<BlockSpot> spots()
    {
        return List.of(new BlockSpot(world, x, y, z), new BlockSpot(world, x, y + 1, z), new BlockSpot(world, x, y + 2, z));
    }

    boolean isInChunk(String chunkWorld, int chunkX, int chunkZ)
    {
        return world.equals(chunkWorld) && (x >> 4) == chunkX && (z >> 4) == chunkZ;
    }

    Dimension dimension()
    {
        World w = bukkitWorld();
        if (w != null)
        {
            return switch (w.getEnvironment())
            {
                case NETHER -> Dimension.NETHER;
                case THE_END -> Dimension.END;
                default -> Dimension.NORMAL;
            };
        }
        // An unloaded world: guess by the usual names
        String lower = world.toLowerCase(Locale.ROOT);
        return lower.endsWith("_nether") ? Dimension.NETHER : lower.endsWith("_the_end") ? Dimension.END : Dimension.NORMAL;
    }

    String plainName()
    {
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name));
    }

    Stone stone()
    {
        return new Stone(id, owner, plainName(), world, dimension(), x, y, z, visibility, enabled);
    }
}
