package imu.GS.Invs;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.GS.Main.Main;
import imu.GS.Managers.ShopManager;
import imu.GS.Managers.UniqueManager;
import imu.GS.ShopUtl.ShopItemBase;
import imu.GS.ShopUtl.ShopItems.ShopItemUnique;
import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;


public class ShopItemPriceModifyINV extends CustomInvLayout 
{
	boolean _edited = false;
	Inventory _lastInv = null;
	ItemStack _orginalStack, _copy, _orginalStack_copy = null;
	ItemStack _empty = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);

	ShopManager _shopManager = null;
	UniqueManager _uniqueManager = null;
	UniquesINV _uinv;
	
	String _pd_modify = "";
	String _pd_vButton = "gs.uModifyVb";
	String _pd_dataIdx = "gs.eDataId";
	
	Double[] _price;
	Double[] _default_price;
	String[] _tooltip_strs= {ChatColor.AQUA+ "======Unique======",
							ChatColor.DARK_PURPLE + "price : " + ChatColor.GOLD +"",
							ChatColor.AQUA+ "======Unique======",
							""+ChatColor.DARK_PURPLE + ChatColor.MAGIC+ "# "+ChatColor.GREEN + "CONFIRM by CLICK!"+ ChatColor.DARK_PURPLE+ChatColor.MAGIC+ " #"};
	
	//int _total_value_states = 2; //include 0
	int _value_state_now = 0;
	
	Double[] price_button_values = {0.1, 1.0, 10.0, 100.0, 1000.0};
	Double[] price_button_multiv = {1.0, 5.0, 10.0};

	String[] _dataNames = {};
	Main _main;
	ShopItemBase _sib;
	public ShopItemPriceModifyINV(Main main, Player player, UniquesINV uinv, ShopItemBase sis) 
	{
		super(main, player, "Modify", 9*3);
		_main = main;
		_uinv = uinv;	
		_pd_modify = _uinv.pd_modify;
		_shopManager = main.get_shopManager();
		_uniqueManager = _shopManager.GetUniqueManager();
		_sib = sis;
		INIT(sis.GetRealItem());
		
	}
	
	public void INIT(ItemStack uniqueItemStack)
	{
		_price = new Double[] {_sib.GetItemPrice().GetPrice()};
		_default_price = _price.clone();
		_orginalStack = uniqueItemStack;
		
		_orginalStack_copy = _orginalStack.clone();
		_copy = _orginalStack.clone();
		
		setData(_sib.GetItemPrice().GetPrice());
		
		_value_state_now = 0;
		
		makeInv();	
		
	}
	
	void setData(Double price)
	{
		_price[0] = price;
		_default_price[0] = price;
		String[] names = {"Price"};
		_dataNames = names;		
	}
	
	protected enum BUTTON implements IButton
	{
		GO_BACK(99),
		COPY(80),
		REMOVE(81),
		VALUE_STATE(10),
		VALUE_PRICE_BUTTON(11),
		RESET_ITEM(12),
		CONFIRM(13);
		
		int type;
		
		BUTTON(int i)
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
		ImusAPI._metods.setPersistenData(stack, _pd_vButton, PersistentDataType.DOUBLE, d);
	}
	
	Double getValueButtonValue(ItemStack stack)
	{
		return ImusAPI._metods.getPersistenData(stack, _pd_vButton, PersistentDataType.DOUBLE);
	}
	
	void setValueDataIndex(ItemStack stack, int id)
	{
		ImusAPI._metods.setPersistenData(stack, _pd_dataIdx, PersistentDataType.INTEGER, id);
	}
	
	Integer getValueDataIndex(ItemStack stack)
	{
		return ImusAPI._metods.getPersistenData(stack, _pd_dataIdx, PersistentDataType.INTEGER);
	}
	void makeInv()
	{				
		Metods.setDisplayName(_empty, " ");
		for(int i = 0; i < _size ; ++i)
		{
			_inv.setItem(i, _empty);
		}
		
		refreshButtons();	
		refrestItem();
				
	}
	
	
	
	void refreshButtons()
	{
		setupButton(BUTTON.GO_BACK, Material.RED_STAINED_GLASS_PANE, ChatColor.AQUA + "<< BACK", 9);
		setupButton(BUTTON.COPY, Material.SLIME_BALL, ChatColor.AQUA + "Copy to your inv", 8);
		setupButton(BUTTON.REMOVE, Material.LAVA_BUCKET, ChatColor.RED + "Remove as unique item", _size-1);
		setupButton( BUTTON.RESET_ITEM, Material.WATER_BUCKET, ChatColor.AQUA + "RESET PRICE", 12);
		
		refreshValueStateButton();
		refrestItem();
		refreshPriceButtons();
	}
	
	void GoBack()
	{
		
		new UniquesINV(_main, _player).openThis();
	}
		
	@Override
	public void onClickInsideInv(InventoryClickEvent e) 
	{
		ItemStack stack = e.getCurrentItem();
		if(stack == null)
			return;
		String bName = getButtonName(stack);
//		if(Strings.isNullOrEmpty(bName))
//			return;
		
		if(bName == null | bName == "") return;
		
		BUTTON button = BUTTON.valueOf(bName);
		ClickType click = e.getClick();
		switch (button) 
		{
		case CONFIRM:
			_edited = true;
			_player.sendMessage(ChatColor.YELLOW + "Unique item has been chanced!");
			_sib.GetItemPrice().SetPrice(_price[0]);
			_uniqueManager.AddUniqueItem((ShopItemUnique)_sib, true);
			//_shopManager.addUniqueItem(_orginalStack, _prices.clone(), false);
			
			GoBack();
			return;
		case COPY:		
			//ImusAPI._metods.moveItemFirstFreeSpaceInv(_sis.GetRealItem(), _player, true);
			//System.out.println("copy: "+_sib.GetRealItem());
			ImusAPI._metods.InventoryAddItemOrDrop(_sib.GetRealItem().clone(), _player);
			
			break;
		case GO_BACK:
			GoBack();
			return;
		case REMOVE:
			_uniqueManager.RemoveUnique(_sib.GetRealItem());			
			ImusAPI._metods.InventoryAddItemOrDrop(_sib.GetRealItem().clone(), _player);
			GoBack();
			return;
		case RESET_ITEM:
			_price = _default_price;
			refreshButtons();
			break;
		case VALUE_PRICE_BUTTON:
			
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

		case VALUE_STATE:
			if(click == ClickType.LEFT)
				changeValueState(1);
			
			if(click == ClickType.RIGHT)
				changeValueState(-1);
			
			refreshButtons();
			return;

		default:
			break;
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
			_value_state_now = _dataNames.length-1;

		
		if(_value_state_now > _dataNames.length-1)
			_value_state_now = 0;
		
		refreshValueStateButton();
	}
	
	void changePrice(int id, Double addAmount)
	{
		Double[] copy_price = _price.clone();
		double newPrice =  Math.round((copy_price[_value_state_now] + addAmount)* 100.0) / 100.0;;
		
		if(newPrice < 0)
		{
			newPrice = 0;
		}

		if(_value_state_now == 0 &&  _price.length > 1 && newPrice > _price[1])
		{
			copy_price[1] = newPrice;
		}
		copy_price[_value_state_now] = newPrice;
		
//		if(!_shopManager.isPriceValid(copy_price))
//		{
//			_player.sendMessage(ChatColor.DARK_RED + "That's not valid price!");
//			return;
//		}
		
		_price = copy_price;
	}
	void refreshValueStateButton()
	{
		ItemStack value_state_button = new ItemStack(Material.BEDROCK);
		switch(_value_state_now)
		{
			case 0:
				value_state_button = new ItemStack(Material.IRON_BLOCK);
				Metods.setDisplayName(value_state_button, ChatColor.AQUA + getValueStateName(_value_state_now));
				break;
			case 1:
				value_state_button = new ItemStack(Material.GOLD_BLOCK);
				Metods.setDisplayName(value_state_button, ChatColor.AQUA +  getValueStateName(_value_state_now));
				break;
			case 2:
				value_state_button = new ItemStack(Material.DIAMOND_BLOCK);
				Metods.setDisplayName(value_state_button, ChatColor.AQUA +  getValueStateName(_value_state_now));
				break;
		}
		String lore =ChatColor.DARK_PURPLE + "Changed between ";
		for(int i = 0; i < _dataNames.length; ++i)
		{
			lore += _dataNames[i]+ " ";
		}
		ImusAPI._metods.addLore(value_state_button, lore, false);
		//_uinv.setButtonSwitch(value_state_button, LABELS.VALUE_STATE.getType());
		SetButton(value_state_button, BUTTON.VALUE_STATE);
		_inv.setItem(4, value_state_button);
	}
	
	void setSinglePriceButton(Material material,String displayName, double buttonValue, int valueIndex, int itemSlot)
	{
		ItemStack pButton = new ItemStack(material);
		setValueButtonValue(pButton, buttonValue);
		SetButton(pButton, BUTTON.VALUE_PRICE_BUTTON);
		setValueDataIndex(pButton, valueIndex);
		Metods.setDisplayName(pButton,displayName);
		
		ImusAPI._metods.addLore(pButton, ChatColor.GREEN+"M3"+ ChatColor.AQUA +"  / "+ ChatColor.RED + "     :  "+ChatColor.YELLOW+price_button_multiv[2]+"x", false);
		ImusAPI._metods.addLore(pButton, ChatColor.GREEN+"SM1"+ ChatColor.AQUA +" / "+ ChatColor.RED + "SM2:  "+ChatColor.YELLOW+price_button_multiv[1]+"x", false);
		ImusAPI._metods.addLore(pButton, ChatColor.GREEN+"M1"+ ChatColor.AQUA +"  / "+ ChatColor.RED + "  M2:  "+ChatColor.YELLOW+price_button_multiv[0]+"x", false);
		ImusAPI._metods.addLore(pButton, "---------------",false);
		ImusAPI._metods.addLore(pButton, ChatColor.DARK_PURPLE + getValueStateName(valueIndex) +": "+ChatColor.GOLD + _price[valueIndex], false);
		
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
		//_uinv.setButtonSwitch(_copy, LABELS.CONFIRM.getType());
		SetButton(_copy, BUTTON.CONFIRM);
		_inv.setItem(4+9, _copy);
	}
	
	void removeToolTip()
	{
		for(String str : _tooltip_strs)
		{
			ImusAPI._metods.removeLore(_copy, str);
		}
	}
	
	void setTooltip()
	{
		removeToolTip();
		String[] tooltip_strs= {_tooltip_strs[0],
								_tooltip_strs[1] + _price[0],
								_tooltip_strs[2],
								_tooltip_strs[3]};
		for(String str : tooltip_strs)
		{
			ImusAPI._metods.addLore(_copy, str, false);
		}
	}

	

	@Override
	public void setupButtons() {

	}

	@Override
	public void invClosed(InventoryCloseEvent arg0) {

	}
}
