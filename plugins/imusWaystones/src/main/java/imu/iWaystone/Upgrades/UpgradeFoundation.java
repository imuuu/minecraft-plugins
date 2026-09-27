package imu.iWaystone.Upgrades;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.iAPI.Other.Metods;
import imu.iWaystone.Waystones.Waystone;
import imu.iWaystones.Enums.UpgradeType;

public class UpgradeFoundation extends BaseUpgrade
{

	public UpgradeFoundation()
	{
		_refreshDescWithToolTip = true;
	}
	
	@Override
	Tier[] SetTiers() 
	{
		Tier[] tiers = new Tier[4];
//		tiers[0] = new Tier(new ItemStack[] {new ItemStack(Material.COPPER_BLOCK,1)});
//		tiers[0]._value = 60 * 60 * 2;
				
		tiers[0] = new Tier(new ItemStack[] {new ItemStack(Material.GOLD_BLOCK,1)});
		tiers[0]._value = 60 * 90;
		
		tiers[1] = new Tier(new ItemStack[] {new ItemStack(Material.DIAMOND_BLOCK,1)});
		tiers[1]._value = 60 * 40;
		
		tiers[2] = new Tier(new ItemStack[] {new ItemStack(Material.NETHERITE_BLOCK,1)});
		tiers[2]._value = 60 * 25;
		
		tiers[3] = new Tier(null);
		tiers[3]._value = 60;
		

		return tiers;
	}

	@Override
	public String[] GetDescription() 
	{
		String[] arr = new String[]
				{
						"&dYour Waystone will be the fastest when foundation is maxed out!",
						"",
						"&3&lCurrent Tier &3=> &9Base Cooldown: " + Metods.FormatTime((long)(GetTier()._value*1000)),

				};
		
		return arr;
	}

	@Override
	public double GetTierReduceValue() 
	{
		return 0;
	}

	@Override
	public String GetDisplayName() 
	{
		return "&5Foundation Upgrade";
	}

	@Override
	public Material GetMaterial() 
	{
		return Material.DIAMOND_BLOCK;
	}

	@Override
	public UpgradeType GetType() 
	{
		return UpgradeType.FOUNDATION;
	}

	@Override
	public double GetCombinedValue(double value) 
	{
		return 0;
	}

	@Override
	public void ButtonPressUpgradeTier(Player player, Waystone ws, int tierBeforeUpgrade) 
	{
		
	}

}
