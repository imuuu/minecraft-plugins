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

public class subStoreRemoveINFCmd implements CommandInterface
{
	Main _main = null;

	public subStoreRemoveINFCmd(Main main) 
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
        	ItemStack stack = player.getInventory().getItemInMainHand();
        	if(stack != null && stack.getType() != Material.AIR)
        	{
        		Shop shop = shopManager.getShop(nameShop);
        		player.sendMessage(ChatColor.DARK_PURPLE +"Item has been removed from shop named: "+shop.getDisplayName());
        		shop.removeInfItemFromShop(stack);

        	}else
        	{
        		player.sendMessage(ChatColor.RED+"You don't have item in your hand!");
            }
        	
        	return false;
        }
        player.sendMessage(ChatColor.RED + "Couldn't find shop name with that");
    	
        return false;
    }
    
   
   
}