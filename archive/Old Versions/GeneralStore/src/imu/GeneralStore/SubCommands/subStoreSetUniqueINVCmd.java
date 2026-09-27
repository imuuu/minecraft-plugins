package imu.GeneralStore.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.GeneralStore.Interfaces.CommandInterface;
import imu.GeneralStore.Other.ShopManager;
import imu.GeneralStore.main.Main;

public class subStoreSetUniqueINVCmd implements CommandInterface
{
	Main _main = null;

	Player player;
	ShopManager shopManager = null;
	
	public subStoreSetUniqueINVCmd(Main main)
	{
		_main = main;
		shopManager = _main.getShopManager();
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        player = (Player) sender;
        
        shopManager.openUniqueINV(player);
        //player.sendMessage("/gs uniques");
		
        return false;
    }
    
   
    
    
   
}