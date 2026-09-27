package imu.iWaystone.Upgrades;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.iAPI.Other.Metods;
import imu.iWaystone.Waystones.Waystone;
import imu.iWaystones.Enums.UpgradeType;

public abstract class BaseUpgrade 
{
	public UpgradeType _id;	
	Tier[] _tiers;
	private int _currentTier = 0;
	protected String[] _description;
	public double _tierReduceValue = 0.0;
	public ItemStack _displayItem;
	protected boolean _refreshDescWithToolTip = false;

	public BaseUpgrade()
	{
		_id = GetType();
		_tiers = SetTiers();
		_tierReduceValue = GetTierReduceValue();
		_description = GetDescription();
		_displayItem = new ItemStack(GetMaterial());
		Metods.setDisplayName(_displayItem, GetDisplayName());
		//Tooltip();
	}
	
	abstract Tier[] SetTiers();
	
	public abstract String[] GetDescription();
	public abstract double GetTierReduceValue();
	public abstract String GetDisplayName();
	
	public abstract Material GetMaterial();
	public abstract UpgradeType GetType();
	public abstract double GetCombinedValue(double value);
	public abstract void ButtonPressUpgradeTier(Player player, Waystone ws,int tierBeforeUpgrade);
	
	
	public boolean IsEnabled()
	{
		return true;
	}
	
	public void Tooltip()
	{
		if(_refreshDescWithToolTip) 
		{
			_description = GetDescription();
		}
		
		ArrayList<String> lores = new ArrayList<>();
		//lores.add("&e"+_description);
		for(String descs : _description)
		{
			lores.add(descs);
		}
		lores.add(" ");
		lores.add("&b===== &6Tiers &b=====");
		for(int i = 0; i < _tiers.length-1; ++i)
		{
			if(i != _currentTier)
			{
				lores.add("&7=Tier &8"+(i+1));
				continue;
			}
			
			lores.add("&e&l=>Tier &6&l"+(i+1));
			lores.add(" ");

			lores.add("&e== &2NEXT TIER COST &e==");
			for(ItemStack stack : _tiers[i]._cost)
			{
				lores.add("&9==> &3"+stack.getType()+ ": &b"+stack.getAmount());
			}
			lores.add(" ");
			lores.add("&4===> &e&k#&7(&bCLICK&7)&e&k# &eto &5UPGRADE!");
			//System.out.println("setting up upgrade");
			break;
			
		}
		
		if(_currentTier >= GetMaxTier())
		{
			lores.add("&e== &5MAXED OUT! &e==");
		}
		
		//System.out.println("lores: "+lores);
		Metods._ins.SetLores(_displayItem, lores.toArray(String[] :: new), false);
	}
	
	public static BaseUpgrade GetNewUpgrade(UpgradeType type) 
	{
		switch(type)
		{
		case CAST_TIME:
			return new UpgradeCastTime();
		case COOLDOWN:
			return new UpgradeCooldown();
		case DIMENSION:
			return new UpgradeDimension();
		case XP_USAGE:
			return new UpgradeXPusage();
		case FOUNDATION:
			return new UpgradeFoundation();
		case RENAME:
			return new UpgradeNameChange();
		case BUILD:
			break;
		default:
			break;

		}
		return null;
	}
	
	public int GetCurrentTier()
	{
		return _currentTier;
	}
	
	public void SetCurrentier(int tierlvl)
	{
		if(tierlvl < 0) tierlvl = 0;
		if(tierlvl > GetMaxTier()) tierlvl = GetMaxTier();
		_currentTier = tierlvl;
	}
	
	public void IncreaseCurrentTier(int amount)
	{
		if(amount < 0) amount = 0;
		if(_currentTier > GetMaxTier()) 
		{
			amount = 0;		
		}
		
		
		_currentTier += amount;
		//System.out.println("Current tier: "+_currentTier);
	}
	
	public Tier GetTier()
	{
		if(_currentTier >= _tiers.length) return _tiers[_tiers.length-1];
		
		return _tiers[_currentTier];
	}
	
	public Tier GetTier(int id)
	{
		return _tiers[id];
	}
	
	public boolean IsMaxTier()
	{
		return _tiers.length-1 == _currentTier;
	}
	public int GetMaxTier()
	{
		return _tiers.length-1;
	}
	
	public ItemStack[] GetCost()
	{
		return _tiers[_currentTier]._cost;
	}
	
}
