package me.imu.imusminiquests.Managers;

import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Quests.Quest;
import me.imu.imusminiquests.Quests.QuestItem;
import me.imu.imusminiquests.Quests.TargetSet;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * Decides where quest panels are found: in loot chests the first time they are opened, and
 * with a small chance from mob kills and block breaks. Which quest it is gets picked by each
 * quest's drop-weight.
 */
public class ManagerQuestDrops implements Listener
{
    private final ImusMiniQuests _plugin;
    private final ManagerQuests _quests;
    private TargetSet _blockMaterials;
    private List<Pattern> _chestLootTables = List.of();

    public ManagerQuestDrops(ImusMiniQuests plugin, ManagerQuests quests)
    {
        _plugin = plugin;
        _quests = quests;
        reload();
    }

    public void reload()
    {
        FileConfiguration config = _plugin.getConfig();

        List<String> materials = config.getStringList("drops.block-break-materials");
        _blockMaterials = materials.isEmpty()
                ? null
                : TargetSet.parseMaterials(materials, _plugin.getLogger(), "config.yml drops.block-break-materials");

        List<Pattern> lootTables = new ArrayList<>();
        for (String table : config.getStringList("drops.chest-loot-tables"))
        {
            String regex = String.join(".*", List.of(table.toLowerCase(Locale.ROOT).split("\\*", -1)).stream()
                    .map(Pattern::quote).toList());
            lootTables.add(Pattern.compile(regex));
        }
        _chestLootTables = lootTables;
    }

    private boolean isEnabled()
    {
        return _plugin.getConfig().getBoolean("drops.enabled", true);
    }

    private static boolean rollChance(double chance)
    {
        return chance > 0 && ThreadLocalRandom.current().nextDouble() < chance;
    }

    /**
     * A new panel of a random quest, or null when the chance fails or no quest can be found.
     */
    private ItemStack roll(double chance)
    {
        if (!isEnabled() || !rollChance(chance)) return null;

        Quest quest = _quests.rollDrop();
        return quest == null ? null : QuestItem.create(quest);
    }

    private ItemStack rollAndTell(Player player, double chance)
    {
        ItemStack drop = roll(chance);
        if (drop != null)
            player.sendMessage(_plugin.getMessage("dropped").replace("%quest%", _quests.getQuest(drop).getName()));
        return drop;
    }

    /**
     * True when breaking this material can roll a quest drop.
     */
    public boolean isDropMaterial(Material material)
    {
        return _blockMaterials != null && _blockMaterials.matches(material);
    }

    /**
     * Rolls a drop for a mob the player killed. Returns the dropped panel, which the caller adds
     * to the mob's drops, or null.
     */
    public ItemStack rollMobDrop(Player killer, LivingEntity victim)
    {
        FileConfiguration config = _plugin.getConfig();
        if (config.getBoolean("drops.only-hostile-mobs", true) && !(victim instanceof Enemy)) return null;

        return rollAndTell(killer, config.getDouble("drops.mob-kill-chance", 0));
    }

    /**
     * Rolls a drop for a block the player broke and drops it at the block.
     */
    public void rollBlockDrop(Player player, Material material, Location location)
    {
        if (!isDropMaterial(material)) return;

        ItemStack drop = rollAndTell(player, _plugin.getConfig().getDouble("drops.block-break-chance", 0));
        if (drop != null) location.getWorld().dropItemNaturally(location.toCenterLocation(), drop);
    }

    /**
     * Rolls a panel for a chest that BetterStructures is filling and puts it in a random empty
     * slot, so it doesn't always sit in the first one.
     */
    public void rollBetterStructuresChest(Inventory inventory)
    {
        ItemStack panel = roll(_plugin.getConfig().getDouble("drops.betterstructures-chest-chance", 0));
        if (panel == null) return;

        List<Integer> emptySlots = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSize(); slot++)
        {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.isEmpty()) emptySlots.add(slot);
        }
        if (emptySlots.isEmpty()) return;

        inventory.setItem(emptySlots.get(ThreadLocalRandom.current().nextInt(emptySlots.size())), panel);
    }

    private boolean isChestLootTable(String key)
    {
        if (_chestLootTables.isEmpty()) return key.startsWith("minecraft:chests/");

        for (Pattern pattern : _chestLootTables)
        {
            if (pattern.matcher(key).matches()) return true;
        }
        return false;
    }

    /**
     * Vanilla loot chests (dungeons, villages, shipwrecks, minecart chests...) generate their loot
     * the first time they are opened or broken. The panel goes into the loot list, and the game
     * spreads that list over the chest like any other loot.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLootGenerate(LootGenerateEvent event)
    {
        if (event.getInventoryHolder() == null) return;
        if (!isChestLootTable(event.getLootTable().getKey().toString())) return;

        ItemStack panel = roll(_plugin.getConfig().getDouble("drops.chest-chance", 0));
        if (panel != null) event.getLoot().add(panel);
    }
}
