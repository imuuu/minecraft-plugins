package imu.iAPI.Permissions;

import org.bukkit.permissions.PermissionDefault;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The permissions of one plugin, in the order it lists them.
 * <p>
 * Usually read from the plugin's plugin.yml, where each permission may say more than Bukkit reads:
 * <pre>
 * permissions:
 *   imustnt.admin:
 *     description: Take and give special TNT, change the settings
 *     default: op
 *     level: high        # low, medium, high or very_high
 *     icon: tnt          # optional, the item shown in the menu
 *   it.inv:
 *     description: Same as imustnt.admin
 *     hidden: true       # an old alias, not shown in the menu
 * </pre>
 */
public record PluginPermissions(String plugin, List<PermissionInfo> permissions)
{
    public PluginPermissions
    {
        permissions = List.copyOf(permissions);
    }

    public static PluginPermissions of(String plugin, PermissionInfo... permissions)
    {
        return new PluginPermissions(plugin, List.of(permissions));
    }

    /**
     * The permissions section of a plugin.yml.
     *
     * @param problems gets a line for each value that couldn't be read; the permission is still listed
     */
    public static PluginPermissions fromYaml(String plugin, String pluginYml, Consumer<String> problems)
    {
        Object root = new Yaml().load(pluginYml);
        Object section = root instanceof Map<?, ?> map ? map.get("permissions") : null;
        return read(plugin, section instanceof Map<?, ?> permissions ? permissions : Map.of(), problems);
    }

    /**
     * The permissions map of a plugin.yml, node to its settings.
     */
    public static PluginPermissions read(String plugin, Map<?, ?> permissions, Consumer<String> problems)
    {
        List<PermissionInfo> list = new ArrayList<>();
        for (Map.Entry<?, ?> entry : permissions.entrySet())
        {
            String node = String.valueOf(entry.getKey());
            Map<?, ?> settings = entry.getValue() instanceof Map<?, ?> map ? map : Map.of();
            if (Boolean.TRUE.equals(settings.get("hidden")))
                continue;

            PermissionLevel level = null;
            try
            {
                level = PermissionLevel.parse(text(settings.get("level")));
            } catch (IllegalArgumentException e)
            {
                problems.accept(plugin + ": " + node + ": " + e.getMessage());
            }

            PermissionDefault byDefault = PermissionDefault.OP;
            String defaultText = text(settings.get("default"));
            if (defaultText != null)
            {
                PermissionDefault parsed = PermissionDefault.getByName(defaultText);
                if (parsed != null)
                    byDefault = parsed;
                else
                    problems.accept(plugin + ": " + node + ": unknown default '" + defaultText + "'");
            }

            list.add(new PermissionInfo(node, text(settings.get("description")), level, byDefault,
                    text(settings.get("icon")), children(settings.get("children"))));
        }
        return new PluginPermissions(plugin, list);
    }

    private static String text(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }

    // children: a map of node to true/false, or a list of nodes
    private static List<String> children(Object value)
    {
        List<String> children = new ArrayList<>();
        if (value instanceof Map<?, ?> map)
        {
            for (Map.Entry<?, ?> child : map.entrySet())
            {
                if (!Boolean.FALSE.equals(child.getValue()))
                    children.add(String.valueOf(child.getKey()));
            }
        } else if (value instanceof List<?> nodes)
        {
            for (Object node : nodes)
                children.add(String.valueOf(node));
        }
        return children;
    }

    /**
     * The permission with this node, or null.
     */
    public PermissionInfo get(String node)
    {
        for (PermissionInfo permission : permissions)
        {
            if (permission.node().equalsIgnoreCase(node))
                return permission;
        }
        return null;
    }

    public boolean isEmpty()
    {
        return permissions.isEmpty();
    }

    /**
     * How many permissions there are of a level; null counts those without one.
     */
    public int count(PermissionLevel level)
    {
        return (int) permissions.stream().filter(permission -> permission.level() == level).count();
    }
}
