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
 * Reads and writes quest items. A quest item is a normal ItemStack with the quest id and its
 * progress in its persistent data; it never stacks, so every item keeps its own progress.
 */
public final class QuestItem
{
    private static NamespacedKey _keyQuestId;
    private static NamespacedKey _keyProgress;

    private QuestItem() {}

    public static void init(Plugin plugin)
    {
        _keyQuestId = new NamespacedKey(plugin, CONSTANTS.KEY_QUEST_ID);
        _keyProgress = new NamespacedKey(plugin, CONSTANTS.KEY_PROGRESS);
    }

    public static ItemStack create(Quest quest)
    {
        ItemStack stack = new ItemStack(quest.getMaterial());
        ItemMeta meta = stack.getItemMeta();
        meta.setMaxStackSize(1);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(_keyQuestId, PersistentDataType.STRING, quest.getId());
        pdc.set(_keyProgress, PersistentDataType.INTEGER, 0);
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

    public static boolean isComplete(ItemStack stack, Quest quest)
    {
        return getProgress(stack) >= quest.getRequiredAmount();
    }

    /**
     * Stores the progress, capped to the quest's amount, and redraws the name and lore.
     */
    public static void setProgress(ItemStack stack, Quest quest, int progress)
    {
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(_keyProgress, PersistentDataType.INTEGER,
                Math.max(0, Math.min(progress, quest.getRequiredAmount())));
        stack.setItemMeta(meta);
        refresh(stack, quest);
    }

    /**
     * Redraws the name, lore, glint and model from the quest definition, config.yml and the
     * stored progress.
     */
    public static void refresh(ItemStack stack, Quest quest)
    {
        FileConfiguration config = ImusMiniQuests.getInstance().getConfig();
        int progress = getProgress(stack);
        int amount = quest.getRequiredAmount();
        boolean complete = progress >= amount;

        List<String> lore = new ArrayList<>(quest.getLore());
        if (!lore.isEmpty()) lore.add("");
        lore.add(Metods.msgC(config.getString("lore.objective", "&6Quest: &f%objective%")
                .replace("%objective%", quest.getObjective().describe())));
        lore.add(Metods.msgC(config.getString("lore.progress", "%bar% &e%progress%&7/&e%amount%")
                .replace("%bar%", progressBar(config, progress, amount))
                .replace("%progress%", String.valueOf(progress))
                .replace("%amount%", String.valueOf(amount))));
        if (config.getBoolean("lore.show-rewards", true)) addRewardLines(lore, config, quest);
        for (String line : config.getStringList(complete ? "lore.complete" : "lore.instructions"))
        {
            lore.add(Metods.msgC(line));
        }

        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(quest.getName());
        meta.setLore(lore);
        meta.setEnchantmentGlintOverride(config.getBoolean("item.glint", true) ? Boolean.TRUE : null);
        meta.setItemModel(complete ? completeModel(config, quest) : quest.getModel());
        stack.setItemMeta(meta);
    }

    /**
     * Rewards:
     *  • 8-16 x Experience Bottle      (guaranteed)
     * One of:
     *  • Wooden-Diamond Pickaxe (with slots)
     */
    private static void addRewardLines(List<String> lore, FileConfiguration config, Quest quest)
    {
        List<String> guaranteed = describe(quest.getGuaranteedRewards());
        List<String> pool = quest.getRolls() > 0 ? describe(quest.getPoolRewards()) : List.of();
        ManagerRewardPools pools = ImusMiniQuests.getInstance().getRewardPools();
        int randomRolls = pools.getTiers().isEmpty() ? 0 : quest.getRandomRolls();
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
