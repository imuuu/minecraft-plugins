package imu.GeneralStore.SubCommands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.GeneralStore.Interfaces.CommandInterface;
import imu.GeneralStore.Other.ConfigMaker;
import imu.GeneralStore.Other.ItemMetods;
import imu.GeneralStore.Other.ShopManager;
import imu.GeneralStore.main.Main;

public class subStoreSetPriceCmd implements CommandInterface
{
	Main _main=null;
	ItemMetods itemM = null;
	
	Player player;
	
	ShopManager shopManager = null;
	public subStoreSetPriceCmd(Main main) 
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
            			player.sendMessage(ChatColor.GREEN+"Prices has been chance to Material: "+ChatColor.AQUA+stack.getType().name());
            			player.sendMessage(ChatColor.GOLD+"REMEMBER RELOAD CONFIGS '/gs reload'! After chanced all");
                		
                		setNewPrice(stack, prices);
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
        player.sendMessage("/gs setprice <minPrice> <maxPrice> <priceProsent>");

        
		
        return false;
    }
    
    void setNewPrice(ItemStack stack, Double[] prices)
    {
    	Material m = stack.getType();
    	_main.materialPrices.put(m, prices);
    	String cat = itemM.getMaterialCategory(m);
    	
    	ConfigMaker cm = new ConfigMaker(_main, _main.getMaterialPriceYML());
    	FileConfiguration config = cm.getConfig();
    	
    	config.set(cat+".belong."+m.name()+".minPrice", prices[0]);
    	config.set(cat+".belong."+m.name()+".maxPrice", prices[1]);
    	config.set(cat+".belong."+m.name()+".proEachSell", prices[2]);
    	
    	cm.saveConfig();
    	
    	
    	
    }
    
    
   
}