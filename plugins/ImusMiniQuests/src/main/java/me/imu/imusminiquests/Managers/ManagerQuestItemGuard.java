package me.imu.imusminiquests.Managers;

import me.imu.imusminiquests.Hooks.ImusChallengesHook;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Quests.PanelRarity;
import me.imu.imusminiquests.Quests.Quest;
import me.imu.imusminiquests.Quests.QuestItem;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Right-clicking a quest item claims it when it is complete and shows its progress otherwise.
 * Quest items are never used as what they are made of: they can't be placed, eaten or crafted.
 */
public class ManagerQuestItemGuard implements Listener
{
    private final ImusMiniQuests _plugin;
    private final ManagerQuests _quests;

    public ManagerQuestItemGuard(ImusMiniQuests plugin, ManagerQuests quests)
    {
        _plugin = plugin;
        _quests = quests;
    }

    // Not ignoreCancelled: right-clicking air arrives already cancelled.
    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event)
    {
        ItemStack stack = event.getItem();
        if (!QuestItem.isQuestItem(stack)) return;

        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);
        if (!event.getAction().isRightClick() || event.getHand() == null) return;

        Player player = event.getPlayer();
        Quest quest = _quests.getQuest(stack);
        if (quest == null)
        {
            player.sendMessage(_plugin.getMessage("unknown-quest"));
            return;
        }

        if (!QuestItem.isComplete(stack, quest))
        {
            // A timed attempt that ran out starts over
            if (QuestItem.getTimeLeft(stack, quest) == 0)
            {
                QuestItem.setTimerStart(stack, 0);
                QuestItem.setProgress(stack, quest, 0);
                player.sendMessage(_plugin.getMessage("time-ran-out").replace("%quest%", quest.getName()));
            }
            // Picks up name and lore changes from a reload
            QuestItem.refresh(stack, quest);
            player.getInventory().setItem(event.getHand(), stack);
            player.sendMessage(_plugin.getMessage("not-complete")
                    .replace("%quest%", quest.getName())
                    .replace("%progress%", String.valueOf(QuestItem.getProgress(stack)))
                    .replace("%amount%", String.valueOf(QuestItem.getRequiredAmount(stack, quest))));
            return;
        }

        claim(player, event.getHand(), quest);
    }

    /**
     * Removes the completed quest item from the hand and hands out its rewards, rolled with the
     * luck the player had before this quest. Then adds a quest point and, when ImusChallenges is
     * there, challenge points.
     */
    private void claim(Player player, EquipmentSlot hand, Quest quest)
    {
        ItemStack inHand = player.getInventory().getItem(hand);
        PanelRarity rarity = QuestItem.getRarity(inHand);
        inHand.setAmount(inHand.getAmount() - 1);
        player.getInventory().setItem(hand, inHand.getAmount() > 0 ? inHand : null);
        _plugin.getPanelCarriers().markStale(player);

        ManagerQuestPoints questPoints = _plugin.getQuestPoints();
        // A rarer panel adds its own luck and random rewards on top of the player's
        double luck = Math.min(1, questPoints.getLuck(player) + rarity.bonusLuck());
        quest.giveRewards(player, luck, rarity.extraRandomRewards());
        player.sendMessage(_plugin.getMessage("claimed").replace("%quest%", quest.getName()));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.4f);

        if (questPoints.isEnabled())
        {
            int points = questPoints.addPoints(player, 1);
            player.sendMessage(_plugin.getMessage("quest-points")
                    .replace("%points%", String.valueOf(points))
                    .replace("%luck%", ManagerQuestPoints.formatLuck(questPoints.getLuck(points))));
        }

        int challengePoints = _plugin.getConfig().getInt("imuschallenges.points-per-quest", 1);
        if (_plugin.getConfig().getBoolean("imuschallenges.enabled", true) && challengePoints > 0
                && ImusChallengesHook.isEnabled())
        {
            ImusChallengesHook.addChallengePoints(player, challengePoints);
            player.sendMessage(_plugin.getMessage("challenge-points").replace("%points%", String.valueOf(challengePoints)));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event)
    {
        if (QuestItem.isQuestItem(event.getItemInHand())) event.setCancelled(true);
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event)
    {
        for (ItemStack ingredient : event.getInventory().getMatrix())
        {
            if (QuestItem.isQuestItem(ingredient))
            {
                event.getInventory().setResult(null);
                return;
            }
        }
    }
}
