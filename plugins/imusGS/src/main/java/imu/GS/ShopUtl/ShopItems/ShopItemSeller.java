package imu.GS.ShopUtl.ShopItems;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import com.google.gson.JsonObject;

import imu.GS.Main.Main;
import imu.GS.ShopUtl.ShopNormal;
import imu.GS.ShopUtl.ShopItemBase;
import imu.GS.ShopUtl.ShopItemResult;
import imu.GS.ShopUtl.Customer.ShopItemCustomer;
import imu.GS.ShopUtl.ItemPrice.ItemPrice;
import imu.GS.ShopUtl.ItemPrice.PriceMoney;
import imu.GS.ShopUtl.ItemPrice.PriceOwn;
import imu.iAPI.Main.ImusAPI;


public class ShopItemSeller extends ShopItemBase
{
	int _shopPage = -1;
	int _slot = -1;
	
	
	
	public ShopItemSeller(Main main, ShopNormal shopBase, ItemStack real, int amount) {
		super(main,shopBase, real, amount);
		SetItemPrice(_main.GetMaterialManager().GetPriceMaterialAndCheck(real));
	}
	
	public void SetTargetShopitem(ShopItemBase sib)
	{
		ShopItemCustomer sic = (ShopItemCustomer) sib;
		_customerShopitemTargets.put(sic.GetOwner().getUniqueId(), sic);
	}
	
	
	public ShopItemSeller SetPageAndSlot(int page, int slot)
	{
		_shopPage = page;
		_slot = slot;
		ImusAPI._metods.setPersistenData(_display_stack, _main.get_shopManager().pd_page, PersistentDataType.INTEGER, page);
		ImusAPI._metods.setPersistenData(_display_stack, _main.get_shopManager().pd_slot, PersistentDataType.INTEGER, slot);
		return this;
	}
	
	@Override
	public ItemStack GetDisplayItem() 
	{
		super.GetDisplayItem();
		ImusAPI._metods.setPersistenData(_display_stack, _main.get_shopManager().pd_page, PersistentDataType.INTEGER, _shopPage);
		ImusAPI._metods.setPersistenData(_display_stack, _main.get_shopManager().pd_slot, PersistentDataType.INTEGER, _slot);
		return _display_stack;
	}
	
	@Override
	public void AddAmount(int amount) 
	{
		super.AddAmount(amount);
		_shopBase.AddMaterialCount(GetRealItem().getType(), amount);
	}
	
	public int GetPage()
	{
		return _shopPage;
	}
	
	public int GetSlot()
	{
		return _slot;
	}
	
	public boolean CanShowToPlayer(Player player)
	{
		return true;
	}
	
	@Override
	protected void SetShowPrice(ItemPrice price) 
	{

		if(_shopBase == null)
		{
			return;
		}
					
		if(price instanceof PriceMoney && !(price instanceof PriceOwn))
		{
			double p = price.GetPrice();
			((PriceMoney)price).SetCustomerPrice(p * _shopBase.get_sellM());	
		}
	
	}


	@Override
	public JsonObject GetJsonData() 
	{
		JsonObject obj = new JsonObject();
		return obj;
	}

	@Override
	public void ParseJsonData(JsonObject data) 
	{
		
	}

	@Override
	public ShopItemResult[] GetTransactionResultItemStack() {
		return new ShopItemResult[] {new ShopItemResult(GetRealItem(), GetRealItem().getAmount())};
	}
	
}
