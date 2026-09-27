package imu.GS.SubCmds;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.GS.ENUMs.Cmd_add_options;
import imu.GS.Main.Main;
import imu.GS.Other.CmdData;
import imu.GS.ShopUtl.Shop;
import imu.GS.ShopUtl.ShopNormal;
import imu.GS.ShopUtl.ShopItems.ShopItemStockable;
import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;

public class SubAddStockableCMD implements CommandInterface
{
	Main _main = null;

	CmdData _data;
	public SubAddStockableCMD(Main main, CmdData data) 
	{
		_main = main;
		_data = data;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;
    	//ImusAPI._metods.printArray("add", args);
    	if(args.length < 3)
    	{
    		player.sendMessage(_data.get_syntaxText());
    		return false;
    	}
    	String shopName = StringUtils.join(Arrays.copyOfRange(args, 2, args.length)," ");
    	Shop shop = _main.get_shopManager().GetShop(shopName);
    	
    	if(shop == null)
    	{
    		player.sendMessage(ChatColor.RED + "Shop not found!");
			return false;
    	}
    	
    	if(!(shop instanceof ShopNormal))
    	{
    		player.sendMessage(ChatColor.RED + "Item can't be added this kind of shop!");
			return false;
    	}
    	
    	Cmd_add_options option;
    	try 
    	{
			option = Cmd_add_options.valueOf(args[1].toLowerCase());
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
    	
    	AddToShop(stacks, (ShopNormal)shop, player);
        return false;
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
	
	void AddToShop(ItemStack[] stacks, ShopNormal shop, Player player)
	{
		for(ItemStack stack : stacks)
    	{
    		if(stack == null || stack.getType() == Material.AIR) continue;
    		
    		int amount = stack.getAmount();
			ItemStack clone = stack.clone();
			clone.setAmount(1);
			shop.AddNewItem(new ShopItemStockable(_main, shop, clone , amount),true);    		
			player.sendMessage(Metods.msgC("&9"+ImusAPI._metods.GetItemDisplayName(stack)+" &ahas been added to &3"+shop.GetDisplayName()));
    	}
		
		_main.GetShopManagerSQL().SaveShopAsync(shop);

	}
	
	@Override
	public void FailedMsg(CommandSender arg0, String arg1) {
		
	}
    
   
   
}