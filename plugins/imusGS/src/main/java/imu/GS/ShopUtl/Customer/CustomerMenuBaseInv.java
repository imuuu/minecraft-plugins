package imu.GS.ShopUtl.Customer;


import java.util.ArrayList;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import imu.GS.Invs.BuyCustomPriceINV;
import imu.GS.Invs.CustomerInv;
import imu.GS.Main.Main;
import imu.GS.Managers.ShopManager;
import imu.GS.Managers.UniqueManager;
import imu.GS.Other.LogData;
import imu.GS.ShopUtl.ShopItemBase;
import imu.GS.ShopUtl.ShopItemResult;
import imu.GS.ShopUtl.ShopNormal;
import imu.GS.ShopUtl.ItemPrice.ItemPrice;
import imu.GS.ShopUtl.ItemPrice.PriceCustom;
import imu.GS.ShopUtl.ItemPrice.PriceMaterial;
import imu.GS.ShopUtl.ItemPrice.PriceUnique;
import imu.GS.ShopUtl.ShopItems.ShopItemCustomerShulkerBox;
import imu.GS.ShopUtl.ShopItems.ShopItemSeller;
import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Metods;

public class CustomerMenuBaseInv extends CustomerInv
{
	ArrayList<ShopItemCustomer[]> _shopItemCustomer;
	//ShopItemCustomer[] _playerItemsOthers;
	ItemStack[] p_state_display = new ItemStack[3];
	ItemStack[] p_state_display_hotbar_armor = new ItemStack[2];
	
	final int _player_slots_start = 36;
	final int _shop_slot_start = 0;
	
	//PLAYER_INV_STATE p_state = PLAYER_INV_STATE.NORMAL;
	final double _cdButtonClick = 0.2;
	int _playerInvPage = 0;
	int _shopInvPage = 0;
	ShopNormal _shopBase;
	Main _main;
	ShopManager _sm;
	UniqueManager _uniqueManager;
	int materialCompares_counter = 0;
	
	private PlayerTab _tab = PlayerTab.BLOCKS;
	private int index_tab = 0;
	
	private boolean _includePlayerHotbarAndArmorslots = false;
	
	
	Cooldowns _cd = new Cooldowns();
	
	LinkedList<LogData> logs = new LinkedList<>();
	
	public CustomerMenuBaseInv(Plugin main, Player player, ShopNormal shopBase) {
		super(main, player, shopBase.GetNameWithColor(), 6*9);
		_main = (Main)main;
		_sm = _main.get_shopManager();
		_uniqueManager = _sm.GetUniqueManager();
		_shopBase = shopBase;
		setupButtons();
		ResetPlayerShopItemList();
		
		LoadShopInv();
		LoadPlayerInv();
	}
	
	enum PlayerTab
	{
		BLOCKS,
		ARMOR_TOOLS,
		SHULKER_BOXES
	}
	
	class ClickInfo
	{
		public BUTTON _button;
		public int _click_amount;
		public ClickType _clickType;
		public ShopItemBase _shopItemBase;
		public int _slot;
		public ClickInfo(BUTTON button,int slot ,int amount, ClickType cType, ShopItemBase shopItemBase) 
		{
			_button = button;
			_click_amount = amount;
			_clickType = cType;
			_shopItemBase = shopItemBase;
			_slot = slot;
		}
		
		public void Print()
		{
			System.out.println("============================");
			System.out.println("button: "+_button);
			System.out.println("click amount: "+_click_amount);
			System.out.println("click type: "+_clickType);
			System.out.println("_shopitemBase: "+_shopItemBase);
			if(_shopItemBase != null ) System.out.println("item: "+_shopItemBase.GetRealItem().getType());
			if(_shopItemBase != null ) System.out.println("item amount: "+_shopItemBase.GetRealItem().getAmount());
			System.out.println("slot: "+_slot);
			System.out.println("============================");
		}
	}

	protected enum BUTTON implements IButton
	{
		NONE,
		SHOP_ITEM,
		PLAYER_ITEM,
		GO_LEFT_SHOP,
		GO_RIGHT_SHOP,
		GO_LEFT_PLAYER,
		GO_RIGHT_PLAYER,
		STATE_PLAYER_INV,
		STATE_HOTBAR_ARMOR_SLOTS,
	}
	
	
	
	void SwitchTab()
	{
		index_tab = (index_tab+1) >= PlayerTab.values().length ? 0 : index_tab+1;
		_tab = PlayerTab.values()[index_tab];
		//System.out.println("tab idx: "+index_tab);
		_inv.setItem(31, p_state_display[index_tab]);
	}
	
	void SwitchStateHotbarArmorSlots()
	{
		if(_includePlayerHotbarAndArmorslots)
		{
			_includePlayerHotbarAndArmorslots = false;
			_inv.setItem(33, p_state_display_hotbar_armor[0]);
		}
		else
		{
			_includePlayerHotbarAndArmorslots = true;
			_inv.setItem(33, p_state_display_hotbar_armor[1]);
		}
		
	}
	
	void ResetPlayerShopItemList()
	{
		_shopItemCustomer = new ArrayList<>();
		_shopItemCustomer.add(new ShopItemCustomer[18]);
	}
	
	@EventHandler
	public void ReceivedDrop(EntityPickupItemEvent e)
	{
		if(e.getEntity() instanceof Player)
		{
			if(((Player)e.getEntity()).getUniqueId().equals(_player.getUniqueId()))
			{
				e.setCancelled(true);
				//LoadPlayerInv();
			}		
		}
	}
	
	@Override
	public void invClosed(InventoryCloseEvent e) 
	{
		UnRegisterItems();
		_shopBase.RemoveCustomer(_player.getUniqueId(), false);
			
		
		_main.get_shopManager().SendLogs(_player, logs);
	}

	public void onClickInsideInv(InventoryClickEvent e) 
	{

		

		if(_shopBase.HasInteractLock())
		{
			//System.out.println("interact lock!");
			return;		
		}
		
		if(_shopBase._temp_lock)
		{
			return;
		}
		
		if(!_cd.isCooldownReady("buttonPress")) return;
		
		_cd.setCooldownInSeconds("buttonPress", _cdButtonClick);
		
		
		ItemStack stack = e.getCurrentItem();
		if(stack == null)
			return;
		String buttonName = getButtonName(e.getCurrentItem());
		if(buttonName == null)
			return;

		BUTTON button = BUTTON.valueOf(buttonName);
		
		ClickType c_type = e.getClick();
		int amount = 0;
		ShopItemBase si =  GetCustoemrItemViaSlot(e.getSlot());
		
		if(si == null) si = GetShopItemViaSlot(e.getSlot());
		
		//if(si == null) return;
		
		switch(c_type)
		{
			case LEFT:
				amount = 1;
				break;
			case RIGHT:
				amount = 8;
				break;
			case SHIFT_LEFT:
				amount = 64;
				break;
			case SHIFT_RIGHT:
				
				amount = 1;
				if(si != null) amount = si.Get_amount(); //all

				
				break;
		default:
			break;
		}
		
		if(si != null && amount > si.Get_amount())
			amount = si.Get_amount();
		
		if(amount <= 0) return;
		
		ClickSorter(new ClickInfo(button,e.getSlot(),amount ,c_type, si));
	}

	@Override
	public void setupButtons() 
	{
		ItemStack redLine = Metods.setDisplayName(new ItemStack(Material.RED_STAINED_GLASS_PANE), " ");
		_inv.setItem(28, redLine);_inv.setItem(29, redLine);_inv.setItem(35-1, redLine);
		
		setupButton(BUTTON.GO_LEFT_SHOP, Material.DARK_OAK_SIGN, Metods.msgC("&9<< Shop"), 27);
		setupButton(BUTTON.GO_RIGHT_SHOP, Material.DARK_OAK_SIGN, Metods.msgC("&9Shop >>"), 35);
		setupButton(BUTTON.GO_LEFT_PLAYER, Material.BIRCH_SIGN, Metods.msgC("&9<< Inv"), 30);
		setupButton(BUTTON.GO_RIGHT_PLAYER, Material.BIRCH_SIGN, Metods.msgC("&9Inv >>"), 32);
		p_state_display_hotbar_armor[1] = setupButton(BUTTON.STATE_HOTBAR_ARMOR_SLOTS, Material.SHIELD, Metods.msgC("&9Hotbar and Armor slots &2Included!"), 33);
		p_state_display_hotbar_armor[0] = setupButton(BUTTON.STATE_HOTBAR_ARMOR_SLOTS, Material.CHEST, Metods.msgC("&9Hotbar and Armor slots &cExcluded!"), 33);
		
		
		p_state_display[2] = _metods.hideAttributes(setupButton(BUTTON.STATE_PLAYER_INV, Material.CYAN_SHULKER_BOX, Metods.msgC("&9CONTENT OF SHULKER BOXES"), 31));
		p_state_display[1] = _metods.hideAttributes(setupButton(BUTTON.STATE_PLAYER_INV, Material.NETHERITE_CHESTPLATE, Metods.msgC("&9Tools, Armor..."), 31));
		p_state_display[0] = setupButton(BUTTON.STATE_PLAYER_INV, Material.STONE, Metods.msgC("&9Blocks, Ores..."), 31);
		
		
		
		
		redLine.setType(Material.BLUE_STAINED_GLASS_PANE);
		if(_shopBase.GetCustomersCanOnlyBuy()){for(int i = _size-1; i > _size-19; i--) {_inv.setItem(i, redLine);}}		
		
	}
	

	public void ClickSorter(ClickInfo cInfo)
	{
		//cInfo.Print();
		switch (cInfo._button) 
		{
		case PLAYER_ITEM:
			PrepareSell(cInfo);
			return;
		case SHOP_ITEM:
			//Buy(cInfo);
			PrepareBuy(cInfo);
			return;
		case STATE_PLAYER_INV:
			SwitchTab();
			LoadPlayerInv();
			break;
		case NONE:
			break;			
		case GO_LEFT_PLAYER:
			ChangeCustomerPage(-1);
			RefreshPlayerDisplayPageSlots();
			break;			
		case GO_RIGHT_PLAYER:
			ChangeCustomerPage(1);
			RefreshPlayerDisplayPageSlots();
			break;
		case GO_LEFT_SHOP:
			ChangeShopPage(-1);
			LoadShopInv();
			break;
		case GO_RIGHT_SHOP:
			ChangeShopPage(1);
			LoadShopInv();
			break;
		case STATE_HOTBAR_ARMOR_SLOTS:
			SwitchStateHotbarArmorSlots();
			LoadPlayerInv();
			break;
		}
		
	
	}
	
	// Buy and sell run entirely on the main thread within the click: the stock check, the payment
	// and the item transfer happen in the same tick, so two customers can't both take the last items.

	void PrepareBuy(ClickInfo cInfo)
	{
		ShopItemBase sib = cInfo._shopItemBase;
		if(sib == null || sib.Get_amount() <= 0) return;

		if(sib.GetItemPrice() instanceof PriceCustom)
		{
			// Opening another inventory inside InventoryClickEvent is unsafe, do it next tick
			Bukkit.getScheduler().runTask(_main, () -> new BuyCustomPriceINV(_main, _player, _shopBase, (ShopItemSeller)sib).openThis());
			return;
		}

		int amount = Math.min(cInfo._click_amount, sib.Get_amount());
		double price = sib.GetItemPrice().GetCustomerPrice(amount);
		if(!_shopBase.BuyConfirmation(_player, sib, price, amount, true)) return;

		Buy(sib, amount, price);
	}

	void PrepareSell(ClickInfo cInfo)
	{
		if(cInfo._shopItemBase == null || cInfo._shopItemBase.Get_amount() <= 0) return;

		ShopItemCustomer sic = (ShopItemCustomer)cInfo._shopItemBase;
		int amount = cInfo._click_amount;
		if(!sic.EnoughItems(amount))
		{
			// The inventory changed since the menu was built
			_player.sendMessage(Metods.msgC("&cYour inventory changed, the shop was refreshed. Please try again."));
			LoadPlayerInv();
			return;
		}

		// Price before the sold items are added to the shop, overflow prices depend on the stock
		double price = sic.GetItemPrice().GetCustomerPrice(amount);
		if(!_shopBase.SellConfirmation(_player, sic, price, amount)) return;

		Sell(sic, amount, price);
	}

	void Buy(ShopItemBase sib, int amount, double price)
	{
		ShopItemResult[] resultItems = sib.GetTransactionResultItemStack();

		sib.AddAmount(amount * -1);
		sib.UpdateItem();

		ImusAPI._metods.InventoryAddItemOrDrop(resultItems[0]._stack, _player, amount);

		LoadPlayerInv();
		ShopItemCustomer sic = new ShopItemCustomer(_main,_shopBase ,null,resultItems[0]._stack, amount);

		sic.SetItemPrice(sib.GetItemPrice().clone());

		_main.get_shopManager().LogRegisterPurchace(logs, new LogData(sic, price, amount));
	}

	void Sell(ShopItemCustomer sic, int amount, double price)
	{
		ShopItemResult[] resultItems = sic.GetTransactionResultItemStack();

		sic.AddAmountToPlayer(amount * -1);

		ShopItemSeller sis = (ShopItemSeller)sic.GetTargetShopitem(_player.getUniqueId());

		if(!ImusAPI._metods.isShulkerBox(sic.GetRealItem()) && _tab != PlayerTab.SHULKER_BOXES)
		{
			AddSoldItem(sis,resultItems[0]._stack, amount, price);
			sic.UpdateItem();

			if(_tab == PlayerTab.ARMOR_TOOLS)
			{
				RefreshPlayerDisplayPageSlots();
			}
			return;
		}

		for(ShopItemResult result : resultItems)
		{
			AddSoldItem(sis,result._stack, result._amount, price);
		}

		for(LogData data : ((ShopItemCustomerShulkerBox)sic).GetLogData())
		{
			_main.get_shopManager().LogRegisterPurchace(logs,data);
		}

		LoadPlayerInv();
	}

	void AddSoldItem(ShopItemSeller sis, ItemStack stack, int amount, double price)
	{
		ShopItemSeller log;
		log = new ShopItemSeller(_main, _shopBase, stack, amount);
		if(_tab != PlayerTab.SHULKER_BOXES)
		{
			_main.get_shopManager().LogRegisterPurchace(logs,new LogData(log, price, amount));
		}

		if(sis == null)
		{
			sis = new ShopItemSeller(_main, _shopBase, stack, amount);

			if(_uniqueManager.IsUnique(stack))
			{
				sis.SetItemPrice(new PriceUnique().SetPrice(_main.get_shopManager().GetUniqueManager().GetPriceItem(stack).GetPrice()));
			}

			CheckBucket(sis,stack,amount);

			log.SetItemPrice(sis.GetItemPrice());

			_shopBase.AddNewItem(sis,false);
			return;
		}
		CheckBucket(sis,stack,amount);
		sis.AddAmount(amount);
		sis.UpdateItem();
	}

	void CheckBucket(ShopItemBase sib, ItemStack stack, int amount)
	{
		Material type = stack.getType();
		boolean filledBucket = type == Material.WATER_BUCKET || type == Material.LAVA_BUCKET || type == Material.MILK_BUCKET;
		if(filledBucket && sib.GetItemPrice().GetPrice() > 0)
		{
			Metods._ins.InventoryAddItemOrDrop(new ItemStack(Material.BUCKET), _player, amount);
		}
	}
	

	public void ChangeCustomerPage(int amount)
	{
		_playerInvPage += amount;
		if(_playerInvPage < 0)
			_playerInvPage = 0;
		
		if(_playerInvPage > _shopItemCustomer.size()-1)
			_playerInvPage = _shopItemCustomer.size()-1;
		
	}
	
	public void ChangeShopPage(int amount)
	{
		_shopInvPage += amount;
		if(_shopInvPage < 0)
			_shopInvPage = 0; // _shopBase.get_items().size()-1
		
		if(_shopInvPage > _shopBase.get_items().size()-1)
			_shopInvPage = _shopBase.get_items().size()-1; // 0
		
	}
	
	public void UpdateCustomerSlot(ShopItemCustomer sic,int page, int slot)
	{
		//System.out.println("update customer slot: "+_player.getName());
		_shopItemCustomer.get(page)[slot] = sic;
		if(page != _playerInvPage)
			return;
		
		RefreshSlot(slot+_player_slots_start);
	}
	
	public void UpdateShopSlot(int page, int slot)
	{
		//System.out.println("update shop slot: "+_player.getName());
		if(page != _shopInvPage)
			return;
		
		RefreshSlot(slot);
		
	}
	
	@Override
	public void SetShopSlot(ShopItemSeller sis, int page, int slot) {
		
		if(page != _shopInvPage)
			return;
		
		if(!sis.CanShowToPlayer(_player))
		{
			System.out.println("cant show item: "+slot);
			_inv.setItem(slot, SetButton(sis.GetDisplayItemNotAvailable(), BUTTON.NONE));
			//_inv.setItem(slot, null);
			return;
		}
		_inv.setItem(slot, SetButton(sis.GetDisplayItem(), BUTTON.SHOP_ITEM));
	}
	
	boolean CheckTabItem(ItemStack stack)
	{
		if(_tab == PlayerTab.BLOCKS && (ImusAPI._metods.isArmor(stack) || ImusAPI._metods.isTool(stack) || ImusAPI._metods.isShulkerBox(stack))) return false;
		if(_tab == PlayerTab.ARMOR_TOOLS)
		{
			if((ImusAPI._metods.isArmor(stack) || ImusAPI._metods.isTool(stack)) ) return true; //
			return false;
		}
		
		if(_tab == PlayerTab.SHULKER_BOXES && !ImusAPI._metods.isShulkerBox(stack)) return false;
		
		return true;
	}
	
	ShopItemBase FindSimilarShopFromShop(ShopItemBase searchItem)
	{
		for(ShopItemBase[] page : _shopBase.get_items())
		{
			for(ShopItemBase sib : page)
			{
				if(sib == null) continue;
				
				if(sib.IsSameKind(searchItem)) return sib;
			}
		}
		return null;
	}
	
	ShopItemBase FindSimilarShopFromCustomer(ShopItemBase searchItem)
	{		

		for(ShopItemBase[] page : _shopItemCustomer)
		{
			for(ShopItemBase sib : page)
			{				
				if(sib == null) 
				{
					continue;
				}
								
				if(sib.IsSameKind(searchItem)) 
				{
					//System.out.println("found in: "+pages+ " slot : "+slot+ " player: " +_player.getName());
					return sib;
				}
			}

		}
		return null;
	}
	void PutCustomerItem(ItemStack stack, int page, int slot)
	{
		
		// lis�t� t�ll� referoivan itemin suoraan
		ShopItemCustomer sic;
		ItemPrice itemPrice;
		if(_tab != PlayerTab.SHULKER_BOXES)
		{
			sic = new ShopItemCustomer(_main,_shopBase,_player,stack, stack.getAmount());
			sic.AddPlayerItemStackRef(stack);
			sic.RegisterSlot(_inv, this, page, slot, false);

			if(_uniqueManager.IsUnique(stack))
			{
				itemPrice = new PriceMaterial(stack.getType());
				itemPrice.SetPrice(_uniqueManager.GetPriceItem(stack).GetPrice() * _main.get_shopManager().GetDurabilityReduction(stack));

			}
			else
			{
				itemPrice = _main.GetMaterialManager().GetPriceMaterialAndCheck(stack);
				((PriceMaterial)itemPrice).SetShopBase(_shopBase);
								
			}
			sic.SetItemPrice(itemPrice);
			
			ShopItemSeller sis = (ShopItemSeller)FindSimilarShopFromShop(sic);
			if( sis != null)
			{
				ConnectShopItems(sic, sis);		
			}

			_shopItemCustomer.get(page)[slot] = sic;
			
			return;
		}
		
		sic = new ShopItemCustomerShulkerBox(_main, _shopBase, _player, stack, stack.getAmount());
		sic.AddPlayerItemStackRef(stack);
		sic.RegisterSlot(_inv, this, page, slot, false);
		itemPrice = ((ShopItemCustomerShulkerBox)sic).GetCalculatedPriceFromContent();
		
		if(itemPrice.GetPrice() <= 0) sic.Set_amount(0);
		
		((ShopItemCustomerShulkerBox)sic).SetItemPrice(itemPrice);
		
		_shopItemCustomer.get(page)[slot] = sic;
	}
	
	
	public void LoadPlayerInv()
	{
		if(_shopBase.GetCustomersCanOnlyBuy()) return;

		// Reads the live player inventory and writes the menu, so it stays on the main thread
		ResetPlayerShopItemList();
		_shopBase.ClearShopItemsFromTarget(_player.getUniqueId());
		ItemStack itemStack;
		for (int invSlot = 0; invSlot <  _player.getInventory().getContents().length ; invSlot++) 
		{
			if(!_includePlayerHotbarAndArmorslots && (invSlot < 9  || invSlot > _player.getInventory().getContents().length-6)) continue;
			
			itemStack = _player.getInventory().getContents()[invSlot];
			
			if(itemStack == null) continue;
				
			if(!CheckTabItem(itemStack)) continue;
			
			boolean found = false;

			for(int i = 0; i < _shopItemCustomer.size(); ++i) //page
			{
				for(int l = 0; l < _shopItemCustomer.get(i).length; ++l) //slot
				{
					ShopItemCustomer sic =  _shopItemCustomer.get(i)[l];
					if(sic == null)
					{					

						PutCustomerItem(itemStack, i, l);

						found = true;
						break;
					}
					
					if(_tab != PlayerTab.SHULKER_BOXES && sic.IsSameKind(itemStack))
					{
						sic.AddAmount(itemStack.getAmount());
						sic.AddPlayerItemStackRef(itemStack);
						found = true;
						break;
					}
				}
				if(found)
					break;
			}
			
			if(!found)
			{
				//System.out.println("MAKE2 new page for customer!");
				_shopItemCustomer.add(new ShopItemCustomer[18]);
				int page = _shopItemCustomer.size()-1;
				int slot = 0;

				PutCustomerItem(itemStack, page, slot);
			}
			
		}
		
		ChangeCustomerPage(0);
		RefreshPlayerDisplayPageSlots();

	}

	
	ShopItemSeller GetShopItemViaSlot(int slot)
	{
		Integer ref_page = _sm.GetSISPage(_inv.getItem(slot));
		Integer ref_slot = _sm.GetSISSlot(_inv.getItem(slot));	

		if(ref_page == null || ref_slot == null) return null;
//		System.out.format("page %d", ref_page);
//		System.out.format("slot %d", ref_slot);
		return (ShopItemSeller)_shopBase.GetItem(ref_page, ref_slot);
	}
	
	ShopItemCustomer GetCustoemrItemViaSlot(int slot)
	{
		if(slot >= _player_slots_start && slot <_size)
		{
			return _shopItemCustomer.get(_playerInvPage)[slot-_player_slots_start];						
		}
		return null;
	}
	
	void RefreshSlot(int slot)
	{
		if(slot >= 0 &&  slot <= 27)
		{			
			ShopItemSeller sis = GetShopItemViaSlot(slot);
			if(sis == null )//|| !sis.CanShowToPlayer(_player))
			{
				_inv.setItem(slot, null);
				return;
			}
			
			if(!sis.CanShowToPlayer(_player))
			{
				_inv.setItem(slot, sis.GetDisplayItemNotAvailable());
				return;
			}
			//_inv.setItem(slot, SetButton(_shopBase.get_items().get(_shopInvPage)[slot].GetDisplayItem(), BUTTON.SHOP_ITEM));
			SetShopSlot(sis, _shopInvPage, slot);
		}
		
		if(_shopBase.GetCustomersCanOnlyBuy()) return;
		
		if(slot >= _player_slots_start && slot < _size)
		{
			if(_shopItemCustomer.get(_playerInvPage)[slot-_player_slots_start] == null)
			{
				_inv.setItem(slot, null);
				return;
			}				
			_inv.setItem(slot, SetButton(_shopItemCustomer.get(_playerInvPage)[slot-_player_slots_start].GetDisplayItem(), BUTTON.PLAYER_ITEM));
		}
	}
	void ConnectShopItems(ShopItemCustomer sic, ShopItemSeller sis )
	{
		sic.SetTargetShopitem(sis);
		sis.SetTargetShopitem(sic);
		if(sic.GetItemPrice() instanceof PriceMaterial)
		{
			//System.out.println("Setting shopitem: ");
			((PriceMaterial)sic.GetItemPrice()).SetShopItem(sis);
		}
	}
	public void LoadShopInv()
	{		
		//System.out.println("load shop inv: "+_player.getName());
		int idx = 0;
		for(int i = 0; i < _shopBase.get_items().get(_shopInvPage).length; ++i)
		{
			ShopItemSeller sis = _shopBase.get_items().get(_shopInvPage)[i];
			if(sis == null)// || !sis.CanShowToPlayer(_player))
			{
				//UpdateShopSlot(_shopInvPage, i);		
				if(!_shopBase.IsAbsoluteItemPositions()) idx--;
				_inv.setItem(i, null);
				continue;
			}

			if(!sis.CanShowToPlayer(_player) || !_shopBase.IsAbsoluteItemPositions()) // => tollon nï¿½kyy ettei available//if(!sis.CanShowToPlayer(_player) && !_shopBase.IsAbsoluteItemPositions()) 
			{
				idx--;
				continue;
			}
		
			sis.RegisterSlot(_inv, this, _shopInvPage, i+idx, true);
			
			ShopItemCustomer sic = (ShopItemCustomer)FindSimilarShopFromCustomer(sis);
			if( sic != null)
			{
				ConnectShopItems(sic, sis);		
			}
						
			sis.UpdateItem();

		}
		
	}
	
	void UnRegisterItems()
	{		
		//System.out.println("Unregister items!");
		for(int i = 0; i < _shopItemCustomer.size(); ++i)
		{
			for(int l = 0; l < _shopItemCustomer.get(i).length; ++l)
			{
				if(_shopItemCustomer.get(i)[l] == null)
					continue;
				_shopItemCustomer.get(i)[l].UnRegisterSlot(_inv);
				_shopItemCustomer.get(i)[l].ClearShopitemTarget(_player.getUniqueId());
			}
		}
		
		_shopBase.UnRegisterItems(_inv);
		
	}
	
	void RefreshPlayerDisplayPageSlots()
	{
		for(int i = 0; i < _shopItemCustomer.get(_playerInvPage).length; ++i) //page
		{
			RefreshSlot(i+_player_slots_start);
		}
	}

	
	


}
