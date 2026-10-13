package imu.waystones;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The names floating over waystones. They aren't saved with the world: they are made when the chunk loads
 * and go when it unloads.
 */
final class Holograms implements Listener
{
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final ImusWaystones _plugin;
    private final Waystones _waystones;
    /** The old plugin's armor stand names, imusapi:iw.holo */
    private final NamespacedKey _oldKey = NamespacedKey.fromString("imusapi:iw.holo");
    private final Map<UUID, TextDisplay> _shown = new HashMap<>();

    Holograms(ImusWaystones plugin, Waystones waystones)
    {
        _plugin = plugin;
        _waystones = waystones;
    }

    void show(Waystone ws)
    {
        hide(ws);
        World world = ws.bukkitWorld();
        if (world == null || !world.isChunkLoaded(ws.x >> 4, ws.z >> 4))
            return;
        Location at = new Location(world, ws.x + 0.5, ws.y + 3.2, ws.z + 0.5);
        float range = (float) Math.max(1, _plugin.getConfig().getDouble("name-view-distance", 20)) / 64f;
        TextDisplay display = world.spawn(at, TextDisplay.class, text ->
        {
            text.setPersistent(false);
            text.text(LEGACY.deserialize(ws.name));
            text.setBillboard(Display.Billboard.CENTER);
            text.setViewRange(range);
            text.setShadowed(true);
        });
        _shown.put(ws.id, display);
    }

    void hide(Waystone ws)
    {
        TextDisplay display = _shown.remove(ws.id);
        if (display != null)
            display.remove();
    }

    void showAll()
    {
        for (Waystone ws : _waystones.all())
            show(ws);
    }

    void hideAll()
    {
        _shown.values().forEach(Entity::remove);
        _shown.clear();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e)
    {
        String world = e.getWorld().getName();
        int cx = e.getChunk().getX(), cz = e.getChunk().getZ();
        for (Waystone ws : _waystones.all())
        {
            if (ws.isInChunk(world, cx, cz))
                show(ws);
        }
    }

    /**
     * Removes the old plugin's armor stands as their chunks load.
     */
    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent e)
    {
        for (Entity entity : e.getEntities())
            removeIfOld(entity);
    }

    void removeOldEverywhere()
    {
        for (World world : _plugin.getServer().getWorlds())
            world.getEntitiesByClass(ArmorStand.class).forEach(this::removeIfOld);
    }

    private void removeIfOld(Entity entity)
    {
        if (entity instanceof ArmorStand && entity.getPersistentDataContainer().has(_oldKey, PersistentDataType.STRING))
            entity.remove();
    }
}
