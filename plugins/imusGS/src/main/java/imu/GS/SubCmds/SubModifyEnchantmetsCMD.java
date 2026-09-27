package imu.GS.SubCmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.GS.Invs.EnchantmentModifyINV;
import imu.GS.Main.Main;
import imu.GS.Other.CmdData;
import imu.iAPI.Interfaces.CommandInterface;

public class SubModifyEnchantmetsCMD implements CommandInterface
{
	Main _main = null;

	CmdData _data;
	public SubModifyEnchantmetsCMD(Main main, CmdData data) 
	{
		_main = main;
		_data = data;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;
    	
    	if(args.length < 1)
    	{
    		player.sendMessage(_data.get_syntaxText());
    		return false;
    	}
    	
    	new EnchantmentModifyINV(_main, player).openThis();
    	
        return false;
    }

	@Override
	public void FailedMsg(CommandSender arg0, String arg1) 
	{
		
	}
    
   
   
}