package imu.GeneralStore.SubCommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.GeneralStore.Interfaces.CommandInterface;
import imu.GeneralStore.main.Main;

public class subStoreReloadCmd implements CommandInterface
{
	Main _main = null;

	Player player;
	
	public subStoreReloadCmd(Main main) 
	{
		_main = main;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        player = (Player) sender;
        

        reload();
		
        return false;
    }
    
    void reload()
    {
    	player.sendMessage(ChatColor.DARK_GREEN + "RELOAD");
    	_main.getShopManager().closeShopsInvs();
    	_main.ConfigsSetup();
    	if(_main.isEnableSmartPrices())
    	{
    		player.sendMessage(ChatColor.DARK_PURPLE + "Clearing smartprices");
    		_main.getShopManager().clearSmartPrices();
    	}
    	
    	if(_main.isLoadSmartPricesUpFront())
    	{
    		player.sendMessage(ChatColor.DARK_PURPLE + "Start calculating smartprices...");
    		_main.getShopManager().calculateAllSmart();
    		
    		_main.getItemM().sendMessageLater(player, _main.getShopManager(), ChatColor.GREEN + "Calculations are ready!");
    	}
    }
    
    
   
}