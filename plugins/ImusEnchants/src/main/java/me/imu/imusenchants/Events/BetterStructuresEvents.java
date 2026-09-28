package me.imu.imusenchants.Events;

import com.magmaguy.betterstructures.api.ChestFillEvent;
import me.imu.imusenchants.CONSTANTS;
import me.imu.imusenchants.Managers.ChestLoot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

// Only registered when BetterStructures is installed, this class must not be loaded otherwise.
// BetterStructures fills its chests without loot tables, so LootGenerateEvent never fires for them.
public class BetterStructuresEvents implements Listener
{
	@EventHandler(priority = EventPriority.HIGH)
	public void OnBetterStructureLoot(ChestFillEvent e)
	{
		Inventory inv = e.getContainer().getSnapshotInventory();

		if (CONSTANTS.ENABLE_MENDING_FOUND_ONLY_END || !CONSTANTS.SET_FOUND_ENCHANTED_BOOKS_LEVEL_ONE)
		{
			Events.ProcessFoundItems(Arrays.asList(inv.getContents()), e.getContainer().getWorld());
		}

		for (ItemStack extra : ChestLoot.RollExtras())
		{
			int emptySlot = inv.firstEmpty();
			if (emptySlot < 0)
				return;

			inv.setItem(emptySlot, extra);
		}
	}
}
