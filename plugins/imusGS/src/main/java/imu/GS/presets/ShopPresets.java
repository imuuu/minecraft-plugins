package imu.GS.presets;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

/**
 * The shop presets in plugins/imusGS/presets. Those imusGS comes with are written there when missing, so they
 * can be edited; more can be added as new files.
 */
public class ShopPresets
{
    /**
     * The presets imusGS comes with, in the order a village is built.
     */
    public static final List<String> BUNDLED = List.of("general", "builder", "farmer", "blacksmith", "redstone",
            "fisherman", "alchemist", "librarian", "enchanter", "decorator");

    private final JavaPlugin _plugin;
    private final File _folder;
    private final Map<String, ShopPreset> _presets = new LinkedHashMap<>();

    public ShopPresets(JavaPlugin plugin)
    {
        _plugin = plugin;
        _folder = new File(plugin.getDataFolder(), "presets");
        reload();
    }

    public void reload()
    {
        for (String id : BUNDLED)
        {
            if (!new File(_folder, id + ".yml").exists())
                _plugin.saveResource("presets/" + id + ".yml", false);
        }
        _presets.clear();
        List<String> ids = new ArrayList<>(BUNDLED);
        File[] files = _folder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files != null)
        {
            for (File file : files)
            {
                String id = file.getName().substring(0, file.getName().length() - 4).toLowerCase(Locale.ROOT);
                if (!ids.contains(id))
                    ids.add(id);
            }
        }
        for (String id : ids)
        {
            File file = new File(_folder, id + ".yml");
            if (!file.exists())
                continue;
            try
            {
                _presets.put(id, read(id, YamlConfiguration.loadConfiguration(file)));
            } catch (RuntimeException e)
            {
                _plugin.getLogger().log(Level.WARNING, "Skipped the broken shop preset " + file.getName(), e);
            }
        }
    }

    /**
     * Null when there is no such preset.
     */
    public ShopPreset get(String id)
    {
        return id == null ? null : _presets.get(id.toLowerCase(Locale.ROOT));
    }

    public List<ShopPreset> all()
    {
        return List.copyOf(_presets.values());
    }

    public List<String> ids()
    {
        return List.copyOf(_presets.keySet());
    }

    static ShopPreset read(String id, ConfigurationSection yaml)
    {
        List<ShopPreset.StockItem> stock = new ArrayList<>();
        for (Map<?, ?> entry : yaml.getMapList("stock"))
        {
            Object item = entry.get("item");
            if (item == null)
                continue;
            // Custom items of other plugins are found when a shop is made, they may not be loaded yet
            stock.add(new ShopPreset.StockItem(String.valueOf(item), number(entry.get("max"), 64), number(entry.get("fill"), 16),
                    number(entry.get("minutes"), 30)));
        }
        List<Material> gear = new ArrayList<>();
        for (String name : yaml.getStringList("gear"))
        {
            Material material = Material.matchMaterial(name);
            if (material != null && material.isItem())
                gear.add(material);
        }
        String name = yaml.getString("name", id);
        return new ShopPreset(id, name, yaml.getString("display-name", name), yaml.getBoolean("buy-only", true),
                yaml.getString("npc.profession", "none"), yaml.getStringList("npc.names"), yaml.getString("npc.title", ""),
                lower(yaml.getStringList("tags")), lower(yaml.getStringList("found-tags")), yaml.getDouble("quality", 1),
                yaml.getInt("rotation.items-min", 2), yaml.getInt("rotation.items-max", 4), yaml.getDouble("rotation.gear-share", 0),
                yaml.getBoolean("books", false), yaml.getBoolean("custom-books", false), yaml.contains("flash.gear-share") ? yaml.getDouble("flash.gear-share") : null,
                gear, yaml.getBoolean("flash.fun", false), yaml.getBoolean("flash.specials", false), stock);
    }

    private static int number(Object value, int fallback)
    {
        return value instanceof Number number ? number.intValue() : fallback;
    }

    private static List<String> lower(List<String> list)
    {
        return list.stream().map(s -> s.toLowerCase(Locale.ROOT)).toList();
    }
}
