package imu.iMiniGames.SubCommands;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.iMiniGames.Interfaces.CommandInterface;
import imu.iMiniGames.Main.ImusMiniGames;
import net.md_5.bungee.api.ChatColor;

public class subSpectateCmd implements CommandInterface
{
	ImusMiniGames _main = null;

	String _subCmd = "";
	public subSpectateCmd(ImusMiniGames main, String subCmd) 
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
        	_main.sendBlockedmsg(player);
        	return false;
        }
        
        if(args.length < 3)
        {
        	return false;
        }
        
        if(_main.get_combatGameHandler().isPlayerInArena(player) || _main.get_spleefGameHandler().isPlayerInArena(player))
        {
        	player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou are in arena, spectating isn't posible!"));
        	return false;
        }
        
        String arenaName = StringUtils.join(Arrays.copyOfRange(args, 2, args.length)," ");
        if(_main.get_itemM().doesStrArrayCointainStr(args, "spleef"))
        {
        	_main.get_spleefGameHandler().addSpectator(arenaName, player);
        }
        if(_main.get_itemM().doesStrArrayCointainStr(args, "combat"))
        {
        	_main.get_combatGameHandler().addSpectator(arenaName, player);
        }
        
       
        
		
        return false;
    }
    
   
   
}