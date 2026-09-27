package imu.iMiniGames.SubCommands;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.iMiniGames.Arenas.SpleefArena;
import imu.iMiniGames.Interfaces.CommandInterface;
import imu.iMiniGames.Main.ImusMiniGames;
import net.md_5.bungee.api.ChatColor;

public class subSpleefRemoveCmd implements CommandInterface
{
	ImusMiniGames _main = null;

	String _subCmd = "";
	public subSpleefRemoveCmd(ImusMiniGames main, String subCmd) 
	{
		_main = main;
		_subCmd=subCmd;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;
	
        if(args.length < 3)
    	{
        	player.sendMessage(ChatColor.RED +"Remember: " +_subCmd + " arenaName");
    		return false;
    	}
        
        String arenaName = StringUtils.join(Arrays.copyOfRange(args, 2, args.length)," ");
        SpleefArena arena = (SpleefArena) _main.get_spleefManager().getArena(arenaName);
        if(arena == null)
        {
        	player.sendMessage(ChatColor.RED + "Couldn't find Spleef arena with that name: "+arenaName);
        	return false;
        }
         
        player.sendMessage(ChatColor.AQUA+ arenaName+ ChatColor.DARK_PURPLE + ": Has been removed!");
        _main.get_spleefManager().removeArena(arena);

        
        
  
        
		
        return false;
    }
    
   
   
}