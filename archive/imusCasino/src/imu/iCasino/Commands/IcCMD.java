package imu.iCasino.Commands;


//Imports for the base command class.
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import imu.iAPI.Interfaces.CommandInterface;
import imu.iCasino.Main.CasinoMain;

public class IcCMD implements CommandInterface
{
	CasinoMain _main = null;

	public IcCMD(CasinoMain main)
	{
		_main = main;
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
    	if(args.length > 0)
    		return false;
        
    	
    	
        return true;
    }
	@Override
	public void FailedMsg(CommandSender arg0, String arg1) 
	{

	}
 
}