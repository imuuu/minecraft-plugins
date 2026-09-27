package imu.GS.ShopUtl.ItemPrice;

import imu.iAPI.Other.Metods;

public class PriceMoney extends ItemPrice
{
	double _price = 0;
	protected double _showPrice = 0;
	
	@Override
	public double GetCustomerPrice(int amount) 
	{
		return _showPrice * amount;
	}
	
	public ItemPrice SetPrice(double price)
	{
		_price = Metods.Round(price);
		return this;
	}
	
	public void SetCustomerPrice(double showPrice)
	{
		_showPrice = showPrice;
	}
	
	@Override
	public double GetPrice()
	{
		return _price;
	}
	
	@Override
	public ItemPrice clone()
	{
		PriceMoney priceMoney = (PriceMoney)super.clone();
		priceMoney.SetPrice(_price);
		priceMoney.SetCustomerPrice(_showPrice);
		return priceMoney;
	}
	
	@Override
	public String toString() 
	{
		return "price: "+_price + " showPrice: "+_showPrice;
	}
}
