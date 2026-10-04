package me.imu.imusminiquests.Quests;

import imu.iAPI.Other.Metods;
import me.imu.imusminiquests.CONSTANTS;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Managers.ManagerRewardPools;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reads and writes quest items. A quest item is a normal ItemStack with the quest id, its
 * progress, its rarity and, for timed quests, when the current attempt started, all in its
 * persistent data. It never stacks, so every item keeps its own progress.
 */
public final class QuestItem
{
    private static NamespacedKey _keyQuestId;
    private static NamespacedKey _keyProgress;
    private static NamespacedKey _keyRarity;
    private static NamespacedKey _keyTimerStart;

    private QuestItem() {}

    public static void init(Plugin plugin)
    {
        _keyQuestId = new NamespacedKey(plugin, CONSTANTS.KEY_QUEST_ID);
        _keyProgress = new NamespacedKey(plugin, CONSTANTS.KEY_PROGRESS);
        _keyRarity = new NamespacedKey(plugin, CONSTANTS.KEY_RARITY);
        _keyTimerStart = new NamespacedKey(plugin, CONSTANTS.KEY_TIMER_START);
    }

    /** A new panel with a random rarity. */
    public static ItemStack create(Quest quest)
    {
        return create(quest, ImusMiniQuests.getInstance().getRarities().roll());
    }

    public static ItemStack create(Quest quest, PanelRarity rarity)
    {
        ItemStack stack = new ItemStack(quest.getMaterial());
        ItemMeta meta = stack.getItemMeta();
        meta.setMaxStackSize(1);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(_keyQuestId, PersistentDataType.STRING, quest.getId());
        pdc.set(_keyProgress, PersistentDataType.INTEGER, 0);
        pdc.set(_keyRarity, PersistentDataType.STRING, rarity.name());
        stack.setItemMeta(meta);

        refresh(stack, quest);
        return stack;
    }

    public static boolean isQuestItem(ItemStack stack)
    {
        return getQuestId(stack) != null;
    }

    /**
     * The quest id of the item, or null when it isn't a quest item.
     */
    public static String getQuestId(ItemStack stack)
    {
        if (stack == null || stack.isEmpty() || !stack.hasItemMeta()) return null;
        return stack.getItemMeta().getPersistentDataContainer().get(_keyQuestId, PersistentDataType.STRING);
    }

    public static int getProgress(ItemStack stack)
    {
        if (stack == null || !stack.hasItemMeta()) return 0;
        Integer progress = stack.getItemMeta().getPersistentDataContainer().get(_keyProgress, PersistentDataType.INTEGER);
        return progress == null ? 0 : progress;
    }

    /** The panel's rarity; panels from before rarities count as the most common one. */
    public static PanelRarity getRarity(ItemStack stack)
    {
        String name = stack == null || !stack.hasItemMeta() ? null
                : stack.getItemMeta().getPersistentDataContainer().get(_keyRarity, PersistentDataType.STRING);
        return ImusMiniQuests.getInstance().getRarities().get(name);
    }

    /** How many this panel needs: the quest's amount scaled by the panel's rarity. */
    public static int getRequiredAmount(ItemStack stack, Quest quest)
    {
        return getRarity(stack).scale(quest.getRequiredAmount());
    }

    public static boolean isComplete(ItemStack stack, Quest quest)
    {
        return getProgress(stack) >= getRequiredAmount(stack, quest);
    }

    /** When the current attempt of a timed quest started, 0 when none is running. */
    public static long getTimerStart(ItemStack stack)
    {
        Long start = stack.getItemMeta().getPersistentDataContainer().get(_keyTimerStart, PersistentDataType.LONG);
        return start == null ? 0 : start;
    }

    public static void setTimerStart(ItemStack stack, long start)
    {
        ItemMeta meta = stack.getItemMeta();
        if (start <= 0) meta.getPersistentDataContainer().remove(_keyTimerStart);
        else meta.getPersistentDataContainer().set(_keyTimerStart, PersistentDataType.LONG, start);
        stack.setItemMeta(meta);
    }

    /**
     * Milliseconds left of a timed quest's attempt, 0 when it has run out, -1 when the quest
     * isn't timed or no attempt is running.
     */
    public static long getTimeLeft(ItemStack stack, Quest quest)
    {
        int limit = quest.getObjective().timeLimitSeconds();
        long start = getTimerStart(stack);
        if (limit <= 0 || start <= 0) return -1;
        return Math.max(0, start + limit * 1000L - System.currentTimeMillis());
    }

    /**
     * Stores the progress, capped to the panel's amount, and redraws the name and lore.
     */
    public static void setProgress(ItemStack stack, Quest quest, int progress)
    {
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(_keyProgress, PersistentDataType.INTEGER,
                Math.max(0, Math.min(progress, getRequiredAmount(stack, quest))));
        stack.setItemMeta(meta);
        refresh(stack, quest);
    }

    /**
     * Redraws the name, lore, glint and model from the quest definition, config.yml and the
     * stored progress. The rarity is the last line of the lore, not part of the name.
     */
    public static void refresh(ItemStack stack, Quest quest)
    {
        FileConfiguration config = ImusMiniQuests.getInstance().getConfig();
        PanelRarity rarity = getRarity(stack);
        int progress = getProgress(stack);
        int amount = getRequiredAmount(stack, quest);
        boolean complete = progress >= amount;
        QuestObjective objective = quest.getObjective();

        List<String> lore = new ArrayList<>(quest.getLore());
        if (!lore.isEmpty()) lore.add("");
        lore.add(Metods.msgC(config.getString("lore.objective", "&6Quest: &f%objective%")
                .replace("%objective%", objective.describe(amount))));
        if (objective.timeLimitSeconds() > 0)
        {
            lore.add(Metods.msgC(config.getString("lore.time-limit", "&c⌚ Within %time% &8(the count starts over when time runs out)")
                    .replace("%time%", formatSeconds(objective.timeLimitSeconds()))));
        }
        lore.add(Metods.msgC(config.getString("lore.progress", "%bar% &e%progress%&7/&e%amount%")
                .replace("%bar%", progressBar(config, progress, amount))
                .replace("%progress%", String.valueOf(progress))
                .replace("%amount%", String.valueOf(amount))));
        if (config.getBoolean("lore.show-rewards", true)) addRewardLines(lore, config, quest, rarity);
        for (String line : config.getStringList(complete ? "lore.complete" : "lore.instructions"))
        {
            lore.add(Metods.msgC(line));
        }
        lore.add(Metods.msgC(config.getString("lore.rarity", "&8Rarity: %rarity%").replace("%rarity%", rarity.display())));

        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(quest.getName());
        meta.setLore(lore);
        meta.setEnchantmentGlintOverride(config.getBoolean("item.glint", true) ? Boolean.TRUE : null);
        meta.setItemModel(complete ? completeModel(config, quest) : quest.getModel());
        stack.setItemMeta(meta);
    }

    /** 30 -> "30s", 90 -> "1m 30s" */
    public static String formatSeconds(long seconds)
    {
        if (seconds < 60) return seconds + "s";
        return seconds / 60 + "m" + (seconds % 60 == 0 ? "" : " " + seconds % 60 + "s");
    }

    /**
     * Rewards:
     *  • 8-16 x Experience Bottle      (guaranteed)
     * One of:
     *  • Wooden-Diamond Pickaxe (with slots)
     */
    private static void addRewardLines(List<String> lore, FileConfiguration config, Quest quest, PanelRarity rarity)
    {
        List<String> guaranteed = describe(quest.getGuaranteedRewards());
        List<String> pool = quest.getRolls() > 0 ? describe(quest.getPoolRewards()) : List.of();
        ManagerRewardPools pools = ImusMiniQuests.getInstance().getRewardPools();
        int randomRolls = pools.getTiers().isEmpty() ? 0 : quest.getRandomRolls() + rarity.extraRandomRewards();
        if (guaranteed.isEmpty() && pool.isEmpty() && randomRolls == 0) return;

        String line = config.getString("lore.reward-line", "&8 • &f%reward%");
        lore.add("");
        lore.add(Metods.msgC(config.getString("lore.rewards-header", "&6Rewards:")));
        for (String reward : guaranteed)
            lore.add(Metods.msgC(line.replace("%reward%", reward)));

        if (!pool.isEmpty())
        {
            String poolHeader = quest.getRolls() == 1
                    ? config.getString("lore.rewards-pool-one", "&7One of:")
                    : config.getString("lore.rewards-pool-many", "&7%rolls% of:");
            lore.add(Metods.msgC(poolHeader.replace("%rolls%", String.valueOf(quest.getRolls()))));
            for (String reward : pool)
                lore.add(Metods.msgC(line.replace("%reward%", reward)));
        }

        if (randomRolls > 0)
        {
            lore.add(Metods.msgC(config.getString("lore.random-rewards", "&d+%count% random reward(s) &8(%range%&8)")
                    .replace("%count%", String.valueOf(randomRolls))
                    .replace("%range%", pools.describeRange())));
        }
    }

    private static List<String> describe(List<QuestReward> rewards)
    {
        return rewards.stream().map(QuestReward::describe).filter(d -> d != null && !d.isBlank()).toList();
    }

    private static NamespacedKey completeModel(FileConfiguration config, Quest quest)
    {
        String model = config.getString("item.complete-model", "");
        NamespacedKey key = model.isBlank() ? null : NamespacedKey.fromString(model.toLowerCase(Locale.ROOT));
        return key != null ? key : quest.getModel();
    }

    private static String progressBar(FileConfiguration config, int progress, int amount)
    {
        int length = Math.max(1, config.getInt("lore.bar-length", 20));
        String filled = config.getString("lore.bar-filled", "&a|");
        String empty = config.getString("lore.bar-empty", "&8|");
        int filledCount = amount <= 0 ? length : (int) ((long) progress * length / amount);

        return filled.repeat(filledCount) + empty.repeat(length - filledCount);
    }
}
