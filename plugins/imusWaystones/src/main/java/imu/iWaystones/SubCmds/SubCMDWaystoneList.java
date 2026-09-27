package imu.iWaystones.SubCmds;


import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.iAPI.Interfaces.CommandInterface;
import imu.iWaystones.Main.ImusWaystones;
import imu.iWaystones.Managers.WaystoneManager;
import imu.iWaystones.Other.CmdData;

public class SubCMDWaystoneList implements CommandInterface
{
	CmdData _data;
	WaystoneManager _wManager;
	public SubCMDWaystoneList(CmdData data) 
	{
		_data = data;
		_wManager = ImusWaystones._instance.GetWaystoneManager();
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	
		
		_wManager.OpenWaystoneList((Player)sender);
        return false;
    }
    
   
	@Override
	public void FailedMsg(CommandSender arg0, String arg1) {
		
	}
    
   
   
}