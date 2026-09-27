package imu.iGeneralStore.SubCmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


import imu.iGeneralStore.Interfaces.CommandInterface;
import imu.iGeneralStore.Main.Main;

public class subOpenCmd implements CommandInterface
{
	Main _main = null;

	String _subCmd = "";
	public subOpenCmd(Main main, String subCmd) 
	{
		_main = main;
		_subCmd=subCmd;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
       // Player player = (Player) sender;
	
       
        
       
        
		
        return false;
    }
    
   
   
}