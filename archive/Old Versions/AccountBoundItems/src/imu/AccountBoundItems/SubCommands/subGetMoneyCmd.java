package imu.AccountBoundItems.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.AccountBoundItems.Interfaces.CommandInterface;
import imu.AccountBoundItems.Other.ItemABI;
import imu.AccountBoundItems.main.Main;
import net.md_5.bungee.api.ChatColor;

public class subGetMoneyCmd implements CommandInterface
{
	ItemABI itemAbi = new ItemABI();
	Player player;
	Main main = Main.getInstance();
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        player = (Player) sender;

        if(args.length == 2)
        {
        	if(itemAbi.isDigit(args[1]))
        	{
        		
        		double price = Double.parseDouble(args[1]);
        		ItemStack money = itemAbi.getMoneyItem(price);
        		itemAbi.moveItemFirstFreeSpaceInv(money, player, true,false);
        		player.sendMessage(ChatColor.AQUA + "Here is CHECK: "+ (int)price);
        	}
        	return false;
        	
        }     
        player.sendMessage("Give amount of money you want");
    	return false;
    }
     
}