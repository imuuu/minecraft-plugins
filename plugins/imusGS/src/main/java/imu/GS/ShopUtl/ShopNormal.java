package imu.GS.ShopUtl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import imu.GS.Invs.ShopModINV;
import imu.GS.Main.Main;
import imu.GS.ShopUtl.Customer.Customer;
import imu.GS.ShopUtl.Customer.CustomerMenuBaseInv;
import imu.GS.ShopUtl.ItemPrice.PriceMoney;
import imu.GS.ShopUtl.ShopItems.ShopItemSeller;
import imu.GS.ShopUtl.ShopItems.ShopItemStockable;
import imu.iAPI.Interfaces.ITuple;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;
import imu.iAPI.Other.Tuple;

public class ShopNormal extends Shop
{
	
	private  int shopHolderSize = 27;
	
	private ArrayList<ShopItemSeller[]> _items = new ArrayList<ShopItemSeller[]>();
	private HashMap<Material, Integer> _materialCount = new HashMap<>();
	
	private double _sellM = 5.0;
	private double _buyM  = 1.0;
	private int _saveIfPossible_s = 10;
	
	private double _expire_percent = 0.1f;
	private int _expire_cooldown_m = 60;
	private boolean _absoluteItemPosition = true;
	private String _cd_expire = "expire";
	
	private Cooldowns _cds;
	
	
	
	private boolean _intererActlocked = false;
	private boolean _customers_can_only_buy = false;
	
	
	
	BukkitTask _saveRunnable = null;
	
	public ShopNormal(Main main, UUID uuid, String name, int pages)
	{
		super(main, uuid, name);		
		_cds = new Cooldowns();
		SetNewExpire();

		for(int i = 0; i < pages; ++i)
		{
			get_items().add(new ShopItemSeller[shopHolderSize]);
		}
		
		//_items.put(0, new ShopItemBase[shopHolderSize]);
	}
	public void SetCustomersCanOnlyBuy(boolean canBuy)
	{
		_customers_can_only_buy = canBuy;
	}
	
	public boolean GetCustomersCanOnlyBuy()
	{
		return _customers_can_only_buy;
	}
	
	public boolean IsAbsoluteItemPositions()
	{
		return _absoluteItemPosition;
	}
	
	
	
	public boolean HasExpired()
	{
		return _cds.isCooldownReady(_cd_expire);
	}
	
	public void SetNewExpire()
	{
		_cds.setCooldownInSeconds(_cd_expire, _expire_cooldown_m * 60);
	}
	
	
	
	public void SaveIfPossible()
	{
		if(_saveRunnable != null) _saveRunnable.cancel();
		
		_saveRunnable =  new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				if(!HasCustomers())
				{
					_main.get_shopManager().GetShopManagerSQL().SaveShopAsync((ShopNormal)_main.get_shopManager().GetShop(GetUUID()));
				}
				_saveRunnable = null;
			}
		}.runTaskLaterAsynchronously(_main, 20 * _saveIfPossible_s);
	}
	
	public void SetLockToInteract(boolean lock)
	{
		_intererActlocked = lock;
	}
	
	public boolean HasInteractLock() 
	{
		return _intererActlocked;
	}
	
	public boolean BuyConfirmation(Player player, ShopItemBase sib, double price,int amount, boolean withdraw)
	{
		if(_main.get_econ() == null ) {
			return false;
		}
		//double price = sib.GetItemPrice().GetCustomerPrice(amount);

		if(_main.get_econ().getBalance(player) >= price)
		{
			if(!withdraw) return true;
			
			_main.get_econ().withdrawPlayer(player, price);
			player.sendMessage(Metods.msgC("&4Buy &9confirmed! Purchase value: &e "+Metods.Round(price)+" &2$&9. &9Balance: &e"+Metods.Round(_main.get_econ().getBalance(player))+"&2$&9."));
			return true;
		}
		
		if(!withdraw) return false;
		
		player.sendMessage(Metods.msgC("&4Buy &cCanceled! &9Balance isn't enough! Purchase value: &e "+Metods.Round(price)+" &2$&9. &9Balance: &e"+Metods.Round(_main.get_econ().getBalance(player))+"&2$&9."));
		return false;
	}
	
	public boolean SellConfirmation(Player player, ShopItemBase sib, double price, int amount)
	{
		if(_main.get_econ() == null ) {
			return false;
		}
		//double price = sib.GetItemPrice().GetCustomerPrice(amount) ;

		_main.get_econ().depositPlayer(player, price );
		
		player.sendMessage(Metods.msgC("&3Sell &9confirmed! Sold value: &e "+Metods.Round(price)+" &2$&9. &9Balance: &e"+Metods.Round(_main.get_econ().getBalance(player))+"&2$&9."));
		return true;
	}
	
//	void AddNewCustomer(Player player)
//	{
//		//System.out.println("add customer");
//		if(_temp_lock || HasLocked() || _temp_modifying_lock) 
//		{
//			player.sendMessage(Metods.msgC("&9The Shop is temporarily closed! Come back laiter!"));
//			if(!player.isOp())
//			{
//				return;
//			}
//		}
//		AddCustomer(player, (CustomInvLayout)new CustomerMenuBaseInv(_main,player, this));
//
//	}
	
	public void ClearShopItemsFromTarget(UUID uuid_player)
	{
		for(ShopItemBase[] page : get_items())
		{
			for(ShopItemBase sib : page)
			{
				if(sib == null) continue;
				
				sib.ClearShopitemTarget(uuid_player);
			}
		}
	}
	
	@Override
	public void RemoveCustomer(UUID uuid_player, boolean closeInv)
	{
		super.RemoveCustomer(uuid_player, closeInv);
		
		ClearShopItemsFromTarget(uuid_player);

		if(!HasCustomers()) 
		{
			ArrangeShopItems(true, false);
			SaveIfPossible();
		}
		
	}

	
	public void LoadCustomerInvs(boolean loadShopSide, boolean loadPlayerSide)
	{
		for(Customer customer : GetCustomers())
		{
			CustomerMenuBaseInv shopInv = (CustomerMenuBaseInv)customer.GetInv();
			if(loadShopSide)shopInv.LoadShopInv();
			if(loadPlayerSide)shopInv.LoadPlayerInv();		
		}
	}
	

	public void SaveDataAsync()
	{
		new BukkitRunnable() 
		{			
			@Override
			public void run() 
			{
				RemoveCustomerALL();

				ArrangeShopItems(true,false);
				_main.get_shopManager().SaveShop(GetUUID(), false);

			}
		}.runTaskAsynchronously(_main);
	}
	
	public void ClearCrap()
	{
		RemoveCustomerALL();
		_temp_lock = true;
		ArrangeShopItems(true, true);
		
		_temp_lock = false;
	}

	public void AddMaterialCount(Material mat, int amount)
	{
		
		if(!_materialCount.containsKey(mat)) _materialCount.put(mat, 0);
		int amountNow = _materialCount.get(mat)+amount;
		//System.out.println("mat: "+mat.name() + " amount: "+amount+ "amountNow: "+amountNow);
		_materialCount.put(mat, amountNow);
		
		if(_materialCount.get(mat) <= 0)_materialCount.remove(mat);
	}
	
	public int GetMaterialCount(Material mat)
	{
		return _materialCount.get(mat) != null ? _materialCount.get(mat) : 0;
	}
	

	void LoadMaterialCount()
	{
		_materialCount.clear();
		for(int page = 0; page < get_items().size(); ++page)
		{
			for(int i = 0; i < get_items().get(page).length; ++i)
			{
				ShopItemBase sib = _items.get(page)[i];
				if(sib == null) continue;
				AddMaterialCount(sib.GetRealItem().getType(), sib.Get_amount());
			}
		}
	}
	
	public void ArrangeShopItems(boolean removeEmpties, boolean removeCrap)
	{
		//System.out.println("arrange shop");
		_materialCount.clear();
		SetLockToInteract(true);
		for(int page = _items.size()-1; page >= 0 ; page--)
		{
			boolean isEmpty = true;

			ShopItemSeller[] items = new ShopItemSeller[shopHolderSize];
			int idx = 0;
			Set<Integer> stockSlots = new HashSet<>();
			
			
			for(int slot = 0;  slot < _items.get(page).length; ++slot)
			{
				ShopItemBase sib = _items.get(page)[slot];
				
				if(removeEmpties && sib != null && sib.Get_amount() <= 0 && !(sib instanceof ShopItemStockable))
				{
					continue;
				}
				
				if(sib != null && removeCrap && sib.getClass().equals(ShopItemSeller.class))
				{
					continue;
				}
				
				if( sib != null || (sib instanceof ShopItemStockable)) //if( sib != null && sib.Get_amount() > 0 || (sib instanceof ShopItemStockable))
				{
					//System.out.println("==> shopItem found");
					if(sib instanceof ShopItemStockable)
					{
						//System.out.println("stocable");
						stockSlots.add(slot);
						items[slot]=_items.get(page)[slot];
						AddMaterialCount(items[slot].GetRealItem().getType(), items[slot].Get_amount());
						
					}
					else
					{
						if(stockSlots.contains(idx)) while(stockSlots.contains(++idx));
						
						
						items[idx] =_items.get(page)[slot].SetPageAndSlot(page, idx);
						AddMaterialCount(items[idx].GetRealItem().getType(), items[idx].Get_amount());
						idx++;
					}					
					isEmpty = false;
				}

			}
			
			if(isEmpty && page != 0)
			{
				_items.remove(_items.size()-1);
			}
			else
			{
				_items.set(page, items);
			}
		}
		
		if(HasCustomers())
			LoadCustomerInvs(true, false);
		
		
		//Metods._ins.printHashMap(_materialCount);
		
		SetLockToInteract(false);
	}
	
	public void SetItem(ShopItemSeller sis, int page, int slot)
	{
		if(page >= _items.size())
		{
			int pagesNeeded = page - (_items.size()-1);
			for(int i = 0 ; i < pagesNeeded; ++i) {_items.add(new ShopItemSeller[shopHolderSize]);}
		}
		get_items().get(page)[slot] = sis.SetPageAndSlot(page, slot);
		
		//AddMaterialCount(sis.GetRealItem().getType(), sis.Get_amount());
	}
	
	public ShopItemBase GetItem(int page, int index)
	{
		if(page < get_items().size())
		{
			return get_items().get(page)[index];
		}
		return null;
	}
	
	public void RemoveItem(int page, int idx)
	{
		//ShopItemBase sib = get_items().get(page)[idx];
		ShopItemBase sib = _items.get(page)[idx];
		_items.get(page)[idx] = null;
		RemoveCustomerALL();
		
		ArrangeShopItems(false,false);
		
		new BukkitRunnable() {		
			@Override
			public void run() 
			{
				ArrayList<ShopItemBase> ar = new ArrayList<ShopItemBase>();
				ar.add(sib);
				_main.GetShopManagerSQL().DeleteShopItem(ar, true);			
			}
		}.runTaskAsynchronously(_main);
		
	}
	
	public void UnRegisterItems(Inventory inv)
	{
		for(int i = 0; i < get_items().size(); ++i)
		{
			for(int l = 0; l < get_items().get(i).length; ++l)
			{
				if(get_items().get(i)[l] == null)
					continue;
				
				get_items().get(i)[l].UnRegisterSlot(inv);
			}
		}
	}
	
	void RegisterAndLoadNewItemsClients()
	{
		//System.out.println("LOAD all clients shop inv!");
		for(Customer customer : GetCustomers())
		{
			((CustomerMenuBaseInv)customer.GetInv()).LoadShopInv();
		}
		
	}
	

	public void AddNewItem(ShopItemSeller sis, boolean setAmount)
	{
		
		//System.out.println("new item: "+sis.GetRealItem().getType());
		if(sis.GetItemPrice() instanceof PriceMoney)
		{
			((PriceMoney)sis.GetItemPrice()).SetCustomerPrice(sis.GetItemPrice().GetPrice() * _sellM);
		}
		
		
		
		ITuple<Integer, Integer> firstFree = null;
		int page = 0;
		for(; page < get_items().size(); ++page)
		{
			for(int i = 0; i < get_items().get(page).length; ++i)
			{
				ShopItemBase sib = get_items().get(page)[i];
				if(sib == null)
				{
					if(firstFree == null)
						firstFree = new Tuple<Integer, Integer>(page,i);
					continue;
				}
				
				if(sib.IsSameKind(sis))
				{
					if(setAmount)
					{
						sib.Set_amount(sis.Get_amount());
						
					}
					else
					{
						sib.AddAmount(sis.Get_amount());
					}
					

					sib.UpdateItem();
					
					LoadMaterialCount();
					return;
				}
			}
		}
		

		if(firstFree == null)
		{
			//System.out.println("Shop: Not free space found, make new page!");
			get_items().add(new ShopItemSeller[shopHolderSize]);
			get_items().get(get_items().size()-1)[0] =  sis.SetPageAndSlot(get_items().size()-1, 0);
			
			//SaveNewItemAsync(get_items().get(get_items().size()-1)[0]);
			LoadMaterialCount();
			RegisterAndLoadNewItemsClients();
			
			return;
		}

		get_items().get(firstFree.GetKey())[firstFree.GetValue()] = sis.SetPageAndSlot(firstFree.GetKey(), firstFree.GetValue());
		LoadMaterialCount();
		RegisterAndLoadNewItemsClients();
		return;
	}
	
	public void RefreshPrices()
	{
		int page = 0;
		for(; page < get_items().size(); ++page)
		{
			for(int i = 0; i < get_items().get(page).length; ++i)
			{
				ShopItemBase sib = get_items().get(page)[i];
				if(sib == null)
				{
					continue;
				}
								
				if(sib.GetItemPrice() instanceof PriceMoney)
				{
					double price = ((PriceMoney)sib.GetItemPrice()).GetPrice();
					((PriceMoney)sib.GetItemPrice()).SetCustomerPrice(price * _sellM);
				}
				
			}
		}
	}
	
	void SaveNewItemAsync(ShopItemSeller sis)
	{
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				_main.GetShopManagerSQL().SaveShopItem(sis,true);
			}
		}.runTaskAsynchronously(_main);
	}
	
	public ArrayList<ShopItemSeller[]> get_items() {
		return _items;
	}
	
	public double get_sellM() {
		return _sellM;
	}

	public void set_sellM(double _sellM) 
	{
		this._sellM = _sellM;
	}

	public double get_buyM() {
		return _buyM;
	}

	public void set_buyM(double _buyM) {
		this._buyM = _buyM;
	}

	public double get_expire_percent() {
		return _expire_percent;
	}

	public void set_expire_percent(double _expire_percent) {
		this._expire_percent = _expire_percent;
	}

	public int get_expire_cooldown_m() {
		return _expire_cooldown_m;
	}

	public void set_expire_cooldown_m(int _expire_cooldown_m) {
		this._expire_cooldown_m = _expire_cooldown_m;
	}

	public String get_cd_expire() {
		return _cd_expire;
	}

	public void set_cd_expire(String _cd_expire) {
		this._cd_expire = _cd_expire;
	}
	
	
	
	public void SetAbsolutePosBool(boolean b)
	{
		_absoluteItemPosition = b;
	}
	
	public boolean GetAbsolutePosBool() {return _absoluteItemPosition;}
	
	@Override
	public void OpenModify(Player player) 
	{
		new ShopModINV(_main, player, this).openThis();
	}
	
	
	public void OpenShop(Player player) 
	{		
		AddCustomer(player, (CustomInvLayout)new CustomerMenuBaseInv(_main,player, this));
	}
	
}
