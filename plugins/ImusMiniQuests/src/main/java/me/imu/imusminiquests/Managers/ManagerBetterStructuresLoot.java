package me.imu.imusminiquests.Managers;

import com.magmaguy.betterstructures.api.ChestFillEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Puts quest panels in BetterStructures chests. Kept apart from ManagerQuestDrops and only
 * registered when BetterStructures is on the server, because loading this class needs
 * BetterStructures' event class.
 */
public class ManagerBetterStructuresLoot implements Listener
{
    private final ManagerQuestDrops _drops;

    public ManagerBetterStructuresLoot(ManagerQuestDrops drops)
    {
        _drops = drops;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChestFill(ChestFillEvent event)
    {
        _drops.rollBetterStructuresChest(event.getContainer().getInventory());
    }
}
