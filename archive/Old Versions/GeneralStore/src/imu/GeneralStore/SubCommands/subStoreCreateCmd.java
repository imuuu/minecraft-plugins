package imu.GeneralStore.SubCommands;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.GeneralStore.Interfaces.CommandInterface;
import imu.GeneralStore.Other.ShopManager;
import imu.GeneralStore.main.Main;

public class subStoreCreateCmd implements CommandInterface
{
	Main _main = null;
	
	public subStoreCreateCmd(Main main) 
	{
		_main = main;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;
    	if(args.length < 2)
        {
    		player.sendMessage("Remember give your shop a name");
    		return false;
        }
    	
        ShopManager shopManager = _main.getShopManager();
        
        
        String nameShop = StringUtils.join(Arrays.copyOfRange(args, 1, args.length)," ");
        nameShop = _main.getItemM().addColor(nameShop);
        if(shopManager.isExists(nameShop))
        {
        	player.sendMessage(ChatColor.RED + "Shop name already exists");
        	return false;
        }
        
        shopManager.addShop(nameShop, false);
        player.sendMessage(ChatColor.GREEN+"Shop has been made!");
        
        
		
        return false;
    }
   
}