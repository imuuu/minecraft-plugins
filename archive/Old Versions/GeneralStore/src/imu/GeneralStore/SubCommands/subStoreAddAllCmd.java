package imu.GeneralStore.SubCommands;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.GeneralStore.Interfaces.CommandInterface;
import imu.GeneralStore.Other.Shop;
import imu.GeneralStore.Other.ShopManager;
import imu.GeneralStore.main.Main;

public class subStoreAddAllCmd implements CommandInterface
{
	Main _main = null;
	
	public subStoreAddAllCmd(Main main) 
	{
		_main = main;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;
	
    	ShopManager shopManager = _main.getShopManager();
        
    	
        
        String nameShop = StringUtils.join(Arrays.copyOfRange(args, 2, args.length)," ");

        if(shopManager.isExists(nameShop))
        {
        	int count = 0;
        	for(ItemStack s : player.getInventory().getContents())
        	{
        		if(s != null && s.getType() != Material.AIR)
            	{
            		Shop shop = shopManager.getShop(nameShop);
            		player.sendMessage(ChatColor.DARK_PURPLE +"Item "+ChatColor.AQUA+s.getType().name()+ChatColor.DARK_PURPLE +" has been added as infinity item to shop named: "+shop.getDisplayName());
            		shop.putInfItemToShop(s);  	
            	}
        		
        		count++;
        		if(count > 8)
        		{
        			break;
        		}
        	}
        	
        	return false;
        }
        player.sendMessage(ChatColor.RED + "Couldn't find shop name with that");
    	
    	
  
        
		
        return false;
    }
    
   
   
}