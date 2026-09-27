package imu.GeneralStore.Other;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.GeneralStore.main.Main;
import net.md_5.bungee.api.ChatColor;

public class EnchantInvModify extends CustomInvLayout implements Listener
{
	ItemStack _orginalStack, _copy,_orginalStack_copy = null;
	ItemStack _empty = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);

	EnchantsManager _enchM = null;
	
	String _pd_modify = "";
	String _pd_vButton = "gs.eModifyVb";
	String _pd_dataIdx = "gs.eDataId";
	
	Double[] _prices = {};
	Double[] _defautl_prices = {};
	String[] _tooltip_strs= {ChatColor.AQUA+ "======Ench======",
							ChatColor.DARK_PURPLE + "maxPrice : " + ChatColor.GOLD +"",
							ChatColor.DARK_PURPLE + "minPrice  : " + ChatColor.GOLD +"",
							ChatColor.DARK_PURPLE + "maxlevel : " + ChatColor.GOLD +"",
							ChatColor.DARK_PURPLE + "minlevel  : " + ChatColor.GOLD +"",
							ChatColor.AQUA+ "======Ench======",
							""+ChatColor.DARK_PURPLE + ChatColor.MAGIC+ "# "+ChatColor.GREEN + "CONFIRM by CLICK!"+ ChatColor.DARK_PURPLE+ChatColor.MAGIC+ " #"};
	
	int _total_value_state_min = 2;
	int _total_value_states = 3; //include 0
	int _value_state_now = 0;
	
	Double[] price_button_values = {0.1, 1.0, 10.0, 100.0, 1000.0};
	Double[] price_button_multiv = {1.0, 5.0, 10.0};
	
	String[] _dataNames = {};
	
	Enchantment _enchment = null;
	int realMinLvl = 1;
	int realMaxLvl = 1;
	
	public EnchantInvModify(Main main, Player player, String name, ItemStack enchItem) 
	{
		super(main, player, name, 9*3);
		
		_main.getServer().getPluginManager().registerEvents(this, _main);
		
		_enchM = _main.getEnchManager();
		_pd_modify = _enchM.pd_modify;
		
		INIT(enchItem);
	}
	
	public void INIT(ItemStack uniqueItemStack)
	{
		_orginalStack = uniqueItemStack;
		_orginalStack_copy = _orginalStack.clone();
		_enchM.removeModifyData(_orginalStack_copy);
		_copy = _orginalStack.clone();
		
		setData(_enchM.getEnchPriceData(_orginalStack));

		_value_state_now = _total_value_state_min;
		_enchment = _enchM.getEnchantFromStack(uniqueItemStack);
		realMinLvl = _enchment.getStartLevel();
		realMaxLvl = _enchment.getMaxLevel();
		
		makeInv();		
	}
	
	void setData(Double[] data)
	{

		_prices = data;
		_defautl_prices = _prices.clone();
		String[] names = {"Min Level","Max Level","Min Price", "Max Price"};
		_dataNames = names;
		
		
	}
	
	enum LABELS
	{
		GO_BACK(99),
		COPY(80),
		RESET_DEFAULT(81),
		VALUE_STATE(10),
		VALUE_PRICE_BUTTON(11),
		RESET_ITEM(12),
		CONFIRM(13),
		RESET_ITEM_DEFAULT(14);
		
				
		int type;
		
		LABELS(int i)
		{
			this.type = i;
		}
		public int getType()
		{
			return type;
		}		
	}
	
	void makeInv()
	{		
		
		itemM.setDisplayName(_empty, " ");
		for(int i = 0; i < _size ; ++i)
		{
			_inv.setItem(i, _empty);
		}
		
		refreshButtons();
		
		refrestItem();
				
	}
	
	void setSwitch(LABELS label, Material material, String displayName,int itemSlot)
	{
		ItemStack sbutton = new ItemStack(material);
		itemM.setDisplayName(sbutton, displayName);
		setButtonSwitch(sbutton, label.getType());
		_inv.setItem(itemSlot, sbutton);
	}
	
	void refreshButtons()
	{
		setSwitch(LABELS.GO_BACK, Material.RED_STAINED_GLASS_PANE , ChatColor.AQUA + "<< BACK", 9);
		setSwitch(LABELS.RESET_DEFAULT, Material.LAVA_BUCKET,  ChatColor.RED + "RESET DEFAULT VALUES", _size-1);
		setSwitch(LABELS.RESET_ITEM, Material.WATER_BUCKET, ChatColor.AQUA + "CLEAR CHANGES", 12);
		setSwitch(LABELS.RESET_ITEM_DEFAULT, Material.BEDROCK, ChatColor.AQUA + "RESET ITEM DATA DEFAULT VALUES", 14);
		
		refreshValueStateButton();
		refrestItem();
		refreshPriceButtons();
	}
	
	void setValueButtonValue(ItemStack stack, Double d)
	{
		itemM.setPersistenData(stack, _pd_vButton, PersistentDataType.DOUBLE, d);
	}
	
	Double getValueButtonValue(ItemStack stack)
	{
		return itemM.getPersistenData(stack, _pd_vButton, PersistentDataType.DOUBLE);
	}
	
	void setValueDataIndex(ItemStack stack, int id)
	{
		itemM.setPersistenData(stack, _pd_dataIdx, PersistentDataType.INTEGER, id);
	}
	
	Integer getValueDataIndex(ItemStack stack)
	{
		return itemM.getPersistenData(stack, _pd_dataIdx, PersistentDataType.INTEGER);
	}
	
	void goBack()
	{
		_enchM.removeModifyData(_orginalStack);
		_enchM.openEnchantINV(_player);
	}
	
	
	@EventHandler
	public void onInvCloseEvent(InventoryCloseEvent e)
	{
		if(isThisInv(e))
		{
			HandlerList.unregisterAll(this);
		}
	}
	
	@EventHandler
	public void onInvClickEvent(InventoryClickEvent e) 
	{
		if(isThisInv(e))
		{
			e.setCancelled(true);
			ItemStack stack = e.getCurrentItem();
			Integer switch_button = getButtonSwitch(stack);
			ClickType click = e.getClick();
			
			
			if(switch_button != null)
			{
				if(switch_button == LABELS.GO_BACK.getType())
				{
					goBack();
					return;
				}
				
				if(switch_button == LABELS.COPY.getType())
				{
					itemM.moveItemFirstFreeSpaceInv(_orginalStack_copy.clone(), _player, true);
					return;
				}
				
				if(switch_button == LABELS.RESET_DEFAULT.getType())
				{
					Double[] price = {(double)realMinLvl, (double)realMaxLvl, 0.0,0.0};
					confirm(price);
					goBack();
					return;
				}
				
				if(switch_button == LABELS.RESET_ITEM_DEFAULT.getType())
				{
					System.out.println("its this");
					Double[] price = {(double)realMinLvl, (double)realMaxLvl, 0.0,0.0};
					_prices = price;
					refreshButtons();
					return;
				}
				
				if(switch_button == LABELS.VALUE_STATE.getType())
				{
					if(click == ClickType.LEFT)
						changeValueState(1);
					
					if(click == ClickType.RIGHT)
						changeValueState(-1);
					
					refreshButtons();
					return;
				}
				
				if(switch_button == LABELS.VALUE_PRICE_BUTTON.getType())
				{
					Double price_amount = getValueButtonValue(stack);
					int dataIndex = getValueDataIndex(stack);
					Double multip = 1.0;
					if(click == ClickType.LEFT)
					{
						multip = price_button_multiv[0];
					}
					else if(click == ClickType.SHIFT_LEFT)
					{
						multip = price_button_multiv[1];
					}					
					else if(click == ClickType.RIGHT)
					{
						multip = price_button_multiv[0] * -1;
					}					
					else if(click == ClickType.SHIFT_RIGHT)
					{
						multip = price_button_multiv[1] * -1;
					}
					else if(click == ClickType.MIDDLE)
					{
						multip = price_button_multiv[2];
					}
					
					changePrice(dataIndex, price_amount*multip); 
					refreshButtons();
					return;
				}
				
				if(switch_button == LABELS.RESET_ITEM.getType())
				{
					_prices = _defautl_prices.clone();
					refreshButtons();
				}
				
				if(switch_button == LABELS.CONFIRM.getType())
				{
					
					confirm(_prices);
					goBack();
				}				
			}
		}
	}
	
	void confirm(Double[] data)
	{
		_player.sendMessage(ChatColor.YELLOW + "Enchant has been modified");
		_enchM.addNewEnchant(_enchment, data,true);		
	}
	
	String getValueStateName(int id)
	{
		return _dataNames[id];
	}
	void changeValueState(int i)
	{
		_value_state_now += i;
		if(_value_state_now < _total_value_state_min)
			_value_state_now = _total_value_states;

		
		if(_value_state_now > _total_value_states)
			_value_state_now = _total_value_state_min;
		
		refreshValueStateButton();
	}
	
	void changePrice(int id, Double addAmount)
	{
		Double[] copy_price = _prices.clone();
		double newPrice =  Math.round((copy_price[id] + addAmount)* 100.0) / 100.0;;
		
		if(newPrice < 0)
		{
			newPrice = 0;
		}
		
		if(id == 0 && newPrice > _prices[1])
		{
			copy_price[1] = newPrice;
		}
		
		if(id == 2 && newPrice > _prices[3])
		{
			copy_price[3] = newPrice;
		}
		
		copy_price[id] = newPrice;
		
		if(!_enchM.isEnchantPriceValid(copy_price))
		{
			_player.sendMessage(ChatColor.DARK_RED + "That's not valid data!");
			return;
		}
		
		_prices = copy_price;
	}
	void refreshValueStateButton()
	{
		ItemStack value_state_button = new ItemStack(Material.BEDROCK);
		switch(_value_state_now)
		{
			case 0:
				value_state_button = new ItemStack(Material.IRON_BLOCK);
				itemM.setDisplayName(value_state_button, ChatColor.AQUA + getValueStateName(_value_state_now));
				break;
			case 1:
				value_state_button = new ItemStack(Material.GOLD_BLOCK);
				itemM.setDisplayName(value_state_button, ChatColor.AQUA +  getValueStateName(_value_state_now));
				break;
			case 2:
				value_state_button = new ItemStack(Material.DIAMOND_BLOCK);
				itemM.setDisplayName(value_state_button, ChatColor.AQUA +  getValueStateName(_value_state_now));
				break;
			case 3:
				value_state_button = new ItemStack(Material.EMERALD_BLOCK);
				itemM.setDisplayName(value_state_button, ChatColor.AQUA +  getValueStateName(_value_state_now));
				break;
		}
		
		itemM.addLore(value_state_button, ChatColor.DARK_PURPLE + "Changed between "+ getValueStateName(2)+", " +getValueStateName(3), false);
		setButtonSwitch(value_state_button, LABELS.VALUE_STATE.getType());
		_inv.setItem(4, value_state_button);
	}
	
	void setSinglePriceButton(Material material,String displayName, double buttonValue, int valueIndex, int itemSlot)
	{
		ItemStack pButton = new ItemStack(material);
		setValueButtonValue(pButton, buttonValue);
		setButtonSwitch(pButton, LABELS.VALUE_PRICE_BUTTON.getType());
		setValueDataIndex(pButton, valueIndex);
		itemM.setDisplayName(pButton,displayName);
		
		itemM.addLore(pButton, ChatColor.GREEN+"M3"+ ChatColor.AQUA +"  / "+ ChatColor.RED + "     :  "+ChatColor.YELLOW+price_button_multiv[2]+"x", false);
		itemM.addLore(pButton, ChatColor.GREEN+"SM1"+ ChatColor.AQUA +" / "+ ChatColor.RED + "SM2:  "+ChatColor.YELLOW+price_button_multiv[1]+"x", false);
		itemM.addLore(pButton, ChatColor.GREEN+"M1"+ ChatColor.AQUA +"  / "+ ChatColor.RED + "  M2:  "+ChatColor.YELLOW+price_button_multiv[0]+"x", false);
		itemM.addLore(pButton, "---------------",false);
		itemM.addLore(pButton, ChatColor.DARK_PURPLE + getValueStateName(valueIndex) +": "+ChatColor.GOLD + _prices[valueIndex], false);
		
		_inv.setItem(itemSlot, pButton);
	}
	
	void refreshPriceButtons()
	{
		int first = 20;
		
		String disp = ChatColor.YELLOW + "===> "+ChatColor.GREEN+"+"+ ChatColor.AQUA +"/"+ ChatColor.RED + "- ";
		Material[] mats = {Material.COAL_ORE,Material.IRON_ORE,Material.GOLD_ORE,Material.DIAMOND_ORE,Material.EMERALD_ORE};
		
		String[] displays = {disp +ChatColor.GOLD + price_button_values[0], disp +ChatColor.GOLD + price_button_values[1], 
							disp +ChatColor.GOLD + price_button_values[2], disp +ChatColor.GOLD + price_button_values[3], 
							disp +ChatColor.GOLD + price_button_values[4]};
 				
		setSinglePriceButton(Material.IRON_INGOT, disp +ChatColor.GOLD + "1.0",1.0, 0, 3);
		setSinglePriceButton(Material.GOLD_INGOT, disp +ChatColor.GOLD + "1.0",1.0, 1, 5);
			
		for(int i = 0; i < 5;++i)
		{
			setSinglePriceButton(mats[i],displays[i],price_button_values[i],_value_state_now,(first+i));	
		}	
	}
		
	void refrestItem()
	{
		setTooltip();
		setButtonSwitch(_copy, LABELS.CONFIRM.getType());
		_inv.setItem(4+9, _copy);
	}
	
	void removeToolTip()
	{
		for(String str : _tooltip_strs)
		{
			itemM.removeLore(_copy, str);
		}
	}
	
	void setTooltip()
	{
		removeToolTip();
		String[] tooltip_strs= {_tooltip_strs[0],
								_tooltip_strs[1] + _prices[3],
								_tooltip_strs[2] + _prices[2],
								_tooltip_strs[3] + _prices[1],
								_tooltip_strs[4] + _prices[0],
								_tooltip_strs[5],
								_tooltip_strs[6]};
		for(String str : tooltip_strs)
		{
			itemM.addLore(_copy, str, false);
		}
	}
}
