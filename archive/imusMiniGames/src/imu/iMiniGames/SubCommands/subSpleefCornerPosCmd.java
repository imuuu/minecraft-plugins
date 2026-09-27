package imu.iMiniGames.SubCommands;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.iMiniGames.Arenas.SpleefArena;
import imu.iMiniGames.Interfaces.CommandInterface;
import imu.iMiniGames.Main.ImusMiniGames;
import net.md_5.bungee.api.ChatColor;

public class subSpleefCornerPosCmd implements CommandInterface
{
	ImusMiniGames _main = null;

	String _subCmd = "";
	int max_distance = 500;
	
	boolean override_thicknes = false;
	public subSpleefCornerPosCmd(ImusMiniGames main, String subCmd) 
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
               
        String str_pos =ChatColor.GOLD + "Arena:  "+ChatColor.AQUA +arenaName + ChatColor.GOLD +" has set corner position: ";
        String str_addNext =ChatColor.BLUE +  "Please add second position as well! With same command!";
        Location loc = new Location(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockY()-1, player.getLocation().getBlockZ());
        if(arena.getPlatformCorner(0) == null)
		{
			arena.setPlatformCorner(0, loc);
			player.sendMessage(str_pos + "1");
			player.sendMessage(str_addNext);
		}
		else if(arena.getPlatformCorner(1) == null )		
		{
			if(arena.getPlatformCorner(0).getWorld() == loc.getWorld())
			{
				if(loc.distance(arena.getPlatformCorner(0)) < max_distance)
				{
					if(!override_thicknes && loc.getBlockY() != arena.getPlatformCorner(0).getBlockY())
					{
						player.sendMessage(ChatColor.GREEN + "Other position y was differend so this position has but to same y");
						loc = new Location(loc.getWorld(), loc.getBlockX(), arena.getPlatformCorner(0).getBlockY(), loc.getBlockZ());
					}
					
					arena.setPlatformCorner(1, loc);
					player.sendMessage(str_pos + "2");
					arena.calculateCorners();
					
					//arena.fillWithSnow(arena.getPlatformCorner(0),arena.getPlatformCorner(1));
					
				}else
				{
					player.sendMessage(ChatColor.RED + "Total distance is too large! Over: "+max_distance);
				}
				
			}else
			{
				player.sendMessage(ChatColor.RED + "World isn't the same!");
			}
			
		}
		else
		{
			arena.clearPlatformCorners();
			arena.setPlatformCorner(0, loc);
			player.sendMessage(ChatColor.RED + "Locations have been cleared and position 1 set to this location.");
			player.sendMessage(str_addNext);
			return false;
		}

        
        
  
        
		
        return false;
    }
    
   
   
}