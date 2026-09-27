package imu.GS.SubCmds;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import imu.GS.ENUMs.Cmd_add_options;
import imu.GS.Main.Main;
import imu.GS.Other.CmdData;
import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Other.Metods;

public class SubUnSetMaterialOverflowCMD implements CommandInterface
{
	Main _main = null;

	CmdData _data;
	//HashMap<UUID, List<String>> _chosenValues;
	public SubUnSetMaterialOverflowCMD(Main main, CmdData data) 
	{
		_main = main;
		_data = data;
		//_chosenValues = new HashMap<>();
		_data.set_syntaxText("/"+_data.get_cmd_name() + " {hand/hotbar/inv}");
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;
    	if(args.length != 4)
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

    	SendMaterialDataAsync(stacks,player);
    	
        return false;
    }
    
    void SendMaterialDataAsync(ItemStack[] stacks,Player player)
    {
    	new BukkitRunnable() 
    	{
			@Override
			public void run() 
			{
				LinkedList<Material> overflows = new LinkedList<>();
				for(ItemStack stack : stacks)
				{
					if(stack == null) continue;
					
					if(!_main.GetMaterialManager().GetPriceMaterial(stack.getType()).HasOverflow()) continue;
					
					player.sendMessage(Metods.msgC("&e"+"Material overflow &4unset &eto &b"+stack.getType()));
					overflows.add(stack.getType());
				}
				
				_main.GetMaterialManager().RemoveMaterialOverflow(overflows);
				
				
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