package imu.TokenTp.SubCommands;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.TokenTp.Interfaces.CommandInterface;
import imu.TokenTp.main.Main;
import net.md_5.bungee.api.ChatColor;

public class subSetDescCmd implements CommandInterface
{
	Main _main = null;

	public subSetDescCmd(Main main) 
	{
		_main = main;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;

    	if(args.length < 2)
    	{
    		player.sendMessage("Something wrong?");
    	}
    	
    	ItemStack stack = player.getInventory().getItemInMainHand();
    	if(_main.getTeleTokenManager().isTeleToken(stack))
    	{
    		player.sendMessage(ChatColor.GOLD + "Description has been set");
    		String desc = StringUtils.join(Arrays.copyOfRange(args, 2, args.length)," ");
    		ItemStack copy = _main.getTeleTokenManager().modifyTeleToken(stack, desc, null);
    		
    		stack.setAmount(0);
    		player.getInventory().setItemInMainHand(copy);
   		
    	}
		
    	
        return false;
    }
    
   
   
}