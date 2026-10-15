package imu.iAPI.Permissions;

import imu.iAPI.Utilities.TextUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;

/**
 * A main command for a plugin that has none of its own, only so it has "/&lt;command&gt; perms" like the others:
 * <pre>
 * commands:
 *   imusdifficult:
 *     description: ImusDifficult's permissions menu
 *     usage: /&lt;command&gt; perms
 * </pre>
 * and in onEnable {@code PermissionsCommand.register(this, "imusdifficult");}
 */
public final class PermissionsCommand implements TabExecutor
{
    private final JavaPlugin _plugin;

    private PermissionsCommand(JavaPlugin plugin)
    {
        _plugin = plugin;
    }

    public static void register(JavaPlugin plugin, String command)
    {
        PluginCommand pluginCommand = plugin.getCommand(command);
        if (pluginCommand == null)
        {
            plugin.getLogger().warning("No /" + command + " in plugin.yml, so no permissions command");
            return;
        }
        PermissionsCommand executor = new PermissionsCommand(plugin);
        pluginCommand.setExecutor(executor);
        pluginCommand.setTabCompleter(executor);
    }

    /**
     * "perms" or "permissions".
     */
    public static boolean isPermissions(String arg)
    {
        return arg.equalsIgnoreCase("perms") || arg.equalsIgnoreCase("permissions");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length == 0 || !isPermissions(args[0]))
        {
            sender.sendMessage(TextUtils.Color("&e/" + label + " perms &7- " + _plugin.getName() + "'s permissions"));
            return true;
        }
        if (sender instanceof Player player)
            PaperPermissions.open(player, _plugin.getName());
        else
            sender.sendMessage("Only players can open the permissions menu");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length != 1 || !sender.hasPermission(PaperPermissions.USE_PERMISSION))
            return List.of();
        return "perms".startsWith(args[0].toLowerCase(Locale.ROOT)) ? List.of("perms") : List.of();
    }
}
