package imu.iGeneralStore.SubCmds;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.iGeneralStore.Interfaces.CommandInterface;
import imu.iGeneralStore.Main.Main;
import imu.iGeneralStore.Other.CmdData;
import imu.iGeneralStore.ShopUtl.Shop;
import net.md_5.bungee.api.ChatColor;

public class subShopOpenCMD implements CommandInterface
{
	Main _main = null;

	CmdData _data;
	public subShopOpenCMD(Main main, CmdData data) 
	{
		_main = main;
		_data = data;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;
    	if(args.length < 3)
    	{
    		player.sendMessage(_data.get_syntaxText());
    		return false;
    	}
    	String shopName = StringUtils.join(Arrays.copyOfRange(args, 2, args.length)," ");
    	
    	Shop shop = _main.get_shopManager().getShop(shopName);
    	if(shop != null)
    	{
    		_main.get_shopManager().openShop(player, shopName);
    	}else
    	{
    		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cShop name not found by &9"+shopName));
    	}
    	
        
       
        
		
        return false;
    }
    
   
   
}