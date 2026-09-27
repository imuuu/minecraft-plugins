package imu.iCards.SubCmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.iAPI.CmdUtil.CmdData;
import imu.iAPI.Interfaces.CommandInterface;

public class SubTestCMD implements CommandInterface
{
	CmdData _data;

	public SubTestCMD(CmdData data)
	{
		_data = data;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		Player player = (Player) sender;
		if (args.length < 4)
		{
			player.sendMessage(_data.get_syntaxText());
			return false;
		}

		return false;
	}

	@Override
	public void FailedMsg(CommandSender arg0, String arg1)
	{

	}

}