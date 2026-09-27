package imu.iCards.Other;

import org.bukkit.inventory.ItemStack;

public class Card extends Weighted_Drop
{
	private String _name;
	private String _description;
	
	private String[] _lores;
	
	private int _maxStack = 1;
	
	private ItemStack[] _items;

	public Card(String name, int maxStack, double weight, Category category)
	{
		super(weight, category);
		
		_name = name;
		_maxStack = maxStack;
		
		_lores = new String[] {"lore1","lore2","lore3"};
	}

	public String GetName()
	{
		return this._name;
	}
	
	public void SetName(String name)
	{
		this._name = name;
	}

	public int GetMaxStack() 
	{
		return _maxStack;
	}

	public String Get_description()
	{
		return _description;
	}

	public void Set_description(String _description)
	{
		this._description = _description;
	}

	public String[] Get_lores()
	{
		return _lores;
	}

	public void Set_lores(String[] _lores)
	{
		this._lores = _lores;
	}
	
	
}
