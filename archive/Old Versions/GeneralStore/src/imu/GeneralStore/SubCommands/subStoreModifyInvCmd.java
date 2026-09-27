package imu.GeneralStore.SubCommands;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.GeneralStore.Interfaces.CommandInterface;
import imu.GeneralStore.Other.Shop;
import imu.GeneralStore.Other.ShopManager;
import imu.GeneralStore.main.Main;
import net.md_5.bungee.api.ChatColor;

public class subStoreModifyInvCmd implements CommandInterface
{
	Main _main = null;

	Player player;
	ShopManager shopManager = null;
	
	public subStoreModifyInvCmd(Main main)
	{
		_main = main;
		shopManager = _main.getShopManager();
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        player = (Player) sender;
        
        //shopManager.openUniqueINV(player);
        //player.sendMessage("/gs uniques");
        String nameShop = StringUtils.join(Arrays.copyOfRange(args, 1, args.length)," ");

        if(shopManager.isExists(nameShop))
        {
        	Shop shop = shopManager.getShop(nameShop);
        	_main.getShopModManager().openModShopInv(player, shop);
        }
        else
        {
        	player.sendMessage(ChatColor.RED + "Shop no found!");
        }
		
        return false;
    }
    
   
    
    
   
}