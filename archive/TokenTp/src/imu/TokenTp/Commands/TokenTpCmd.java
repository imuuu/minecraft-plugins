package imu.TokenTp.Commands;


//Imports for the base command class.
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import imu.TokenTp.Interfaces.CommandInterface;
import imu.TokenTp.main.Main;
import imu.iAPI.Other.Metods;
 
public class TokenTpCmd implements CommandInterface
{

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
    	if(args.length > 0)
    		return false;
        
    	
    	
        return true;
    }
 
}