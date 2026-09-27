package imu.GS.ShopUtl.ShopItems;


import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonObject;

import imu.GS.ENUMs.ShopItemType;
import imu.GS.Main.Main;
import imu.GS.ShopUtl.ShopNormal;
import imu.GS.ShopUtl.ShopItemBase;
import imu.GS.ShopUtl.ShopItemResult;
import imu.GS.ShopUtl.ItemPrice.ItemPrice;
import imu.GS.ShopUtl.ItemPrice.PriceUnique;

public class ShopItemUnique extends ShopItemBase
{
	
	protected double _uniquePrice = 0;

	public ShopItemUnique(Main main, ShopNormal shopBase, ItemStack real, int amount) 
	{
		super(main, shopBase, real, amount);
		_type = ShopItemType.UNIQUE;
		SetItemPrice(new PriceUnique().SetPrice(_uniquePrice));
	}
	
	@Override
	public void SetTargetShopitem(ShopItemBase sib) 
	{
		//Not implemented
	}
	
	@Override
	public void SetUUID(UUID uuid) 
	{
		_uuid = uuid;
		//_main.get_shopManager().GetUniqueManager().PutPDuuid(_real_stack, uuid);
		//System.out.println("UUID set to real: "+uuid);
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
		// TODO Auto-generated method stub
		
	}



	@Override
	protected void SetShowPrice(ItemPrice price) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public ShopItemResult[] GetTransactionResultItemStack() {
		return null;
	}

	
}
