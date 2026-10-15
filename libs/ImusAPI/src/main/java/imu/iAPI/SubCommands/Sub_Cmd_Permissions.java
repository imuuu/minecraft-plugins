package imu.iAPI.SubCommands;

import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Menus.Paper.PaperMenus;
import imu.iAPI.Permissions.PaperPermissions;
import imu.iAPI.Permissions.PermissionPluginsMenu;
import imu.iAPI.Permissions.PermissionTarget;
import imu.iAPI.Permissions.PlayerPermissionsMenu;
import imu.iAPI.Permissions.PluginPermissions;
import imu.iAPI.Utilities.TextUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /ia perms [plugin] [player]: the permissions menu. Without a plugin every plugin with permissions, without a
 * player the players close by. /ia perms reload reads permissions.yml again after editing it by hand (console too).
 */
public class Sub_Cmd_Permissions implements CommandInterface
{
	private final PaperPermissions _permissions;

	public Sub_Cmd_Permissions(PaperPermissions permissions)
	{
		_permissions = permissions;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if (args.length == 2 && args[1].equalsIgnoreCase("reload") && _permissions.permissionsOf("reload") == null)
		{
			int players = _permissions.reload();
			sender.sendMessage(TextUtils.Color("&apermissions.yml read again: &e" + players + " &aplayers with permissions given or taken"));
			return true;
		}
		if (!(sender instanceof Player player))
		{
			sender.sendMessage("Only players can open the permissions menu. The console can use /ia perms reload");
			return true;
		}
		if (args.length < 2)
		{
			PaperMenus.open(player, new PermissionPluginsMenu(_permissions));
			return true;
		}
		if (args.length < 3)
		{
			PaperPermissions.open(player, args[1]);
			return true;
		}

		PluginPermissions plugin = _permissions.permissionsOf(args[1]);
		if (plugin == null || plugin.isEmpty())
		{
			sender.sendMessage(TextUtils.Color("&c" + args[1] + " has no permissions to change"));
			return true;
		}
		PermissionTarget target = _permissions.find(args[2]);
		if (target == null)
		{
			sender.sendMessage(TextUtils.Color("&cNobody called " + args[2] + " has played here"));
			return true;
		}
		PaperMenus.open(player, new PlayerPermissionsMenu(_permissions, plugin, target));
		return true;
	}

	@Override
	public void FailedMsg(CommandSender arg0, String arg1)
	{
	}
}
