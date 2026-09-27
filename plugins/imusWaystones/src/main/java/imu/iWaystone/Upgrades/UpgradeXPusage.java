package imu.iWaystone.Upgrades;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.iWaystone.Waystones.Waystone;
import imu.iWaystones.Enums.UpgradeType;

public class UpgradeXPusage extends BaseUpgrade
{
	@Override
	public boolean IsEnabled()
	{
		return false;
	}
	
	@Override
	Tier[] SetTiers() 
	{
		Tier[] tiers = new Tier[11];
		tiers[0] = new Tier(new ItemStack[] {new ItemStack(Material.IRON_BLOCK,1)});
		tiers[1] = new Tier(new ItemStack[] {new ItemStack(Material.IRON_BLOCK,3)});
		tiers[2] = new Tier(new ItemStack[] {new ItemStack(Material.IRON_BLOCK,5)});
		tiers[3] = new Tier(new ItemStack[] {new ItemStack(Material.GOLD_BLOCK,1)});
		tiers[4] = new Tier(new ItemStack[] {new ItemStack(Material.GOLD_BLOCK,5)});
		tiers[5] = new Tier(new ItemStack[] {new ItemStack(Material.GOLD_BLOCK,10)});
		tiers[6] = new Tier(new ItemStack[] {new ItemStack(Material.DIAMOND,3)});
		tiers[7] = new Tier(new ItemStack[] {new ItemStack(Material.IRON_BLOCK,10),new ItemStack(Material.GOLD_BLOCK,5),new ItemStack(Material.DIAMOND_BLOCK,2)});
		tiers[8] = new Tier(new ItemStack[] {new ItemStack(Material.GOLD_BLOCK,10), new ItemStack(Material.DIAMOND_BLOCK,3)});
		tiers[9] = new Tier(new ItemStack[] {new ItemStack(Material.GOLD_BLOCK,5),new ItemStack(Material.DIAMOND_BLOCK,5),new ItemStack(Material.NETHERITE_BLOCK,1)});
		tiers[10] = new Tier(new ItemStack[] {null});
		return tiers;
	}

	@Override
	public String[] GetDescription() 
	{
		return new String[]{"&eEvery &6&lTier &eReduces xp level use by &2"+_tierReduceValue+" &aL"};
	}

	@Override
	public double GetTierReduceValue() 
	{
		return 1.0;
	}

	@Override
	public String GetDisplayName() 
	{
		return "&6Reduce xp usage";
	}

	@Override
	public Material GetMaterial() 
	{
		return Material.EXPERIENCE_BOTTLE;
	}

	@Override
	public UpgradeType GetType() 
	{
		return UpgradeType.XP_USAGE;
	}

	@Override
	public double GetCombinedValue(double value) 
	{
		return value - _tierReduceValue * GetCurrentTier();
	}

	@Override
	public void ButtonPressUpgradeTier(Player player, Waystone ws, int tierBeforeUpgrade) 
	{
		
	}

	

}
