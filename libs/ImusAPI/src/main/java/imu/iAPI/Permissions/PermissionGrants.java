package imu.iAPI.Permissions;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * The permissions given to and taken from each player in the permissions menu. Kept in imusAPI's permissions.yml:
 * <pre>
 * players:
 *   0f6c...:
 *     name: Steve
 *     grants: [imustnt.admin, -hometele.use]   # "-" = taken away
 * </pre>
 */
public final class PermissionGrants
{
    private record Player(String name, Map<String, Boolean> grants) {}

    private final Map<UUID, Player> _players = new LinkedHashMap<>();

    /**
     * True when given, false when taken away, null when the player has what the plugin gives by default.
     */
    public Boolean get(UUID player, String node)
    {
        Player entry = _players.get(player);
        return entry == null ? null : entry.grants.get(key(node));
    }

    /**
     * Every node given (true) or taken (false).
     */
    public Map<String, Boolean> of(UUID player)
    {
        Player entry = _players.get(player);
        return entry == null ? Map.of() : Collections.unmodifiableMap(entry.grants);
    }

    /**
     * Gives (true), takes away (false) or resets (null) a node.
     *
     * @return whether anything changed
     */
    public boolean set(UUID player, String name, String node, Boolean value)
    {
        Player entry = _players.computeIfAbsent(player, id -> new Player(name, new LinkedHashMap<>()));
        if (name != null && !name.equals(entry.name))
            _players.put(player, entry = new Player(name, entry.grants));
        Boolean old = value == null ? entry.grants.remove(key(node)) : entry.grants.put(key(node), value);
        if (entry.grants.isEmpty())
            _players.remove(player);
        return !Objects.equals(old, value);
    }

    /**
     * The player's name when they were last given something, or null.
     */
    public String nameOf(UUID player)
    {
        Player entry = _players.get(player);
        return entry == null ? null : entry.name;
    }

    /**
     * How many players have something given or taken.
     */
    public int size()
    {
        return _players.size();
    }

    // Permission nodes are case-insensitive in Bukkit
    private static String key(String node)
    {
        return node.toLowerCase(Locale.ROOT);
    }

    public void load(ConfigurationSection file)
    {
        _players.clear();
        ConfigurationSection players = file.getConfigurationSection("players");
        if (players == null)
            return;
        for (String id : players.getKeys(false))
        {
            UUID player;
            try
            {
                player = UUID.fromString(id);
            } catch (IllegalArgumentException e)
            {
                continue;
            }
            String name = players.getString(id + ".name");
            for (String grant : players.getStringList(id + ".grants"))
            {
                boolean taken = grant.startsWith("-");
                set(player, name, taken ? grant.substring(1) : grant, !taken);
            }
        }
    }

    public void save(ConfigurationSection file)
    {
        file.set("players", null);
        for (Map.Entry<UUID, Player> entry : _players.entrySet())
        {
            String path = "players." + entry.getKey();
            List<String> grants = new ArrayList<>();
            entry.getValue().grants.forEach((node, given) -> grants.add(given ? node : "-" + node));
            file.set(path + ".name", entry.getValue().name);
            file.set(path + ".grants", grants);
        }
    }
}
