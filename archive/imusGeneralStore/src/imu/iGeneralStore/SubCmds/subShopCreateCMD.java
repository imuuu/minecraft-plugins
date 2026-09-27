package imu.iGeneralStore.SubCmds;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.iGeneralStore.Interfaces.CommandInterface;
import imu.iGeneralStore.Main.Main;
import imu.iGeneralStore.Other.CmdData;
import net.md_5.bungee.api.ChatColor;

public class subShopCreateCMD implements CommandInterface
{
	Main _main = null;

	CmdData _data;
	public subShopCreateCMD(Main main, CmdData data) 
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
    	String shopName = StringUtils.join(Arrays.copyOfRange(args, 1, args.length)," ");
    	
    	_main.get_shopManager().createShop(shopName);
    	_main.get_shopManager().saveShop(shopName, true);
    	
    	player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
    			"&6New Shop has meen made! Named as "+shopName));
        
       
        
		
        return false;
    }
    
   
   
}