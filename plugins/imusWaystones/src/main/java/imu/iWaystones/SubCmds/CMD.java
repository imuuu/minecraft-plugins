package imu.iWaystones.SubCmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import imu.iAPI.Interfaces.CommandInterface;

public class CMD implements CommandInterface
{

	@Override
	public void FailedMsg(CommandSender arg0, String arg1) {
		
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		return false;
	}

}
