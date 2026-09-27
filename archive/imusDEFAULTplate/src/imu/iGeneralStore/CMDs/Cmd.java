package imu.iGeneralStore.CMDs;


//Imports for the base command class.
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import imu.iGeneralStore.Interfaces.CommandInterface;
import imu.iGeneralStore.Main.Main;
 
public class Cmd implements CommandInterface
{
	Main _main = null;

	public Cmd(Main main)
	{
		_main = main;
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
    	
    	System.out.println("HERE WE GO!");
    	if(args.length > 0)
    		return false;
        
    	
    	
        return true;
    }
 
}