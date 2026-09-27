package imu.GS.SubCmds;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.GS.Main.Main;
import imu.GS.Other.CmdData;
import imu.GS.ShopUtl.Shop;
import imu.iAPI.Interfaces.CommandInterface;

public class SubShopOpenCMD implements CommandInterface
{
	Main _main = null;

	CmdData _data;
	public SubShopOpenCMD(Main main, CmdData data) 
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
    	
    	Shop shop = _main.get_shopManager().GetShop(shopName);
    	if(shop != null)
    	{
    		//player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTrying to open &9"+shopName));
    		//shop.AddNewCustomer(player);
    		shop.Open(player);
    	}else
    	{
    		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cShop name not found by &9"+shopName));
    	}
    	
        
       
        
		
        return false;
    }

	@Override
	public void FailedMsg(CommandSender arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
    
   
   
}