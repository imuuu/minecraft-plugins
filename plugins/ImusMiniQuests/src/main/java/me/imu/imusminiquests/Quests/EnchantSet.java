package me.imu.imusminiquests.Quests;

import me.imu.imusminiquests.Hooks.ImusEnchantsHook;
import me.imu.imusminiquests.ImusMiniQuests;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.logging.Logger;

/**
 * Exact enchants for an item reward:
 * <pre>
 * enchants: [FORTUNE:3, EFFICIENCY:4, UNBREAKING:3]   every one of them
 * enchant-count: 2                                    or only this many, picked at random
 * enchant-count: 1-3                                  (a range leans to the top with luck)
 * unsafe: true                                        also enchants that don't fit the item
 * </pre>
 * Levels can go past the vanilla maximum (EFFICIENCY:6). Without unsafe, enchants that can't go
 * on the item or clash with one already on it are skipped, so a list can be shared by a pickaxe
 * and a sword: the sword gets unbreaking but not fortune. Books (BOOK or ENCHANTED_BOOK) store
 * the enchants instead and take any of them. Enchants switched off in ImusEnchants are never
 * given, and neither are enchants that are still locked (ManagerUnlocks).
 */
public final class EnchantSet
{
    private record Entry(Enchantment enchantment, int level) {}

    private final List<Entry> _entries;
    private final int _minCount;
    private final int _maxCount;
    private final boolean _unsafe;

    private EnchantSet(List<Entry> entries, int minCount, int maxCount, boolean unsafe)
    {
        _entries = List.copyOf(entries);
        _minCount = minCount;
        _maxCount = maxCount;
        _unsafe = unsafe;
    }

    /**
     * Reads enchants / enchant-count / unsafe from a reward entry, or null when it has none.
     */
    public static EnchantSet fromConfig(ConfigurationSection section, Logger log, String context)
    {
        if (!section.isList("enchants")) return null;

        List<Entry> entries = new ArrayList<>();
        for (String raw : section.getStringList("enchants"))
        {
            String[] parts = raw.trim().split(":", 2);
            NamespacedKey key = NamespacedKey.fromString(parts[0].toLowerCase(Locale.ROOT));
            Enchantment enchantment = key == null ? null : Registry.ENCHANTMENT.get(key);
            if (enchantment == null)
            {
                log.warning(context + ": unknown enchant " + parts[0]);
                continue;
            }

            int level = 1;
            if (parts.length > 1)
            {
                try
                {
                    level = Math.max(1, Integer.parseInt(parts[1].trim()));
                }
                catch (NumberFormatException e)
                {
                    log.warning(context + ": " + raw + " needs a number after the colon, using level 1");
                }
            }
            entries.add(new Entry(enchantment, level));
        }
        if (entries.isEmpty()) return null;

        int min = entries.size();
        int max = entries.size();
        if (section.contains("enchant-count"))
        {
            String count = String.valueOf(section.get("enchant-count")).replace(" ", "");
            try
            {
                String[] range = count.split("-", 2);
                min = Integer.parseInt(range[0]);
                max = range.length > 1 ? Integer.parseInt(range[1]) : min;
            }
            catch (NumberFormatException e)
            {
                log.warning(context + ": enchant-count must look like 2 or 1-3, not " + count);
            }
            min = Math.max(0, Math.min(min, entries.size()));
            max = Math.max(min, Math.min(max, entries.size()));
        }

        return new EnchantSet(entries, min, max, section.getBoolean("unsafe", false));
    }

    /**
     * Adds the enchants to the item. A BOOK becomes an ENCHANTED_BOOK holding them.
     */
    public ItemStack apply(ItemStack stack, double luck)
    {
        boolean book = stack.getType() == Material.BOOK || stack.getType() == Material.ENCHANTED_BOOK;
        if (stack.getType() == Material.BOOK) stack = stack.withType(Material.ENCHANTED_BOOK);

        List<Entry> usable = new ArrayList<>();
        for (Entry entry : _entries)
        {
            if (ImusEnchantsHook.isEnabled() && !ImusEnchantsHook.isVanillaEnchantEnabled(entry.enchantment())) continue;
            // e.g. mending before anyone on the server has found it
            if (ImusMiniQuests.getInstance().getUnlocks().isLocked(entry.enchantment())) continue;
            if (!book && !_unsafe && !entry.enchantment().canEnchantItem(stack)) continue;
            usable.add(entry);
        }
        Collections.shuffle(usable);

        // Every enchant in the list, or a random handful of the ones that fit
        int count = _minCount == _entries.size() && _maxCount == _entries.size()
                ? usable.size()
                : Luck.between(_minCount, _maxCount, luck);

        ItemMeta meta = stack.getItemMeta();
        List<Enchantment> added = new ArrayList<>();
        for (Entry entry : usable)
        {
            if (added.size() >= count) break;
            if (!_unsafe && added.stream().anyMatch(other -> other.conflictsWith(entry.enchantment()))) continue;

            if (meta instanceof EnchantmentStorageMeta storage)
                storage.addStoredEnchant(entry.enchantment(), entry.level(), true);
            else
                meta.addEnchant(entry.enchantment(), entry.level(), true);
            added.add(entry.enchantment());
        }
        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * For the reward list: "Fortune III, Efficiency IV", or "2 of: Fortune III, Efficiency V, ..."
     */
    public String describe()
    {
        List<String> names = _entries.stream().map(e -> name(e.enchantment()) + " " + roman(e.level())).toList();
        String list = String.join(", ", names);
        if (_minCount == _entries.size() && _maxCount == _entries.size()) return list;

        String count = _minCount == _maxCount ? String.valueOf(_minCount) : _minCount + "-" + _maxCount;
        return count + " of: " + list;
    }

    private static String name(Enchantment enchantment)
    {
        return QuestReward.prettyName(enchantment.getKey().getKey());
    }

    private static String roman(int level)
    {
        String[] numerals = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return level < numerals.length ? numerals[level] : String.valueOf(level);
    }
}
