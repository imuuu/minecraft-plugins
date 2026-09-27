package imu.GeneralStore.Other;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.GeneralStore.main.Main;
import net.md_5.bungee.api.ChatColor;

public class UniquesINVmodify extends CustomInvLayout implements Listener
{
	boolean _newItem = false;
	boolean _edited = false;
	Inventory _lastInv = null;
	ItemStack _orginalStack, _copy,_orginalStack_copy = null;
	ItemStack _empty = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);

	ShopManager _shopManager = null;
	UniquesINV _uinv;
	
	String _pd_modify = "";
	String _pd_vButton = "gs.uModifyVb";
	String _pd_dataIdx = "gs.eDataId";
	
	Double[] _prices = {};
	Double[] _defautl_prices = {};
	String[] _tooltip_strs= {ChatColor.AQUA+ "======Unique======",
							ChatColor.DARK_PURPLE + "percent : " + ChatColor.GOLD +"",
							ChatColor.DARK_PURPLE + "maxPrice: " + ChatColor.GOLD + "",
							ChatColor.DARK_PURPLE + "minPrice : " + ChatColor.GOLD + "",
							ChatColor.AQUA+ "======Unique======",
							""+ChatColor.DARK_PURPLE + ChatColor.MAGIC+ "# "+ChatColor.GREEN + "CONFIRM by CLICK!"+ ChatColor.DARK_PURPLE+ChatColor.MAGIC+ " #"};
	
	int _total_value_states = 2; //include 0
	int _value_state_now = 0;
	
	Double[] price_button_values = {0.1, 1.0, 10.0, 100.0, 1000.0};
	Double[] price_button_multiv = {1.0, 5.0, 10.0};

	String[] _dataNames = {};
	
	public UniquesINVmodify(Main main, Player player, String name, UniquesINV uinv) 
	{
		super(main, player, name, 9*3);
		
		_main.getServer().getPluginManager().registerEvents(this, _main);
		_uinv = uinv;	
		_pd_modify = _uinv.pd_modify;
		_shopManager = _main.getShopManager();
		_shopManager.uniqueInvModifies.put(_player, this);
		
	}
	
	public void INIT(ItemStack uniqueItemStack, boolean newItem)
	{
		_orginalStack = uniqueItemStack;
		_orginalStack_copy = _orginalStack.clone();
		_shopManager.removeInModify(_orginalStack_copy);
		_copy = _orginalStack.clone();
		
		setData(_shopManager.getUniqueItemPrice(_orginalStack));
		
		_value_state_now = 0;
		_newItem = newItem;
		makeInv();		
	}
	
	void setData(Double[] data)
	{
		_prices = data;
		_defautl_prices = _prices.clone();
		String[] names = {"Min Price", "Max Price" , "Percent"};
		_dataNames = names;		
	}
	
	enum LABELS
	{
		GO_BACK(99),
		COPY(80),
		REMOVE(81),
		VALUE_STATE(10),
		VALUE_PRICE_BUTTON(11),
		RESET_ITEM(12),
		CONFIRM(13);
				
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
		setSwitch(LABELS.GO_BACK, Material.RED_STAINED_GLASS_PANE, ChatColor.AQUA + "<< BACK", 9);
		setSwitch(LABELS.COPY, Material.SLIME_BALL, ChatColor.AQUA + "Copy to your inv", 8);
		setSwitch(LABELS.REMOVE, Material.LAVA_BUCKET, ChatColor.RED + "Remove as unique item", _size-1);
		setSwitch( LABELS.RESET_ITEM, Material.WATER_BUCKET, ChatColor.AQUA + "RESET PRICE", 12);
		
		refreshValueStateButton();
		refrestItem();
		refreshPriceButtons();
	}
	
	void goBack()
	{
		if(_newItem && !_edited && itemM.isEveryThingThis(_prices, 0.0))
		{
			Double[] price = {0.0, 0.0, 0.0};
			_shopManager.addUniqueItem(_orginalStack, price, true);
			_shopManager.removeUniqueTag(_orginalStack_copy);
			itemM.moveItemFirstFreeSpaceInv(_orginalStack_copy, _player, true);
		}
		_shopManager.openUniqueINV(_player);
	}
	
	@EventHandler
	public void onInvOpen(InventoryOpenEvent e)
	{
		if(isThisInv(e))
		{
			_shopManager.addInv(_player);
		}
	}
	
	@EventHandler
	public void onInvCloseEvent(InventoryCloseEvent e)
	{
		if(isThisInv(e))
		{
			_shopManager.removeOpenedInv(_player);
			_uinv.removeInModify(_orginalStack);
			if(_newItem && !_edited && itemM.isEveryThingThis(_prices, 0.0))
			{
				Double[] price = {0.0, 0.0, 0.0};
				_shopManager.addUniqueItem(_orginalStack, price, true);
				_shopManager.removeUniqueTag(_orginalStack_copy);
				itemM.moveItemFirstFreeSpaceInv(_orginalStack_copy, _player, true);
			}
			
		}
	}
	
	@EventHandler
	public void onInvClickEvent(InventoryClickEvent e) 
	{
		if(isThisInv(e))
		{
			e.setCancelled(true);
			ItemStack stack = e.getCurrentItem();
			Integer switch_button = _uinv.getButtonSwitch(stack);
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
					ItemStack _orginalStack_copy_clone = _orginalStack_copy.clone();
					_shopManager.removeUniqueTag(_orginalStack_copy_clone);
					itemM.moveItemFirstFreeSpaceInv(_orginalStack_copy_clone, _player, true);
					return;
				}
				
				if(switch_button == LABELS.REMOVE.getType())
				{
					Double[] price = {0.0,0.0,0.0};
					_shopManager.addUniqueItem(_orginalStack, price,true);
					goBack();
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
					_edited = true;
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
					_edited = true;
					_player.sendMessage(ChatColor.YELLOW + "Unique item has been chanced!");
					_shopManager.addUniqueItem(_orginalStack, _prices.clone(), false);
					goBack();
				}
				
			}
		}
	}
	
	String getValueStateName(int index)
	{		
		return _dataNames[index];
	}
	void changeValueState(int i)
	{
		_value_state_now += i;
		if(_value_state_now < 0)
			_value_state_now = _total_value_states;

		
		if(_value_state_now > _total_value_states)
			_value_state_now = 0;
		
		refreshValueStateButton();
	}
	
	void changePrice(int id, Double addAmount)
	{
		Double[] copy_price = _prices.clone();
		double newPrice =  Math.round((copy_price[_value_state_now] + addAmount)* 100.0) / 100.0;;
		
		if(newPrice < 0)
		{
			newPrice = 0;
		}
		
		if(_value_state_now == 0 && newPrice > _prices[1])
		{
			copy_price[1] = newPrice;
		}
		copy_price[_value_state_now] = newPrice;
		
		if(!_shopManager.isPriceValid(copy_price))
		{
			_player.sendMessage(ChatColor.DARK_RED + "That's not valid price!");
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
		}
		itemM.addLore(value_state_button, ChatColor.DARK_PURPLE + "Changed between "+ getValueStateName(0)+", " +getValueStateName(1) +", " + getValueStateName(2), false);
		_uinv.setButtonSwitch(value_state_button, LABELS.VALUE_STATE.getType());
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
 					
		for(int i = 0; i < 5;++i)
		{
			if(_value_state_now == 2 && i > 2)
			{
				_inv.setItem(first+i, _empty);
				continue;
			}
			
			setSinglePriceButton(mats[i],displays[i],price_button_values[i],_value_state_now,(first+i));
		}	
	}
	
	
	void refrestItem()
	{
		setTooltip();
		_uinv.setButtonSwitch(_copy, LABELS.CONFIRM.getType());
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
								_tooltip_strs[1] + _prices[2],
								_tooltip_strs[2] + _prices[1],
								_tooltip_strs[3] + _prices[0],
								_tooltip_strs[4],
								_tooltip_strs[5]};
		for(String str : tooltip_strs)
		{
			itemM.addLore(_copy, str, false);
		}
	}
}
