package imu.spawners;

import imu.spawners.engine.Luck;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Every player's spawner luck, in playerData.yml in the old imusSpawners' format.
 */
class LuckStore
{
    private final ImusSpawners _plugin;
    private final File _file;
    private final Map<UUID, Luck.Stats> _stats = new HashMap<>();
    private final Map<UUID, String> _names = new HashMap<>();

    LuckStore(ImusSpawners plugin)
    {
        _plugin = plugin;
        _file = new File(plugin.getDataFolder(), "playerData.yml");
        load();
    }

    private void load()
    {
        ConfigurationSection players = YamlConfiguration.loadConfiguration(_file).getConfigurationSection("playerData");
        if (players == null)
            return;
        for (String key : players.getKeys(false))
        {
            try
            {
                UUID uuid = UUID.fromString(key);
                ConfigurationSection player = players.getConfigurationSection(key);
                _stats.put(uuid, new Luck.Stats(player.getDouble("currentChanceBonus"), player.getInt("totalMinedSpawners"),
                        player.getInt("totalSpawnerGot")));
                _names.put(uuid, player.getString("name", key));
            } catch (IllegalArgumentException ignored)
            {
            }
        }
    }

    Luck.Stats get(UUID player)
    {
        return _stats.getOrDefault(player, Luck.Stats.NEW);
    }

    void set(UUID player, String name, Luck.Stats stats)
    {
        _stats.put(player, stats);
        _names.put(player, name);
        YamlConfiguration data = yaml();
        Bukkit.getScheduler().runTaskAsynchronously(_plugin, () -> write(data));
    }

    void saveNow()
    {
        write(yaml());
    }

    private YamlConfiguration yaml()
    {
        YamlConfiguration data = new YamlConfiguration();
        for (Map.Entry<UUID, Luck.Stats> entry : _stats.entrySet())
        {
            String path = "playerData." + entry.getKey() + ".";
            data.set(path + "name", _names.get(entry.getKey()));
            data.set(path + "currentChanceBonus", entry.getValue().bonus());
            data.set(path + "totalMinedSpawners", entry.getValue().tries());
            data.set(path + "totalSpawnerGot", entry.getValue().got());
        }
        return data;
    }

    private synchronized void write(YamlConfiguration data)
    {
        try
        {
            data.save(_file);
        } catch (IOException e)
        {
            _plugin.getLogger().warning("Couldn't save playerData.yml: " + e.getMessage());
        }
    }
}
