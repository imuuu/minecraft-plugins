package imu.WorldRestore.Commands;


//Imports for the base command class.
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import imu.WorldRestore.Interfaces.CommandInterface;
import imu.WorldRestore.main.Main;
 
public class WorldRestoreCmd implements CommandInterface
{
	Main _main = null;
	public WorldRestoreCmd(Main main)
	{
		_main = main;
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
    
    	if(args.length > 0)
    		return false;
        
    	sender.sendMessage("Write /help wr get more info!");
    	
    	
        return true;
    }
 
}