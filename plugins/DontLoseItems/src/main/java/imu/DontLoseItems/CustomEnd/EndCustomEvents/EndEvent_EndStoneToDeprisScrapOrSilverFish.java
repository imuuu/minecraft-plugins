package imu.DontLoseItems.CustomEnd.EndCustomEvents;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import imu.DontLoseItems.CustomEnd.EndEvents;

public class EndEvent_EndStoneToDeprisScrapOrSilverFish extends EndEvent
{
	private final double _chanceToBeSilverNetherCrap = 0.3;
	public EndEvent_EndStoneToDeprisScrapOrSilverFish()
	{
		super("Endstone drops Nether Crap or Silver Fish", 11);
		ChestLootAmount = 1;
	}

	@Override
	public void OnEventStart()
	{
		
	}

	@Override
	public void OnEventEnd()
	{
		AddChestLootBaseToAll(ChestLootAmount);
		
	}

	@Override
	public void OnPlayerLeftMiddleOfEvent(Player player)
	{
		
	}

	@Override
	public void OnPlayerJoinMiddleOfEvent(Player player)
	{
		
	}

	@Override
	public void OnOneTickLoop()
	{
		
	}
	@EventHandler
	private void OnBlockBreak(BlockBreakEvent e)
	{
		if(e.getBlock().getType() != Material.END_STONE) return;

		Location loc = e.getBlock().getLocation();
		if(!EndEvents.Instance.IsPlayerUnstableArea(loc)) return;
		
		if (Math.random() < _chanceToBeSilverNetherCrap) 
		{
            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.NETHERITE_SCRAP));
        }
		else
        {
			loc.getWorld().spawnEntity(loc, EntityType.SILVERFISH);
        }
	}

	@Override
	public String GetEventName()
	{		
		return "Endstone to nether Scrap or Silver Fish";
	}

	@Override
	public String GetRewardInfo()
	{
		return "Chestloot roll+"+ChestLootAmount;
	}

	@Override
	public String GetDescription()
	{	
		return "Endstone to nether Scrap or Silver Fish";
	}

}
