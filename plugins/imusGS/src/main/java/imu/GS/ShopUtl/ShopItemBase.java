package imu.GS.ShopUtl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonObject;

import imu.GS.ENUMs.ShopItemType;
import imu.GS.Invs.CustomerInv;
import imu.GS.Main.Main;
import imu.GS.ShopUtl.Customer.CustomerMenuBaseInv;
import imu.GS.ShopUtl.Customer.ShopItemCustomer;
import imu.GS.ShopUtl.ItemPrice.ItemPrice;
import imu.GS.ShopUtl.ItemPrice.PriceMoney;
import imu.GS.ShopUtl.ItemPrice.PriceOwn;
import imu.GS.ShopUtl.ShopItems.ShopItemSeller;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;

public abstract class ShopItemBase 
{
	protected UUID _uuid;
	protected ItemStack _real_stack;
	protected ShopItemType _type = ShopItemType.NORMAL;
	protected ItemStack _display_stack;
	ItemStack _display_stack_not_available;
	private ItemStack _display_out_of_stock = Metods.setDisplayName(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), "&9-");
	
	protected int _amount = 0;
	
	String lore_amount_str;
	String lore_price_str;
	
	protected String[] _lores;
	protected Metods _metods;

	HashMap<Inventory, SlotInfo> _slotPositions = new HashMap<>();
	ItemPrice _price;
	protected Main _main;
	protected ShopNormal _shopBase;
	
	private Set<String> _tags = new HashSet<>();
	public boolean AddGlow = false;
	
	protected ConcurrentHashMap<UUID, ShopItemBase> _customerShopitemTargets = new ConcurrentHashMap<>();
	
	public ShopItemBase(Main main, ShopNormal shopBase, ItemStack real, int amount) 
	{
		_metods = ImusAPI._metods;
		_main = main;
		SetUUID(UUID.randomUUID());	
		_shopBase = shopBase;
		_real_stack = real.clone();
		_display_stack = real.clone();
		_display_stack.setAmount(1);
		_display_stack_not_available = ImusAPI._metods.addLore(_display_stack.clone(), Metods.msgC("&cNOT AVAILABLE"), false);
			
		_amount = amount;
		LoadLores();
		_price = _main.GetMaterialManager().GetPriceMaterialAndCheck(real);
		
		
	}
	enum LoreSpot
	{
		AMOUNT;
	}
	
	public abstract void SetTargetShopitem(ShopItemBase sib);
	
	public ShopItemBase GetTargetShopitem(UUID uuid_player)
	{
		return _customerShopitemTargets.get(uuid_player);
	}
	
	public boolean HasTargetShopitem(UUID uuid_player)
	{
		return _customerShopitemTargets.containsKey(uuid_player);
	}
	
	public void ClearShopitemTarget(UUID uuid_player)
	{
		_customerShopitemTargets.remove(uuid_player);
	}
	
	public void SetDisplayOutOfStock(ItemStack stack)
	{
		_display_out_of_stock = stack.clone();
	}
	
	protected ItemStack GetDisplayOutOfStock()
	{
		return _display_out_of_stock;
	}
	
	public Set<String> GetTags()
	{
		return _tags;
	}
	
	public boolean AddTag(String tagName)
	{
		if(_tags.contains(tagName.toLowerCase())) return false;
		_tags.add(tagName.toLowerCase());
		return true;
	}
	
	public void RemoveTag(String tagName)
	{
		_tags.remove(tagName.toLowerCase());
	}
	
	public boolean HasTag(String tagName)
	{
		//System.out.println("CHECKING IF SHOP HAS TAG: "+tagName+ "   "+_tags.contains(tagName.toLowerCase()));
		return _tags.contains(tagName.toLowerCase());
	}
	
	public void ClearTags()
	{
		_tags.clear();
	}
	
	class SlotInfo
	{
		Inventory _inv = null;
		CustomerInv _customerInv;
		int _page = -1;
		int _slot = -1;
		boolean _shopItem = false;
		public SlotInfo(Inventory _inv, CustomerInv customerInv,int _page, int _slot, boolean _shopItem) 
		{
			this._inv = _inv;
			this._page = _page;
			this._slot = _slot;
			this._shopItem = _shopItem;
			this._customerInv = customerInv;
		}
	}
	
	public ShopNormal GetShop()
	{
		return _shopBase;
	}
	
//	public ShopItemType GetItemType()
//	{
//		return _type;
//	}
	
	public abstract JsonObject GetJsonData();
	public abstract void ParseJsonData(JsonObject data);
	
	public void SetUUID(UUID uuid)
	{
		_uuid = uuid;
	}
	
	public UUID GetUUID()
	{
		return _uuid;
	}
	
	public void SetItemPrice(ItemPrice price)
	{

		if(price instanceof PriceOwn)
		{
			((PriceMoney)price).SetCustomerPrice(price.GetPrice());
		}
		else
		{
			
			SetShowPrice(price);	
		}
		
		_price = price;
		toolTip();
	}
	protected abstract void SetShowPrice(ItemPrice price);
	
	public abstract ShopItemResult[] GetTransactionResultItemStack();
	
	public ItemPrice GetItemPrice()
	{
		return _price;
	}
	
	public void RegisterSlot(Inventory inv, CustomerInv customerInv, int page, int slot, boolean shopItem)
	{
		_slotPositions.put(inv, new SlotInfo(inv, customerInv, page, slot, shopItem));
	}
	
	public void UnRegisterSlot(Inventory inv)
	{
		_slotPositions.remove(inv);
	}
	
	void LoadLores()
	{
		_lores = new String[5];
		String front = (this instanceof ShopItemSeller ? "&2BUY " : "&3SELL ");
		_lores[0] =  Metods.msgC("&9____ &6Amount: &a");
		_lores[1] =  Metods.msgC(" &bM&21  "+front+" :  &e1  : &5");
		_lores[2] =  Metods.msgC(" &bM&42  "+front+" :  &e8  : &5");
		_lores[3] =  Metods.msgC("&9S&bM&21  "+front+" : &e64 : &5");
		_lores[4] =  Metods.msgC("&9S&bM&42  "+front+" : &eAll : &5");

	}
	protected LinkedList<String> GetLores()
	{
		LinkedList<String> lores = new LinkedList<>();
		//String empty = "&4-";
		
		
		lores.add( _lores[0] + _amount+" &9____");
		
		if(_amount >= 1)	lores.add(_lores[1]+GetItemPrice().GetCustomerPriceStr(1)); 
		if(_amount >= 8)	lores.add(_lores[2]+GetItemPrice().GetCustomerPriceStr(8));		
		if(_amount >= 64) 	lores.add(_lores[3]+GetItemPrice().GetCustomerPriceStr(64));
		
		if(_amount != 1 && _amount != 8 && _amount != 64) lores.add(_lores[4]+GetItemPrice().GetCustomerPriceStr(_amount));
		
		
		
		
//		if(lores.get(3).equalsIgnoreCase(empty))
//		{
//			lores[3] = 	(_amount  	>  8 	? 	_lores[4]+GetItemPrice().GetCustomerPriceStr(_amount) 	: empty);
//		}
//		else
//		{
//			lores[4] = 	(_amount  	>= 64 	? 	_lores[4]+GetItemPrice().GetCustomerPriceStr(_amount) 	: empty);
//		}
		
		
		
		
		return lores;
	}
	protected void toolTip()
	{
		if(_amount <= 0)
		{
			_display_stack = _display_out_of_stock;
			return;
		}
		_display_stack = _real_stack.clone();
		_display_stack.setAmount(1);
		
		_metods.addLore(_display_stack, GetLores());

	}
	
	public SlotInfo GetSlotInfo(Inventory inv)
	{
		return _slotPositions.get(inv);
	}

	public ItemStack GetDisplayItem()
	{
		toolTip();
		if(AddGlow) 
		{
			ImusAPI._metods.AddGlow(_display_stack);
		}
		return _display_stack;
	}
	
	public ItemStack GetDisplayItemNotAvailable()
	{
		return _display_stack_not_available;
	}
	
	public ItemStack GetRealItem()
	{
		return _real_stack;
	}
	
	public Boolean IsSameKind(ShopItemBase item)
	{

		return item.GetRealItem().isSimilar(_real_stack);
	}
	
	public Boolean IsSameKind(ItemStack item)
	{
		return item.isSimilar(_real_stack);
	}
	
	public void UpdateItem()
	{
		for(SlotInfo sInfo : _slotPositions.values())
		{
			if(!sInfo._shopItem)
			{

				((CustomerMenuBaseInv)sInfo._customerInv).UpdateCustomerSlot((ShopItemCustomer)this, sInfo._page, sInfo._slot);
				return;
			}
			else
			{

				sInfo._customerInv.SetShopSlot((ShopItemSeller)this, sInfo._page, sInfo._slot);
			}
		}
	}
	
	public void AddAmount(int amount)
	{
		_amount+= amount;			
	}
	
	void CheckDisplayIte()
	{
		if(_amount <= 0)
		{
			_amount = 0;
			GetDisplayItem().setType(Material.BLACK_STAINED_GLASS_PANE);
		}
		else
		{
			if(GetDisplayItem().getType() != GetRealItem().getType())
			{
				GetDisplayItem().setType(GetRealItem().getType());
			}
		}
		
		//SetLoreAtSpot(LoreSpot.AMOUNT);
		toolTip();
	}
	
	public int Get_amount() {
		return _amount;
	}

	
	public void Set_amount(int _amount) 
	{
		this._amount = _amount;
		//CheckDisplayItem();
	}
	
	public ShopItemType GetItemType()
	{
		return _type;
	}
}
