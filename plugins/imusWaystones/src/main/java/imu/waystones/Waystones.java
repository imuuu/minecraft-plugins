package imu.waystones;

import imu.waystones.engine.Cooldowns;
import imu.waystones.engine.Stone;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * All waystones, who has found which, and the cooldowns.
 */
final class Waystones
{
    private final WaystoneDb _db;
    private final Map<UUID, Waystone> _byId = new LinkedHashMap<>();
    private final Map<BlockSpot, Waystone> _byBlock = new HashMap<>();
    private final Map<UUID, Set<UUID>> _discovered = new HashMap<>();
    private final Cooldowns _cooldowns = new Cooldowns();
    private Holograms _holograms;
    private boolean _loaded;

    Waystones(WaystoneDb db)
    {
        _db = db;
    }

    void setHolograms(Holograms holograms)
    {
        _holograms = holograms;
    }

    void load(WaystoneDb.Loaded loaded)
    {
        _byId.values().forEach(ws -> _holograms.hide(ws));
        _byId.clear();
        _byBlock.clear();
        _discovered.clear();
        for (Waystone ws : loaded.waystones())
            index(ws);
        loaded.discovered().forEach((player, ids) -> _discovered.put(player, new HashSet<>(ids)));
        _loaded = true;
    }

    boolean isLoaded()
    {
        return _loaded;
    }

    private void index(Waystone ws)
    {
        _byId.put(ws.id, ws);
        for (BlockSpot spot : ws.spots())
            _byBlock.put(spot, ws);
    }

    Waystone get(UUID id)
    {
        return _byId.get(id);
    }

    /**
     * The waystone the block is part of, or null.
     */
    Waystone at(Block block)
    {
        return block == null ? null : _byBlock.get(BlockSpot.of(block));
    }

    Collection<Waystone> all()
    {
        return _byId.values();
    }

    List<Stone> stones()
    {
        List<Stone> stones = new ArrayList<>();
        for (Waystone ws : _byId.values())
            stones.add(ws.stone());
        return stones;
    }

    Cooldowns cooldowns()
    {
        return _cooldowns;
    }

    void add(Waystone ws)
    {
        index(ws);
        _db.save(ws);
        discover(ws.owner, ws);
        _holograms.show(ws);
    }

    void remove(Waystone ws)
    {
        if (_byId.remove(ws.id) == null)
            return;
        for (BlockSpot spot : ws.spots())
            _byBlock.remove(spot);
        _discovered.values().forEach(ids -> ids.remove(ws.id));
        _cooldowns.forget(ws.id);
        _holograms.hide(ws);
        _db.delete(ws.id);
    }

    /**
     * Saves the waystone's settings and name, and shows the name.
     */
    void save(Waystone ws)
    {
        _db.save(ws);
        _holograms.show(ws);
    }

    void savePanel(Waystone ws, UUID player)
    {
        _db.savePanel(ws, player);
    }

    Set<UUID> discovered(UUID player)
    {
        return _discovered.getOrDefault(player, Set.of());
    }

    /**
     * Marks the waystone found by the player; true when it is new to them.
     */
    boolean discover(UUID player, Waystone ws)
    {
        if (!_discovered.computeIfAbsent(player, p -> new HashSet<>()).add(ws.id))
            return false;
        _db.discover(player, ws.id);
        return true;
    }
}
