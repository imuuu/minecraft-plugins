package imu.GS.ShopUtl.ItemPrice;

import imu.iAPI.Other.Metods;

public abstract class ItemPrice  implements Cloneable
{
	public abstract double GetPrice();
	public abstract ItemPrice SetPrice(double price);
	public abstract double GetCustomerPrice(int amountA);
	public ItemPrice clone()
	{
		try {
			return (ItemPrice) super.clone();
		} catch (CloneNotSupportedException e) {

		}
		return null;
	}
	public String GetCustomerPriceStr(int amount)
	{
		return String.valueOf((Metods.Round(GetCustomerPrice(amount))))+" &2$";
	}
}
