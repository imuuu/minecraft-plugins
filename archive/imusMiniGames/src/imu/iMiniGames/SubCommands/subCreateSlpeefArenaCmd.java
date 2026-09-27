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

public class subCreateSlpeefArenaCmd implements CommandInterface
{
	ImusMiniGames _main = null;
	String _subCmd = "";
	public subCreateSlpeefArenaCmd(ImusMiniGames main, String subCmd) 
	{
		_main = main;
		_subCmd=subCmd;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;
        System.out.println("Args l: "+args.length);
        System.out.println("cmd1 : "+cmd.getDescription());
        System.out.println("cmd2 : "+cmd.getName());
        System.out.println("cmd3 : "+cmd.getLabel());
        System.out.println("cmd4 : "+cmd.getUsage());
        System.out.println("cmd5 : "+commandLabel);
    	
        
        if(args.length < 3)
    	{
        	player.sendMessage(ChatColor.RED +"Remember: " +_subCmd + " arenaName");
    		return false;
    	}
        
        String arenaName = StringUtils.join(Arrays.copyOfRange(args, 2, args.length)," ");
        //_main.get_spleefManager().createSpleefArena(arenaName);
        _main.get_spleefManager().addArena(new SpleefArena(arenaName));
               
        player.sendMessage(ChatColor.GOLD + "You have created Spleef arena named: "+ChatColor.AQUA +arenaName);
        player.sendMessage(ChatColor.GOLD + "Remember add two corner positions and spawns");
    	
    	
  
        
		
        return false;
    }
    
   
   
}