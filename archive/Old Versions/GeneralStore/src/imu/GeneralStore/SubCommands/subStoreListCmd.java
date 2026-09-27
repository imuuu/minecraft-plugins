package imu.GeneralStore.SubCommands;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.GeneralStore.Interfaces.CommandInterface;
import imu.GeneralStore.Other.Shop;
import imu.GeneralStore.Other.ShopManager;
import imu.GeneralStore.main.Main;
import net.md_5.bungee.api.ChatColor;

public class subStoreListCmd implements CommandInterface
{
	Main _main = null;
	
	public subStoreListCmd(Main main)
	{
		_main = main;
	}

	@Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;
        ShopManager shopManager = _main.getShopManager();
        
        player.sendMessage(ChatColor.AQUA + "==Available shops==");
        HashMap<String, Shop> shops = shopManager.getShops();
        for(String name : shops.keySet())
        {
        	player.sendMessage(name);
        }
        
        return false;
    }
    
   
 
}