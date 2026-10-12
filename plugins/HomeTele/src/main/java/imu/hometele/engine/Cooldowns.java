package imu.hometele.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * When each player last did each action ("home", "sethome"). The wait is worked out from the cooldown setting
 * when asked, so an admin changing the setting changes the waits already on.
 */
public class Cooldowns
{
    private record Key(UUID player, String action) {}

    private final Map<Key, Long> _lastUsed = new HashMap<>();

    public void used(UUID player, String action, long nowMillis)
    {
        _lastUsed.put(new Key(player, action), nowMillis);
    }

    /**
     * Milliseconds until the player can do the action again, 0 when they can now.
     */
    public long remaining(UUID player, String action, double cooldownSeconds, long nowMillis)
    {
        Long last = _lastUsed.get(new Key(player, action));
        if (last == null)
            return 0;
        long left = last + (long) (Math.max(0, cooldownSeconds) * 1000) - nowMillis;
        return Math.max(0, left);
    }

    public void clear(UUID player, String action)
    {
        _lastUsed.remove(new Key(player, action));
    }
}
