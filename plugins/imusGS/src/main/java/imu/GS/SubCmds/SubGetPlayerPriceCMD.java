package imu.GS.SubCmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import imu.GS.ENUMs.Cmd_add_options;
import imu.GS.Main.Main;
import imu.GS.Other.CmdData;
import imu.GS.ShopUtl.ItemPrice.PriceMaterial;
import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Other.Metods;

public class SubGetPlayerPriceCMD implements CommandInterface
{
	Main _main = null;

	CmdData _data;
	//HashMap<UUID, List<String>> _chosenValues;
	public SubGetPlayerPriceCMD(Main main, CmdData data) 
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
    	if(args.length < 1)
    	{
    		player.sendMessage(_data.get_syntaxText());
    		return false;
    	}   
    	Cmd_add_options option;
    	try 
    	{
			option = Cmd_add_options.valueOf(args[1].toLowerCase());
		} 
    	catch (Exception e) 
    	{
    		option = Cmd_add_options.hand;
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

    	GetMaterialDataPrice(stacks, player);
    	    	
        return false;
    }
    
    void GetMaterialDataPrice(ItemStack[] stacks,Player player)
    {
    	new BukkitRunnable() 
    	{
			@Override
			public void run() 
			{
				for(ItemStack stack : stacks)
				{
					if(stack == null) continue;
					PriceMaterial pm =_main.GetMaterialManager().GetPriceMaterialAndCheck(stack);
					double lastprice = Metods.Round(pm.GetPrice());
					player.sendMessage(Metods.msgC("&b"+stack.getType().name()+" &eprice is: &2"+lastprice +"&2$ &r"+ (pm.HasOverflow() ? "&b&k# &4Price drops after: &2"+pm.GetOverflow().get_softCap()+" &9items":"")));
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