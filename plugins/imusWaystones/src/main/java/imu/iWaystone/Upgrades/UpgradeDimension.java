package imu.iWaystone.Upgrades;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.iWaystone.Waystones.Waystone;
import imu.iWaystones.Enums.UpgradeType;

public class UpgradeDimension extends BaseUpgrade
{

	@Override
	Tier[] SetTiers() 
	{
		Tier[] tiers = new Tier[3];
		tiers[0] = new Tier(new ItemStack[] {new ItemStack(Material.ENDER_PEARL,8),new ItemStack(Material.BLAZE_ROD,16), new ItemStack(Material.RED_MUSHROOM,16)});
		tiers[1] = new Tier(new ItemStack[] {new ItemStack(Material.ENDER_PEARL,10),new ItemStack(Material.ENDER_EYE,32),new ItemStack(Material.PURPUR_BLOCK,64)});
		tiers[2] = new Tier(null);
		return tiers;
	}

	@Override
	public String[] GetDescription() {

		return new String[]{"&eUprade makes posible to see Waystones from other dimension","&3Tier 1: &bNormal-dimension","&3Tier 2: &bNether-dimension","&3Tier 3: &bEnd-dimension"};
	}

	@Override
	public double GetTierReduceValue() 
	{
		return 1;
	}

	@Override
	public String GetDisplayName() {
		return "&3Transfer Upgrade";
	}

	@Override
	public Material GetMaterial() 
	{
		return Material.ENDER_PEARL;
	}

	@Override
	public UpgradeType GetType() 
	{
		return UpgradeType.DIMENSION;
	}

	@Override
	public double GetCombinedValue(double value) {
		return 0;
	}
	
	public boolean IsNetherUnlocked()
	{
		return GetCurrentTier() >= 1;
	}
	
	public boolean IsEndUnlocked()
	{
		return GetCurrentTier() >= 2;
	}

	@Override
	public void ButtonPressUpgradeTier(Player player, Waystone ws, int tierBeforeUpgrade) {
		
	}

	
	
}
