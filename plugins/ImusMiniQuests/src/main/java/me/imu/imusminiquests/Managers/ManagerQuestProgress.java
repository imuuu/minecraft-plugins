package me.imu.imusminiquests.Managers;

import imu.iAPI.Events.FakeBlockBreakEvent;
import io.papermc.paper.event.player.PlayerTradeEvent;
import me.imu.imusminiquests.Enums.OBJECTIVE_TYPE;
import me.imu.imusminiquests.Events.QuestCompleteEvent;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Quests.Quest;
import me.imu.imusminiquests.Quests.QuestItem;
import me.imu.imusminiquests.Quests.QuestObjective;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
        if (player.getGameMode() == GameMode.CREATIVE) return 0;

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

            int required = QuestItem.getRequiredAmount(stack, quest);
            int progress = QuestItem.getProgress(stack);
            if (progress >= required) continue;

            // Timed quests: the first progress starts the clock, and when it has run out the
            // count starts over from this progress
            if (objective.timeLimitSeconds() > 0)
            {
                long timeLeft = QuestItem.getTimeLeft(stack, quest);
                if (timeLeft == 0 && progress > 0)
                    player.sendMessage(_plugin.getMessage("time-ran-out").replace("%quest%", quest.getName()));
                if (timeLeft <= 0 || progress == 0)
                {
                    progress = 0;
                    QuestItem.setTimerStart(stack, System.currentTimeMillis());
                }
            }

            int newProgress = Math.min(progress + amount, required);
            QuestItem.setProgress(stack, quest, newProgress);
            if (newProgress >= required) QuestItem.setTimerStart(stack, 0);
            inv.setItem(slot, stack);
            updated++;

            if (newProgress >= required) onComplete(player, quest, stack);
            else showProgress(player, quest, stack, newProgress, required);

            if (!all) break;
        }
        return updated;
    }

    private void showProgress(Player player, Quest quest, ItemStack stack, int progress, int required)
    {
        if (!_plugin.getConfig().getBoolean("progress.action-bar", true)) return;

        String text = _plugin.getMessage("action-bar")
                .replace("%quest%", quest.getName())
                .replace("%progress%", String.valueOf(progress))
                .replace("%amount%", String.valueOf(required));
        long timeLeft = QuestItem.getTimeLeft(stack, quest);
        if (timeLeft > 0)
            text += _plugin.getMessage("action-bar-time").replace("%time%", QuestItem.formatSeconds((timeLeft + 999) / 1000));
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

    private static int countInInventory(Player player, Material type)
    {
        int count = 0;
        for (ItemStack stack : player.getInventory().getStorageContents())
        {
            if (stack != null && stack.getType() == type) count += stack.getAmount();
        }
        return count;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event)
    {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getAction() == InventoryAction.NOTHING) return;

        ItemStack result = event.getCurrentItem();
        if (result == null || result.isEmpty()) return;
        Material type = result.getType();

        if (!event.isShiftClick())
        {
            addProgress(player, OBJECTIVE_TYPE.CRAFT, o -> o.targets().matches(type), result.getAmount());
            return;
        }

        // Shift-click crafts as many as the ingredients and free space allow, and the event
        // doesn't say how many. Count what arrived in the inventory once the craft is done.
        int before = countInInventory(player, type);
        Bukkit.getScheduler().runTask(_plugin, () ->
        {
            int crafted = countInInventory(player, type) - before;
            if (crafted > 0) addProgress(player, OBJECTIVE_TYPE.CRAFT, o -> o.targets().matches(type), crafted);
        });
    }

    // Taking smelted items out of a furnace, blast furnace or smoker. Hoppers don't count.
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSmeltExtract(FurnaceExtractEvent event)
    {
        if (event.getItemAmount() <= 0) return;

        Material type = event.getItemType();
        addProgress(event.getPlayer(), OBJECTIVE_TYPE.SMELT, o -> o.targets().matches(type), event.getItemAmount());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event)
    {
        Material type = event.getItem().getType();
        addProgress(event.getEnchanter(), OBJECTIVE_TYPE.ENCHANT, o -> o.targets().matches(type), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreed(EntityBreedEvent event)
    {
        if (!(event.getBreeder() instanceof Player player)) return;

        LivingEntity baby = event.getEntity();
        addProgress(player, OBJECTIVE_TYPE.BREED, o -> o.targets().matches(baby), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTrade(PlayerTradeEvent event)
    {
        Material type = event.getTrade().getResult().getType();
        addProgress(event.getPlayer(), OBJECTIVE_TYPE.TRADE, o -> o.targets().matches(type), 1);
    }

    // Walked distance not yet counted, per player: progress moves in whole blocks
    private final Map<UUID, Double> _walked = new HashMap<>();

    /**
     * WALK quests: every whole block walked (or sprinted, swum...) on foot counts once. Flying,
     * gliding, riding and teleports don't count. The target is the block underfoot, so a quest
     * can ask for walking on sand.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event)
    {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getWorld() != to.getWorld()) return;

        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double distanceSquared = dx * dx + dz * dz;
        if (distanceSquared == 0) return;

        Player player = event.getPlayer();
        if (player.isFlying() || player.isGliding() || player.isInsideVehicle()) return;
        // A jump this big in one move is a teleport or lag, not walking
        if (distanceSquared > 4) return;

        double walked = _walked.merge(player.getUniqueId(), Math.sqrt(distanceSquared), Double::sum);
        if (walked < 1) return;

        int blocks = (int) walked;
        _walked.put(player.getUniqueId(), walked - blocks);
        Material underfoot = to.clone().subtract(0, 0.1, 0).getBlock().getType();
        addProgress(player, OBJECTIVE_TYPE.WALK, o -> o.targets().matches(underfoot), blocks);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        _walked.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event)
    {
        Material type = event.getItem().getType();
        addProgress(event.getPlayer(), OBJECTIVE_TYPE.EAT, o -> o.targets().matches(type), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTame(EntityTameEvent event)
    {
        if (!(event.getOwner() instanceof Player player)) return;

        LivingEntity tamed = event.getEntity();
        addProgress(player, OBJECTIVE_TYPE.TAME, o -> o.targets().matches(tamed), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShear(PlayerShearEntityEvent event)
    {
        Entity sheared = event.getEntity();
        addProgress(event.getPlayer(), OBJECTIVE_TYPE.SHEAR, o -> o.targets().matches(sheared), 1);
    }
}
