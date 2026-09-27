package imu.GS.SubCmds;

import java.util.LinkedList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import imu.GS.ENUMs.Cmd_add_options;
import imu.GS.Main.Main;
import imu.GS.Other.CmdData;
import imu.GS.Other.MaterialOverflow;
import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;

public class SubSetMaterialOverflowCMD implements CommandInterface
{
	Main _main = null;

	CmdData _data;
	//HashMap<UUID, List<String>> _chosenValues;
	public SubSetMaterialOverflowCMD(Main main, CmdData data) 
	{
		_main = main;
		_data = data;
		//_chosenValues = new HashMap<>();
		_data.set_syntaxText("/"+_data.get_cmd_name() + " {hand/hotbar/inv} {softcap} {minPrice} {dropPercent} ");
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;
    	if(args.length != 7)
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
    	
    	if(!ImusAPI._metods.isDigit(args[4]) || !ImusAPI._metods.isDigit(args[5]) || !ImusAPI._metods.isDigit(args[6])) return false;
    	
    	int softcap = (int)Double.parseDouble(args[4]);
    	double minprice = Double.parseDouble(args[5]);
    	double dropPercent = 1.0 - Double.parseDouble(args[6]) / 100.00;
    	

    	SendMaterialDataAsync(stacks, softcap, dropPercent, minprice, player);
    	
        return false;
    }
    
    void SendMaterialDataAsync(ItemStack[] stacks, int softcap, double dropPercent, double minPrice, Player player)
    {
    	new BukkitRunnable() 
    	{
			@Override
			public void run() 
			{
				LinkedList<MaterialOverflow> overflows = new LinkedList<>();
				for(ItemStack stack : stacks)
				{
					if(stack == null) continue;
					
					overflows.add(new MaterialOverflow(stack.getType(), softcap, dropPercent, 1, minPrice));
				}
				
				_main.GetMaterialManager().SaveMaterialOverflowAsync(overflows);
				player.sendMessage(Metods.msgC("&b"+"Material overflow set to &2"+overflows.size()+" &ecap: "+softcap +  " &cminprice: "+minPrice+" &3drop percent each sell: "+(Metods.Round((1.0-dropPercent) * 100.00))));
				
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