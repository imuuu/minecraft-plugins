package imu.DontLoseItems.CustomEnd.EndCustomEvents;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import imu.DontLoseItems.CustomEnd.EndEvents;

public class EndEvent_EndStoneToDeprisScrap extends EndEvent
{
	private final double _chanceToBeNetherScrap = 0.6;
	public EndEvent_EndStoneToDeprisScrap()
	{
		super("Endstone chance to be nether scrap!", 7);
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
		
		if (Math.random() < _chanceToBeNetherScrap) 
		{
            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.NETHERITE_SCRAP));
        }
	}

	@Override
	public String GetEventName()
	{		
		return "Endstone to nether Scrap!";
	}

	@Override
	public String GetRewardInfo()
	{
		return "Chestloot roll+"+ChestLootAmount;
	}

	@Override
	public String GetDescription()
	{	
		return "Endstone drops nether scrap";
	}

}
