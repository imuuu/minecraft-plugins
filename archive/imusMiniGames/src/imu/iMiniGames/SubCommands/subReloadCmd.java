package imu.iMiniGames.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.iMiniGames.Interfaces.CommandInterface;
import imu.iMiniGames.Main.ImusMiniGames;
import net.md_5.bungee.api.ChatColor;

public class subReloadCmd implements CommandInterface
{
	ImusMiniGames _main = null;
	String _subCmd = "";
	public subReloadCmd(ImusMiniGames main, String subCmd) 
	{
		_main = main;
		_subCmd=subCmd;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;
       
       _main.get_spleefGameHandler().loadSettingConfig(true);
       _main.get_spleefManager().loadPotionsConfig();
       
       _main.get_combatManager().loadPotionsConfig();
       _main.get_combatGameHandler().loadSettingConfig(true);
        player.sendMessage(ChatColor.GOLD + "Configs reloaded");

    	
    	
  
        
		
        return false;
    }
    
   
   
}