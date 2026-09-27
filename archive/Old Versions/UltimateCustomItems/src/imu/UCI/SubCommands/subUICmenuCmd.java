package imu.UCI.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.UCI.Interfaces.CommandInterface;
import imu.UCI.main.Main;

public class subUICmenuCmd implements CommandInterface
{
	Main _main = null;
	public subUICmenuCmd(Main main) 
	{
		_main = main;

	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;
    	_main.getMainMenuManager().openNewInv(player);
        return false;
    }
    
   
    
   
   
}