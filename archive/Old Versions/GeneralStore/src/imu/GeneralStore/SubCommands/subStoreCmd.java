package imu.GeneralStore.SubCommands;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.GeneralStore.Interfaces.CommandInterface;
import imu.GeneralStore.main.Main;

public class subStoreCmd implements CommandInterface
{
	Main _main = null;
	public subStoreCmd(Main main) 
	{
		_main = main;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;

        if(args.length == 1)
        {
        	newInvenotry(player, "General Store");
        }
        else
        {
        	 String nameShop = StringUtils.join(Arrays.copyOfRange(args, 1, args.length)," ");
        	 newInvenotry(player, nameShop);
        }
				
        return false;
    }
    
    public void newInvenotry(Player player,String shopName)
    {
    	_main.getShopManager().openShop(player, shopName);
    	
    }
 
}