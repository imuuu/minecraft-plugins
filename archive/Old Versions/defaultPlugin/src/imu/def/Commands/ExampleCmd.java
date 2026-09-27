package imu.def.Commands;

//Imports for the base command class.
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.def.Interfaces.CommandInterface;
 
public class ExampleCmd implements CommandInterface
{
 
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
    	if(args.length > 0)
    		return false;
        
    	
    	Player p = (Player) sender;

        p.sendMessage("Example!");
        return true;
    }
 
}