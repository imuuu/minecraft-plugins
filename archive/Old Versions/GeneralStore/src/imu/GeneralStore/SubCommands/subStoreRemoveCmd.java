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

public class subStoreRemoveCmd implements CommandInterface
{
	Main _main = null;
	
	ItemMetods itemM = null;
	
	public subStoreRemoveCmd(Main main)
	{
		_main = main;
		itemM = _main.getItemM();
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;
     
        if(args.length < 3)
        {
    		player.sendMessage("Remember give your shop a name");
    		return false;
        }
        ShopManager shopManager = _main.getShopManager();
      
        
        String nameShop = StringUtils.join(Arrays.copyOfRange(args, 2, args.length)," ").toLowerCase().replace("false", "").replace("true", "");
        //nameShop=nameShop.substring(1,nameShop.length()-1);
        if(shopManager.isExists(nameShop))
        {
        	
        	
        	if(args.length > 3 && itemM.doesStrArrayCointainStr(args, "true"))
        	{
        		player.sendMessage(ChatColor.GREEN+"Shop has been removed!");
            	shopManager.removeShop(nameShop);
            	
        	}else if(args.length > 3 && itemM.doesStrArrayCointainStr(args, "false"))
        	{
        		player.sendMessage(ChatColor.GREEN+"Remove process canceled");
            	
        	}else
        	{
        		player.sendMessage(ChatColor.GOLD+ "Are you sure you wanna remove this shop?");
        		itemM.sendYesNoConfirm(player, "/"+ cmd.getName()+ " remove shop "+nameShop+" true", "/"+ cmd.getName()+ " remove shop "+nameShop+" false");
        	}
        	return false;
        }
        
        player.sendMessage(ChatColor.RED + "Couldn't find shop with that name!");
       
        
        
		
        return false;
    }
    
   
   
}