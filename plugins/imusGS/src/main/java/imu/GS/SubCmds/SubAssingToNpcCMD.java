package imu.GS.SubCmds;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.GS.Main.Main;
import imu.GS.Other.CmdData;
import imu.iAPI.Interfaces.CommandInterface;

public class SubAssingToNpcCMD implements CommandInterface
{
	Main _main = null;

	CmdData _data;
	public SubAssingToNpcCMD(Main main, CmdData data) 
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
    		player.sendMessage("/"+_data.get_cmd_name()+"<scriptName> shop <shopname>");
    		return false;
        }
    	
    	//ImusAPI._metods.printArray("test", args);
        String nameShop = StringUtils.join(Arrays.copyOfRange(args, 2, args.length)," ");
        //System.out.println("shop name: "+nameShop);
        if(_main.get_shopManager().GetShop(nameShop) == null)
        {
        	player.sendMessage(ChatColor.RED + "Shopname not found!");
        	return false;
        }

        String script_name = args[1].toLowerCase();
        String full_scriptName=_main.GetDenizenSCreator().CreateAssignScript(script_name, nameShop);
        if(full_scriptName != null)
        {
        	player.sendMessage("Script name is: " + ChatColor.DARK_PURPLE+full_scriptName);

            player.sendMessage(ChatColor.GREEN+"Shop named "+_main.get_shopManager().GetShop(nameShop).GetDisplayName()+" has assign");
            player.sendMessage(ChatColor.GREEN+"Remember reload denizen scripts /ex reload!");
        }else
        {
        	player.sendMessage(ChatColor.RED + "Something went wrong, did you forgot restart server.. or there isnt denizen script folder?");
        }
        return false;
    }
    
  
    
	@Override
	public void FailedMsg(CommandSender arg0, String arg1) {
		
	}
    
   
   
}