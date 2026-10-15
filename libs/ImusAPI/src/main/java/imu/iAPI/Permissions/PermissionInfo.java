package imu.iAPI.Permissions;

import org.bukkit.permissions.PermissionDefault;

import java.util.List;
import java.util.Objects;

/**
 * One permission as the permissions menu shows it.
 *
 * @param node        e.g. "imustnt.admin"
 * @param description what it gives, shown in the menu
 * @param level       how much it gives, or null when the plugin doesn't say
 * @param byDefault   who has it without being given it (plugin.yml's {@code default}, op when not set)
 * @param icon        an item id for the menu ("tnt"), or null for the on/off dye
 * @param children    the permissions it gives with it
 */
public record PermissionInfo(String node, String description, PermissionLevel level, PermissionDefault byDefault,
                             String icon, List<String> children)
{
    public PermissionInfo
    {
        Objects.requireNonNull(node, "node");
        description = description == null ? "" : description;
        byDefault = byDefault == null ? PermissionDefault.OP : byDefault;
        children = children == null ? List.of() : List.copyOf(children);
    }

    /**
     * A permission only ops have by default.
     */
    public static PermissionInfo of(String node, String description, PermissionLevel level)
    {
        return new PermissionInfo(node, description, level, PermissionDefault.OP, null, List.of());
    }

    public PermissionInfo withDefault(PermissionDefault byDefault)
    {
        return new PermissionInfo(node, description, level, byDefault, icon, children);
    }

    public PermissionInfo withIcon(String icon)
    {
        return new PermissionInfo(node, description, level, byDefault, icon, children);
    }

    public PermissionInfo withChildren(List<String> children)
    {
        return new PermissionInfo(node, description, level, byDefault, icon, children);
    }

    /**
     * Whether a player has it when nobody gave or took it.
     */
    public boolean hasByDefault(boolean op)
    {
        return byDefault.getValue(op);
    }

    /**
     * "everyone", "ops", "nobody" or "everyone but ops".
     */
    public String defaultText()
    {
        return switch (byDefault)
        {
            case TRUE -> "everyone";
            case FALSE -> "nobody";
            case OP -> "ops";
            case NOT_OP -> "everyone but ops";
        };
    }
}
