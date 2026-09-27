package imu.GeneralStore.Managers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.GeneralStore.Invs.ShopModINV;
import imu.GeneralStore.Invs.ShopModModifyINV;
import imu.GeneralStore.Invs.ShopModModifyOverrideAllINV;
import imu.GeneralStore.Other.ItemMetods;
import imu.GeneralStore.Other.Shop;
import imu.GeneralStore.main.Main;
import net.md_5.bungee.api.ChatColor;

public class ShopModManager 
{
	Main _main;
	ItemMetods _itemM;

	public ShopModManager(Main main) 
	{
		_main = main;
	}
	
	public void openModShopInv(Player player, Shop shop)
	{
		ShopModINV sMod = new ShopModINV(_main, player, shop.getDisplayName() + ChatColor.DARK_PURPLE + " MODIFY", shop);
		sMod.openThis();
	}
	
	
	/**
	 * 
	 * @param player
	 * @param stack orginal item
	 * @param shop shop where it came to modify it
	 * @param anwsers anwser list for values of attriputes.. put null if its fresh new inv!
	 */
	public void openModShopModifyInv(Player player, ItemStack stack, Shop shop, String[] anwsers)
	{		
		new ShopModModifyINV(_main, player, ChatColor.DARK_PURPLE + " ==> MODIFY", stack, shop, anwsers);
	}
	
	public void openModShopModifyOVERRIDE_ALL_Inv(Player player, ItemStack stack, Shop shop, String[] anwsers)
	{		
		new ShopModModifyOverrideAllINV(_main, player, ChatColor.GOLD +""+ChatColor.BOLD +"OVERRIDE ALL", stack, shop, anwsers);
	}
	
//	void openModShopModifyInvLAST(Player player)
//	{
//		ItemStack stack = last_modifyWindow.get(player).getFirst();
//		Shop shop = last_modifyWindow.get(player).getSecond();
//		if(stack == null || shop == null)
//		{
//			System.out.println("COULDNT open last modify inv!");
//			return;
//		}
//		new ShopModModifyINV(_main, player, getStackDisplayName(stack) + ChatColor.DARK_PURPLE + " MODIFY item", stack, shop, setWaitingAnw(player, null));
//	}
//	
//	void setLastModifyInv(Player p, ItemStack stack, Shop shop)
//	{
//		last_modifyWindow.put(p, new Pair<ItemStack, Shop>(stack, shop));
//	}
//	
//	public String[] setWaitingAnw(Player p, Integer idx)
//	{
//		String[] answers = conv_answers.get(p);
//		if(answers == null )
//		{
//			answers = new String[27];
//			Arrays.fill(answers, "-1x1-");
//			
//		}
//		if(idx != null)
//		{
//			answers[idx] = null;
//		}
//				
//		conv_answers.put(p, answers);
//		return conv_answers.get(p);
//	}
//	
//	public void setAnswer(Player p, String ans)
//	{
//		String[] answers = conv_answers.get(p);
//		
//		if(answers == null)
//		{
//			System.out.println("Anwser not found?");
//		}
//		
//		for(int i = 0; i < answers.length; ++i)
//		{
//			if(answers[i] == null)
//			{
//				System.out.println("answer set!");
//				answers[i] = ans;
//				break;
//			}
//		}
//	}
	
	String getStackDisplayName(ItemStack stack)
	{
		String dis_name = "";
		if(stack.hasItemMeta())
		{
			dis_name = stack.getItemMeta().getDisplayName();
		}else
		{
			dis_name = stack.getType().toString();
		}
		return dis_name;
	}
}
