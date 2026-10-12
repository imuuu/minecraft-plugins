package imu.hometele;

import imu.hometele.engine.HomeBook;
import imu.hometele.engine.Spot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * The homes in plugins/HomeTele/homes.yml:
 * <pre>
 * homes:
 *   &lt;uuid&gt;:
 *     name: Alex
 *     homes:
 *       base: {world: world, x: 10.5, y: 64, z: -3.5, yaw: 90, pitch: 0}
 * </pre>
 * The old HomeTele's one home per player ({@code homes.<uuid>.loc}) is read as a home named "home".
 */
public class HomeStore
{
    private final JavaPlugin _plugin;
    private final File _file;

    public HomeStore(JavaPlugin plugin)
    {
        _plugin = plugin;
        _file = new File(plugin.getDataFolder(), "homes.yml");
    }

    public HomeBook load()
    {
        HomeBook book = new HomeBook();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(_file);
        ConfigurationSection players = yaml.getConfigurationSection("homes");
        if (players == null)
            return book;
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
            ConfigurationSection section = players.getConfigurationSection(id);
            if (section == null)
                continue;
            if (section.contains("name"))
                book.setPlayerName(player, section.getString("name"));

            // The old format
            if (section.contains("loc"))
            {
                try
                {
                    Location loc = section.getLocation("loc");
                    if (loc != null && loc.getWorld() != null)
                        book.load(player, HomeBook.DEFAULT_NAME, spot(loc));
                } catch (IllegalArgumentException e)
                {
                    _plugin.getLogger().warning("Skipped the old home of " + id + ": " + e.getMessage());
                }
            }

            ConfigurationSection homes = section.getConfigurationSection("homes");
            if (homes == null)
                continue;
            for (String name : homes.getKeys(false))
            {
                ConfigurationSection h = homes.getConfigurationSection(name);
                if (h != null && h.contains("world"))
                    book.load(player, name, new Spot(h.getString("world"), h.getDouble("x"), h.getDouble("y"), h.getDouble("z"),
                            (float) h.getDouble("yaw"), (float) h.getDouble("pitch")));
            }
        }
        return book;
    }

    /**
     * Writes every home; the file is written in the background.
     */
    public void save(HomeBook book)
    {
        YamlConfiguration yaml = new YamlConfiguration();
        for (UUID player : book.owners())
        {
            String base = "homes." + player;
            yaml.set(base + ".name", book.playerName(player));
            for (Map.Entry<String, Spot> home : book.homes(player).entrySet())
            {
                String path = base + ".homes." + home.getKey();
                Spot spot = home.getValue();
                yaml.set(path + ".world", spot.world());
                yaml.set(path + ".x", spot.x());
                yaml.set(path + ".y", spot.y());
                yaml.set(path + ".z", spot.z());
                yaml.set(path + ".yaw", (double) spot.yaw());
                yaml.set(path + ".pitch", (double) spot.pitch());
            }
        }
        String text = yaml.saveToString();
        Runnable write = () ->
        {
            try
            {
                _file.getParentFile().mkdirs();
                Files.writeString(_file.toPath(), text, StandardCharsets.UTF_8);
            } catch (IOException e)
            {
                _plugin.getLogger().log(Level.SEVERE, "Couldn't save the homes", e);
            }
        };
        if (_plugin.isEnabled())
            Bukkit.getScheduler().runTaskAsynchronously(_plugin, write);
        else
            write.run();
    }

    public static Spot spot(Location loc)
    {
        return new Spot(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    /**
     * Null when the spot's world isn't loaded.
     */
    public static Location location(Spot spot)
    {
        var world = Bukkit.getWorld(spot.world());
        return world == null ? null : new Location(world, spot.x(), spot.y(), spot.z(), spot.yaw(), spot.pitch());
    }
}
