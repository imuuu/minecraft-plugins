package imu.iAPI.Config;

import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.ImusTabCompleter;
import imu.iAPI.Other.Metods;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

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

    private final Plugin _plugin;
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
        _plugin = plugin;
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
        return _plugin.getName().toLowerCase(Locale.ROOT);
    }

    // Opening and changing ===========================================================================================

    public boolean canEdit(CommandSender sender)
    {
        return _permission == null || sender.hasPermission(_permission);
    }

    public void open(Player player)
    {
        if (!canEdit(player))
        {
            player.sendMessage(Metods.msgC("&cYou don't have permission to change these settings!"));
            return;
        }
        new ConfigMenuInventory(this).open(player);
    }

    /**
     * Validates, saves and applies a new value. Messages the player with the result.
     *
     * @return true when the value was saved
     */
    public boolean setValue(Player player, ConfigEntry entry, Object value)
    {
        if (!canEdit(player))
        {
            player.sendMessage(Metods.msgC("&cYou don't have permission to change these settings!"));
            return false;
        }

        Object oldValue = entry.get(_plugin.getConfig());
        if (Objects.equals(oldValue, value))
            return false;

        _plugin.getConfig().set(entry.getPath(), value);
        _plugin.saveConfig();
        _plugin.getLogger().info(player.getName() + " changed " + entry.getPath() + ": "
                + entry.format(oldValue) + " -> " + entry.format(value));
        player.sendMessage(Metods.msgC("&9" + entry.getName() + " &7changed &c" + entry.format(oldValue)
                + " &7-> &a" + entry.format(value)));

        try
        {
            if (_onChange != null)
                _onChange.run();
        } catch (Exception e)
        {
            player.sendMessage(Metods.msgC("&cThe value was saved, but applying it failed. See the console."));
            _plugin.getLogger().severe("Applying config change of " + entry.getPath() + " failed");
            e.printStackTrace();
        }
        return true;
    }

    public Plugin getPlugin()
    {
        return _plugin;
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
