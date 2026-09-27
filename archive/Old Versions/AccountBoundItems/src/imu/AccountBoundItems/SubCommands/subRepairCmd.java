package imu.AccountBoundItems.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.AccountBoundItems.Interfaces.CommandInterface;
import imu.AccountBoundItems.Other.ItemABI;
import imu.AccountBoundItems.Other.ServerMethods;
import imu.AccountBoundItems.main.Main;

public class subRepairCmd implements CommandInterface
{
	ItemABI itemAbi = new ItemABI();
	ServerMethods serverM= new ServerMethods();
	Player player;
	Main main = Main.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
    	player = (Player) sender;
    	
    	if(args.length == 2)
    	{
    		Player target_player = serverM.getPlayerOnServer(args[1]);
    		if(target_player != null)
    		{
    			player = target_player;
    		}
    	}
    	
    	ItemStack stack = player.getInventory().getItemInMainHand();	
    	if(itemAbi.isBroken(stack))
    	{
    		player.sendMessage("Item has been repaired!");
    		itemAbi.repair(stack);
    	}
    	else
    	{
    		player.sendMessage("That item isn't broken!");
    	}
    	
    	
        return false;
    }
    
    
   
}