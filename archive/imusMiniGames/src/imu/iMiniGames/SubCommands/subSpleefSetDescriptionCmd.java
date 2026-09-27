package imu.iMiniGames.SubCommands;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;

import imu.iMiniGames.Arenas.SpleefArena;
import imu.iMiniGames.Interfaces.CommandInterface;
import imu.iMiniGames.Main.ImusMiniGames;
import imu.iMiniGames.Prompts.ConvPromptSetDescriptionArena;
import net.md_5.bungee.api.ChatColor;

public class subSpleefSetDescriptionCmd implements CommandInterface
{
	ImusMiniGames _main = null;

	String _subCmd = "";
	int max_distance = 500;
	
	boolean override_thicknes = false;
	public subSpleefSetDescriptionCmd(ImusMiniGames main, String subCmd) 
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
               
        
        ConversationFactory cf = null;
		String question = null;
		Conversation conv = null;
		
		cf = new ConversationFactory(_main);
		question = ChatColor.DARK_PURPLE + "Give "+arena.get_name()+" description";
		conv = cf.withFirstPrompt(new ConvPromptSetDescriptionArena(_main, player, question, arena)).withLocalEcho(true).buildConversation(player);
		conv.begin();
        

        
        
  
        
		
        return false;
    }
    
   
   
}