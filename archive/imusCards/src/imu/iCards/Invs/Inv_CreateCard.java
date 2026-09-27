package imu.iCards.Invs;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.iAPI.Convs.ConvModData;
import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Interfaces.IModDataInv;
import imu.iAPI.Interfaces.IModDataValue;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ImusUtilities;
import imu.iCards.Main.ImusCards;
import imu.iCards.Other.Card;
import imu.iCards.Other.Weighted_Drop;

public class Inv_CreateCard extends CustomInvLayout implements IModDataInv
{
	
	public class CardDropItem extends Weighted_Drop
	{
		public ItemStack Stack;
		public int StackAmount = 1;
		public CardDropItem(ItemStack stack, double weight)
		{
			super(weight, null);
			this.Stack = stack;
		}
	}
	
	private ArrayList<CardDropItem> _dropItem;
	private final String REAL_ITEM_ID = "real";
	private final String ITEM_INDEX = "index";
	
	//private final int MAX_MODIFY_ROWS = CARD_SECTION.values().length-1;
	
	private int _page = 0;
	
	private int[] _selector_array;

	private int _clickedSlot;
	private ClickType _clickedType;
	
	private HashMap<IButton, Integer> _max_modify_rows;
	
	private Card _card;
	
	public Inv_CreateCard(Player player, Card card)
	{
		super(ImusCards._instance, player, "Creating Card", 9 * 4);
		
		_dropItem = new ArrayList<>();
		//_denyItemMove = DENY_ITEM_MOVE.BOTH;
		_selector_array = new int[_size];
		
		_max_modify_rows = new HashMap<>();
		
		_max_modify_rows.put(BUTTON.CARD_ITEM, CARD_SECTION.values().length-1);
		
		_card = card;
		
		_max_modify_rows.put(BUTTON.LORES, _card.Get_lores().length-1);
	}
	
	enum BUTTON implements IButton, IModDataValue
	{
		NONE,
		GO_LEFT,
		GO_RIGHT,
		CONFIRM,
		CARD_ITEM,
		NAME,
		DESCRIPTION,
		LORES,
		TRIGGER_EVENTS,
	}
	
	enum CARD_SECTION
	{		
		WEIGHT_VALUE,
		STACK_AMOUNT,	
	}
	
	BUTTON GetButtonPress(InventoryClickEvent e)
	{
		ItemStack stack = e.getCurrentItem();
		if(stack == null)
			return BUTTON.NONE;
		
		String buttonName = getButtonName(e.getCurrentItem());
		if(buttonName == null)
			return BUTTON.NONE;
		
		return BUTTON.valueOf(buttonName);
	}
	
	private int GetMaxRows(IButton button)
	{

		if(_max_modify_rows.containsKey(button)) return _max_modify_rows.get(button);
		
		return 1;
	}
	
	@Override
	public void invClosed(InventoryCloseEvent e)
	{
		
	}
	
	void Modify_Ench(CARD_SECTION selector)
	{
		String str = "";
		switch (selector) 
		{
			case WEIGHT_VALUE:
				str = "&3Give &2Weight Value";
			break;
			case STACK_AMOUNT:
				str = "&3Give &2Stack Amount";
			break;
		}
		Metods._ins.ConversationWithPlayer(_player, new ConvModData(BUTTON.CARD_ITEM,this,str));
		_player.closeInventory();
	}
	
	private CARD_SECTION GetCurrentSection() { return CARD_SECTION.values()[_selector_array[_clickedSlot]];	}
	
	private int GetCurrentSectionIndex() { return _selector_array[_clickedSlot]; };
	
	@Override
	public void onClickInsideInv(InventoryClickEvent e)
	{
		BUTTON button = GetButtonPress(e);
		Integer slot = GetSLOT(e.getCurrentItem());
		
		System.out.println("slot: "+slot + " button: "+button + " click: "+e.getClick());
		
		if(slot == null) return;
		
		_clickedSlot = slot;
		_clickedType = e.getClick();
		switch (button) 
		{
		case CARD_ITEM:
			
			if(e.getClick() == ClickType.MIDDLE)
			{
				_dropItem.remove((int)slot);
				_selector_array[slot] = 0;
				setupButtons();			
				return;
			}
			
			if(e.getClick() == ClickType.RIGHT) 
			{
				MoveSelector_OnItem(e.getCurrentItem(), button);
				RefresItemTooltip(slot);
				return;
			}
			Modify_Ench(GetCurrentSection());
			return;
		case LORES:
		{
			_max_modify_rows.put(BUTTON.LORES, _card.Get_lores().length-1);
			
			if(e.getClick() == ClickType.RIGHT)
			{
				MoveSelector_OnItem(e.getCurrentItem(), button);
				RefreshButton_Lores();
				return;
			}
			if(_clickedType == ClickType.MIDDLE)
			{
				if(_card.Get_lores() == null || _card.Get_lores().length == 0) return;
				
				String[] lores = _card.Get_lores().clone();
 				lores = ImusUtilities.RemoveElementAtIndex(lores, GetCurrentSectionIndex());
				_card.Set_lores(lores);
				_max_modify_rows.put(BUTTON.LORES, _card.Get_lores().length-1);
				_selector_array[slot] = 0;
				RefreshButton_Lores();
				return;
			}
			
			if(_clickedType != ClickType.SHIFT_LEFT && _clickedType != ClickType.SHIFT_RIGHT)
			{
				if(_card.Get_lores() == null || _card.Get_lores().length == 0) return;
			}
			
			
			
			String str = "&3Give lore";
			Metods._ins.ConversationWithPlayer(_player, new ConvModData(BUTTON.LORES,this,str));
			_player.closeInventory();
			return;
		}
		case NAME:
		{
			String str = "&3Give Card Name";
			Metods._ins.ConversationWithPlayer(_player, new ConvModData(BUTTON.NAME,this,str));
			_player.closeInventory();
			return;
		}
		case NONE:
			return;
		
		default:
			break;
	
		}
		
	}

	@Override
	protected void onClickPlayerInv(InventoryClickEvent e)
	{
		if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
		
		AddItem(e.getCurrentItem());
		setupButtons();
	}
	
	void MoveSelector_OnItem(ItemStack stack, IButton button)
	{
		Integer slot = GetSLOT(stack);
		_selector_array[slot]++;

		if(_selector_array[slot] < 0) _selector_array[slot] = GetMaxRows(button);
		if(_selector_array[slot] > GetMaxRows(button)) _selector_array[slot] = 0;
	}
	

	void RefresItemTooltip(int slot)
	{					
		CardDropItem info = _dropItem.get(slot);
		String[] lores = new String[]
				{
						"&6M1 &2Modify &6value &e& &6M2 &bMove &6Selector",
						"&6M3 &cRemove",
						" ",					
						"&6 Weight:&2 " + info.Get_weight(),
						"&6 Stack Amount:&2 " + info.StackAmount,

				};

		Metods._ins.SetLores(info.Stack, MakeItFat(lores,slot,3), false);
		SetITEM(slot, info.Stack);
	}
	
	String[] MakeItFat(String[] lores, int slot,int offset)
	{
		for(int l = 0; l < lores.length; l++)
		{						
			if((_selector_array[slot]+offset) != (l+offset)) continue;
			String[] ar = lores[(l+offset)].split(" ");
			for(int ll= 0; ll < ar.length; ll++)
			{
				ar[ll] = "&l"+ar[ll];
			}

			lores[(l+offset)] = "&b&l==> &r"+Metods._ins.CombineArrayToOneString(ar, " ");
			break;
		}
		return lores;
	}
	@Override
	public void setupButtons()
	{
		for(int i = _size-9; i < _size; i++) {setupButton(BUTTON.NONE, Material.CYAN_STAINED_GLASS_PANE, " ", i);}
		//setupButton(BUTTON.GO_LEFT, Material.BIRCH_SIGN, "&b<<", _size-9);
		//setupButton(BUTTON.GO_RIGHT, Material.BIRCH_SIGN, "&b>>", _size-1);
		setupButton(BUTTON.CONFIRM, Material.DIAMOND, "&bConfirm Items & Continue", _size-1);
		
		
		RefreshButton_Name();
		RefreshButton_Lores();
		RefreshButton_EventTrigger();

		
		for(int i = 0; i < 27; ++i)
		{
			int index = i + _page;
			
			if(index >= _dropItem.size()) 
			{
				SetITEM(index,Material.AIR);
				continue;
			}

			RefresItemTooltip(index);
		}
	}
	private void RefreshButton_Lores()
	{
		ItemStack stack = new ItemStack(Material.PAPER);
		Metods.setDisplayName(stack, "&bSet Lores");
		//setupButton(BUTTON.LORES, Material.PAPER, "&bSet Lores", _size-6);
		String[] lores_top = 
			{
				"&6M1 &2Modify &6value &e& &6M2 &bMove &6Selector",
				"&bS&6M1 &2Add &fnew &9Below &e& &bS&6M2 &2Add &9Top",
				"&6M3 &cRemove &3Selected Lore",
				" ",		
			};
		
		String[] lores;
		if(_card.Get_lores() == null || _card.Get_lores().length == 0)
		{
			lores = ImusUtilities.CombineArrays(lores_top,  new String[] {"&9= No Lores= "});
		}
		else
		{
			lores = ImusUtilities.CombineArrays(lores_top,  _card.Get_lores().clone());
			lores = MakeItFat(lores, _size-6, lores_top.length);
		}
		
		
		
		Metods._ins.SetLores(stack,lores , false);
		SetButton(stack, BUTTON.LORES);
		SetITEM(_size-6, stack);
		
	}
	
	private void RefreshButton_Name()
	{

		ItemStack stack = new ItemStack(Material.NAME_TAG);
		Metods.setDisplayName(stack, "&bGive Card name");
		String[] lores = new String[] {
				"&6Name: &r"+_card.GetName(),
		};
		
		Metods._ins.SetLores(stack,lores , false);
		SetButton(stack, BUTTON.NAME);
		SetITEM(_size-8, stack);
	}
	
	private void RefreshButton_EventTrigger()
	{
		ItemStack stack = new ItemStack(Material.BEACON);
		Metods.setDisplayName(stack, "&bAdd Trigger Events");
		String[] lores = new String[] {
				"&6=== EVENTS === ",
		};
		
		Metods._ins.SetLores(stack,lores , false);
		SetButton(stack, BUTTON.TRIGGER_EVENTS);
		SetITEM(_size-4, stack);
	}
//	private void SetIndex(ItemStack stack, int i)
//	{
//		ImusAPI._metods.setPersistenData(stack, ITEM_INDEX, PersistentDataType.INTEGER, i);
//	}
//	
//	private int GetIndex(ItemStack stack)
//	{
//		return ImusAPI._metods.getPersistenData(stack, ITEM_INDEX, PersistentDataType.INTEGER);
//	}
	@Override
	public void openThis() 
	{
		super.openThis();
		setupButtons();
	}
	
	private void AddItem(ItemStack stack)
	{
		
		ItemStack clone = stack.clone();
		clone.setAmount(1);
		SetButton(clone, BUTTON.CARD_ITEM);
		String str_realStack = ImusAPI._metods.EncodeItemStack(stack);
		ImusAPI._metods.setPersistenData(clone, REAL_ITEM_ID, PersistentDataType.STRING, str_realStack);
		
		CardDropItem cardDrop = new CardDropItem(clone, 1);
		_dropItem.add(cardDrop);
	}

	@Override
	public boolean SetModData(IModDataValue value, String anwser)
	{
		BUTTON button = (BUTTON) value;
		
		boolean IsNumber = Metods._ins.isDigit(anwser);
		
		switch (button)
		{
		case CARD_ITEM:
		{
			CARD_SECTION section = GetCurrentSection();
			
			if(!IsNumber) return false;
			double number = Double.parseDouble(anwser);
			
			switch (section)
			{
			case WEIGHT_VALUE:
			{
				_dropItem.get(_clickedSlot).Set_weight(number);
				openThis();
				return true;
			}
			case STACK_AMOUNT:
			{
				_dropItem.get(_clickedSlot).StackAmount = (int)number;
				openThis();
				return true;
			}
				
			}
			return false;
		}
		case CONFIRM:
			break;

		case LORES:
			String[] lores = _card.Get_lores().clone();
			int index = 0;
			if(_clickedType == ClickType.SHIFT_LEFT)
			{
				index = GetCurrentSectionIndex()+1;
				
				if(_card.Get_lores() == null || _card.Get_lores().length == 0) index = 0;
				
				lores = ImusUtilities.AddElementAtIndex(lores, anwser, index);
			}
			if(_clickedType == ClickType.SHIFT_RIGHT)
			{
				index = GetCurrentSectionIndex();
				
				if(index <= 0 || _card.Get_lores() == null || _card.Get_lores().length == 0) index = 0;
				

				lores = ImusUtilities.AddElementAtIndex(lores, anwser, index);
			}
			if(_clickedType == ClickType.LEFT)
			{
				lores[GetCurrentSectionIndex()] = anwser;
			}

			_card.Set_lores(lores);
			_max_modify_rows.put(BUTTON.LORES, _card.Get_lores().length-1);
			openThis();
			break;
		case NAME:
			_card.SetName(anwser);
			openThis();
			break;
		case NONE:
			break;
		case TRIGGER_EVENTS:
			break;
		default:
			break;
		
		}
		
		
		
		
		return false;
	}

	@Override
	public void SetModDataFAILED(IModDataValue modData, String arg1, String arg2)
	{
		
	}

}
