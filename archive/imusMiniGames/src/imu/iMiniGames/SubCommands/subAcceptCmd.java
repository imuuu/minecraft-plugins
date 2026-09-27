package imu.iMiniGames.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import imu.iMiniGames.Interfaces.CommandInterface;
import imu.iMiniGames.Main.ImusMiniGames;
import net.md_5.bungee.api.ChatColor;

public class subAcceptCmd implements CommandInterface
{
	ImusMiniGames _main = null;

	String _subCmd = "";
	Inventory _tempInv;
	public subAcceptCmd(ImusMiniGames main, String subCmd) 
	{
		_main = main;
		_subCmd=subCmd;
		_tempInv = _main.getServer().createInventory(null, 9, "temp");
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;
	
        if(_main.isPlayerBlocked(player))
        {
        	_main.sendBlockedmsg(player);
        	return false;
        }
        
        player.sendMessage(" ");
        if(_main.get_itemM().doesStrArrayCointainStr(args, "spleef"))
        {
        	if(_main.get_itemM().doesStrArrayCointainStr(args, "confirm:yes"))
            {
            	player.sendMessage(" ");
            	
            	if(!_main.get_spleefGameHandler().requestAnwser(player.getUniqueId(), true))
            	{
            		player.sendMessage("You have already answered or your request has expired");
            	}else
            	{
            		player.sendMessage(ChatColor.DARK_GREEN + "You have accept match");
            	}
            }
            if(_main.get_itemM().doesStrArrayCointainStr(args, "confirm:no"))
            {
            	
            	if(!_main.get_spleefGameHandler().requestAnwser(player.getUniqueId(), false))
            	{
            		player.sendMessage("You have already answered or your request has expired");
            	}else
            	{
            		player.sendMessage(ChatColor.RED + "You have denied match");
            	}
            }
        }
        if(_main.get_itemM().doesStrArrayCointainStr(args, "combat"))
        {
        	if(_main.get_itemM().doesStrArrayCointainStr(args, "confirm:yes"))
            {
            	player.sendMessage(" ");
            	
            	if(!_main.get_combatGameHandler().requestAnwser(player.getUniqueId(), true))
            	{
            		player.sendMessage("You have already answered or your request has expired");
            	}else
            	{
            		player.sendMessage(ChatColor.DARK_GREEN + "You have accept match");
            	}
            }
            if(_main.get_itemM().doesStrArrayCointainStr(args, "confirm:no"))
            {
            	
            	if(!_main.get_combatGameHandler().requestAnwser(player.getUniqueId(), false))
            	{
            		player.sendMessage("You have already answered or your request has expired");
            	}else
            	{
            		player.sendMessage(ChatColor.RED + "You have denied match");
            	}
            }
        }
        player.openInventory(_tempInv);
        player.closeInventory();
       
        
		
        return false;
    }
    
   
   
}