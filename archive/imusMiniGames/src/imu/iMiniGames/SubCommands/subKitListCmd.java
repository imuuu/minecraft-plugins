package imu.iMiniGames.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.iMiniGames.Interfaces.CommandInterface;
import imu.iMiniGames.Main.ImusMiniGames;
import imu.iMiniGames.Managers.CombatManager;
import imu.iMiniGames.Other.ArenaKit;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class subKitListCmd implements CommandInterface
{
	ImusMiniGames _main = null;
	String _subCmd = "";
	CombatManager _com;
	String _sub_cmd; 
 	public subKitListCmd(ImusMiniGames main, String sub_cmd) 
	{
		_main = main;
		_sub_cmd = sub_cmd;
		_com = _main.get_combatManager();
	}

 	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;

        if(args.length < 2)
    	{
        	player.sendMessage(ChatColor.RED + "Something wrong");
    		return false;
    	}
        
        if(!_main.get_combatManager().getArena_kits().isEmpty())
        {

        	for(ArenaKit kit : _main.get_combatManager().getArena_kits())
        	{
        		TextComponent msg = new TextComponent( kit.get_kitNameWithColor()+ChatColor.translateAlternateColorCodes('&', " &l&a(Click)"));
        		msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/img combat kit get "+kit.get_kitName()));
        		msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to set your inv to kit")));
        		player.spigot().sendMessage(msg);
        	}
        	return true;
        	
        }

    	player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThere isn't any kits!"));
    	
    	
  
        
		
        return false;
    }
    
    
    
   
   
}