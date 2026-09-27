package imu.GS.CMDs;


import org.bukkit.Material;
//Imports for the base command class.
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.GS.Main.Main;
import imu.GS.Other.MaterialSmartData;
import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Main.ImusAPI;
 
public class Cmd implements CommandInterface
{
	Main _main = null;

	public Cmd(Main main)
	{
		_main = main;
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {

    	Player player = (Player) sender;
    	
    	if(player.isOp() && ImusAPI._metods.doesStrArrayCointainStr(args, "kamat"))
    	{
    		for(int i = 0; i < player.getInventory().getContents().length-6; ++i)
    		{
    			player.getInventory().setItem(i, new ItemStack(Material.values()[i],64));
    		}
    	}
    	
    	if(player.isOp() && ImusAPI._metods.doesStrArrayCointainStr(args, "smart"))
    	{
    		MaterialSmartData data = new MaterialSmartData(player.getInventory().getItemInMainHand().getType(), 1);
    		data.Calculate();
    		System.out.println("getprice: "+data.GetPrice());
    	}
    	
    	if(args.length > 0)
    		return false;
      
    	
    	
        return true;
    }
    
	@Override
	public void FailedMsg(CommandSender arg0, String arg1) 
	{
		
	}
 
}