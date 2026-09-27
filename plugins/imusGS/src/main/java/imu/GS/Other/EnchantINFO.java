package imu.GS.Other;

import org.bukkit.enchantments.Enchantment;

import imu.GS.ShopUtl.ItemPrice.ItemPrice;
import imu.GS.ShopUtl.ItemPrice.PriceMoney;
import imu.iAPI.Other.Metods;

public class EnchantINFO 
{
	private int _minLevel = 0;
	private int _maxLevel = 0;
	private ItemPrice _minPrice = new PriceMoney();
	private ItemPrice _maxPrice = new PriceMoney();
	private Enchantment _ench;
	private double _rawMultiplier = 0.3f;
	
	public EnchantINFO(Enchantment ench, int minLevel, int maxLevel, double rawMultiplier) 
	{
		_minLevel = minLevel;
		_maxLevel = maxLevel;
		_ench = ench;
		Set_rawMultiplier(rawMultiplier);
		
	}
	
	public String GetName()
	{
		return _ench.getKey().getKey().toLowerCase();
	}
	
	public Enchantment GetEnchantment()
	{
		return _ench;
	}
	
	public int GetMinLevel()
	{
		return _minLevel;
	}
	
	public int GetMaxLevel()
	{
		return _maxLevel;
	}
	
	public void SetMaxLevel(int lvl)
	{
		if(lvl < 0) lvl = _ench.getMaxLevel();
		_maxLevel = lvl;
	}
	
	public ItemPrice GetMinPrice()
	{
		return _minPrice;
	}
	
	public ItemPrice GetMaxPrice()
	{
		return _maxPrice;
	}
	
	public boolean SetPrice(ItemPrice min, ItemPrice max)
	{
		if(!min.getClass().equals(max.getClass())) return false;
		
		if(min.GetPrice() > max.GetPrice()) return false;
		
		_minPrice = min;
		_maxPrice = max;
		return true;
	}
	
	public ItemPrice GetPrice(int levelNow)
	{
		return new PriceMoney().SetPrice(PriceCalculation(levelNow));
	}
	
	double PriceCalculation(double levelNow)
	{
		double price = 0;
		double maxDmin = _maxPrice.GetPrice() / _minPrice.GetPrice();
		double top = _minPrice.GetPrice();
		double lower = Math.pow(maxDmin, 1.0/((double)_maxLevel-1.0));
		double end = Math.pow(Math.pow(maxDmin, 1.0/((double)_maxLevel-1.0)), levelNow);
		price = (top/lower) * end;	
		
		if(levelNow >= _maxLevel)
			price = _maxPrice.GetPrice();
		
		return price;
	}

	public double Get_rawMultiplier() {
		return Metods.Round(_rawMultiplier);
	}

	public void Set_rawMultiplier(double _rawMultiplier) {
		this._rawMultiplier = _rawMultiplier;
	}
}
