package imu.TokenTp.SubCommands;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.TokenTp.CustomItems.ItemTeleTokenCard;
import imu.TokenTp.Enums.TeleTokenType;
import imu.TokenTp.Enums.TokenType;
import imu.TokenTp.Interfaces.CommandInterface;
import imu.TokenTp.main.Main;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;

public class subSpawnCardCmd implements CommandInterface
{
	Main _main = null;
	Metods _itemM = null;
	
	String[] codes = {"pos", "bind","player"};
	String _sub_str = "";
	public subSpawnCardCmd(Main main, String subStr) 
	{
		_main = main;
		_itemM = ImusAPI._metods;
		_sub_str = subStr;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;

    	if(args.length < 2)
    	{
    		player.sendMessage("/"+cmd.getName());
    	}
		ItemTeleTokenCard card = null;
    	if(args.length > 2 && _itemM.doesStrArrayCointainStr(args, codes[0]))
    	{
    		card = new ItemTeleTokenCard(_main);
    		card.setAllData("Not set", "Not set", player.getLocation(), TeleTokenType.CARD, TokenType.TOKEN_TO_LOCATION);
    	}
    	else if(args.length > 2 && _itemM.doesStrArrayCointainStr(args, codes[1]))
    	{
    		card = new ItemTeleTokenCard(_main);
    		card.setAllData("Not set", "Not set", null, TeleTokenType.CARD, TokenType.TOKEN_TO_LOCATION);
    	}
    	else if(args.length > 2 && _itemM.doesStrArrayCointainStr(args, codes[2]))
    	{
    		card = new ItemTeleTokenCard(_main);
    		card.setAllData("Not set", "Not set", new Location(player.getWorld(),0,0,0), TeleTokenType.CARD, TokenType.TOKEN_TO_PLAYER);
    	}else
    	{
    		player.sendMessage(ChatColor.GOLD+ "What kind of type you want to spawn?");
    		
    		HashMap<String, String> hMap = new HashMap<String, String>();
    		
    		hMap.put("This_position", "/"+cmd.getName() +" "+ _sub_str+ " " +codes[0]);
    		hMap.put("Bind", "/"+cmd.getName() +" "+ _sub_str+ " " +codes[1]);
    		hMap.put("PlayerTp", "/"+cmd.getName() +" "+ _sub_str+ " " +codes[2]);
    		
    		_itemM.SendMessageCommands(player, "",hMap, "","/");
    	}
    	
    	if(card != null)
    	{
    		player.sendMessage(ChatColor.DARK_PURPLE + "Here is your card!");
    		player.getInventory().addItem(card);
    	}
    	
        return false;
    }
    
   
   
}