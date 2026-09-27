package imu.AccountBoundItems.SubCommands;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.AccountBoundItems.Interfaces.CommandInterface;
import imu.AccountBoundItems.Other.Cooldowns;
import imu.AccountBoundItems.Other.ItemABI;
import imu.AccountBoundItems.main.Main;
import net.md_5.bungee.api.ChatColor;

public class subBindCostCmd implements CommandInterface
{
	ItemABI itemAbi = new ItemABI();
	Player player;
	Main main = Main.getInstance();
	
	int cd_s = 10;
	String cdName="bind.waiting";
	
	HashMap<Player, ItemStack> lastItem = new HashMap<Player, ItemStack>();
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
    	player = (Player) sender;
    
    	ItemStack stack = player.getInventory().getItemInMainHand();	
    	if(itemAbi.isBound(stack))
    	{
    		player.sendMessage("That item is already bound!");
    		return false;
    	}
    	Cooldowns cd = main.playerCds.get(player);
    	
    	double cost = itemAbi.getItemCost(stack,true);
    	
    	if(cd == null)
    	{
    		Cooldowns cdNow = new Cooldowns();
    		cdNow.addCooldownInSeconds(cdName, cd_s);
    		main.playerCds.put(player,cdNow);
    		lastItem.put(player, stack);
    		
    	}else
    	{

    		if(!cd.isCooldownReady(cdName) && lastItem.get(player).isSimilar(stack))
    		{
    			
    			if(itemAbi.repair(stack, player, cost))
    			{
    				player.sendMessage(ChatColor.AQUA + "Item has been bound!");
    				itemAbi.setBind(stack, player, true);
        			cd.removeCooldown(cdName);
        			return false;
    			}
    			return false;
    			
    		}
    		cd.addCooldownInSeconds(cdName, cd_s);
    		lastItem.put(player, stack);
    	}
    	player.sendMessage("This bind cost you: "+ Math.round(cost));
    	player.sendMessage(ChatColor.GREEN + "Click me one more time to "+ChatColor.AQUA+"BIND"+ChatColor.GREEN+" it!");
    	
    	
    	
    	
        return false;
    }
    
    
   
}