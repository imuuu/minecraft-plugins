package imu.WorldRestore.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.WorldRestore.Interfaces.CommandInterface;
import imu.WorldRestore.Other.ItemMetods;
import imu.WorldRestore.main.Main;
import net.md_5.bungee.api.ChatColor;

public class subWrClearChunkDataCmd implements CommandInterface
{
	Main _main = null;
	ItemMetods _itemM = null;
	public subWrClearChunkDataCmd(Main main) 
	{
		_main = main;
		_itemM = main.getItemM();
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;
		if(_itemM.doesStrArrayCointainStr(args, "true"))
    	{
    		player.sendMessage(ChatColor.GREEN+"Chunks data has been removed!");
    		_main.getChunkFileHandler().clearChunksFile();
        	
    	}else if(_itemM.doesStrArrayCointainStr(args, "false"))
    	{
    		player.sendMessage(ChatColor.GREEN+"Remove process canceled");
        	
    	}else
    	{
    		player.sendMessage(ChatColor.GOLD+ "Are you sure you wanna remove all visited/flagged chunks data?");
    		_itemM.sendYesNoConfirm(player, "/"+cmd.getName() + " remove chunks data " + "true", "/"+cmd.getName()+" remove chunks data "+"false");
    	}
        return false;
    }
    
  
   
}