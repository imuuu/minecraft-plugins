package imu.GS.SubCmds;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import imu.GS.ENUMs.Cmd_add_options;
import imu.GS.ENUMs.TagSubCmds;
import imu.GS.Main.Main;
import imu.GS.Other.CmdData;
import imu.GS.ShopUtl.Shop;
import imu.GS.ShopUtl.ShopItemBase;
import imu.GS.ShopUtl.ShopNormal;
import imu.GS.ShopUtl.ItemPrice.PriceOwn;
import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;

public class SubTagMaterialCMD implements CommandInterface
{
	Main _main = null;

	CmdData _data;
	
	enum Choise
	{
		materials,
		shopitems
	}
	public SubTagMaterialCMD(Main main, CmdData data) 
	{
		_main = main;
		_data = data;
		//_chosenValues = new HashMap<>();
		_data.set_syntaxText("/"+_data.get_cmd_name() + "{"+ImusAPI._metods.CombineArrayToOneString(TagSubCmds.values(), "/")+"} {(!setPrice,!increasePrice) hand/hotbar/inv} {tag}");
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;
    	//ImusAPI._metods.printArray("tag", args);
    	if(args.length < 5)
    	{  		
    		if(!(args.length > 2 && args[2].equalsIgnoreCase("remove_all_tags")))
    		{
    			player.sendMessage(_data.get_syntaxText());
        		return false;
    		}
    		
    	}  
    	
    	Cmd_add_options option = null;
    	TagSubCmds subCMD = null;
    	Choise choise = null;
    	try {
    		choise = Choise.valueOf(args[1].toLowerCase());
		} catch (Exception e) {return false;}
    	try 
    	{
			option = Cmd_add_options.valueOf(args[3].toLowerCase());
			
		} 
    	catch (Exception e){}
    	
    	ItemStack[] stacks = null;
    	if(option != null)
    	{
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
        	   	
    	}
    	
    	double number = -1;
    	try 
    	{
    		subCMD = TagSubCmds.valueOf(args[2].toLowerCase());
    		if(ImusAPI._metods.isDigit(args[4]))
    		{
    			//System.out.println("it is digit");
    			number = Double.valueOf(args[4]);
    		}
    		
		} 
    	catch (Exception e) {}
    	
    	if(args.length < 5 && choise == Choise.shopitems)
    	{
    		player.sendMessage(ChatColor.RED + "Last number should be digit");
    		return false;
    	}
    	switch (subCMD) 
    	{
		case add:
			if(choise != Choise.materials) return false;
			AddTags(player, stacks, args[4]);
			return false;		
		case remove:
			if(choise != Choise.materials) return false;
			RemoveTag(player, stacks, args[4]);
    		return false;
		case remove_all_tags:
			if(choise == Choise.materials) RemoveAll(player, stacks);
    		return false;
		case set_price:
			if(choise == Choise.materials) SetMaterialPriceAsync(player,number, args[3]);
			if(choise == Choise.shopitems) SetShopItemSetPrice(player,number, args[3]);
			break;
		case increase_price:
			if(choise == Choise.materials) SetMaterialInceasePriceAsync(player, number, args[3]);
			if(choise == Choise.shopitems) SetShopItemIncreasePrice(player, number, args[3]);
			break;
		
		}
    	

        return false;
    }
    
    
    void SetMaterialPriceAsync(Player player,double price, String tag)
    {
    	if( price < 0) 
    	{
    		player.sendMessage(Metods.msgC("&c Couldnt set price with &2"+price));
    		return;
    	}
    	new BukkitRunnable() 
    	{
			@Override
			public void run() 
			{
				_main.GetMaterialManager().SaveMaterialPrice(_main.GetTagManager().GetAllMaterialsWithTag(tag), price);
				player.sendMessage(Metods.msgC("&2All materials with tag '&b"+tag.toLowerCase()+"&2' the price is set to: &a"+price));
				
				
			}
		}.runTaskAsynchronously(_main);
    }
    
    void SetShopItemSetPrice(Player player, double price, String tag)
    {
    	if( price < 0) 
    	{
    		player.sendMessage(Metods.msgC("&c Couldnt set price with &2"+price));
    		return;
    	}
    	int count = 0;
    	for(Shop s : _main.get_shopManager().GetShops())
    	{
    		if(!(s instanceof ShopNormal)) continue;
    		
    		ShopNormal shop = (ShopNormal)s;
    		boolean closeShop = false;
    		for(ShopItemBase[] pages : shop.get_items())
    		{
    			for(ShopItemBase sib : pages)
    			{
    				if(sib != null && sib.HasTag(tag))
    				{
    					if(sib.GetItemPrice() instanceof PriceOwn)
    					{
    						sib.SetItemPrice(sib.GetItemPrice().SetPrice(price));
    						count++;
    					}
    					closeShop = true;
    				}
    			}
    		}
    		if(closeShop) shop.RemoveCustomerALL();
    		_main.GetShopManagerSQL().SaveShopAsync(shop);
    	}
    	
    	player.sendMessage(Metods.msgC("&2All shopitems&7(&e"+count+"&7)&2 with tag '&b"+tag.toLowerCase()+"&2' the price is set to: &a"+price));
    }
    
    void SetShopItemIncreasePrice(Player player, double multiplier, String tag)
    {
    	
    	if( multiplier < 0) 
    	{
    		player.sendMessage(Metods.msgC("&c Couldnt increase with negative multiplier => &2"+multiplier));
    		return;
    	}
    	int count = 0;
    	for(Shop s : _main.get_shopManager().GetShops())
    	{
    		if(!(s instanceof ShopNormal)) continue;
    		
    		ShopNormal shop = (ShopNormal)s;
    		
    		boolean closeShop = false;
    		for(ShopItemBase[] pages : shop.get_items())
    		{
    			for(ShopItemBase sib : pages)
    			{
    				if(sib != null && sib.HasTag(tag))
    				{
    					if(sib.GetItemPrice() instanceof PriceOwn)
    					{
    						double price = sib.GetItemPrice().GetPrice() * multiplier;
    						sib.SetItemPrice(sib.GetItemPrice().SetPrice(price));
    						count++;
    					}
    					closeShop = true;
    				}
    			}
    		}
    		if(closeShop) shop.RemoveCustomerALL();
    		_main.GetShopManagerSQL().SaveShopAsync(shop);
    	}
    	
    	player.sendMessage(Metods.msgC("&2All shopitems&7(&e"+count+"&7)&2 with tag '&b"+tag.toLowerCase()+"&2' the price is increased by: &b"+multiplier));
    }
    
    void SetMaterialInceasePriceAsync(Player player,double multiplier, String tag)
    {
    	if( multiplier < 0) 
    	{
    		player.sendMessage(Metods.msgC("&c Couldnt increase with negative multiplier => &2"+multiplier));
    		return;
    	}
    	new BukkitRunnable() 
    	{
			@Override
			public void run() 
			{
				for(Material mat : _main.GetTagManager().GetAllMaterialsWithTag(tag))
				{
					double price = _main.GetMaterialManager().GetPriceMaterial(mat).GetPrice();
					_main.GetMaterialManager().SaveMaterialPrice(new ArrayList<Material>(Arrays.asList(mat)), price * multiplier);
				}
				
				player.sendMessage(Metods.msgC("&2All materials with tag '&b"+tag.toLowerCase()+"&2' the price is increased by: &b"+multiplier));
				
				
			}
		}.runTaskAsynchronously(_main);
    }
    
    void AddTags(Player player, ItemStack[] stacks, String tag)
    {
    	for(ItemStack stack : stacks)
    	{
    		if(stack == null) continue;
    		_main.GetTagManager().SaveTagAsync(stack.getType(), tag);
    		player.sendMessage(Metods.msgC("&2Tag named &3"+tag.toLowerCase()+" &2has been &eadded &2to material &b"+stack.getType()));
    	}
    	
    }
    
    void RemoveTag(Player player,ItemStack[] stacks, String tag)
    {
    	for(ItemStack stack : stacks)
    	{
    		if(stack == null) continue;
    		_main.GetTagManager().RemoveTagAsync(stack.getType(), tag);
    		player.sendMessage(Metods.msgC("&2Tag named &3"+tag.toLowerCase()+" &2has been &cremoved &2to material &b"+stack.getType()));
    	}
    }
    
    void RemoveAll(Player player,ItemStack[] stacks)
    {
    	for(ItemStack stack : stacks)
    	{
    		if(stack == null) continue;
    		_main.GetTagManager().RemoveAllAsync(stack.getType());
    		player.sendMessage(Metods.msgC("&2All tags from material &b"+stack.getType()+" &2has been &cremoved"));
    	}
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