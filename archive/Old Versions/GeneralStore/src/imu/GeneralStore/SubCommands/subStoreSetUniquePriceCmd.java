package imu.GeneralStore.SubCommands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.GeneralStore.Interfaces.CommandInterface;
import imu.GeneralStore.Other.ItemMetods;
import imu.GeneralStore.Other.ShopManager;
import imu.GeneralStore.main.Main;

public class subStoreSetUniquePriceCmd implements CommandInterface
{
	Main _main =null;
	ItemMetods itemM = null;
	
	Player player;
	ShopManager shopManager = null;
	public subStoreSetUniquePriceCmd(Main main)
	{
		_main = main;
		shopManager = _main.getShopManager();
		itemM = _main.getItemM();
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        player = (Player) sender;
        if(args.length > 3)
        {
        	if(itemM.isDigit(args[1]) && itemM.isDigit(args[2]) && itemM.isDigit(args[3]))
            {
            	ItemStack stack = player.getInventory().getItemInMainHand();
            	if(stack != null && stack.getType() != Material.AIR)
            	{
            		
            		Double[] prices= {Double.parseDouble(args[1]),Double.parseDouble(args[2]),Double.parseDouble(args[3])};
            		if(shopManager.isPriceValid(prices))
            		{
            			setNewUniquePrice(stack, prices);      
            		}else
            		{
            			player.sendMessage(shopManager.getStr_invalid_price());
            		}
            		    		
            	}else
            	{
            		player.sendMessage(ChatColor.RED+"You don't have item in your hand!");
            	}
            	
            	
            	return false;
            }
        }else
        {
        	
        }
        player.sendMessage("/gs unique <minPrice> <maxPrice> <priceProsent>");
		
        return false;
    }
    
    void setNewUniquePrice(ItemStack stack, Double[] prices)
    {
    	_main.getShopManager().addUniqueItem(stack, prices,true);
    	
    	if(!_main.getShopManager().isUnique(stack))
    	{
    		player.sendMessage(ChatColor.DARK_GREEN+"Unique item removed");
    	}else
    	{
    		player.sendMessage(ChatColor.GREEN+"Unique has been added");
    	}
    	
    	
    	
    }
    
    
   
}