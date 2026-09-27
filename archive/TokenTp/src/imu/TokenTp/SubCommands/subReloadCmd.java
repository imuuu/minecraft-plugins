package imu.TokenTp.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.TokenTp.CustomItems.ItemTeleTokenBase;
import imu.TokenTp.CustomItems.ItemTeleTokenCard;
import imu.TokenTp.Interfaces.CommandInterface;
import imu.TokenTp.main.Main;
import net.md_5.bungee.api.ChatColor;

public class subReloadCmd implements CommandInterface
{
	Main _main = null;
	
	public subReloadCmd(Main main) 
	{
		_main = main;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;
    	player.sendMessage(ChatColor.AQUA + "Reloading configs");
    	_main.reloadConfigSetting();
		
    	
		
        return false;
    }
    
   
   
}