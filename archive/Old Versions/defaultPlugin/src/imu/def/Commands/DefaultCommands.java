package imu.def.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;



public class DefaultCommands implements CommandExecutor
{

	public String cmd1 ="cord";
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] arg) 
	{
		if(sender instanceof Player)
		{
			if(cmd.getName().equalsIgnoreCase(cmd1))
			{
				sender.sendMessage(((Player) sender).getLocation().toString());
			}
			
		}else
		{
			sender.sendMessage(ChatColor.RED +"Only player can use this command!");
			return true;
		}
		
		return false;
	}

}
