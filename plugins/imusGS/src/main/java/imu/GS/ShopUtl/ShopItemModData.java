package imu.GS.ShopUtl;

import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import imu.GS.ENUMs.ModDataShopStockable;
import imu.GS.Interfaces.IModData;
import imu.GS.Interfaces.IModDataValues;
import imu.GS.ShopUtl.ItemPrice.ItemPrice;
import imu.GS.ShopUtl.ItemPrice.PriceCustom;
import imu.GS.ShopUtl.ItemPrice.PriceOwn;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Tuple;



public class ShopItemModData implements Cloneable, IModData
{
	//public double _ownPrice = -1;
	public int _maxAmount  = -1;
	public int _fillAmount = -1;
	public int _fillDelayMinutes = -1;
	
	public int _sellTimeStart = -1;
	public int _sellTimeEnd = -1;
	
	public LinkedList<Tuple<Integer,Location>> _locations;
	
	public LinkedList<String> _permissions;
	public LinkedList<String> _worldNames;
	public LinkedList<String> _tags;
	
	public ItemPrice _itemPrice;
	
	public String GetValueStr(IModDataValues v, String trueFrontText, String trueBackText,String falseStr)
	{
		ModDataShopStockable value = (ModDataShopStockable)v;
		String str ="";
		if(trueFrontText == null) trueFrontText = "";
		if(trueBackText == null) trueBackText = "";
		switch (value) 
		{
		case CUSTOM_PRICE:
			if(_itemPrice instanceof PriceOwn)
			{
				str += trueFrontText+_itemPrice.GetPrice() +trueBackText;
				return str;
			}
			if(_itemPrice instanceof PriceCustom)
			{
				str += trueFrontText+"Multiple Price" +trueBackText;
				return str;
			}
			str += falseStr;
			break;
		case MAX_AMOUNT:
			str += _maxAmount != -1 ? trueFrontText+_maxAmount+trueBackText : falseStr;
			break;
		case FILL_AMOUNT:
			str += _fillAmount != -1 ? trueFrontText+_fillAmount+trueBackText : falseStr;
			break;
		case FILL_DELAY:
			str += _fillDelayMinutes != -1 ? trueFrontText+_fillDelayMinutes +trueBackText: falseStr;
			break;
		case SELL_TIME_START:
			str += _sellTimeStart != -1 ? trueFrontText+_sellTimeStart +trueBackText: falseStr;
			break;
		case SELL_TIME_END:
			str += _sellTimeEnd != -1 ? trueFrontText+_sellTimeEnd+trueBackText : falseStr;
			break;
		case PERMISSIONS:
			str += _permissions != null ? trueFrontText+ImusAPI._metods.CombineArrayToOneString(_permissions.toArray(), "; ")+trueBackText : falseStr;
			break;
		case WORLD_NAMES:
			str += _worldNames != null ? trueFrontText+ImusAPI._metods.CombineArrayToOneString(_worldNames.toArray(), "; ")+trueBackText : falseStr;
			break;
		case DISTANCE_LOC:
			//str += _locations != null ? trueFrontText + _locWorld +" "+_locX+ " "+_locY+" "+_locZ+trueBackText : falseStr;
			String[] strs = null;
			if(_locations != null)
			{
				strs = new String[_locations.size()];
				for(int i = 0; i < strs.length; ++i)
				{
					Location loc =  _locations.get(i).GetValue();
					String locStr = loc.getWorld().getName()+" "+loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ();
					strs[i] = _locations.get(i).GetKey() + " "+locStr;
				}
			}		
			str += _locations != null ? trueFrontText + ImusAPI._metods.CombineArrayToOneString(strs, "; ")+trueBackText : falseStr;
			break;
		case TAGS:
			str += _tags != null ? trueFrontText+ImusAPI._metods.CombineArrayToOneString(_tags.toArray(), "; ")+trueBackText : falseStr;
			break;
	
		}

		return str;
	}
	
	public boolean SetAndCheck(IModDataValues v, String str)
	{
		ModDataShopStockable value = (ModDataShopStockable) v;
		switch (value) 
		{

		case CUSTOM_PRICE:
			if(!ImusAPI._metods.isDigit(str)) return false;
			_itemPrice = new PriceOwn().SetPrice(Double.parseDouble(str));
			System.out.println("itemprice set: "+_itemPrice);
			break;
		case MAX_AMOUNT:
			if(!ImusAPI._metods.isDigit(str)) return false;
			_maxAmount = Integer.parseInt(str);
			break;
		case FILL_AMOUNT:
			if(!ImusAPI._metods.isDigit(str)) return false;
			_fillAmount = Integer.parseInt(str);
			break;
		case FILL_DELAY:
			if(!ImusAPI._metods.isDigit(str)) return false;
			_fillDelayMinutes = Integer.parseInt(str);
			break;
		case DISTANCE_LOC:
			if(!IsLocationValid(str)) return false;
			AddLocation(str);
			break;
		case SELL_TIME_START:
			if(!ImusAPI._metods.isDigit(str)) return false;
			_sellTimeStart = Integer.parseInt(str);
			break;
		case SELL_TIME_END:
			if(!ImusAPI._metods.isDigit(str)) return false;
			_sellTimeEnd = Integer.parseInt(str);
			break;
		case PERMISSIONS:
			AddPermission(str);
			break;
		case WORLD_NAMES:
			AddWorldName(str);				
			break;
		case TAGS:
			AddTag(str.toLowerCase());
			break;
		
		}
		return true;
	}
	
	public void AddLocation(int distance, Location loc)
	{
		if(_locations == null) _locations = new LinkedList<>();
		_locations.add(new Tuple<Integer, Location>(distance, loc));
	}
	
	public void AddLocation(String loc_str)
	{
		String[] parts = loc_str.split(" ");
		Location loc = new Location(Bukkit.getWorld(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
		AddLocation(Integer.parseInt(parts[0]), loc);
	}
	
	public void AddTag(String tagName)
	{
		if(_tags == null) _tags = new LinkedList<>();
		_tags.add(tagName.toLowerCase());
	}
	
	public void ClearTags()
	{
		_tags = null;
	}
	boolean IsLocationValid(String loc_str)
	{
		String[] parts = loc_str.split(" ");
		if(parts.length != 5) return false;
		if(!(Bukkit.getWorlds().stream().anyMatch(world -> world.getName().equals(parts[1])))) return false;
		if(!(ImusAPI._metods.isDigit(parts[2])) ||!(ImusAPI._metods.isDigit(parts[3])) || !(ImusAPI._metods.isDigit(parts[3]))) return false;
		
		return true;
	}
	public void ClearLocations()
	{
		_locations = null;
	}
	
	public void AddPermission(String permission)
	{
		if(_permissions == null) _permissions = new LinkedList<>();
		_permissions.add(permission);
	}
	
	public void ClearPermissions()
	{
		_permissions = null;
	}
	
	public void AddWorldName(String worldName)
	{
		if(_worldNames == null) _worldNames = new LinkedList<>();
		_worldNames.add(worldName);
		
	}
	
	public void ClearWorldNames()
	{
		_worldNames = null;
	}
	
	public ShopItemModData clone()
	{
		ShopItemModData modData = new ShopItemModData();
		modData._maxAmount = _maxAmount;
		modData._sellTimeEnd = _sellTimeEnd;
		modData._sellTimeStart = _sellTimeStart;
		modData._fillAmount = _fillAmount;
		modData._fillDelayMinutes = _fillDelayMinutes;
		modData._itemPrice = _itemPrice != null ? _itemPrice.clone() : null;
		
		if(_locations != null)			
		{
			for(Tuple<Integer,Location> locs : _locations)
			{
				modData.AddLocation(locs.GetKey(), locs.GetValue());
			}
		}
		
		if(_permissions != null)
		{
			for(String permission : _permissions)
			{
				modData.AddPermission(permission);
			}
		}
		
		if(_worldNames != null)
		{
			for(String worldName : _worldNames)
			{
				modData.AddWorldName(worldName);
			}
		}
		
		if(_tags != null)
		{
			for(String tagName : _tags)
			{
				modData.AddTag(tagName);
			}
		}
		
			
		return modData;
	}
}
