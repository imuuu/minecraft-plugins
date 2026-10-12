package imu.hometele.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Every player's homes by name. Names are kept in lower case, so "Base" and "base" are the same home.
 */
public class HomeBook
{
    public static final String DEFAULT_NAME = "home";

    public enum SetResult
    {
        SET, REPLACED, LIMIT_REACHED
    }

    private final Map<UUID, Map<String, Spot>> _homes = new HashMap<>();
    private final Map<UUID, String> _names = new HashMap<>();

    public static String key(String name)
    {
        return name == null || name.isBlank() ? DEFAULT_NAME : name.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Sets the home; a new home only while the player has fewer than {@code maxHomes}. Replacing an existing one
     * is always allowed.
     */
    public SetResult set(UUID player, String name, Spot spot, int maxHomes)
    {
        Map<String, Spot> homes = _homes.computeIfAbsent(player, p -> new LinkedHashMap<>());
        String key = key(name);
        if (homes.containsKey(key))
        {
            homes.put(key, spot);
            return SetResult.REPLACED;
        }
        if (homes.size() >= Math.max(1, maxHomes))
            return SetResult.LIMIT_REACHED;
        homes.put(key, spot);
        return SetResult.SET;
    }

    /**
     * Restores a saved home, without the limit.
     */
    public void load(UUID player, String name, Spot spot)
    {
        _homes.computeIfAbsent(player, p -> new LinkedHashMap<>()).put(key(name), spot);
    }

    /**
     * The home, or null. Without a name: the only home when the player has one, else the one named "home".
     */
    public Spot get(UUID player, String name)
    {
        Map<String, Spot> homes = _homes.getOrDefault(player, Map.of());
        if (name == null || name.isBlank())
        {
            if (homes.size() == 1)
                return homes.values().iterator().next();
            return homes.get(DEFAULT_NAME);
        }
        return homes.get(key(name));
    }

    public boolean delete(UUID player, String name)
    {
        Map<String, Spot> homes = _homes.get(player);
        if (homes == null || homes.remove(key(name)) == null)
            return false;
        if (homes.isEmpty())
            _homes.remove(player);
        return true;
    }

    /**
     * The player's home names, in the order they were set.
     */
    public List<String> names(UUID player)
    {
        return List.copyOf(_homes.getOrDefault(player, Map.of()).keySet());
    }

    public Map<String, Spot> homes(UUID player)
    {
        return Collections.unmodifiableMap(_homes.getOrDefault(player, Map.of()));
    }

    public Set<UUID> owners()
    {
        return Collections.unmodifiableSet(_homes.keySet());
    }

    /**
     * The player's last known name, for lists of everyone's homes.
     */
    public String playerName(UUID player)
    {
        return _names.getOrDefault(player, player.toString());
    }

    public void setPlayerName(UUID player, String name)
    {
        _names.put(player, name);
    }
}
