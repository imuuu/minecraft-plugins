package imu.imusSpawners.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.iAPI.Interfaces.CommandInterface;

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