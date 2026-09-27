package imu.iMiniGames.Commands;


//Imports for the base command class.
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import imu.iMiniGames.Interfaces.CommandInterface;
import imu.iMiniGames.Main.ImusMiniGames;
 
public class ImgMgCmd implements CommandInterface
{
	ImusMiniGames _main = null;

	public ImgMgCmd(ImusMiniGames main)
	{
		_main = main;
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
    	
    	
    	if(args.length > 0)
    		return false;
        
    	_main.get_spleefManager().loadArenas();
    	
        return true;
    }
 
}