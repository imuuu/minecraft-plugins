package imu.GS.SubCmds;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.GS.Main.Main;
import imu.GS.Other.CmdData;
import imu.iAPI.Interfaces.CommandInterface;

public class SubShopCreateCMD implements CommandInterface
{
	Main _main = null;

	CmdData _data;
	public SubShopCreateCMD(Main main, CmdData data) 
	{
		_main = main;
		_data = data;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;
    	
    	if(args.length < 2)
    	{
    		player.sendMessage(_data.get_syntaxText());
    		return false;
    	}
    	String shopName = StringUtils.join(Arrays.copyOfRange(args, 2, args.length)," ");
    	
    	_main.get_shopManager().CreateNewShop(shopName);
    	//_main.get_shopManager().SaveShop(shopName, true);
    	
    	player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
    			"&6New Shop has meen made! Named as &9"+shopName));
        

        return false;
    }

	@Override
	public void FailedMsg(CommandSender arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
    
   
   
}