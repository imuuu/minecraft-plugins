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

public class subSpleefClearSpawnPositionsCmd implements CommandInterface
{
	ImusMiniGames _main = null;

	String _subCmd = "";
	public subSpleefClearSpawnPositionsCmd(ImusMiniGames main, String subCmd) 
	{
		_main = main;
		_subCmd=subCmd;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;
	
        if(args.length < 4)
    	{
        	player.sendMessage(ChatColor.RED +"Remember: " +_subCmd + " arenaName");
    		return false;
    	}
        
        String arenaName = StringUtils.join(Arrays.copyOfRange(args, 3, args.length)," ");
        SpleefArena arena = (SpleefArena) _main.get_spleefManager().getArena(arenaName);

        if(arena == null)
        {
        	player.sendMessage(ChatColor.RED + "Couldn't find Spleef arena with that name: "+arenaName);
        	return false;
        }
               
        String str_pos = ChatColor.GOLD + "Arena:  "+ChatColor.AQUA +arenaName + ChatColor.GOLD +"Removed spawn positions";
        //String str_addNext =ChatColor.BLUE +  "Please add second position as well! With same command!";
        
        player.sendMessage(str_pos);
        arena.clearSpawnPositions();
        
  
        
		
        return false;
    }
    
   
   
}