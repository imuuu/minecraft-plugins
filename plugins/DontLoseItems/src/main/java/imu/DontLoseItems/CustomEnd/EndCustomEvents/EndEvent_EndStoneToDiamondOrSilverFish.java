package imu.DontLoseItems.CustomEnd.EndCustomEvents;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import imu.DontLoseItems.CustomEnd.EndEvents;
import imu.iAPI.Other.Metods;

public class EndEvent_EndStoneToDiamondOrSilverFish extends EndEvent
{
	private final double _chanceToBeDiamond = 0.5f;
	public EndEvent_EndStoneToDiamondOrSilverFish()
	{
		super("Endstone to Diamonds or Silver Fish", 10);
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
		ItemStack stack = new ItemStack(Material.DIAMOND,3);
		
		for(Player p : GetPlayers())
		{	
			Metods._ins.InventoryAddItemOrDrop(stack.clone(), p);
		}
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

		
		if (Math.random() < _chanceToBeDiamond) 
		{
			loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.DIAMOND));
        }
		else
        {
        	 loc.getWorld().spawnEntity(loc, EntityType.SILVERFISH);
        }
	}

	@Override
	public String GetEventName()
	{		
		return "Diamond Time or Silver Fish!";
	}

	@Override
	public String GetRewardInfo()
	{
		return "Get 3 diamonds and chestloot +"+ChestLootAmount;
	}

	@Override
	public String GetDescription()
	{
		
		return "End stone drops diamonds or Silve Fish";
	}

}
