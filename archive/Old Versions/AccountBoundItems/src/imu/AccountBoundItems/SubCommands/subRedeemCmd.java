package imu.AccountBoundItems.SubCommands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.AccountBoundItems.Interfaces.CommandInterface;
import imu.AccountBoundItems.Other.ItemABI;
import imu.AccountBoundItems.main.Main;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class subRedeemCmd implements CommandInterface
{
	ItemABI itemAbi = new ItemABI();
	Player player;
	Main main = Main.getInstance();
	Economy econ = Main.getEconomy();
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        player = (Player) sender;

    	ItemStack stack = player.getInventory().getItemInMainHand();
    	if(stack == null || stack.getType() == Material.AIR)
    	{
    		player.sendMessage(ChatColor.RED + "You need to have item in your hand");
    		return false;
    	}
    	
    	if(itemAbi.getPersistenData(stack, main.keyNames.get("check"), PersistentDataType.INTEGER) == 1)
    	{
    		EconomyResponse res = econ.depositPlayer(player, itemAbi.getItemCost(stack,true)*stack.getAmount());
    		player.sendMessage(ChatColor.GOLD + "Here is your "+res.amount+"! ");
    		
    		//give real money
    		stack.setAmount(0);
    	}
    	
        
    	return false;
    }
     
}