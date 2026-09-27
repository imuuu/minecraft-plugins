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

public class SubModifyShopCMD implements CommandInterface
{
	Main _main = null;

	CmdData _data;
	public SubModifyShopCMD(Main main, CmdData data) 
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
    	if(shop == null)
    	{
    		player.sendMessage(ChatColor.RED + "Couldnt find the shop named: "+shopName);
    		return false;
    	}
    	shop.OpenModify(player);
        return false;
    }

	@Override
	public void FailedMsg(CommandSender arg0, String arg1) 
	{
		// TODO Auto-generated method stub
		
	}
    
   
   
}