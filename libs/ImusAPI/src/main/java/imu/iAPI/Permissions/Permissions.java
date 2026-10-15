package imu.iAPI.Permissions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Every plugin's permissions in one place, for the permissions menu (/ia perms). imusAPI registers the plugins
 * built on it from their plugin.yml on its own; a plugin only calls {@link #register} for permissions its
 * plugin.yml doesn't list.
 */
public final class Permissions
{
    private static final Map<String, PluginPermissions> REGISTERED = new ConcurrentHashMap<>();
    private static Consumer<List<String>> _onChange = names -> {};

    private Permissions() {}

    /**
     * Lists a plugin's permissions in place of what it had.
     */
    public static void register(PluginPermissions permissions)
    {
        REGISTERED.put(key(permissions.plugin()), permissions);
        _onChange.accept(names());
    }

    public static void unregister(String plugin)
    {
        if (REGISTERED.remove(key(plugin)) != null)
            _onChange.accept(names());
    }

    /**
     * The plugin's permissions, or null. Any case.
     */
    public static PluginPermissions get(String plugin)
    {
        return REGISTERED.get(key(plugin));
    }

    /**
     * By plugin name.
     */
    public static List<PluginPermissions> registered()
    {
        List<PluginPermissions> list = new ArrayList<>(REGISTERED.values());
        list.sort(Comparator.comparing(permissions -> key(permissions.plugin())));
        return list;
    }

    public static List<String> names()
    {
        return registered().stream().map(PluginPermissions::plugin).toList();
    }

    /**
     * The permission with this node in any registered plugin, or null.
     */
    public static PermissionInfo find(String node)
    {
        for (PluginPermissions permissions : REGISTERED.values())
        {
            PermissionInfo permission = permissions.get(node);
            if (permission != null)
                return permission;
        }
        return null;
    }

    /**
     * Gets the plugin names after every change, e.g. for tab completion.
     */
    public static void onChange(Consumer<List<String>> onChange)
    {
        _onChange = onChange == null ? names -> {} : onChange;
    }

    /**
     * Forgets every plugin, for tests.
     */
    public static void clear()
    {
        REGISTERED.clear();
        _onChange.accept(List.of());
    }

    private static String key(String plugin)
    {
        return plugin.toLowerCase(Locale.ROOT);
    }
}
