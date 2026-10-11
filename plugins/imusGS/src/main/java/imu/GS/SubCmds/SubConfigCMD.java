package imu.GS.SubCmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.iAPI.Config.ConfigMenu;
import imu.iAPI.Interfaces.CommandInterface;

public class SubConfigCMD implements CommandInterface
{
	private final ConfigMenu _menu;

	public SubConfigCMD(ConfigMenu menu)
	{
		_menu = menu;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if (!(sender instanceof Player player))
		{
			sender.sendMessage("Only players can open the settings menu. Edit plugins/imusGS/config.yml instead.");
			return true;
		}
		_menu.open(player);
		return true;
	}

	@Override
	public void FailedMsg(CommandSender sender, String msg)
	{

	}
}
