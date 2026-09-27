package imu.GeneralStore.Invs;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.GeneralStore.Managers.ShopModManager;
import imu.GeneralStore.Other.CustomInvLayout;
import imu.GeneralStore.Other.Shop;
import imu.GeneralStore.Prompts.ConvPromptModModifyINV;
import imu.GeneralStore.main.Main;
import net.md_5.bungee.api.ChatColor;

public class ShopModModifyINV extends CustomInvLayout implements Listener
{
	ShopModManager _smm;
	ItemStack orginal_item;
	ItemStack copy_item;
	
	String pd_buttonType = "gs.sModModI.buttonType";
	String pd_buttonAnswer ="gs.sModModI.buttonAnser";

	Shop _shop;
	String[] _answers;

	boolean _newInv = false;
	
	Integer _stack_id;
	public ShopModModifyINV(Main main, Player player, String name, ItemStack mod_itemStack, Shop shop, String[] answers) 
	{
		super(main, player, name, 9*3);
		
		main.getServer().getPluginManager().registerEvents(this, _main);

		orginal_item = mod_itemStack;
		copy_item = new ItemStack(mod_itemStack);
		_shop = shop;
		_smm = main.getShopModManager();
		_stack_id = _shop.FindIndexForShopStack(mod_itemStack);
		
		if(answers == null)
			_newInv = true;

		initAnswers(answers);
		
		
		openThis();
	}
		
	enum BUTTON
	{
		NONE,
		BACK,
		CONFIRM,
		CUSTOM_AMOUNT,
		SET_PERMISSION,
		SET_DELAY_AMOUNT,
		SET_CUSTOM_PRICE,
		SET_WORLDS,
		SET_CAN_BE_SOLD,
		SET_DISTANCE,
		REMOVE_INF,
		SET_TIME_SELL;
						
	}
	
	void initAnswers(String[] answers)
	{
		if(answers == null)
		{
			answers = new String[_size];
			Arrays.fill(answers, null);
		}
		
		_answers = answers;
	}
	void setButton(ItemStack stack, BUTTON b)
	{
		itemM.setPersistenData(stack, pd_buttonType, PersistentDataType.STRING, b.toString());
	}
	
	BUTTON getButton(ItemStack stack)
	{
		String button = itemM.getPersistenData(stack, pd_buttonType, PersistentDataType.STRING);
		if(button != null)
			return BUTTON.valueOf(button);
		
		return BUTTON.NONE;
	}
	
	ItemStack setupButton(BUTTON b, Material material, String displayName, int itemSlot)
	{
		ItemStack sbutton = new ItemStack(material);
		itemM.setDisplayName(sbutton, displayName);
		setButton(sbutton, b);
		_inv.setItem(itemSlot, sbutton);
		return _inv.getItem(itemSlot);
	}
	
	void makeInv()
	{
		for(int i = 0; i < _inv.getContents().length; ++i)
		{
			_inv.setItem(i, itemM.setDisplayName(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), " "));
		}
		
		int id = 0;
		setupButton(BUTTON.BACK, Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "<< BACK", 0);
		setupButton(BUTTON.CONFIRM, Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "<< CONFIRM", 9*2);
		_inv.setItem(9, new ItemStack(copy_item));
		setupButton(BUTTON.REMOVE_INF, Material.LAVA_BUCKET, ChatColor.RED + ""+ChatColor.BOLD + "Remove this items from shop", 8);
		
		
		id = 2;
		ItemStack custom_amount = setupButton(BUTTON.CUSTOM_AMOUNT, Material.GLASS_PANE, ChatColor.DARK_PURPLE + "Custom amount", id);
		itemM.addLore(custom_amount, ChatColor.AQUA + "M1: "+ChatColor.GREEN + "Set"+ChatColor.AQUA + " M2: "+ChatColor.RED + "Remove", false);
		Integer c_amount = _shop.getPDCustomAmount(orginal_item);
		if(c_amount != null && _answers[id] == null && _newInv)
		{
			_answers[id] = String.valueOf(c_amount);
		}
		
		id = 4;
		ItemStack setPerms = setupButton(BUTTON.SET_PERMISSION, Material.GLASS_PANE, ChatColor.DARK_PURPLE + "Set permission", id);
		itemM.addLore(setPerms, ChatColor.AQUA + "M1: "+ChatColor.GREEN + "Set"+ChatColor.AQUA + " M2: "+ChatColor.RED + "Remove", false);
		String perm=_shop.getPDCustomPermission(orginal_item);
		if(perm != null && _answers[id] == null && _newInv)
		{
			_answers[id] = perm;
		}
		
		id = 6;
		ItemStack setDelay_Amount = setupButton(BUTTON.SET_DELAY_AMOUNT, Material.GLASS_PANE, ChatColor.DARK_PURPLE + "Set Stock Delay(min) and FillAmount", id);
		itemM.addLore(setDelay_Amount, ChatColor.AQUA + "M1: "+ChatColor.GREEN + "Set"+ChatColor.AQUA + " M2: "+ChatColor.RED + "Remove", false);
		itemM.addLore(setDelay_Amount, ChatColor.YELLOW + "How often stock will be filled and", true);
		itemM.addLore(setDelay_Amount, ChatColor.YELLOW + "how many it fills. Cant go over", true);
		itemM.addLore(setDelay_Amount, ChatColor.YELLOW + "shop amount(or custom amount)", true);
		itemM.addLore(setDelay_Amount, ChatColor.BLUE + "Minimum time: "+ _shop.getStockCheckTime()/60, true);
		
		Integer delay_Amount = _shop.getPDCustomStockAmount(orginal_item);
		Integer delay=_shop.getPDCustomStockDelay(orginal_item);
		if(delay_Amount != null && delay != null && _answers[id] == null && _newInv)
		{			
			_answers[id] = delay +" "+delay_Amount;
		}
		
		id = 12;
		ItemStack setCusPrice = setupButton(BUTTON.SET_CUSTOM_PRICE, Material.GLASS_PANE, ChatColor.DARK_PURPLE + "Set Custom Price", id);
		itemM.addLore(setCusPrice, ChatColor.AQUA + "M1: "+ChatColor.GREEN + "Set"+ChatColor.AQUA + " M2: "+ChatColor.RED + "Remove", false);
		itemM.addLore(setCusPrice, ChatColor.YELLOW + "Set own price for item in this shop!", true);
		itemM.addLore(setCusPrice, ChatColor.YELLOW + "Be carefull with money explote! Cant be too cheap!", true);
		String customPrice = _shop.getPDCustomPrice(orginal_item);
		
		if(customPrice != null && _answers[id] == null && _newInv)
		{
			_answers[id] = customPrice.replace(":", " ");
		}
		
		id = 14;
		ItemStack setWorlds = setupButton(BUTTON.SET_WORLDS, Material.GLASS_PANE, ChatColor.DARK_PURPLE + "Set Spesific worlds", id);
		itemM.addLore(setWorlds, ChatColor.AQUA + "M1: "+ChatColor.GREEN + "Set"+ChatColor.AQUA + " M2: "+ChatColor.RED + "Remove", false);
		itemM.addLore(setWorlds, ChatColor.YELLOW + "Set worlds where you can get this item and", true);
		itemM.addLore(setWorlds, ChatColor.YELLOW + "Item will be removed(from inv) if entered wrong world", true);
		String customWorlds = _shop.getPDCustomWorlds(orginal_item);
		
		if(customWorlds != null && _answers[id] == null && _newInv)
		{
			_answers[id] = customWorlds;
		}
		
		id = 20;
		ItemStack setCanBeSold = setupButton(BUTTON.SET_CAN_BE_SOLD, Material.GLASS_PANE,  ChatColor.DARK_PURPLE + "Can Item sold back to GS", id);
		itemM.addLore(setCanBeSold, ChatColor.AQUA + "M1: "+ChatColor.GREEN + "Change"+ChatColor.AQUA + " M2: "+ChatColor.AQUA + "True", false);
		itemM.addLore(setCanBeSold, ChatColor.YELLOW + "If true, you can sold it back. If false you cant!", true);
		Integer setCanSold = _shop.getPDCustomCanSoldBack(orginal_item);
		
		if(setCanSold == null || setCanSold == 1)
		{
			_answers[id] = "true";
		}
		else if(setCanSold != null )
		{
			_answers[id] = "false";
		}
		
		id = 22;
		ItemStack setDistance_item = setupButton(BUTTON.SET_DISTANCE, Material.GLASS_PANE,  ChatColor.DARK_PURPLE + "Set Distance radius. You position as anchor!", id);
		itemM.addLore(setDistance_item, ChatColor.AQUA + "M1: "+ChatColor.GREEN + "Set"+ChatColor.AQUA + " M2: "+ChatColor.RED + "Remove", false);
		itemM.addLore(setDistance_item, ChatColor.YELLOW + "Set your location and item will be shown in given radius", true);
		String distance_pd_str = _shop.getPDCustomSoldDistance(orginal_item);
		
		if(distance_pd_str != null && _answers[id] == null && _newInv)
		{
			_answers[id] = distance_pd_str;
		}
		
		id = 24;
		ItemStack setTimeSell = setupButton(BUTTON.SET_TIME_SELL, Material.GLASS_PANE,  ChatColor.DARK_PURPLE + "Set sell time", id);
		itemM.addLore(setTimeSell, ChatColor.AQUA + "M1: "+ChatColor.GREEN + "Set"+ChatColor.AQUA + " M2: "+ChatColor.RED + "Remove", false);
		itemM.addLore(setTimeSell, ChatColor.YELLOW + "Set time when item will be apearing in shop", true);
		String timeSell_str = _shop.getPDCustomTimeSell(orginal_item);
		
		if(timeSell_str != null && _answers[id] == null && _newInv)
		{
			_answers[id] = timeSell_str;
		}
		
		
	}
	
	void setAnwsers()
	{
		String str = "Set to: ";
		int idx;
		//_main.getItemM().printArray("ans", _answers);
		for(int i = 0; i < _answers.length; ++i)
		{
			String ans = _answers[i];
			if(ans != null)
			{
				
				idx = itemM.findLoreIndex(_inv.getItem(i), str);
				if(idx > -1)
				{
					itemM.reSetLore(_inv.getItem(i), ChatColor.AQUA + str +ChatColor.DARK_GREEN+ans, idx);
				}else
				{
					itemM.addLore(_inv.getItem(i), ChatColor.AQUA + str +ChatColor.DARK_GREEN+ans, true);
				}
				
				setPDanswer(_inv.getItem(i), ans);
				
				if(ans.equalsIgnoreCase("false"))
				{
					_inv.getItem(i).setType(Material.RED_STAINED_GLASS_PANE);
				}
				else if(ans.equalsIgnoreCase("sure"))
				{
					itemM.removeLore(_inv.getItem(i), "Are you sure? Press M2 to confirm!");
					itemM.addLore(_inv.getItem(i), ChatColor.BLUE + "Are you sure? Press"+ChatColor.WHITE+ " M2 "+ ChatColor.BLUE + "to confirm!", false);
				}
				else
				{
					_inv.getItem(i).setType(Material.BLUE_STAINED_GLASS_PANE);
				}
				
				
			}else
			{
				BUTTON b = getButton(_inv.getItem(i));
				if(_inv.getItem(i) != null && b != BUTTON.NONE && b != BUTTON.BACK && b != BUTTON.CONFIRM && b != BUTTON.REMOVE_INF)
				{
					itemM.removeLore(_inv.getItem(i), str);
					removePDanwser(_inv.getItem(i));
					_inv.getItem(i).setType(Material.GLASS_PANE);
				}
				
			}
			
			
		}
			
	

	}
	
	void setPDanswer(ItemStack s, String ans)
	{
		itemM.setPersistenData(s, pd_buttonAnswer, PersistentDataType.STRING, ans);
	}
	void removePDanwser(ItemStack s)
	{
		itemM.removePersistenData(s, pd_buttonAnswer);
	}
	
	boolean confirm()
	{
		_stack_id = _shop.FindIndexForShopStack(orginal_item);
		if(_stack_id == null)
		{
			_player.sendMessage(ChatColor.RED + "Something went wrong couldnt find stack from shop! Confirming FAILED!");
			return true;
			
		}
		
		_player.sendMessage(ChatColor.YELLOW + "=========================================");
		ArrayList<Integer> wrongs = new ArrayList<>();
		for(int i = 0; i < _inv.getContents().length; ++i)
		{
			ItemStack s = _inv.getItem(i);
			if(s != null)
			{
				
				BUTTON button = getButton(s);
				String str;
				switch (button) 
				{
				case CUSTOM_AMOUNT:
					
					str= itemM.getPersistenData(s, pd_buttonAnswer, PersistentDataType.STRING);
					String cus_str = ChatColor.DARK_PURPLE + "Custom amount";
					if(str != null)
					{
						
						
						if(itemM.isDigit(str))
						{
							Integer amount = NumberUtils.createInteger(str);
							if(amount == _shop.getPDCustomAmount(_shop.getShopStacks().get(_stack_id)))
							{
								continue;
							}
							
							_shop.setPDCustomAmount(_shop.getShopStacks().get(_stack_id), amount);
							_player.sendMessage(cus_str+" set to: "+ChatColor.GOLD +amount);
						}
						else
						{
							wrongs.add(i);
							_player.sendMessage(cus_str+ ChatColor.RED + ": Something went wrong? Invalid syntax ?");
						}
						
					}else
					{
						if(_shop.removePDCustomAmount(_shop.getShopStacks().get(_stack_id)))
							_player.sendMessage(cus_str+ ChatColor.RED + ": removed");
					}

					
					break;
					
					
				case SET_PERMISSION:
					str = itemM.getPersistenData(s, pd_buttonAnswer, PersistentDataType.STRING);
					
					String perm_str = ChatColor.DARK_PURPLE + "Permission name";
					
					if(str != null)
					{
						if(str.equalsIgnoreCase(_shop.getPDCustomPermission(_shop.getShopStacks().get(_stack_id))))
						{
							continue;
						}
						
						
						_shop.setPDCustomPermission(_shop.getShopStacks().get(_stack_id), str.toLowerCase());
						_player.sendMessage(perm_str+" set to: "+ChatColor.GOLD +str.toLowerCase());
						
					}else
					{
						if(_shop.removePDCustomPermission(_shop.getShopStacks().get(_stack_id)))
							_player.sendMessage(perm_str+ ChatColor.RED + ": removed");
					}
					
					break;
					
					
				case SET_DELAY_AMOUNT:
					str = itemM.getPersistenData(s, pd_buttonAnswer, PersistentDataType.STRING);
					String stock_str = ChatColor.DARK_PURPLE + "Stock Delay and Stock Amount";
					if(str != null)
					{
						String[] delay_amount_str = str.trim().replaceAll(" +"," ").split(" ");
						if(delay_amount_str.length > 1  && itemM.isDigit(delay_amount_str[0]) && itemM.isDigit(delay_amount_str[1]))
						{
							int d_delay =Integer.parseInt(delay_amount_str[0]);
							int d_amount =Integer.parseInt(delay_amount_str[1]);
							
							if(_shop.getPDCustomAmount(_shop.getShopStacks().get(_stack_id)) == null)
							{
								int c_a = _shop.getShopStackAmount(_shop.getShopStacks().get(_stack_id));
								_shop.setPDCustomAmount(_shop.getShopStacks().get(_stack_id), c_a);
								_player.sendMessage(ChatColor.BLUE + "Couldnt find Custom amount.. so it set to stack size from shop: "+c_a);
								_player.sendMessage(ChatColor.BLUE + "This need to be set if you have set stock delay and amount");
							}
							
							if(_shop.getPDCustomStockDelay(_shop.getShopStacks().get(_stack_id)) != null 
									&& _shop.getPDCustomStockAmount(_shop.getShopStacks().get(_stack_id)) != null
									&& d_delay == _shop.getPDCustomStockDelay(_shop.getShopStacks().get(_stack_id)) 
									&& d_amount == _shop.getPDCustomStockAmount(_shop.getShopStacks().get(_stack_id)))
							{
								continue;
							}
							_shop.setPDCustomStockDelay(_shop.getShopStacks().get(_stack_id), d_delay);
							_shop.setPDCustomStockAmount(_shop.getShopStacks().get(_stack_id),d_amount );
							
							_player.sendMessage(stock_str+" set to: "+ChatColor.GOLD +d_delay+" "+d_amount);
							
							
						}
						else
						{
							wrongs.add(i);
							_player.sendMessage(stock_str+ ChatColor.RED + ": Something went wrong? Invalid syntax ?");
						}
					}else
					{
						_shop.removePDCustomStockDelay(_shop.getShopStacks().get(_stack_id));
						if(_shop.removePDCustomStockAmount(_shop.getShopStacks().get(_stack_id)))
							_player.sendMessage(stock_str+ ChatColor.RED + ": removed");
					}
					
					break;
				case SET_CUSTOM_PRICE:
					str = itemM.getPersistenData(s, pd_buttonAnswer, PersistentDataType.STRING);
					String custom_price = ChatColor.DARK_PURPLE + "Custom price";
					
					if(str != null)
					{
						String[] prices_str = str.trim().replaceAll(" +"," ").split(" ");
						boolean valueOK = true;
						if(prices_str.length > 2 && itemM.isDigit(prices_str[0]) && itemM.isDigit(prices_str[1]) && itemM.isDigit(prices_str[2]))
			            {
							Double[] ds = {Double.parseDouble(prices_str[0]),Double.parseDouble(prices_str[1]),Double.parseDouble(prices_str[2])};
							if(_main.getShopManager().isPriceValid(ds))
							{
								_shop.setPDCustomPrice(_shop.getShopStacks().get(_stack_id), ds[0]+":"+ds[1]+":"+ds[2]);
								_player.sendMessage(custom_price+" set to: "+ChatColor.GOLD + str);
							}
							else
							{
								valueOK = false;
							}
			            }
						else
			            {
			            	valueOK = false;
			            }
						
						if(!valueOK)
						{
							wrongs.add(i);
							_player.sendMessage(custom_price+ ChatColor.RED + ": Something went wrong? Invalid syntax? Remember minPrice < maxPrice");
						}
					}else
					{
						if(_shop.removePDCustomPrice(_shop.getShopStacks().get(_stack_id)))
							_player.sendMessage(custom_price+ ChatColor.RED + ": removed");
					}
					
					break;
					
				case SET_WORLDS:
					
					str = itemM.getPersistenData(s, pd_buttonAnswer, PersistentDataType.STRING);
					String custom_worlds_str = ChatColor.DARK_PURPLE + "Spesific worlds";
					if(str != null)
					{
						String worlds_str = str.trim().replaceAll(" +"," ").toLowerCase().replace("this", _player.getWorld().getName());
						_shop.setPDCustomWorlds(_shop.getShopStacks().get(_stack_id), worlds_str);
						_player.sendMessage(custom_worlds_str+" set to: "+ChatColor.GOLD + worlds_str);
					}else
					{
						if(_shop.removePDCustomWorlds(_shop.getShopStacks().get(_stack_id)))
							_player.sendMessage(custom_worlds_str+ ChatColor.RED + ": removed");
					}
					break;
				
				case SET_CAN_BE_SOLD:
					
					str = itemM.getPersistenData(s, pd_buttonAnswer, PersistentDataType.STRING);
					String custom_canSold_str = ChatColor.DARK_PURPLE + "Can be sold";
					
					if(str.equalsIgnoreCase("true"))
					{
						_shop.removePDCustomCanSoldBack(_shop.getShopStacks().get(_stack_id));
						_player.sendMessage(custom_canSold_str+" set to: "+ChatColor.GOLD + "true");
					}else
					{
						_shop.setPDCustomCanSoldBack(_shop.getShopStacks().get(_stack_id));
						_player.sendMessage(custom_canSold_str+" set to: "+ChatColor.GOLD + "false");
					}
					break;
				case SET_DISTANCE:
					
					str = itemM.getPersistenData(s, pd_buttonAnswer, PersistentDataType.STRING);
					String custom_distance_str = ChatColor.DARK_PURPLE + "Distance radius and loc";
					boolean fa = false;
					if(str != null)
					{
						String[] distance_str = str.trim().replaceAll(" +"," ").toLowerCase().split(" ");
						Integer distance = null;
						
						int x = 0;
						int y = 0;
						int z = 0;
						
						String w_name = "";
						if(distance_str.length > 4)
						{
							System.out.println("4");
							if(itemM.isDigit(distance_str[0]) && itemM.isDigit(distance_str[1]) && itemM.isDigit(distance_str[2]) && itemM.isDigit(distance_str[3]))
							{
								distance = Integer.parseInt(distance_str[0]);
								x = Integer.parseInt(distance_str[1]);
								y = Integer.parseInt(distance_str[2]);
								z = Integer.parseInt(distance_str[3]);
								w_name = distance_str[4].toLowerCase();
							}else
							{
								fa = true;
							}
							
						}
						else if(distance_str.length > 0 && itemM.isDigit(distance_str[0]))
						{
							distance = Integer.parseInt(distance_str[0]);
							x = _player.getLocation().getBlockX();
							y = _player.getLocation().getBlockY();
							z = _player.getLocation().getBlockZ();
							w_name =  _player.getLocation().getWorld().getName().toLowerCase();
						}else
						{						
							fa = true;
						}
						
						String full_str = distance + " "+x+" "+y+" "+z+" "+w_name;
						_shop.setPDCustomSoldDistance(_shop.getShopStacks().get(_stack_id), full_str);
						_player.sendMessage(custom_distance_str+" set to: "+ChatColor.GOLD + full_str);
						
					}
					else
					{
						if(_shop.removePDCustomSoldDistance(_shop.getShopStacks().get(_stack_id)))
							_player.sendMessage(custom_distance_str+ ChatColor.RED + ": removed");
					}
					if(fa)
					{
						_shop.removePDCustomSoldDistance(_shop.getShopStacks().get(_stack_id));
						_player.sendMessage(custom_distance_str+ ChatColor.RED + ": Something went wrong? Give only radius!");
						wrongs.add(i);
					}
					break;
				case SET_TIME_SELL:
					str = itemM.getPersistenData(s, pd_buttonAnswer, PersistentDataType.STRING);
					String customTimeSell = ChatColor.DARK_PURPLE + "Time to sell";

					if(str != null)
					{
						String[] sellTimes = str.trim().replaceAll(" +"," ").split(" ");
						if(sellTimes.length > 1 && itemM.isDigit(sellTimes[0]) && itemM.isDigit(sellTimes[1]))
						{
							int time1 =Integer.parseInt(sellTimes[0]);
							int time2 =Integer.parseInt(sellTimes[1]);
							
							if((time1 >= 0 && time1 <= 24000) && (time2 >= 0 && time2 <= 24000))
							{
								_shop.setPDCustomTimeSell(_shop.getShopStacks().get(_stack_id), time1+" "+time2);
								_player.sendMessage(customTimeSell+" set to: "+ChatColor.GOLD + time1+" "+time2);
							}
							else
							{
								_player.sendMessage(customTimeSell+ ChatColor.RED + ": Times should be between 0 and 24000");
								wrongs.add(i);
							}
							
						}
						else
						{
							_player.sendMessage(customTimeSell+ ChatColor.RED + ": Something went wrong?");
							wrongs.add(i);
						}
					}
					else
					{
						if(_shop.removePDCustomTimeSell(_shop.getShopStacks().get(_stack_id)))
						{
							_player.sendMessage(customTimeSell+ ChatColor.RED + ": removed");
						}
							
					}
					
					break;
				default:
					break;
					}
				
			}
			
		}
		_player.sendMessage(ChatColor.YELLOW + "=========================================");
		
		if(!wrongs.isEmpty())
		{
			for(int i : wrongs)
			{
				_inv.getItem(i).setType(Material.YELLOW_STAINED_GLASS);
			}
			return false;
		}else
		{
			return true;
		}
	}
	
	@EventHandler
	public void onInvOpen(InventoryOpenEvent e)
	{
		if(isThisInv(e))
		{
			_shop.set_closed(true);
			makeInv();
			setAnwsers();
		}
	}
	
	@EventHandler
	public void onInvClose(InventoryCloseEvent e)
	{
		if(isThisInv(e))
		{
			HandlerList.unregisterAll(this);
			_shop.set_closed(false);
		}
	}
	
	@EventHandler
	public void onInvClickEvent(InventoryClickEvent e) 
	{
		int rawSlot = e.getRawSlot();
		int slot = e.getSlot();
		
		if(isThisInv(e) && (rawSlot == slot))
		{			
			e.setCancelled(true);
			ItemStack stack = e.getCurrentItem();
			
			BUTTON button = getButton(stack);
			
			ConversationFactory cf = null;
			String question = null;
			Conversation conv = null;
			if(e.getClick() == ClickType.LEFT)
			{
				switch (button) 
				{
				case NONE:
					
					break;
				case BACK:
					_smm.openModShopInv(_player, _shop);
					break;
				case CONFIRM:
					if(confirm())						
						_smm.openModShopInv(_player, _shop);
					
					break;
				case CUSTOM_AMOUNT:

					cf = new ConversationFactory(_main);
					question = ChatColor.DARK_PURPLE + "Give Custom amount?";
					conv = cf.withFirstPrompt(new ConvPromptModModifyINV(_main, _player, orginal_item, _shop,_answers, slot,question)).withLocalEcho(true).buildConversation(_player);
					conv.begin();
					
					_player.closeInventory();
					break;
				case SET_PERMISSION:
					cf = new ConversationFactory(_main);
					question = ChatColor.DARK_PURPLE + "Give Permission name?";
					conv = cf.withFirstPrompt(new ConvPromptModModifyINV(_main, _player, orginal_item, _shop,_answers, slot,question)).withLocalEcho(true).buildConversation(_player);
					conv.begin();
					
					_player.closeInventory();
					
					break;
				case SET_DELAY_AMOUNT:
					cf = new ConversationFactory(_main);
					question = ChatColor.DARK_PURPLE + "Give Delay and fill_amount. Seperate by space(ex: 10 3)";
					conv = cf.withFirstPrompt(new ConvPromptModModifyINV(_main, _player, orginal_item, _shop,_answers, slot,question)).withLocalEcho(true).buildConversation(_player);
					conv.begin();
					
					_player.closeInventory();
					break;
				case SET_CUSTOM_PRICE:
					
					cf = new ConversationFactory(_main);
					question = ChatColor.DARK_PURPLE + "Give price < min > < max > < % >. Seperate by space(ex: 9 11 2)";
					conv = cf.withFirstPrompt(new ConvPromptModModifyINV(_main, _player, orginal_item, _shop,_answers, slot,question)).withLocalEcho(true).buildConversation(_player);
					conv.begin();
					
					_player.closeInventory();
					break;
				case SET_WORLDS:
					cf = new ConversationFactory(_main);
					question = ChatColor.DARK_PURPLE + "Give Worlds. Seperate by space(ex: world world_nether this(takes world where you are) )";
					conv = cf.withFirstPrompt(new ConvPromptModModifyINV(_main, _player, orginal_item, _shop,_answers, slot, question)).withLocalEcho(true).buildConversation(_player);
					conv.begin();
					
					_player.closeInventory();
				case SET_CAN_BE_SOLD:
					
					if(_answers[slot] != null && _answers[slot].equalsIgnoreCase("true"))
					{
						_answers[slot]="false";
					}else
					{
						_answers[slot]="true";
					}
					_player.sendMessage(ChatColor.DARK_PURPLE + "Can be sold set to: "+_answers[slot]);
					setAnwsers();
					break;
				case SET_DISTANCE:
					cf = new ConversationFactory(_main);
					question = ChatColor.DARK_PURPLE + "Give Distance?";
					conv = cf.withFirstPrompt(new ConvPromptModModifyINV(_main, _player, orginal_item, _shop,_answers, slot, question)).withLocalEcho(true).buildConversation(_player);
					conv.begin();
					
					_player.closeInventory();
					break;
				case REMOVE_INF:
					_answers[slot]="sure";
					setAnwsers();
					break;
				case SET_TIME_SELL:
					cf = new ConversationFactory(_main);
					question = ChatColor.DARK_PURPLE + "Give startTime and endTime  Seperate by space(ex: 0 13000(whole daytime)(min:0 max:24000)";
					conv = cf.withFirstPrompt(new ConvPromptModModifyINV(_main, _player, orginal_item, _shop,_answers, slot, question)).withLocalEcho(true).buildConversation(_player);
					conv.begin();
					_player.closeInventory();
					break;	
				default:
					break;
				}
			}
				else if(e.getClick() == ClickType.RIGHT)
			{
				switch (button) 
				{
				case CUSTOM_AMOUNT:
					_answers[slot] = null;
					setAnwsers();
					break;
				case SET_PERMISSION:
					_answers[slot] = null;
					setAnwsers();
					break;
				case SET_DELAY_AMOUNT:
					_answers[slot] = null;
					setAnwsers();
					break;
				case SET_CUSTOM_PRICE:
					_answers[slot] = null;
					setAnwsers();
					break;
				case SET_WORLDS:
					_answers[slot] = null;
					setAnwsers();
					break;
				case SET_DISTANCE:
					_answers[slot] = null;
					setAnwsers();
					break;
				case SET_CAN_BE_SOLD:
					_answers[slot]="true";
					setAnwsers();
					break;
				case REMOVE_INF:
					if(_answers[slot].equalsIgnoreCase("sure"))
					{
						_shop.removeItemFromShopNEW(orginal_item,true);
						_main.getShopModManager().openModShopInv(_player, _shop);
					}
					break;
				case SET_TIME_SELL:
					_answers[slot]=null;
					setAnwsers();
					break;
				default:
					break;

			}
			
			}
			
		}	
	}
	
	
	
}
