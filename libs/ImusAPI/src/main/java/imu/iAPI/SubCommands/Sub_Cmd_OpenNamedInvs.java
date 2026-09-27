package imu.iAPI.SubCommands;


import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.iAPI.CmdUtil.CmdData;
import imu.iAPI.FastInventory.Manager_FastInventories;
import imu.iAPI.Interfaces.CommandInterface;


public class Sub_Cmd_OpenNamedInvs implements CommandInterface
{
	CmdData _data;

	public Sub_Cmd_OpenNamedInvs(CmdData data) 
	{
		_data = data;
		//_wManager = ImusWaystones._instance.GetWaystoneManager();
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	if(args.length <= 1) return false;
    	
		String invName = StringUtils.join(Arrays.copyOfRange(args, 1, args.length)," ");

		//if(invName == null || invName.isBlank()) return false;
		if(invName == null || invName.isEmpty()) return false;

		Manager_FastInventories.Instance.OpenFastInv(invName, (Player)sender);
		
		
        return true;
    }
    
   
	@Override
	public void FailedMsg(CommandSender arg0, String arg1) {
		
	}
    
   
   
}