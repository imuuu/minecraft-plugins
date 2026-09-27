package imu.iWaystone.Upgrades;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iAPI.Other.Metods;
import imu.iWaystone.Waystones.Waystone;
import imu.iWaystones.Enums.ConvUpgradeModData;
import imu.iWaystones.Enums.UpgradeType;
import imu.iWaystones.Invs.WaystoneUpgradeMenu;
import imu.iWaystones.Main.ImusWaystones;
import imu.iWaystones.Other.ConvUpgrade;

public class UpgradeNameChange extends BaseUpgrade
{

	@Override
	Tier[] SetTiers() {
		Tier[] tiers = new Tier[2];
		tiers[0] = new Tier(new ItemStack[] {new ItemStack(Material.NAME_TAG,1)});
		tiers[1] = new Tier(null);
		return tiers;
	}

	@Override
	public String[] GetDescription() 
	{
		return new String[] {"&eBy upgrading able to rename waystone &b&lonce&e!","&eTier stays same!"};
	}

	@Override
	public double GetTierReduceValue() 
	{
		return 0;
	}

	@Override
	public String GetDisplayName() 
	{
		return "&6Rename Your Waystone!";
	}

	@Override
	public Material GetMaterial() 
	{
		return Material.NAME_TAG;
	}

	@Override
	public UpgradeType GetType() 
	{
		return UpgradeType.RENAME;
	}

	@Override
	public double GetCombinedValue(double value) 
	{
		return 0;
	}

	@Override
	public void ButtonPressUpgradeTier(Player player, Waystone ws, int tierBeforeUpgrade) 
	{
		
		new BukkitRunnable() 
		{
			
			@Override
			public void run() 
			{
				player.closeInventory();
				Metods._ins.ConversationWithPlayer(player, new ConvUpgrade(ConvUpgradeModData.RENAME, new WaystoneUpgradeMenu(ImusWaystones._instance, player, ImusWaystones._instance.GetWaystoneManager().GetWaystone(ws.GetUUID())), "&3Give waystone &2new &3name?"));
			}
		}.runTask(ImusWaystones._instance);
		
	}

	@Override
	public void IncreaseCurrentTier(int amount)
	{
		
	}
}
