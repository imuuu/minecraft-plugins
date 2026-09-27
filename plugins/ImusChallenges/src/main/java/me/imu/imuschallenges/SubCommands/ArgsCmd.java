package me.imu.imuschallenges.SubCommands;

import imu.iAPI.Interfaces.CommandInterface;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ArgsCmd implements CommandInterface
{
 
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        
        return false;
    }

	@Override
	public void FailedMsg(CommandSender arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
 
}