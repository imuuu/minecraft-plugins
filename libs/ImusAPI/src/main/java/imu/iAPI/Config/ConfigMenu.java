package imu.iAPI.Config;

import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Menus.Paper.PaperMenus;
import imu.iAPI.Menus.Paper.PaperViewer;
import imu.iAPI.Menus.Viewer;
import imu.iAPI.Other.ImusTabCompleter;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An in-game editor for a plugin's config.yml. The plugin lists the values that may be edited; ImusAPI shows
 * them in an inventory, asks for new values in a dialog, saves config.yml and calls the plugin's reload hook.
 *
 * <pre>
 * ConfigMenu menu = new ConfigMenu(plugin, "Challenge Shop", "ic.config", this::reload);
 * menu.addInt("shop.reminder-interval-minutes", "Reminder interval", Material.CLOCK, 0, 1440).note("now");
 * menu.register(); // listed in /ia config
 * </pre>
 */
public class ConfigMenu
{
    private static final Map<String, ConfigMenu> _registered = new LinkedHashMap<>();
    private static final String NO_PERMISSION = "&cYou don't have permission to change these settings!";

    /**
     * Where the values live: a plugin's config.yml, or a plain configuration in tests.
     */
    public interface Store
    {
        String pluginName();

        FileConfiguration config();

        void save();

        Logger logger();

        static Store of(Plugin plugin)
        {
            return new Store()
            {
                public String pluginName() { return plugin.getName(); }

                public FileConfiguration config() { return plugin.getConfig(); }

                public void save() { plugin.saveConfig(); }

                public Logger logger() { return plugin.getLogger(); }
            };
        }
    }

    private final Store _store;
    private final String _title;
    private final String _permission;
    private final Runnable _onChange;
    private final List<ConfigEntry> _entries = new ArrayList<>();

    /**
     * @param permission needed to open the menu and change values
     * @param onChange   runs on the main thread after a value is saved; the plugin re-reads its config here
     */
    public ConfigMenu(Plugin plugin, String title, String permission, Runnable onChange)
    {
        this(Store.of(plugin), title, permission, onChange);
    }

    public ConfigMenu(Store store, String title, String permission, Runnable onChange)
    {
        _store = store;
        _title = title;
        _permission = permission;
        _onChange = onChange;
    }

    // Entries ========================================================================================================

    public ConfigEntry addInt(String path, String name, Material icon, int min, int max)
    {
        return add(new ConfigEntry(path, name, icon, ConfigEntry.Type.INT).range(min, max));
    }

    public ConfigEntry addDouble(String path, String name, Material icon, double min, double max)
    {
        return add(new ConfigEntry(path, name, icon, ConfigEntry.Type.DOUBLE).range(min, max));
    }

    public ConfigEntry addBoolean(String path, String name, Material icon)
    {
        return add(new ConfigEntry(path, name, icon, ConfigEntry.Type.BOOLEAN));
    }

    public ConfigEntry addString(String path, String name, Material icon)
    {
        return add(new ConfigEntry(path, name, icon, ConfigEntry.Type.STRING));
    }

    /**
     * A value picked from a fixed list; clicking it in the menu moves to the next option.
     */
    public ConfigEntry addChoice(String path, String name, Material icon, List<String> options)
    {
        return add(new ConfigEntry(path, name, icon, ConfigEntry.Type.CHOICE).options(options));
    }

    private ConfigEntry add(ConfigEntry entry)
    {
        _entries.add(entry);
        return entry;
    }

    // Registry =======================================================================================================

    /**
     * Lists this menu in /ia config. Call {@link #unregister()} in onDisable.
     */
    public void register()
    {
        _registered.put(getId(), this);
        updateTabCompletion();
    }

    public void unregister()
    {
        _registered.remove(getId(), this);
        updateTabCompletion();
    }

    private static void updateTabCompletion()
    {
        ImusTabCompleter tab = ImusAPI._instance == null ? null : ImusAPI._instance.GetCMD1_TabCompleter();
        if (tab != null)
            tab.SetRule("/ia config", 2, new ArrayList<>(_registered.keySet()));
    }

    public static Collection<ConfigMenu> getRegistered()
    {
        return Collections.unmodifiableCollection(_registered.values());
    }

    public static ConfigMenu getRegistered(String id)
    {
        return _registered.get(id.toLowerCase(Locale.ROOT));
    }

    public String getId()
    {
        return _store.pluginName().toLowerCase(Locale.ROOT);
    }

    // Opening and changing ===========================================================================================

    public boolean canEdit(CommandSender sender)
    {
        return canEdit(PaperViewer.of(sender));
    }

    public boolean canEdit(Viewer viewer)
    {
        return _permission == null || viewer.hasPermission(_permission);
    }

    public void open(Player player)
    {
        if (!canEdit(player))
        {
            PaperViewer.of(player).message(NO_PERMISSION);
            return;
        }
        PaperMenus.open(player, new ConfigEntriesMenu(this));
    }

    public boolean setValue(CommandSender sender, ConfigEntry entry, Object value)
    {
        return setValue(PaperViewer.of(sender), entry, value);
    }

    /**
     * Validates, saves and applies a new value. Messages the player (or the console) with the result.
     *
     * @return true when the value was saved
     */
    public boolean setValue(Viewer viewer, ConfigEntry entry, Object value)
    {
        if (!canEdit(viewer))
        {
            viewer.message(NO_PERMISSION);
            return false;
        }

        FileConfiguration config = _store.config();
        Object oldValue = entry.get(config);
        if (Objects.equals(oldValue, value))
            return false;

        config.set(entry.getPath(), value);
        _store.save();
        _store.logger().info(viewer.name() + " changed " + entry.getPath() + ": "
                + entry.format(oldValue) + " -> " + entry.format(value));
        viewer.message("&9" + entry.getName() + " &7changed &c" + entry.format(oldValue)
                + " &7-> &a" + entry.format(value));

        try
        {
            if (_onChange != null)
                _onChange.run();
        } catch (Exception e)
        {
            viewer.message("&cThe value was saved, but applying it failed. See the console.");
            _store.logger().log(Level.SEVERE, "Applying config change of " + entry.getPath() + " failed", e);
        }
        return true;
    }

    /**
     * The entry of this config path, or null.
     */
    public ConfigEntry getEntry(String path)
    {
        for (ConfigEntry entry : getEntries())
        {
            if (entry.getPath().equalsIgnoreCase(path))
                return entry;
        }
        return null;
    }

    public String getPluginName()
    {
        return _store.pluginName();
    }

    public FileConfiguration getConfig()
    {
        return _store.config();
    }

    public String getTitle()
    {
        return _title;
    }

    public List<ConfigEntry> getEntries()
    {
        return Collections.unmodifiableList(_entries);
    }
}
