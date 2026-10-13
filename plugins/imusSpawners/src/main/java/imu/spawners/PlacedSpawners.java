package imu.spawners;

import imu.spawners.engine.Spot;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Spawners players have placed. The spawner block itself remembers its owner (imusspawners:owner). The old
 * imusSpawners listed them in spawnerData.yml; those still count until they are broken.
 */
class PlacedSpawners
{
    private static final String NOBODY = "NONE";

    private final ImusSpawners _plugin;
    private final NamespacedKey _ownerKey;
    private final File _legacyFile;
    /** The old list: spot to owner uuid, or NOBODY */
    private final Map<Spot, String> _legacy = new HashMap<>();
    private boolean _legacyChanged;

    PlacedSpawners(ImusSpawners plugin)
    {
        _plugin = plugin;
        _ownerKey = new NamespacedKey(plugin, "owner");
        _legacyFile = new File(plugin.getDataFolder(), "spawnerData.yml");
        loadLegacy();
    }

    private void loadLegacy()
    {
        if (!_legacyFile.exists())
            return;
        YamlConfiguration data = YamlConfiguration.loadConfiguration(_legacyFile);
        for (String key : data.getKeys(false))
        {
            Spot spot = Spot.parse(key);
            if (spot != null)
                _legacy.put(spot, data.getString(key + ".ownerUUID", NOBODY));
        }
        if (!_legacy.isEmpty())
            _plugin.getLogger().info(_legacy.size() + " placed spawners in the old spawnerData.yml");
    }

    private static Spot spot(Block block)
    {
        return new Spot(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    boolean isPlaced(Block block)
    {
        if (block.getState() instanceof CreatureSpawner spawner && spawner.getPersistentDataContainer().has(_ownerKey, PersistentDataType.STRING))
            return true;
        return _legacy.containsKey(spot(block));
    }

    void markPlaced(Block block, UUID owner)
    {
        if (!(block.getState() instanceof CreatureSpawner spawner))
            return;
        spawner.getPersistentDataContainer().set(_ownerKey, PersistentDataType.STRING, owner.toString());
        spawner.update();
    }

    /**
     * Forgets the spawner of the old list once it is gone.
     */
    void broken(Block block)
    {
        if (_legacy.remove(spot(block)) == null)
            return;
        _legacyChanged = true;
        YamlConfiguration data = legacyYaml();
        Bukkit.getScheduler().runTaskAsynchronously(_plugin, () -> write(data));
    }

    void saveLegacyNow()
    {
        if (_legacyChanged)
            write(legacyYaml());
    }

    private YamlConfiguration legacyYaml()
    {
        YamlConfiguration data = new YamlConfiguration();
        for (Map.Entry<Spot, String> entry : _legacy.entrySet())
            data.set(entry.getKey().key() + ".ownerUUID", entry.getValue());
        return data;
    }

    private synchronized void write(YamlConfiguration data)
    {
        try
        {
            if (data.getKeys(false).isEmpty())
                _legacyFile.delete();
            else
                data.save(_legacyFile);
        } catch (IOException e)
        {
            _plugin.getLogger().warning("Couldn't save spawnerData.yml: " + e.getMessage());
        }
    }
}
