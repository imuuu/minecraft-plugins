package imu.iMiniGames.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.iMiniGames.Interfaces.CommandInterface;
import imu.iMiniGames.Main.ImusMiniGames;
import net.md_5.bungee.api.ChatColor;

public class subSpleefAcceptCmd implements CommandInterface
{
	ImusMiniGames _main = null;

	String _subCmd = "";
	public subSpleefAcceptCmd(ImusMiniGames main, String subCmd) 
	{
		_main = main;
		_subCmd=subCmd;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;
	
        player.sendMessage(" ");
        player.sendMessage(" ");
        player.sendMessage(" ");
        if(_main.get_itemM().doesStrArrayCointainStr(args, "confirm:yes"))
        {
        	player.sendMessage(" ");
        	
        	if(!_main.get_spleefGameHandler().requestAnwser(player.getUniqueId(), true))
        	{
        		player.sendMessage("You have already answered or your request has expired");
        	}else
        	{
        		player.sendMessage(ChatColor.DARK_GREEN + "You have accept match");
        	}
        }
        if(_main.get_itemM().doesStrArrayCointainStr(args, "confirm:no"))
        {
        	
        	if(!_main.get_spleefGameHandler().requestAnwser(player.getUniqueId(), false))
        	{
        		player.sendMessage("You have already answered or your request has expired");
        	}else
        	{
        		player.sendMessage(ChatColor.RED + "You have denied match");
        	}
        }
       
        
		
        return false;
    }
    
   
   
}