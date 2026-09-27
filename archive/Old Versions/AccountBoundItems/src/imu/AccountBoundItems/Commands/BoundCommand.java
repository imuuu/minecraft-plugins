package imu.AccountBoundItems.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import imu.AccountBoundItems.Interfaces.CommandInterface;
import imu.AccountBoundItems.Other.ItemABI;
 
public class BoundCommand implements CommandInterface
{
	ItemABI itemAbi = new ItemABI();
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
    	double price = 0;
    	
    	if(price == 0)
    	{
    		if(args.length > 0)
        		return false;
    	}
    	    	
        return true;
    }
 
}