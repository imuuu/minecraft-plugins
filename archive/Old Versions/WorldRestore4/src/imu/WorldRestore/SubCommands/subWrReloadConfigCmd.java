package imu.WorldRestore.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import imu.WorldRestore.Interfaces.CommandInterface;
import imu.WorldRestore.main.Main;
import net.md_5.bungee.api.ChatColor;

public class subWrReloadConfigCmd implements CommandInterface
{
	Main _main = null;
	public subWrReloadConfigCmd(Main main) 
	{
		_main = main;
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	
        _main.UpdateSettingConfig();
        sender.sendMessage(ChatColor.DARK_PURPLE + "Config reloaded");
        return false;
    }
    
   
   
}