package imu.GS.Invs;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.base.Strings;

import imu.GS.Main.Main;
import imu.GS.Other.CustomPriceData;
import imu.GS.Prompts.ConvCCPINV;
import imu.GS.Prompts.ConvCCPINVsavePC;
import imu.GS.ShopUtl.ShopItemModData;
import imu.GS.ShopUtl.ItemPrice.PriceCustom;
import imu.GS.ShopUtl.ShopItems.ShopItemStockable;
import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.CustomInvLayout;

public class CreateCustomPriceInv extends CustomInvLayout
{
	ShopItemStockable _sis;
	ShopStocableModifyINV _smmi;
	ShopItemModData _modData;
	Boolean[] _slotLock;
	CPriceItem[] _priceItems;
	Main _main;
	
	int _moneyPos = _size - 3;
	int _itemPos = _size - 5;
	
	int _uniqueSlots[] = {_moneyPos, _itemPos};
	
	public CreateCustomPriceInv(Plugin main, Player player, ShopStocableModifyINV smmi, ShopItemStockable sis, ShopItemModData modData) 
	{
		super(main, player, "&9=== &6Create Custom Price &9===", 9*6);
		_main = (Main)main;
		_sis = sis;
		_smmi = smmi;
		_modData = modData;
		_denyItemMove = DENY_ITEM_MOVE.NONE;
		_slotLock = new Boolean[_size];
		_priceItems = new CPriceItem[_size];
		Arrays.fill(_slotLock, false);
		setupButtons();
		LoadModData();
		//System.out.println("CONSTRUCT");
		
	}
	public class CCPdata
	{
		public int _slot;
		public double value;
		CCPdata(int slot){_slot = slot;}
	}
	
	class CPriceItem
	{
		public ItemStack _realStack;
		private ItemStack _displayStack;
		public int _slot;
		private double _value = 1;
		boolean roundIt = true;
		BUTTON _button;
		public CPriceItem(ItemStack real, int slot, BUTTON button)
		{
			//System.out.println("construct of cprice: "+ real.getType());
			_value = real.getAmount();
			_realStack = new ItemStack(real);
			_displayStack =  new ItemStack(real);
			real.setAmount(0);
			_displayStack.setAmount(1);
			_slot = slot;
			_button = button;
			
		}
		
		public double GetAmount()
		{
			return _value;
		}
		
		public ItemStack GetDisplayItem()
		{

			_displayStack = _realStack.clone();
			Tooltip();
			return _displayStack;
		}
		
		public void SetAmount(double amount)
		{
			_value = amount;
			if(roundIt) _value = Math.round(amount);
			
		}
		
		void Tooltip()
		{
			SetButton(_displayStack, _button);
			String[] lores =
			{
				"&bM1: &2Set new Amount  &bM2: &cRemove",
				"&9Amount: &1"+_value,
			};
			ImusAPI._metods.addLore(_displayStack, lores);
			//System.out.println("setting value of  cprice: "+amount);
		}
	}
	enum BUTTON implements IButton
	{
		NONE,
		BACK,
		CONFIRM,
		PRICE_ITEM,
		CLONE_ITEMS,
		ITEM,
		LOAD_TEMP_SAVED_CUSTOM_PRICE,
		SAVE_TEMP_CUSTOM_PRICE,
	}
	boolean IsUniqueSlot(int slot)
	{
		for(int i = 0; i < _uniqueSlots.length; i++) {if(_uniqueSlots[i] == slot) return true;}
		return false;
	}
	void LoadModData()
	{
		if(!(_modData._itemPrice instanceof PriceCustom))
			return;
		
		int i = 0;
		for(CustomPriceData data : ((PriceCustom)_modData._itemPrice).GetItems())
		{
			_priceItems[i] = new CPriceItem(data._stack.clone(),i, BUTTON.PRICE_ITEM);
			_priceItems[i++].SetAmount(data._amount);
		}
		if(((PriceCustom)_modData._itemPrice).GetPrice() > 0)
		{
			_priceItems[_moneyPos].roundIt = false;
			_priceItems[_moneyPos].SetAmount(((PriceCustom)_modData._itemPrice).GetPrice());
			
		}
		_priceItems[_itemPos].SetAmount((double)((PriceCustom)_modData._itemPrice).GetMinimumStackAmount());
		LoadPriceItems();
	}
	
	public void LoadPriceCustom(PriceCustom pc)
	{
		
		for(int i = 0; i < pc.GetItems().length; i++)
		{
			SetCPriceItem(i, pc.GetItems()[i]._stack.clone(), pc.GetItems()[i]._amount, BUTTON.PRICE_ITEM);
		}
		_priceItems[_moneyPos].SetAmount(pc.GetPrice());
		_priceItems[_moneyPos].roundIt = false;
		LoadPriceItems();
	}
	
	public void SetData(CCPdata data)
	{
		if(data._slot == _moneyPos)
		{
			_priceItems[data._slot].roundIt = false;
		}
		_priceItems[data._slot].SetAmount(data.value);

		
		LoadPriceItems();
	}
	
	@Override
	public void setupButtons() {
		
		setupButton(BUTTON.BACK, Material.RED_STAINED_GLASS_PANE,"&cBACK", _size-9); _slotLock[_size-9] = true;
		
		setupButton(BUTTON.CONFIRM, Material.GREEN_STAINED_GLASS_PANE,"&1CONFIRM", _size-1); _slotLock[_size-1] = true;	
		for(int i = _size-2; i > _size-9; i--) {setupButton(BUTTON.NONE, Material.BLACK_STAINED_GLASS_PANE," ", i); _slotLock[i] = true;}
		//setupButton(BUTTON.PRICE_ITEM, Material.PAPER,"&9ADD Money", _moneyPos);// _slotLock[_moneyPos] = true;
		
		LockItem(setupButton(BUTTON.PRICE_ITEM, Material.PAPER,"&9Add Money", _moneyPos), _moneyPos, BUTTON.PRICE_ITEM);
		
		ItemStack stack = _sis.GetRealItem().clone();
		_metods.addLore(stack, "&6Minium sell amount", false);
		LockItem(stack, _itemPos, BUTTON.ITEM);
		
		
		setupButton(BUTTON.CLONE_ITEMS, Material.SLIME_BALL,"&bClone Items to your inv", _size-8); _slotLock[_size-8] = true;
		setupButton(BUTTON.LOAD_TEMP_SAVED_CUSTOM_PRICE, Material.ANCIENT_DEBRIS,"&bLoad temp cPrices", _size-7); _slotLock[_size-7] = true;
		setupButton(BUTTON.SAVE_TEMP_CUSTOM_PRICE, Material.NETHERITE_SCRAP,"&bSave this cPrice as temp", _size-6); _slotLock[_size-6] = true;
		_priceItems[_moneyPos].SetAmount(0);
	}
	
	
	boolean CheckSlotLock(int slot)
	{
		if(slot < 0 || slot >= _size) return false;
		return _slotLock[slot];
	}
	
	void LoadPriceItems()
	{
		for(int i = 0; i < _inv.getSize()-10; i++) {_inv.setItem(i, null); _slotLock[i] = false;}
		for(CPriceItem cpi : _priceItems)
		{
			if(cpi == null) continue;
			
			_inv.setItem(cpi._slot, cpi.GetDisplayItem());
			_slotLock[cpi._slot] = true;
		}
	}
	
	void CheckInv()
	{
		new BukkitRunnable() 
		{		
			@Override
			public void run() 
			{
				//System.out.println("loadInv");
				for(int i= 0; i < _size-9; i++)
				{
					ItemStack stack  = _inv.getItem(i);
					if(stack == null) 
					{
						//System.out.println("slot: "+i +" is null");
						_slotLock[i] = false;
						continue;
					}
					boolean found = false;
					for(CPriceItem cpi : _priceItems)
					{
						if(cpi != null && cpi._realStack.isSimilar(stack) && cpi._slot != i)
						{
							cpi.SetAmount(1);
							//System.out.println("removing :"+stack.getType()+ " slot: "+i);
							stack.setAmount(0);
							_slotLock[i] = false;
							found = true;
							break;
						}
					}
					if(found) continue;
					
					//if(_slotLock[i]) continue;
					LockItem(stack, i, BUTTON.PRICE_ITEM);
				}
			}
		}.runTaskLater(_plugin,1);
		
		LoadPriceItems();
	}
	
	void SetCPriceItem(int idx, ItemStack stack, int amount, BUTTON button)
	{
		stack.setAmount(1);
		_priceItems[idx] = new CPriceItem(stack, idx, button);		
		_priceItems[idx]._value = amount;
		
		_slotLock[idx] = true;
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				_inv.setItem(idx, _priceItems[idx].GetDisplayItem());
			}
		}.runTaskLater(_main, 1);
	}
	
	void LockItem(ItemStack stack, int slot, BUTTON button)
	{
		if(stack == null || stack.getType() == Material.AIR) return ;
		if(slot < 0 || slot > _size-1) return;
		if(_priceItems[slot] != null) return;
		//System.out.println("new lock: "+stack.getType() + " slot: "+ slot);
		SetCPriceItem(slot, stack, stack.getAmount(), button);
		
//		_priceItems[slot] = new CPriceItem(stack, slot, button);		
//		
//		_slotLock[slot] = true;
//		new BukkitRunnable() {
//			
//			@Override
//			public void run() 
//			{
//				_inv.setItem(slot, _priceItems[slot].GetDisplayItem());
//			}
//		}.runTaskLater(_main, 1);
		
		
	}
	
	
	
	@EventHandler
	public void OnDrag(InventoryDragEvent e)
	{		
		if(isThisInv(e))
		{
			CheckInv();			
		}
	}
	
	@EventHandler
	public void onClickk(InventoryClickEvent e)
	{
		//System.out.println("click2");
		if(isThisInv(e))
		{
			if(e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) 
			{
				CheckInv();
				return;
			}
		}
		
	}
	
	BUTTON GetBUTTON(ItemStack stack)
	{
		if(stack == null) return BUTTON.NONE;
		String bName = getButtonName(stack);		
		if(Strings.isNullOrEmpty(bName)) return BUTTON.NONE;
		return BUTTON.valueOf(bName);
	}
	
	PriceCustom CreatePriceCustom()
	{
		ArrayList<CustomPriceData> stacks = new ArrayList<CustomPriceData>();
		double money = 0;
		for(CPriceItem cpi : _priceItems)
		{
			if(cpi == null) continue;
			if(cpi._slot == _moneyPos) 
			{
				money = cpi._value;
				continue;
			}
			
			if(cpi._slot == _itemPos) continue;
			
			ItemStack cpiStack = cpi._realStack.clone();
			cpiStack.setAmount(1);
			stacks.add(new CustomPriceData(cpiStack, (int)cpi._value));
		}
		CustomPriceData[] array = new CustomPriceData[stacks.size()];
		for(int i = 0; i < array.length; ++i) {array[i] = stacks.get(i);}
		return (PriceCustom)new PriceCustom().SetItemsAndPrice(array, money, (int)_priceItems[_itemPos].GetAmount());
	}
	
	@Override
	public void onClickInsideInv(InventoryClickEvent e) 
	{
		//System.out.println("click");
		if(CheckSlotLock(e.getSlot())) e.setCancelled(true);
		
		ConversationFactory cf;
		String question;
		Conversation conv;
		switch (GetBUTTON(e.getCurrentItem())) 
		{
		case NONE:
			break;
		case BACK:
			_player.closeInventory();
			_smmi.openThis();
			return;
		case CLONE_ITEMS:
			for(CPriceItem item : _priceItems) {if(item != null && !IsUniqueSlot(item._slot))ImusAPI._metods.InventoryAddItemOrDrop(item._realStack.clone(), _player);}
			break;
		case CONFIRM:
//			ArrayList<CustomPriceData> stacks = new ArrayList<CustomPriceData>();
//			double money = 0;
//			for(CPriceItem cpi : _priceItems)
//			{
//				if(cpi == null) continue;
//				if(cpi._slot == _moneyPos) 
//				{
//					money = cpi._value;
//					continue;
//				}
//				
//				if(cpi._slot == _itemPos) continue;
//				
//				ItemStack cpiStack = cpi._realStack.clone();
//				cpiStack.setAmount(1);
//				stacks.add(new CustomPriceData(cpiStack, (int)cpi._value));
//			}
//			CustomPriceData[] array = new CustomPriceData[stacks.size()];
//			for(int i = 0; i < array.length; ++i) {array[i] = stacks.get(i);}
			_modData._itemPrice = CreatePriceCustom();
			_player.closeInventory();
			_smmi.SetModData(_modData);
			_smmi.openThis();
			return;
		case PRICE_ITEM:
			if(e.getClick() == ClickType.RIGHT) 
			{
				//System.out.println("remove");
				if(e.getSlot() == _moneyPos) {
					_priceItems[e.getSlot()].SetAmount(0);
					_inv.setItem(_priceItems[e.getSlot()]._slot, _priceItems[e.getSlot()].GetDisplayItem());
					return;
				}
				ItemStack stack =_priceItems[e.getSlot()]._realStack.clone();
				ImusAPI._metods.InventoryAddItemOrDrop(stack, _player);
				_priceItems[e.getSlot()]._realStack.setAmount(0);
				_priceItems[e.getSlot()]._displayStack.setAmount(0);
				_priceItems[e.getSlot()] = null;
				_slotLock[e.getSlot()] = false;
				_inv.setItem(e.getSlot(), null);
				//CheckInv();
				
				return;
			}
			cf = new ConversationFactory(_main);
			question = "&3Give new amount";
			conv = cf.withFirstPrompt(new ConvCCPINV(_main, this, new CCPdata(e.getSlot()), question)).withLocalEcho(true).buildConversation(_player);
			conv.begin();
			_player.closeInventory();
			return;
		case ITEM:
			if(e.getClick() == ClickType.RIGHT) 
			{
				_priceItems[e.getSlot()]._value = _sis.Get_amount();
				_inv.setItem(e.getSlot(), _priceItems[e.getSlot()].GetDisplayItem());
				return;
			}
			
			cf = new ConversationFactory(_main);
			question = "&3Give minimum amount to sell";
			conv = cf.withFirstPrompt(new ConvCCPINV(_main, this, new CCPdata(e.getSlot()), question)).withLocalEcho(true).buildConversation(_player);
			conv.begin();
			_player.closeInventory();
			return;
		case LOAD_TEMP_SAVED_CUSTOM_PRICE:
			new LoaderPriceCustomInv(_main, _player, _smmi, _sis, _modData, CreatePriceCustom()).openThis();
			return;
		case SAVE_TEMP_CUSTOM_PRICE:
			question = "&3Give save name for temp custom price";
			ImusAPI._metods.ConversationWithPlayer(_player, new ConvCCPINVsavePC(_main, this, CreatePriceCustom(), question));
			_player.closeInventory();
			return;
		
		}
		
		if(e.getClick() != ClickType.RIGHT) 
		{
			LockItem(_droppedStack, _droppedSlot, BUTTON.PRICE_ITEM);
			e.getCursor().setAmount(0);
			//System.out.println("cursor: "+);
		}
	}
	
	
	
	
	@Override
	public void invClosed(InventoryCloseEvent e) 
	{
		_main.UnregisterInv(this);
	}
	
	@Override
	public void openThis() 
	{
		super.openThis();
		_main.RegisterInv(this);
	}
	
	

}
