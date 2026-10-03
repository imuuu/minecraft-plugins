package me.imu.imusminiquests.Managers;

import imu.iAPI.Events.FakeBlockBreakEvent;
import me.imu.imusminiquests.Enums.OBJECTIVE_TYPE;
import me.imu.imusminiquests.Events.QuestCompleteEvent;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Quests.Quest;
import me.imu.imusminiquests.Quests.QuestItem;
import me.imu.imusminiquests.Quests.QuestObjective;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.function.Predicate;

/**
 * Listens to what players do and moves the matching quest items in their inventory forward.
 * Also the single place that handles block breaks, so the player-placed check, progress and
 * drops see the block in the same state.
 */
public class ManagerQuestProgress implements Listener
{
    private final ImusMiniQuests _plugin;
    private final ManagerQuests _quests;
    private final ManagerPlacedBlocks _placedBlocks;
    private final ManagerQuestDrops _drops;

    public ManagerQuestProgress(ImusMiniQuests plugin, ManagerQuests quests, ManagerPlacedBlocks placedBlocks, ManagerQuestDrops drops)
    {
        _plugin = plugin;
        _quests = quests;
        _placedBlocks = placedBlocks;
        _drops = drops;
    }

    /**
     * Adds progress to the quest items in the player's inventory whose objective is of this type
     * and accepts the target. With progress.mode FIRST only the first unfinished one gets it.
     *
     * @return how many quest items moved forward
     */
    public int addProgress(Player player, OBJECTIVE_TYPE type, Predicate<QuestObjective> matches, int amount)
    {
        boolean all = "ALL".equalsIgnoreCase(_plugin.getConfig().getString("progress.mode", "FIRST"));
        PlayerInventory inv = player.getInventory();
        int updated = 0;

        for (int slot = 0; slot < inv.getSize(); slot++)
        {
            ItemStack stack = inv.getItem(slot);
            Quest quest = _quests.getQuest(stack);
            if (quest == null) continue;

            QuestObjective objective = quest.getObjective();
            if (objective.type() != type || !matches.test(objective)) continue;

            int progress = QuestItem.getProgress(stack);
            if (progress >= quest.getRequiredAmount()) continue;

            int newProgress = Math.min(progress + amount, quest.getRequiredAmount());
            QuestItem.setProgress(stack, quest, newProgress);
            inv.setItem(slot, stack);
            updated++;

            if (newProgress >= quest.getRequiredAmount()) onComplete(player, quest, stack);
            else showProgress(player, quest, newProgress);

            if (!all) break;
        }
        return updated;
    }

    private void showProgress(Player player, Quest quest, int progress)
    {
        if (!_plugin.getConfig().getBoolean("progress.action-bar", true)) return;

        String text = _plugin.getMessage("action-bar")
                .replace("%quest%", quest.getName())
                .replace("%progress%", String.valueOf(progress))
                .replace("%amount%", String.valueOf(quest.getRequiredAmount()));
        player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(text));
    }

    private void onComplete(Player player, Quest quest, ItemStack stack)
    {
        Bukkit.getPluginManager().callEvent(new QuestCompleteEvent(player, quest, stack));
        player.sendMessage(_plugin.getMessage("completed").replace("%quest%", quest.getName()));
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);
    }

    /**
     * Planted crops are always player placed, so they are never marked; they count only when
     * fully grown instead.
     */
    private static boolean isCrop(Block block)
    {
        Material type = block.getType();
        return Tag.CROPS.isTagged(type) || type == Material.NETHER_WART || type == Material.COCOA;
    }

    private static boolean isUnripeCrop(Block block)
    {
        BlockData data = block.getBlockData();
        return isCrop(block) && data instanceof Ageable ageable && ageable.getAge() < ageable.getMaximumAge();
    }

    private static boolean isFromSpawner(LivingEntity entity)
    {
        CreatureSpawnEvent.SpawnReason reason = entity.getEntitySpawnReason();
        return reason == CreatureSpawnEvent.SpawnReason.SPAWNER || reason == CreatureSpawnEvent.SpawnReason.TRIAL_SPAWNER;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Block block = event.getBlockPlaced();
        if (isCrop(block)) return;

        Material type = block.getType();
        if (_quests.isBreakTarget(type) || _drops.isDropMaterial(type)) _placedBlocks.mark(block);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event)
    {
        // Other plugins fire this to ask claim plugins for permission; nothing was really broken.
        if (event instanceof FakeBlockBreakEvent) return;

        Block block = event.getBlock();
        boolean placed = _placedBlocks.isPlacedByPlayer(block);
        if (placed) _placedBlocks.unmark(block);

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (placed && _plugin.getConfig().getBoolean("progress.ignore-player-placed-blocks", true)) return;
        if (isUnripeCrop(block)) return;

        Material type = block.getType();
        addProgress(player, OBJECTIVE_TYPE.BREAK_BLOCK, o -> o.targets().matches(type), 1);
        _drops.rollBlockDrop(player, type, block.getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null || killer.getGameMode() == GameMode.CREATIVE) return;
        if (isFromSpawner(entity) && !_plugin.getConfig().getBoolean("progress.count-spawner-mobs", false)) return;

        addProgress(killer, OBJECTIVE_TYPE.KILL_ENTITY, o -> o.targets().matches(entity), 1);

        ItemStack drop = _drops.rollMobDrop(killer, entity);
        if (drop != null) event.getDrops().add(drop);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event)
    {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof Item caught)) return;

        Material type = caught.getItemStack().getType();
        addProgress(event.getPlayer(), OBJECTIVE_TYPE.FISH, o -> o.targets().matches(type), 1);
    }
}
