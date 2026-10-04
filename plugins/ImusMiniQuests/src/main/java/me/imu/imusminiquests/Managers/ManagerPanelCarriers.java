package me.imu.imusminiquests.Managers;

import me.imu.imusminiquests.Enums.OBJECTIVE_TYPE;
import me.imu.imusminiquests.Quests.Quest;
import me.imu.imusminiquests.Quests.QuestItem;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Remembers which kinds of unfinished quest panels each player carries, so the progress
 * listeners can skip everyone else without looking at their inventory: a player with no WALK
 * panel costs one map lookup per block walked.
 * <p>
 * The tag is marked stale on everything that can change an inventory (pickups, clicks, drags,
 * drops, deaths, joins, opening a panel...) and rescanned the next time it is asked for. Items
 * added by other plugins without an event are caught by the safety nets: a tag older than
 * {@link #MAX_AGE_MS} is rescanned, and the walk listener marks players who aren't carrying a
 * walk panel stale every few blocks.
 */
public class ManagerPanelCarriers implements Listener
{
    private static final long MAX_AGE_MS = 30_000;

    private record Tag(Set<OBJECTIVE_TYPE> types, long scannedAt) {}

    private final ManagerQuests _quests;
    private final Map<UUID, Tag> _tags = new HashMap<>();

    public ManagerPanelCarriers(ManagerQuests quests)
    {
        _quests = quests;
    }

    /**
     * True when the player carries an unfinished panel whose objective is of this type.
     */
    public boolean carries(Player player, OBJECTIVE_TYPE type)
    {
        Tag tag = _tags.get(player.getUniqueId());
        if (tag == null || System.currentTimeMillis() - tag.scannedAt() > MAX_AGE_MS)
            tag = scan(player);
        return tag.types().contains(type);
    }

    /** Forget the player's tag, so the next question rescans their inventory. */
    public void markStale(HumanEntity player)
    {
        _tags.remove(player.getUniqueId());
    }

    /** Forget every tag, e.g. after a reload changed which quests exist. */
    public void markAllStale()
    {
        _tags.clear();
    }

    private Tag scan(Player player)
    {
        Set<OBJECTIVE_TYPE> types = EnumSet.noneOf(OBJECTIVE_TYPE.class);
        for (ItemStack stack : player.getInventory().getContents())
        {
            Quest quest = _quests.getQuest(stack);
            if (quest != null && !QuestItem.isComplete(stack, quest)) types.add(quest.getObjective().type());
        }

        Tag tag = new Tag(types, System.currentTimeMillis());
        _tags.put(player.getUniqueId(), tag);
        return tag;
    }

    // Everything below only marks the tag stale; the rescan waits until it is needed

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPickup(EntityPickupItemEvent event)
    {
        if (event.getEntity() instanceof Player player) markStale(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClick(InventoryClickEvent event)
    {
        markStale(event.getWhoClicked());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDrag(InventoryDragEvent event)
    {
        markStale(event.getWhoClicked());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event)
    {
        markStale(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDrop(PlayerDropItemEvent event)
    {
        markStale(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event)
    {
        markStale(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event)
    {
        markStale(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event)
    {
        markStale(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event)
    {
        markStale(event.getPlayer());
    }
}
