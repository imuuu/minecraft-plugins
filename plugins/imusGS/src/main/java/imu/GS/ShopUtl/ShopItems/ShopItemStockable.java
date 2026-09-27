package imu.GS.ShopUtl.ShopItems;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonObject;

import imu.GS.ENUMs.ShopItemType;
import imu.GS.Main.Main;
import imu.GS.ShopUtl.ShopNormal;
import imu.GS.ShopUtl.ShopItemModData;
import imu.GS.ShopUtl.ItemPrice.ItemPrice;
import imu.GS.ShopUtl.ItemPrice.PriceCustom;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Tuple;

public class ShopItemStockable extends ShopItemSeller
{
	ShopItemModData _modData;
	
	protected Cooldowns _cd;
	public ShopItemStockable(Main main, ShopNormal shopBase, ItemStack real, int amount) 
	{
		super(main, shopBase, real, amount);
		_type = ShopItemType.STOCKABLE;
		GetModData()._maxAmount = amount;
		
	}
		
	public ShopItemModData GetModData()
	{
		if(_modData == null) _modData = new ShopItemModData();
		return _modData;
	}
	
	@Override
	public void SetItemPrice(ItemPrice price) 
	{
		super.SetItemPrice(price);
		GetModData()._itemPrice = price;
	}
	
	public void SetModData(ShopItemModData modData)
	{
		_modData = modData.clone();
		SetMaxAmount(_modData._maxAmount);
		SetFillAmount(_modData._fillAmount);
		SetFillDelayMinutes(_modData._fillDelayMinutes);
		ClearTags();
		if(_modData._tags != null)
		{
			for(String tag : modData._tags)
			{
				GetTags().add(tag);
			}
		}
		
		if(_modData._itemPrice instanceof ItemPrice)//if(_modData._ownPrice != -1)
		{
			SetItemPrice(_modData._itemPrice);
		}
	}
	
	@Override
	public boolean AddTag(String tagName) 
	{
		boolean b = super.AddTag(tagName);
		if(b)
		{
			_modData.AddTag(tagName.toLowerCase());
		}
		return b;
	}
	
	@Override
	public boolean CanShowToPlayer(Player player) 
	{
		if(_modData == null) return true; 
		
		if(_modData._sellTimeStart != -1 && _modData._sellTimeEnd != -1)
		{
			System.out.println("checking can show sell time");
			int w_t = (int) player.getWorld().getTime();
			if(_modData._sellTimeStart < _modData._sellTimeEnd)
			{
				if(!(w_t > _modData._sellTimeStart && w_t < _modData._sellTimeEnd))
				{
					return true;
				}
			}
			else
			{
				if((w_t < _modData._sellTimeStart) && (w_t > _modData._sellTimeEnd))
				{
					return true;
				}
			}
			return false;
		}
		
		if(_modData._locations != null)
		{
			for(Tuple<Integer,Location> disLoc : _modData._locations)
			{
				if(player.getLocation().getWorld() != disLoc.GetValue().getWorld()) continue;
				if(player.getLocation().distance(disLoc.GetValue()) < disLoc.GetKey())
				{
					return true;
				}
			}
			return false;
		}
		
		if(_modData._permissions != null)
		{
			System.out.println("checking can show permission");
			for(String permission : _modData._permissions)
			{
				if(player.hasPermission(permission))
				{
					return true;
				}
			}
			return false;
		}
		
		
		return true;
		
	}
	
	@Override
	protected void toolTip() 
	{
		if(GetItemPrice() instanceof PriceCustom)
		{
			String[] lores = {
					_lores[0]+Get_amount()+" &9____",
					"&9(&l&eClick&r&9) to see price!",
					"",

					
			};
			_display_stack = _real_stack.clone();
			_display_stack.setAmount(1);
			_metods.addLore(_display_stack, lores);
			return;
		}
		super.toolTip();
	}
	
	@Override
	public void AddAmount(int amount)
	{
		if(_modData._fillAmount > 0)
		{
			super.AddAmount(amount);
			if(Get_amount() > _modData._maxAmount)
				Set_amount(_modData._maxAmount);
			return;
		}
			
		//System.out.println("Stocable stack, amount doesnt decrease");
	}
	
	@Override
	public void Set_amount(int _amount) {
		super.Set_amount(_amount);
		_modData._maxAmount = _amount;
		
	}
	
	@Override
	public JsonObject GetJsonData() 
	{
		JsonObject obj = super.GetJsonData();
		//obj.addProperty("maxAmount", _modData._maxAmount);
		//obj.addProperty("fillAmount", _modData._fillAmount);
		//obj.addProperty("fillDelayMinutes", _modData._fillDelayMinutes);
		return obj;
	}

	@Override
	public void ParseJsonData(JsonObject data) 
	{
		super.ParseJsonData(data);
//		SetMaxAmount(data.get("maxAmount").getAsInt());
//		SetFillAmount(data.get("fillAmount").getAsInt());
//		SetFillDelayMinutes(data.get("fillDelayMinutes").getAsInt());
		
	}
	
	public void Fill()
	{
		AddAmount(_modData._fillAmount);
	}
	
	public void RefreshFillCD()
	{
		if(_cd == null)
			_cd = new Cooldowns();
		
		_cd.setCooldownInSeconds("fill", _modData._fillDelayMinutes * 10);
	}
	
	protected void SetMaxAmount(int amount)
	{
		_modData._maxAmount = amount;
		Set_amount(_modData._maxAmount);
	}
	
	protected void SetFillAmount(int amount)
	{
		_modData._fillAmount = amount;
	}
		
	protected void SetFillDelayMinutes(int minutes)
	{
		_modData._fillDelayMinutes = minutes;
		if(minutes > 0)
		{
			RefreshFillCD();
		}
		else
		{
			_cd = null;
		}
	}
	
	public boolean AbleToFill()
	{
		if(_cd != null && _cd.isCooldownReady("fill"))
			return true;
		
		return false;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
