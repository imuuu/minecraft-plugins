package imu.GeneralStore.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.GeneralStore.Interfaces.CommandInterface;
import imu.GeneralStore.Other.ShopManager;
import imu.GeneralStore.main.Main;
import net.md_5.bungee.api.ChatColor;

public class subStoreLockCmd implements CommandInterface
{
	Main _main = null;
	ShopManager _shopManager = null;
	Player _player = null;
	public subStoreLockCmd(Main main)
	{
		_main = main;
		_shopManager = _main.getShopManager();
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        _player = (Player) sender;
        switcher();
				
        return false;
    }
    
    void switcher()
    {
    	boolean isClosed = _shopManager.isShopsLocked();
    	if(isClosed)
    	{
    		_shopManager.setLockShops(false,true);
    		_player.sendMessage(ChatColor.DARK_PURPLE + "You have now opened all shops and all commands for normal players");
    	}else
    	{
    		_shopManager.setLockShops(true,true);
    		_player.sendMessage(ChatColor.DARK_PURPLE + "You have now closed all shops and this plugins commands from normal players");
    		
    	}
    }
    
   
}