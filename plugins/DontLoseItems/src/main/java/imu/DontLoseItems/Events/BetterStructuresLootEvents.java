package imu.DontLoseItems.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.magmaguy.betterstructures.api.ChestFillEvent;

// The only class that touches BetterStructures classes. Registered only when the plugin
// is on the server: a listener with a missing event class fails to register as a whole,
// which used to take the nether and end loot handlers down with it.
public class BetterStructuresLootEvents implements Listener
{
	@EventHandler
	public void OnBetterStructureLoot(ChestFillEvent e)
	{
		if (e.isCancelled())
			return;

		ChestLootEvents.Instance.OnBetterStructureLoot(e.getContainer());
		EndChestLootEvents.Instance.OnBetterStructureLoot(e.getContainer());
	}
}
