package imu.AccountBoundItems.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.AccountBoundItems.Interfaces.CommandInterface;
import imu.AccountBoundItems.Other.ItemABI;
import net.md_5.bungee.api.ChatColor;

public class subSetPriceCmd implements CommandInterface
{
	ItemABI itemAbi = new ItemABI();
	Player player;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        player = (Player) sender;
        
        if(args.length == 2)
        {
        	if(itemAbi.isDigit(args[1]))
        	{
        		ItemStack stack = player.getInventory().getItemInMainHand();
        		double price = Double.parseDouble(args[1]);
        		itemAbi.setPriceOverride(stack, price);
        		player.sendMessage(ChatColor.AQUA + "Price has been overrided");
        	}
        	return false;
        	
        }
        player.sendMessage("Give amount of money you want");

    	return false;
    }
     
}