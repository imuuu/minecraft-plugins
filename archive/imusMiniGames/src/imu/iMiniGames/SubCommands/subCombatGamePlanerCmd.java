package imu.iMiniGames.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.iMiniGames.Interfaces.CommandInterface;
import imu.iMiniGames.Invs.CombatGamePlaner;
import imu.iMiniGames.Main.ImusMiniGames;
import imu.iMiniGames.Other.CombatDataCard;
import net.md_5.bungee.api.ChatColor;

public class subCombatGamePlanerCmd implements CommandInterface
{
	ImusMiniGames _main = null;

	String _subCmd = "";
	public subCombatGamePlanerCmd(ImusMiniGames main, String subCmd) 
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
        
        if(!_main.get_combatGameHandler().isPlayerPlanInQueue(player) &&!_main.get_combatGameHandler().isPlayerInArena(player))
        {
        	if(_main.get_combatManager().hasPlayerDataCard(player))
            {
        		try {
        			new CombatGamePlaner(_main, player, (CombatDataCard)_main.get_combatManager().getPlayerDataCard(player));
				} 
        		catch (Exception e) 
        		{
        			_main.get_combatManager().clearPlayerDataCards();
        			new CombatGamePlaner(_main, player, new CombatDataCard(player));
					System.out.println("Error planer happend!");
				}
            	 
            	 return true;
            } 
        	new CombatGamePlaner(_main, player, new CombatDataCard(player));
        	return true;
        }
        player.sendMessage(ChatColor.RED + "You are in game or queue! Can't use this command!");
        
        
  
        
		
        return false;
    }
    
   
   
}