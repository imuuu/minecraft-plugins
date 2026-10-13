package imu.waystones.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * When each player last left from each waystone. The wait is counted with the cooldown as it is now, so an
 * upgrade shortens a wait already on.
 */
public final class Cooldowns
{
    private record Key(UUID player, UUID waystone)
    {
    }

    private final Map<Key, Long> _lastUsed = new HashMap<>();

    public void used(UUID player, UUID waystone, long nowMillis)
    {
        _lastUsed.put(new Key(player, waystone), nowMillis);
    }

    public long remaining(UUID player, UUID waystone, double cooldownSeconds, long nowMillis)
    {
        Long last = _lastUsed.get(new Key(player, waystone));
        if (last == null)
            return 0;
        return Math.max(0, last + (long) (Math.max(0, cooldownSeconds) * 1000) - nowMillis);
    }

    public void forget(UUID waystone)
    {
        _lastUsed.keySet().removeIf(key -> key.waystone().equals(waystone));
    }
}
