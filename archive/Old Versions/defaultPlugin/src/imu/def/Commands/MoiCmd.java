package imu.def.Commands;

//Imports for the base command class.
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.def.Interfaces.CommandInterface;
 
public class MoiCmd implements CommandInterface
{
 
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
    	System.out.println("visit:" + cmd.getName());
    	if(args.length == 0)
    		return false;
	
    	if(!args[0].equalsIgnoreCase("nopass"))
    	{
    		System.out.println("args[0]:"+args[0]);
    		return false;
    	}
    		
    	
    	
        Player p = (Player) sender;

        p.sendMessage("Moi!");
        return true;
    }
 
}