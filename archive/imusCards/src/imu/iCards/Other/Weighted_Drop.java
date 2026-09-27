package imu.iCards.Other;

public abstract class Weighted_Drop
{
	private double _weight;
	//private double _chance;
	private Category _category;
	public Weighted_Drop(double weight,Category category)
	{
		super();
		this._weight = weight;
		//this._chance = chance;
		this._category = category;
	}
	
	public double Get_weight()
	{
		return _weight;
	}
	public void Set_weight(double _weight)
	{
		this._weight = _weight;
	}
//	public double Get_chance()
//	{
//		return _chance;
//	}
//	public void Set_chance(double _chance)
//	{
//		this._chance = _chance;
//	}
	public Category Get_category()
	{
		return _category;
	}
	public void Set_category(Category _category)
	{
		this._category = _category;
	}
	
	
}
