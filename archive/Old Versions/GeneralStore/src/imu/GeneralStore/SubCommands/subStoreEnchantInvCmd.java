package imu.GeneralStore.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.GeneralStore.Interfaces.CommandInterface;
import imu.GeneralStore.Other.EnchantsManager;
import imu.GeneralStore.main.Main;

public class subStoreEnchantInvCmd implements CommandInterface
{
	EnchantsManager enchManager = null;
	public subStoreEnchantInvCmd(Main main)
	{	
		enchManager = main.getEnchManager();
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;
        
    	enchManager.openEnchantINV(player);
		
        return false;
    }
    
   
    
    
   
}