package imu.GS.SubCmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.GS.Invs.UniquesINV;
import imu.GS.Main.Main;
import imu.GS.Other.CmdData;
import imu.iAPI.Interfaces.CommandInterface;

public class SubModifyUniqueCMD implements CommandInterface
{
	Main _main = null;

	CmdData _data;
	public SubModifyUniqueCMD(Main main, CmdData data) 
	{
		_main = main;
		_data = data;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;
    	
    	player.sendMessage("Modify inv");
    	new UniquesINV(_main, player).openThis();
        return false;
    }

	@Override
	public void FailedMsg(CommandSender arg0, String arg1) 
	{
		// TODO Auto-generated method stub
		
	}
    
   
   
}