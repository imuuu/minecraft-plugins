package imu.iWaystone.Upgrades;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iAPI.Other.Metods;
import imu.iWaystone.Waystones.Waystone;
import imu.iWaystones.Enums.UpgradeType;
import imu.iWaystones.Main.ImusWaystones;

public class UpgradeBottomBuild extends BaseUpgrade
{

	@Override
	Tier[] SetTiers() 
	{
		Tier[] tiers = new Tier[4];
		tiers[0] = new Tier(new ItemStack[] {new ItemStack(new BuildUpgradeCommon().get_mat())});
		tiers[1] = new Tier(new ItemStack[] {new ItemStack(new BuildUpgradeRare().get_mat())});
		tiers[2] = new Tier(new ItemStack[] {new ItemStack(new BuildUpgradeEpic().get_mat())});
		tiers[3] = new Tier(new ItemStack[] {new ItemStack(new BuildUpgradeLegendary().get_mat())});
		return tiers;
	}

	@Override
	public String[] GetDescription() 
	{
		return new String[]{"&eUpgrade waystone bottom block","&eReplaces old block and gives it back","&6This upgrade is &2Global!"};
	}

	@Override
	public double GetTierReduceValue() 
	{
		return 0;
	}

	@Override
	public String GetDisplayName() {
		return "&5Upgrade Waystone Base Block";
	}

	@Override
	public Material GetMaterial() 
	{
		return Material.STONE;
	}

	@Override
	public UpgradeType GetType() 
	{
		return UpgradeType.BUILD;
	}

	@Override
	public double GetCombinedValue(double value) 
	{
		return 0;
	}
	
	public void ReadTier(BuildUpgrade bUpgrade)
	{
		if(bUpgrade instanceof BuildUpgradeCommon)
		{
			SetCurrentier(1);
			return;
		}
		
		if(bUpgrade instanceof BuildUpgradeRare)
		{
			SetCurrentier(2);
			return;
		}
		
		if(bUpgrade instanceof BuildUpgradeEpic)
		{
			SetCurrentier(3);
			return;
		}
		
		if(bUpgrade instanceof BuildUpgradeLegendary)
		{
			SetCurrentier(4);
			return;
		}
	}

	@Override
	public void ButtonPressUpgradeTier(Player player, Waystone ws ,int tierBeforeUpgrade) 
	{
		if(IsMaxTier()) return;
		
		Block b = ws.GetLowBlock();
		
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				b.getWorld().getBlockAt(b.getLocation()).setType(_tiers[tierBeforeUpgrade]._cost[0].getType());
				Metods._ins.dropItem(_tiers[tierBeforeUpgrade-1]._cost[0].clone(), player, false);
			}
		}.runTask(ImusWaystones._instance);
		
		
	}

	

}
