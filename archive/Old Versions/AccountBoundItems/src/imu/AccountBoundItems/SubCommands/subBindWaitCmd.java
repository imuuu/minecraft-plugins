package imu.AccountBoundItems.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.AccountBoundItems.Interfaces.CommandInterface;
import imu.AccountBoundItems.Other.ItemABI;
import imu.AccountBoundItems.Other.ItemMetods;
import imu.AccountBoundItems.Other.ServerMethods;

public class subBindWaitCmd implements CommandInterface
{
	ItemABI itemAbi = new ItemABI();
	ItemMetods itemM = new ItemMetods();
	ServerMethods serverM= new ServerMethods();
	Player player;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        player = (Player) sender;

    	ItemStack stack = player.getInventory().getItemInMainHand();
    	itemAbi.setWait(stack);
    	//itemAbi.setBind(stack, player,false);
    	
        
    	return false;
    }
     
}