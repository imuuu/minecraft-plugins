package imu.TokenTp.SubCommands;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.TokenTp.Enums.TokenType;
import imu.TokenTp.Interfaces.CommandInterface;
import imu.TokenTp.Managers.TeleTokenManager;
import imu.TokenTp.main.Main;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;
import net.md_5.bungee.api.ChatColor;

public class subTtpTpListCmd implements CommandInterface
{
	Main _main = null;
	Metods _itemM = null;
	TeleTokenManager _ttManager;
	public subTtpTpListCmd(Main main) 
	{
		_main = main;
		_itemM = ImusAPI._metods;
		_ttManager = main.getTeleTokenManager();
				
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;
    	
    	
    	
    	ItemStack stack = player.getInventory().getItemInMainHand();
		 if(stack != null && _ttManager.isToken(stack) && _ttManager.getTokenType(stack) == TokenType.TOKEN_TO_PLAYER)
		 {

			 if(!_ttManager.isTokenActiva(player.getUniqueId()))
		    	{
		    		player.sendMessage("You need token to activated this command!");
		    		return false;
		    	}

			 //player.sendMessage(ChatColor.GOLD + "=======================================");
			 player.sendMessage(ChatColor.GOLD+"====="+ChatColor.DARK_PURPLE+ChatColor.BOLD+"Available players to teleport"+ChatColor.GOLD+"=====");
			 //player.sendMessage(ChatColor.GOLD + "=======================================");
			 player.sendMessage(" ");
			 
			 for(Player targetPlayer : Bukkit.getServer().getOnlinePlayers())
	    	 {
	    		 
	    		 if(targetPlayer.getGameMode() == GameMode.SURVIVAL &&  targetPlayer != player)
	    		 {
	    			 HashMap<String, String> hMap = new HashMap<String, String>();
		    		 //player.sendMessage("=======================================");
					 hMap.put(ChatColor.GOLD+"Send request to teleport to (press this with mouse): "+ChatColor.AQUA +targetPlayer.getName(), "/tttp request " + targetPlayer.getName());			
					 _itemM.SendMessageCommands(player, "",hMap,"", " ");
					 //player.sendMessage("=======================================");
	    		 }
	    		    		 
	    		
	    		
	    	 }
		 }
    	 
		
        return false;
    }
    
   
   
}