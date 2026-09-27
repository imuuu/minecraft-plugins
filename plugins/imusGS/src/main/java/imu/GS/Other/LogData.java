package imu.GS.Other;

import imu.GS.ShopUtl.ShopItemBase;

public class LogData 
{
	private ShopItemBase _shopitem = null;
	private double _price = 0;
	private int _amount = 0;
	
	public LogData(ShopItemBase sib, double price, int amount)
	{
		_shopitem = sib;
		_price = price;
		_amount = amount;
		
	}

	public ShopItemBase Get_shopitem() {
		return _shopitem;
	}

	public double Get_price() {
		return _price;
	}

	public int Get_amount() {
		return _amount;
	}
	
	public void AddAmount(int amount)
	{
		_amount += amount;
	}
}
