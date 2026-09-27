package imu.AccountBoundItems.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.AccountBoundItems.Interfaces.CommandInterface;
import imu.AccountBoundItems.Other.ItemABI;
import imu.AccountBoundItems.Other.ServerMethods;
import imu.AccountBoundItems.main.Main;

public class subRepairAllCmd implements CommandInterface
{
	ItemABI itemAbi = new ItemABI();
	ServerMethods serverM= new ServerMethods();
	Player player;
	Main main = Main.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
    	player = (Player) sender;
    	if(args.length == 3)
    	{
    		Player target_player = serverM.getPlayerOnServer(args[2]);
    		if(target_player != null)
    		{
    			player = target_player;
    		}
    	}


    	itemAbi.repairAll(player.getInventory().getContents());
    	player.sendMessage("All repaired!");
        
    	return false;
    }
     
}