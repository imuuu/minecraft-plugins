package imu.iWaystone.Upgrades;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.iWaystone.Waystones.Waystone;
import imu.iWaystones.Enums.UpgradeType;

public class UpgradeCastTime extends BaseUpgrade
{
	
	public UpgradeCastTime() 
	{
		super();
	}

	@Override
	Tier[] SetTiers() 
	{
		Tier[] tiers = new Tier[10];
		//ItemStack lapisBlock = new ItemStack(Material.LAPIS_BLOCK,22);
		tiers[0] = new Tier(new ItemStack[] {new ItemStack(Material.LAPIS_BLOCK,10)});
		tiers[1] = new Tier(new ItemStack[] {new ItemStack(Material.LAPIS_BLOCK,20)});
		tiers[2] = new Tier(new ItemStack[] {new ItemStack(Material.LAPIS_BLOCK,30)});
		tiers[3] = new Tier(new ItemStack[] {new ItemStack(Material.LAPIS_BLOCK,33)});
		tiers[4] = new Tier(new ItemStack[] {new ItemStack(Material.LAPIS_BLOCK,40)});
		tiers[5] = new Tier(new ItemStack[] {new ItemStack(Material.LAPIS_BLOCK,40), new ItemStack(Material.DIAMOND,2)});
		tiers[6] = new Tier(new ItemStack[] {new ItemStack(Material.LAPIS_BLOCK,30), new ItemStack(Material.DIAMOND,5)});
		tiers[7] = new Tier(new ItemStack[] {new ItemStack(Material.LAPIS_BLOCK,20), new ItemStack(Material.DIAMOND,10)});
		tiers[8] = new Tier(new ItemStack[] {new ItemStack(Material.LAPIS_BLOCK,10), new ItemStack(Material.DIAMOND,12)});
		tiers[9] = new Tier(new ItemStack[] {null});
		
		
		return tiers;
	}

	@Override
	public String[] GetDescription() 
	{
		return new String[] {"&eEvery &6&ltier &ereduces cast time &2"+_tierReduceValue+"&es"};
	}

	@Override
	public double GetTierReduceValue() 
	{
		return 3.0f;
	}

	@Override
	public String GetDisplayName() 
	{
		return  "&2Reduce Cast time";
	}

	@Override
	public Material GetMaterial() {
		return Material.CLOCK;
	}

	@Override
	public UpgradeType GetType() {
		return UpgradeType.CAST_TIME;
	}

	@Override
	public double GetCombinedValue(double value) 
	{
		return value - _tierReduceValue * GetCurrentTier();
	}

	@Override
	public void ButtonPressUpgradeTier(Player player, Waystone ws, int tierBeforeUpgrade) {
		
	}

	
	

	
}
