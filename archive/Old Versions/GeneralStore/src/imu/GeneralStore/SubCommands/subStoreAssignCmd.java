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

public class subStoreAssignCmd implements CommandInterface
{
	Main _main = null;
	String _subStr = "";
	
	public subStoreAssignCmd(Main main, String subStr) 
	{
		_main = main;
		_subStr = subStr;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;
    	if(args.length < 3)
        {
    		player.sendMessage("/"+cmd.getName()+" "+_subStr+" <assingName(without _script)> <shopname>");
    		return false;
        }
    	
        ShopManager shopManager = _main.getShopManager();
        
        
        String nameShop = StringUtils.join(Arrays.copyOfRange(args, 2, args.length)," ");
        
        if(shopManager.getShop(nameShop) == null)
        {
        	player.sendMessage(ChatColor.RED + "Shopname not found!");
        	return false;
        }

        String script_name = args[1].toLowerCase();
        String full_scriptName=_main.getDenSC().createAssignScript(script_name, nameShop);
        if(full_scriptName != null)
        {
        	player.sendMessage("Script name is: " + ChatColor.DARK_PURPLE+full_scriptName);

            player.sendMessage(ChatColor.GREEN+"Shop has been assigned");
            player.sendMessage(ChatColor.GREEN+"Remember reload denizen scripts /ex reload!");
        }else
        {
        	player.sendMessage(ChatColor.RED + "Something went wrong, did you forgot restart server.. or there isnt denizen script folder?");
        }
        
        
        
		
        return false;
    }
   
}