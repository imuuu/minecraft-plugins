package imu.iMiniGames.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.iMiniGames.Interfaces.CommandInterface;
import imu.iMiniGames.Main.ImusMiniGames;
import net.md_5.bungee.api.ChatColor;

public class subBlockMECmd implements CommandInterface
{
	ImusMiniGames _main = null;
	String _subCmd = "";
	public subBlockMECmd(ImusMiniGames main, String subCmd) 
	{
		_main = main;
		_subCmd=subCmd;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;
       
        if(_main.isPlayerBlocked(player))
        {
        	_main.removePlayerBlockMe(player);
        	player.sendMessage(ChatColor.GREEN + "You have unlocked your block! You can use minigame commands and receive invitations..");
        }
        else
        {
        	player.sendMessage(ChatColor.GOLD + "You have blocked your self from minigames. You dont receive invitations or other things");
            player.sendMessage(ChatColor.GOLD + "You can open this lock by writing this same command again!");
            _main.putPlayerBlockMe(player, true);
        }
        
        
    	
    	
  
        
		
        return false;
    }
    
   
   
}