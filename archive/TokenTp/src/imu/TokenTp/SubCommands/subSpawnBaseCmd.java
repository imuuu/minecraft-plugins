package imu.TokenTp.SubCommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.TokenTp.CustomItems.ItemTeleTokenBase;
import imu.TokenTp.Interfaces.CommandInterface;
import imu.TokenTp.main.Main;

public class subSpawnBaseCmd implements CommandInterface
{
	Main _main = null;
	String[] codes = {"pos", "bind","player"};
	String _sub_str = "";
	public subSpawnBaseCmd(Main main) 
	{
		_main = main;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;
    	

    	if(args.length < 2)
    	{
    		player.sendMessage("/"+cmd.getName());
    	}
		ItemTeleTokenBase card = new ItemTeleTokenBase(_main);
		
		player.sendMessage(ChatColor.DARK_PURPLE + "Here is your base!");
		player.getInventory().addItem(card);

        return false;
    }
    
   
   
}