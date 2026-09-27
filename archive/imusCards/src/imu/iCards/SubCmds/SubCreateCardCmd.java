package imu.iCards.SubCmds;


import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.iAPI.CmdUtil.CmdData;
import imu.iAPI.Interfaces.CommandInterface;
import imu.iCards.Managers.CardManager;

public class SubCreateCardCmd implements CommandInterface
{
	CmdData _data;

	public SubCreateCardCmd(CmdData data) 
	{
		_data = data;
		//_wManager = ImusWaystones._instance.GetWaystoneManager();
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	CardManager.Instance.OpenCreateCardInv((Player)sender);


		
		
        return false;
    }
    
   
	@Override
	public void FailedMsg(CommandSender arg0, String arg1) {
		
	}
    
   
   
}