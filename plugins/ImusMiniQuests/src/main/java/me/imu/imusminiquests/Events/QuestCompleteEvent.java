package me.imu.imusminiquests.Events;

import me.imu.imusminiquests.Quests.Quest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Called when a quest item reaches its amount, before the player is told. The reward is
 * handed out later, when the player right-clicks the item.
 */
public class QuestCompleteEvent extends Event
{
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player _player;
    private final Quest _quest;
    private final ItemStack _item;

    public QuestCompleteEvent(Player player, Quest quest, ItemStack item)
    {
        _player = player;
        _quest = quest;
        _item = item;
    }

    public Player getPlayer() {return _player;}

    public Quest getQuest() {return _quest;}

    /** The completed quest item, a copy: changing it does nothing. */
    public ItemStack getItem() {return _item.clone();}

    @Override
    public HandlerList getHandlers() {return HANDLERS;}

    public static HandlerList getHandlerList() {return HANDLERS;}
}
