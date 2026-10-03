package imu.iAPI.SubCommands;

import imu.iAPI.CmdUtil.CmdData;
import imu.iAPI.Config.ConfigMenu;
import imu.iAPI.Config.ConfigMenuListInventory;
import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Other.Metods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /ia config [plugin]: opens the settings menu of one plugin, or the list of all of them.
 */
public class Sub_Cmd_OpenConfigMenus implements CommandInterface
{
	CmdData _data;

	public Sub_Cmd_OpenConfigMenus(CmdData data)
	{
		_data = data;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if (!(sender instanceof Player player))
		{
			sender.sendMessage("Only players can open the settings menu");
			return true;
		}

		if (args.length >= 2)
		{
			ConfigMenu menu = ConfigMenu.getRegistered(args[1]);
			if (menu == null)
			{
				player.sendMessage(Metods.msgC("&cNo settings menu for '" + args[1] + "'"));
				return true;
			}
			menu.open(player);
			return true;
		}

		new ConfigMenuListInventory().open(player);
		return true;
	}

	@Override
	public void FailedMsg(CommandSender arg0, String arg1) {

	}
}
