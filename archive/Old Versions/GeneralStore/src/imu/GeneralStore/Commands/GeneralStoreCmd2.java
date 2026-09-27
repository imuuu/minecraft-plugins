package imu.GeneralStore.Commands;


//Imports for the base command class.
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import imu.GeneralStore.Interfaces.CommandInterface;
import imu.GeneralStore.Other.ShopManager;
import imu.GeneralStore.main.Main;
import net.md_5.bungee.api.ChatColor;
 
public class GeneralStoreCmd2 implements CommandInterface
{
	Main _main = null;
	ShopManager _shopM = null;
	public GeneralStoreCmd2(Main main)
	{
		_main = main;
		_shopM = _main.getShopManager();
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 

    	if(!_main.getShopManager().isReady())
    	{
    		sender.sendMessage(ChatColor.DARK_RED+"You can't use that right now");
    		return true;
    	}
    	
    	if(_shopM.isShopsLocked())
    	{
    		if(!sender.isOp())
    		{
    			sender.sendMessage(ChatColor.RED + "Closed");
    			return true;
    		}
    		
    		sender.sendMessage(ChatColor.YELLOW + "This plugin commands are locked from normal player..");
    		
    		
    	}
    	
    	if(args.length > 0)
    		return false;
        
    	
    	
        return true;
    }
 
}