package imu.GS.ShopUtl;

import java.util.ArrayList;


import imu.GS.ENUMs.EnchantResrictionOptions;
import imu.GS.ENUMs.ModDataItemGenValues;
import imu.GS.Interfaces.IModData;
import imu.GS.Interfaces.IModDataValues;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;


public class ItemGenModData implements IModData
{
	public double _priceMin = -1;
	public double _priceMax = -1;
	
	public int _stack_amount_min = 64;
	public int _stack_amount_max = 64;
	
	public int _fillAmount_min = -1;
	public int _fillAmount_max = -1;
	
	public int _fillTime_min = -1;
	public int _fillTime_max = -1;
	
	public int _selector_ench = 0;
	public int _enchants = 0;
	public int _ench_restrict = 0;
	public int _ench_level_min = -1;
	public int _ench_level_max = -1;
	
	public int _searchOptions = 0;
	public int _generationAmount = 1;
	private ArrayList<String> _searchTags = new ArrayList<>();
	
	Metods _metods;
	
	public ItemGenModData()
	{
		_metods = ImusAPI._metods;
	}
	
	<T> String StrMinMax(T min, T max)
	{
		return "&cmin: "+min+" &2max: "+max;
	}
	
	String SearchOption(int option)
	{
		return "ALL";
	}
	
	void ChangeEnchRestriction()
	{
		_ench_restrict++;
		if(_ench_restrict >= EnchantResrictionOptions.values().length) _ench_restrict = 0;
	}
	
	public void ChangeEnchSelector()
	{
		_selector_ench++;
		if(_selector_ench > 3) _selector_ench = 0;
	}
	
	@Override
	public String GetValueStr(IModDataValues v, String trueFrontText, String trueBackText, String falseStr) {
		ModDataItemGenValues value = (ModDataItemGenValues)v;
		//System.out.println("Getting value for :"+value);
		String str = "";
		switch (value)
		{
		case ENCHANTS:
			str += _enchants;
			break;
		case ENCH_RESTRICT:
			str += EnchantResrictionOptions.values()[_ench_restrict].toString();//_ench_restrict ? "&2True" : "&cFalse";
			break;
		case ENCH_LEVEL_MIN:
			if(_ench_level_min == -1) return falseStr;
			str += _ench_level_min;
			break;
		case ENCH_LEVEL_MAX:
			if(_ench_level_max == -1) return falseStr;
			str += _ench_level_max;
			break;
		case FILL_AMOUNT_RANGE:
			if(_fillAmount_max == -1 || _fillAmount_min == -1) return falseStr;
			str += StrMinMax(_fillAmount_min, _fillAmount_max);
			break;
		case FILL_TIME_RANGE:
			if(_fillTime_max == -1 || _fillTime_min == -1) return falseStr;
			str += StrMinMax(_fillTime_min, _fillTime_max);
			break;
		case GENERATION_AMOUNT:
			str += _generationAmount;
			break;
		case PRICE_RANGE:
			if(_priceMin == -1 || _priceMax == -1) return falseStr;
			str += StrMinMax(_priceMin, _priceMax);
			break;
		case SEARCH_OPTION:
			str += SearchOption(_searchOptions);
			break;
		case STACK_AMOUNT_RANGE:
			str += StrMinMax(_stack_amount_min, _stack_amount_max);
			break;
		case TAGS:
			if(_searchTags == null || _searchTags.size() == 0) return falseStr;
			str += ImusAPI._metods.CombineArrayToOneString(_searchTags.toArray(), "; ");
			break;
		default:
			break;
			
		}
		
		
		return trueFrontText+str+trueBackText;
	}
	String[] SplitMinMax(String str)
	{
		String[] minMax = str.split(" ");
		if(minMax.length != 2) return null;
		return minMax;
	}
	
	boolean IsSplitDigits(String[] strs)
	{
		if(strs == null || strs.length != 2) return false;
		if(!_metods.isDigit(strs[0]) || !_metods.isDigit(strs[1])) return false;
		return true;
	}
	
	boolean IsValidMinMax(double min, double max)
	{	
		return min <= max;
	}
	
	@Override
	public boolean SetAndCheck(IModDataValues v, String str) {
		
		ModDataItemGenValues value = (ModDataItemGenValues)v;
		int min, max;
		double dmin, dmax;
		String[] minMax;
		switch (value)
		{
		case ENCHANTS:
			if(!_metods.isDigit(str)) return false;
			_enchants = Integer.parseInt(str);
			break;
		case ENCH_RESTRICT:
			ChangeEnchRestriction();
			break;
		case ENCH_LEVEL_MIN:
			if(!_metods.isDigit(str)) return false;
			if(!IsValidMinMax(Integer.parseInt(str), _ench_level_max)) return false;
			_ench_level_min = Integer.parseInt(str);
			break;
		case ENCH_LEVEL_MAX:
			if(!_metods.isDigit(str)) return false;
			if(!IsValidMinMax(_ench_level_min, Integer.parseInt(str))) return false;
			_ench_level_max = Integer.parseInt(str);
			break;
		case FILL_AMOUNT_RANGE:
			minMax = SplitMinMax(str);
			if(!IsSplitDigits(minMax)) return false;
			min = Integer.parseInt(minMax[0]);
			max = Integer.parseInt(minMax[1]);
			if(!IsValidMinMax(min, max)) return false;
			_fillAmount_min = min;
			_fillAmount_max = max;
			break;
		case FILL_TIME_RANGE:
			minMax = SplitMinMax(str);
			if(!IsSplitDigits(minMax)) return false;
			min = Integer.parseInt(minMax[0]);
			max = Integer.parseInt(minMax[1]);
			if(!IsValidMinMax(min, max)) return false;
			_fillTime_min = min;
			_fillTime_max = max;
			break;
		case GENERATION_AMOUNT:
			if(!_metods.isDigit(str)) return false;
			_generationAmount = Integer.parseInt(str);
			break;
		case PRICE_RANGE:
			minMax = SplitMinMax(str);
			if(!IsSplitDigits(minMax)) return false;
			dmin = Double.parseDouble(minMax[0]);
			dmax = Integer.parseInt(minMax[1]);
			if(!IsValidMinMax(dmin, dmax)) return false;
			_priceMin = dmin;
			_priceMax = dmax;
			break;
		case SEARCH_OPTION:
			break;
		case STACK_AMOUNT_RANGE:
			minMax = SplitMinMax(str);
			if(!IsSplitDigits(minMax)) return false;
			min = Integer.parseInt(minMax[0]);
			max = Integer.parseInt(minMax[1]);
			if(!IsValidMinMax(min, max)) return false;
			_stack_amount_min = min;
			_stack_amount_max = max;
			break;
		case TAGS:
			AddTag(str);
			break;
		}
		return true;
	}
	
	public void AddTag(String tag)
	{

		_searchTags.add(tag);
	}
	
	public void ClearSearchTags()
	{
		_searchTags.clear();
	}
	
	public String[] GetSearchTags()
	{
		return (String[]) _searchTags.toArray();
	}
 	
	
	public EnchantResrictionOptions GetEnchRestrictionName()
	{
		return EnchantResrictionOptions.values()[_ench_restrict];
	}
}
