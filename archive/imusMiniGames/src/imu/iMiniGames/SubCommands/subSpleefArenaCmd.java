package imu.iMiniGames.SubCommands;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;

import imu.iMiniGames.Arenas.SpleefArena;
import imu.iMiniGames.Interfaces.CommandInterface;
import imu.iMiniGames.Main.ImusMiniGames;
import imu.iMiniGames.Managers.SpleefManager;
import imu.iMiniGames.Prompts.ConvPromptSetDescriptionArena;
import net.md_5.bungee.api.ChatColor;

public class subSpleefArenaCmd implements CommandInterface
{
	ImusMiniGames _main = null;
	String _subCmd = "";
	SpleefManager _sm;
	boolean override_thicknes = false;
	String[] _subs; //{"create", "spawn", "pos", "save", "remove", "lobby","desc"};
	int max_distance = 500;
 	public subSpleefArenaCmd(ImusMiniGames main, String[] sub_cmds) 
	{
		_main = main;
		_subs = sub_cmds;
		_sm = _main.get_spleefManager();
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

    	SpleefArena arena = (SpleefArena)_sm.getArena(arenaName);
    	
    	if(args[1].equalsIgnoreCase(_subs[0]))
    	{
    		//create
    		_sm.addArena(new SpleefArena(arenaName));
          player.sendMessage(ChatColor.GOLD + "You have created Spleef arena named as "+ChatColor.AQUA +arenaName);
          player.sendMessage(ChatColor.GOLD + "Remember set spawnpoints, cornerpositions and lobby. Then save!");
          return true;
    	}
    	
    	if(arena != null)
    	{
    		String arenaPrefix =ChatColor.GOLD + "Arena:  "+ChatColor.AQUA +arenaName + ChatColor.GOLD+" ";
    		if(args[1].equalsIgnoreCase(_subs[1]))
        	{
        		//spawn
    			 arena.sendArenaCreationgINFO(player);
        		 String str_pos = arenaPrefix + "Added Spawnposition: "+ChatColor.DARK_PURPLE+(arena.getTotalSpawnPositions()+1);
        		 player.sendMessage(str_pos);
        		 arena.addSpawnPosition(player.getLocation());
        		 arena.set_maxPlayers(arena.get_maxPlayers()+1);
        		 return true;
        	}
    		if(args[1].equalsIgnoreCase(_subs[2]))
        	{
    			//pos
    			 arena.sendArenaCreationgINFO(player);
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
    					return true;
    				}
    			
    			return true;
        	}
    		if(args[1].equalsIgnoreCase(_subs[3]))
        	{
    			//save
    			arena.sendArenaCreationgINFO(player);
    			_sm.saveArena(arena);
    			player.sendMessage(arenaPrefix+"has been saved");
    			return true;
        	}
    		if(args[1].equalsIgnoreCase(_subs[4]))
        	{
    			//remove
    			_sm.removeArena(arena);
    			player.sendMessage(arenaPrefix+"has been removed");
    			return true;
        	}
    		if(args[1].equalsIgnoreCase(_subs[5]))
        	{
    			//lobby
    			
    			arena.set_spectator_lobby(player.getLocation());
    			arena.sendArenaCreationgINFO(player);
    			player.sendMessage(arenaPrefix+"lobby has been set");
    			return true;
        	}
    		if(args[1].equalsIgnoreCase(_subs[6]))
    		{
    			//desc
    			ConversationFactory cf = null;
    			String question = null;
    			Conversation conv = null;
    			
    			cf = new ConversationFactory(_main);
    			question = ChatColor.DARK_PURPLE + "Give "+arena.get_name()+" description";
    			conv = cf.withFirstPrompt(new ConvPromptSetDescriptionArena(_main, player, question, arena)).withLocalEcho(true).buildConversation(player);
    			conv.begin();
    			return true;
    		}
        	
    	}else
    	{
    		player.sendMessage(ChatColor.RED + "Couldn't find Combat arena with name: "+arenaName);
    	}
    	
  
        
		
        return false;
    }
    
    
    
   
   
}