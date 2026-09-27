package imu.GS.SubCmds;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import imu.GS.ENUMs.Cmd_add_options;
import imu.GS.Main.Main;
import imu.GS.Other.CmdData;
import imu.GS.Other.MaterialSmartData;
import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;

public class SubSetUnsetMaterialSmartPriceCMD implements CommandInterface
{
	Main _main = null;

	CmdData _data;
	//HashMap<UUID, List<String>> _chosenValues;
	boolean _set;
	public SubSetUnsetMaterialSmartPriceCMD(Main main, CmdData data, boolean set) 
	{
		_main = main;
		_data = data;
		_set = set;
		//_chosenValues = new HashMap<>();
		if(_set)
		{
			_data.set_syntaxText("/"+_data.get_cmd_name() + " {hand/hotbar/inv} {multiplier}");
		}
		
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;
    	if((args.length != 5 && _set) || (args.length != 4 && !_set))
    	{
    		player.sendMessage(_data.get_syntaxText());
    		return false;
    	}   
    	Cmd_add_options option;
    	try 
    	{
			option = Cmd_add_options.valueOf(args[3].toLowerCase());
		} 
    	catch (Exception e) 
    	{
			return false;
		}
    	
    	ItemStack[] stacks = null;
    	switch (option) 
    	{
		case hand:
			stacks = Hand(player);
			break;
		case hotbar:
			stacks = Hotbar(player);
			break;
		case inventory:
			stacks = Inv(player);
			break;
		
		}
    	if(_set)
    	{
    		if(!ImusAPI._metods.isDigit(args[4])) return false;
        	
        	double multiplier = Double.parseDouble(args[4]);
        	
        	SendMaterialDataAsync(stacks, multiplier, player);
    	}
    	else
    	{
    		SendMaterialDataAsync(stacks, -1.0, player);
    	}
    	
    	
        return false;
    }
    
    void SendMaterialDataAsync(ItemStack[] stacks, double multiplier, Player player)
    {
    	new BukkitRunnable() 
    	{
			@Override
			public void run() 
			{
				HashSet<Material> mats = new HashSet<>();
				for(ItemStack stack : stacks)
				{
					if(stack == null) continue;
					
					MaterialSmartData data = new MaterialSmartData(stack.getType(), multiplier);
					
					if(!data.Calculate() && _set)
					{
						player.sendMessage(Metods.msgC("&b"+"Material "+stack.getType()+" &4didn't had recipe"));
						continue;
					}
					
					mats.add(stack.getType());
				}
				
				_main.GetMaterialManager().SaveMaterialSmartDataAsync(mats, multiplier);
				if(_set)
				{
					player.sendMessage(Metods.msgC("&b"+"Smart price calculation set to &2"+mats.size()+" &9materials"));
				}else
				{
					player.sendMessage(Metods.msgC("&b"+"Smart price has been &cremoved &bfrom &2"+mats.size()+" &9materials"));
				}
				
				
			}
		}.runTaskAsynchronously(_main);
    }
    
    ItemStack[] Hand(Player player){return new ItemStack[] {player.getInventory().getItemInMainHand()};}

    	
	
    ItemStack[] Hotbar(Player player)
	{
		ItemStack[] stacks = new ItemStack[9];
		for(int i = 0; i < stacks.length; i++) {stacks[i] = player.getInventory().getItem(i);}
		return stacks;
	}
	
    ItemStack[] Inv(Player player)
	{
    	ItemStack[] stacks = new ItemStack[36];
		for(int i = 0; i < stacks.length; i++) {stacks[i] = player.getInventory().getItem(i);}
		return stacks;
	}
	
	@Override
	public void FailedMsg(CommandSender arg0, String arg1) {
		
	}
    
   
   
}