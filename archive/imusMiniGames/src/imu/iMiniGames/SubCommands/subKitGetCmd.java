package imu.iMiniGames.SubCommands;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.iMiniGames.Interfaces.CommandInterface;
import imu.iMiniGames.Main.ImusMiniGames;
import imu.iMiniGames.Managers.CombatManager;
import imu.iMiniGames.Other.ArenaKit;
import net.md_5.bungee.api.ChatColor;

public class subKitGetCmd implements CommandInterface
{
	ImusMiniGames _main = null;
	String _subCmd = "";
	CombatManager _com;

 	public subKitGetCmd(ImusMiniGames main) 
	{
		_main = main;
		_com = _main.get_combatManager();
	}

 	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;
        if(args.length < 4)
    	{
    		return false;
    	}
        
        String kitName = StringUtils.join(Arrays.copyOfRange(args, 3, args.length)," ");

        ArenaKit kit = _main.get_combatManager().getKit(kitName);
        if(kit != null)
        {
        	player.getInventory().setContents(kit.get_kitInv());
        	//_main.get_itemM().printArray(kitName, kit.get_kitInv());
        	player.sendMessage(ChatColor.translateAlternateColorCodes('&', kit.get_kitName() +" &6inv has been spawned"));
        	return true;
        }

    	player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cCoulnd't find a kit!"));
    	
  
        
		
        return false;
    }
    
    
    
   
   
}