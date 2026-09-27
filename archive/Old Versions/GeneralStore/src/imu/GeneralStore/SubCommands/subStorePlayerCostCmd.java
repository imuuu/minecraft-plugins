package imu.GeneralStore.SubCommands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.GeneralStore.Interfaces.CommandInterface;
import imu.GeneralStore.Other.Shop;
import imu.GeneralStore.main.Main;

public class subStorePlayerCostCmd implements CommandInterface
{
	Main _main = null;
	Shop tempShop;
	public subStorePlayerCostCmd(Main main) 
	{
		_main = main;
		tempShop = new Shop(_main, "gs.price.cost",false,false);
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;

    	ItemStack stack = player.getInventory().getItemInMainHand();
  	
    	if(stack != null && stack.getType() != Material.AIR)
    	{
    		
    		ItemStack copy = new ItemStack(stack);
    		tempShop.setShopStackAmount(copy, 64*9);
    		
    		Double[] prices = tempShop.calculatePriceOfItem(copy, 0, true);
    		tempShop.setShopStackAmount(copy, 64*9*3);
    		Double[] prices2 = tempShop.calculatePriceOfItem(copy, 0, true);
    		player.sendMessage(ChatColor.DARK_PURPLE +"====== Price INFO: "+ChatColor.AQUA+copy.getType()+ChatColor.DARK_PURPLE +" ======");
    		player.sendMessage(ChatColor.GREEN+"(Empty shop) In amounts: "
    				+ChatColor.DARK_PURPLE+"1: "+ChatColor.GOLD+prices[0]
    				+ChatColor.DARK_PURPLE+" 8: "+ChatColor.GOLD+prices[1]
    				+ChatColor.DARK_PURPLE+" 64: "+ChatColor.GOLD+prices[2]
    				+ChatColor.DARK_PURPLE+" 9x64: "+ChatColor.GOLD+prices[3]
    				+ChatColor.DARK_PURPLE+" 27x64: "+ChatColor.GOLD+prices2[3]);
    		player.sendMessage("");
    		    
    		System.out.println(player.getName() + "asked price for: "+stack.getType());
    		return false;
    	}else
    	{
    		player.sendMessage(ChatColor.RED+"You don't have item in your hand!");
        }
        return false;
    }
    
   
   
}