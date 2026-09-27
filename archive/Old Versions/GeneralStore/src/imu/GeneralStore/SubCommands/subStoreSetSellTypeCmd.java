package imu.GeneralStore.SubCommands;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.GeneralStore.Interfaces.CommandInterface;
import imu.GeneralStore.Other.ItemMetods;
import imu.GeneralStore.Other.ShopManager;
import imu.GeneralStore.main.Main;

public class subStoreSetSellTypeCmd implements CommandInterface
{
	Main _main = null;
	
	ItemMetods itemM = null;
	
	public subStoreSetSellTypeCmd(Main main)
	{
		_main = main;
		itemM = main.getItemM();
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;
     
        if(args.length < 2)
        {
    		player.sendMessage("Need a shop name!");
    		return false;
        }
        ShopManager shopManager = _main.getShopManager();
      
        
        String nameShop = StringUtils.join(Arrays.copyOfRange(args, 1, args.length)," ").toLowerCase().replace("false", "").replace("true", "");
        //nameShop=nameShop.substring(1,nameShop.length()-1);
        if(shopManager.isExists(nameShop))
        {
        	       	
        	if(args.length > 2 && itemM.doesStrArrayCointainStr(args, "true"))
        	{
        		player.sendMessage(ChatColor.GREEN+"Shop only sells now!");
        		shopManager.setIsSellingOnly(nameShop, true);
            	
        	}else if(args.length > 2 && itemM.doesStrArrayCointainStr(args, "false"))
        	{
        		player.sendMessage(ChatColor.GREEN+"Shop buys and sells now!");
        		shopManager.setIsSellingOnly(nameShop, false);
            	
        	}else
        	{
        		player.sendMessage(ChatColor.GOLD+ "Do you wanna that this shop sell only items?");
        		itemM.sendYesNoConfirm(player, "/"+ cmd.getName()+ " type "+nameShop+" true", "/"+ cmd.getName()+ " type "+nameShop+" false");
        	}
        	return false;
        }
        
        player.sendMessage(ChatColor.RED + "Couldn't find shop with that name!");
       
        
        
		
        return false;
    }
    
   
   
}