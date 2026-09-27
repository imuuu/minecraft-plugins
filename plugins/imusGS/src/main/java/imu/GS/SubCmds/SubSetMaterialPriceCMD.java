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
import imu.GS.ShopUtl.ItemPrice.PriceMaterial;
import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;

public class SubSetMaterialPriceCMD implements CommandInterface
{
	Main _main = null;

	CmdData _data;
	//HashMap<UUID, List<String>> _chosenValues;
	public SubSetMaterialPriceCMD(Main main, CmdData data) 
	{
		_main = main;
		_data = data;
		//_chosenValues = new HashMap<>();
		_data.set_syntaxText("/"+_data.get_cmd_name() + " {hand/hotbar/inv} {price}");
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;
    	if(args.length != 5)
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
    	
    	if(!ImusAPI._metods.isDigit(args[4])) return false;
    	
    	double price = Double.parseDouble(args[4]);
    	SendMaterialDataAsync(stacks, price,player);
    	
    	
        return false;
    }
    
    void SendMaterialDataAsync(ItemStack[] stacks, double price, Player player)
    {
    	new BukkitRunnable() 
    	{
			@Override
			public void run() 
			{
				LinkedList<Material> mats = new LinkedList<>();
				for(ItemStack stack : stacks)
				{
					if(stack == null) continue;
					PriceMaterial pm = _main.GetMaterialManager().GetPriceMaterial(stack.getType());
					if(pm.HasSmartData())
					{
						player.sendMessage(Metods.msgC("&b"+stack.getType().name()+" &4Price didnt set because of smart price enabled! If you wanna set price remove smart price via cmd"));
						continue;
					}
					
					double lastprice = pm.GetPrice();
					mats.add(stack.getType());
					
					double increase = Metods.Round((1.0-lastprice/price)*100.00);
					player.sendMessage(Metods.msgC("&b"+stack.getType().name()+" &eprice set to: &2 "+price+" &7From "+lastprice+ " &9( "+(lastprice <= price ?  "&2" : "&c")+increase+"&e%&9 )"));
				}
				_main.GetMaterialManager().SaveMaterialPrice(mats, price);
				
				
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