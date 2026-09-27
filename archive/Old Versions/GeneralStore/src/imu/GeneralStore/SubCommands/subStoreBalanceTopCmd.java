package imu.GeneralStore.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.GeneralStore.Interfaces.CommandInterface;
import imu.GeneralStore.Other.Shop;
import imu.GeneralStore.main.Main;

public class subStoreBalanceTopCmd implements CommandInterface
{
	Main _main = null;
	Shop tempShop;
	public subStoreBalanceTopCmd(Main main) 
	{
		_main = main;
		tempShop = new Shop(_main, "gs.price.cost",false,false);
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;

    	_main.getBalanceTracker().printTop(player, 20);
    	
        return false;
    }
    
   
   
}