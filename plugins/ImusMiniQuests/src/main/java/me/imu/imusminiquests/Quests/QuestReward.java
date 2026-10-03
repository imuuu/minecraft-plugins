package me.imu.imusminiquests.Quests;

import imu.iAPI.LootTables.ImusLootTable;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.InvUtil;
import me.imu.imusminiquests.Hooks.ImusEnchantsHook;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * One reward entry. It gives one of these, plus optional console commands and a message:
 * <ul>
 *     <li>{@code item: DIAMOND} - a plain item, amount between min and max</li>
 *     <li>{@code tool: PICKAXE} - a tool or armor piece whose tier is rolled from {@code tiers}
 *         (wooden to diamond by default), optionally with ImusEnchants slots or vanilla enchants</li>
 *     <li>{@code enchant-book: random} - an ImusEnchants custom enchant book</li>
 *     <li>{@code slot-core: true} - ImusEnchants Slot Cores, amount between min and max</li>
 * </ul>
 * {@link #describe()} is the line shown in the reward list on the quest panel.
 */
public class QuestReward
{
    private static final Map<String, Integer> DEFAULT_TOOL_TIERS = new LinkedHashMap<>();
    private static final Map<String, Integer> DEFAULT_ARMOR_TIERS = new LinkedHashMap<>();
    private static final Set<String> ARMOR_PIECES = Set.of("HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS");

    static
    {
        DEFAULT_TOOL_TIERS.put("WOODEN", 40);
        DEFAULT_TOOL_TIERS.put("STONE", 30);
        DEFAULT_TOOL_TIERS.put("IRON", 20);
        DEFAULT_TOOL_TIERS.put("GOLDEN", 5);
        DEFAULT_TOOL_TIERS.put("DIAMOND", 5);

        DEFAULT_ARMOR_TIERS.put("LEATHER", 40);
        DEFAULT_ARMOR_TIERS.put("CHAINMAIL", 30);
        DEFAULT_ARMOR_TIERS.put("IRON", 20);
        DEFAULT_ARMOR_TIERS.put("GOLDEN", 5);
        DEFAULT_ARMOR_TIERS.put("DIAMOND", 5);
    }

    private final Supplier<ItemStack> _item;
    private final int _minAmount;
    private final int _maxAmount;
    private final List<String> _commands;
    private final String _message;
    private final String _description;
    private final int _weight;

    private QuestReward(Supplier<ItemStack> item, int minAmount, int maxAmount, List<String> commands, String message,
                        String description, int weight)
    {
        _item = item;
        _minAmount = minAmount;
        _maxAmount = maxAmount;
        _commands = List.copyOf(commands);
        _message = message;
        _description = description;
        _weight = weight;
    }

    public int weight() {return _weight;}

    /**
     * The reward line on the quest panel, or null when the entry shouldn't be listed (only
     * commands and no display text).
     */
    public String describe() {return _description;}

    /**
     * Reads one entry of rewards.pool or rewards.guaranteed. Returns null and logs a warning when
     * the entry gives nothing.
     */
    public static QuestReward fromConfig(ConfigurationSection section, Logger log, String context)
    {
        int min = Math.max(1, section.getInt("min", 1));
        int max = Math.max(min, section.getInt("max", min));
        String amountText = min == max ? (min == 1 ? "" : min + " x ") : min + "-" + max + " x ";

        Supplier<ItemStack> item = null;
        String description = null;

        if (section.isString("item") || section.isString("tool"))
        {
            Supplier<Material> material;
            if (section.isString("item"))
            {
                Material itemMaterial = Material.matchMaterial(section.getString("item"));
                if (itemMaterial == null || !itemMaterial.isItem())
                {
                    log.warning(context + ": unknown item " + section.getString("item"));
                    return null;
                }
                material = () -> itemMaterial;
                description = amountText + (section.isString("name") ? section.getString("name") : prettyName(itemMaterial.name()));
            }
            else
            {
                ImusLootTable tiers = new ImusLootTable();
                description = parseToolTiers(section, tiers, log, context);
                if (description == null) return null;
                material = () -> (Material) tiers.getLoot();
                if (section.isString("name")) description = section.getString("name");
            }

            boolean slots = section.getBoolean("slots", false);
            if (slots && !ImusEnchantsHook.isEnabled())
            {
                log.warning(context + ": slots needs ImusEnchants, the item is given without them");
                slots = false;
            }
            int enchantLevels = Math.max(0, section.getInt("enchant-levels", 0));
            item = gear(material, slots, enchantLevels, section);
            description += slots ? " &d(with slots)" : enchantLevels > 0 ? " &b(enchanted)" : "";
        }
        else if (section.contains("enchant-book"))
        {
            if (!ImusEnchantsHook.isEnabled())
            {
                log.warning(context + ": enchant-book needs ImusEnchants, skipped");
                return null;
            }

            String key = section.getString("enchant-book", "random");
            int level = Math.max(1, section.getInt("level", 1));
            boolean random = key.equalsIgnoreCase("random");
            String enchantName = random ? null : ImusEnchantsHook.getEnchantName(key);
            if (!random && enchantName == null)
            {
                log.warning(context + ": unknown ImusEnchants enchant " + key);
                return null;
            }

            item = () -> ImusEnchantsHook.createBook(key, level);
            description = amountText + "&dCustom Enchant Book" + (random ? "" : " &7(" + enchantName + ")");
        }
        else if (section.contains("slot-core"))
        {
            if (!ImusEnchantsHook.isEnabled())
            {
                log.warning(context + ": slot-core needs ImusEnchants, skipped");
                return null;
            }

            item = () -> ImusEnchantsHook.createSlotCore(1);
            description = amountText + "&dSlot Core";
        }

        List<String> commands = section.getStringList("commands");
        String message = section.getString("message");
        int weight = Math.max(1, section.getInt("weight", 1));
        if (section.isString("display")) description = section.getString("display");

        if (item == null && commands.isEmpty() && message == null)
        {
            log.warning(context + ": reward gives nothing, skipped");
            return null;
        }
        return new QuestReward(item, min, max, commands, message,
                description == null ? null : Metods.msgC("&f" + description), weight);
    }

    /**
     * A new item of the rolled material, with ImusEnchants slots or vanilla enchants when asked.
     */
    private static Supplier<ItemStack> gear(Supplier<Material> material, boolean slots, int enchantLevels, ConfigurationSection section)
    {
        return () ->
        {
            ItemStack stack = new ItemStack(material.get());
            if (slots) stack = ImusEnchantsHook.addSlots(stack);
            else if (enchantLevels > 0)
                stack = Bukkit.getItemFactory().enchantWithLevels(stack, enchantLevels, false, ThreadLocalRandom.current());
            applyNameAndLore(stack, section);
            return stack;
        };
    }

    /**
     * Fills the table with the tiers of tool: PICKAXE, tiers: {WOODEN: 40, ..., DIAMOND: 5} and
     * returns its name for the reward list, like "Wooden-Diamond Pickaxe". Null when no tier is
     * valid.
     */
    private static String parseToolTiers(ConfigurationSection section, ImusLootTable table, Logger log, String context)
    {
        String tool = section.getString("tool").toUpperCase(Locale.ROOT);
        boolean armor = ARMOR_PIECES.contains(tool);

        Map<String, Integer> tiers = new LinkedHashMap<>();
        ConfigurationSection tiersSection = section.getConfigurationSection("tiers");
        if (tiersSection != null)
        {
            for (String tier : tiersSection.getKeys(false))
                tiers.put(tier.toUpperCase(Locale.ROOT), tiersSection.getInt(tier));
        }
        else
        {
            tiers.putAll(armor ? DEFAULT_ARMOR_TIERS : DEFAULT_TOOL_TIERS);
        }

        List<Material> materials = new ArrayList<>();
        for (Map.Entry<String, Integer> tier : tiers.entrySet())
        {
            Material material = Material.matchMaterial(tier.getKey() + "_" + tool);
            if (material == null || !material.isItem())
            {
                log.warning(context + ": there is no " + tier.getKey() + "_" + tool);
                continue;
            }
            if (tier.getValue() <= 0) continue;

            table.add(material, tier.getValue());
            materials.add(material);
        }
        if (materials.isEmpty())
        {
            log.warning(context + ": tool " + tool + " has no valid tiers");
            return null;
        }

        // From the first and last tier as written in the config
        return materials.size() == 1
                ? prettyName(materials.getFirst().name())
                : tierName(materials.getFirst(), tool) + "-" + tierName(materials.getLast(), tool) + " " + prettyName(tool);
    }

    private static void applyNameAndLore(ItemStack stack, ConfigurationSection section)
    {
        if (!section.isString("name") && !section.isList("lore")) return;

        ItemMeta meta = stack.getItemMeta();
        if (section.isString("name")) meta.setDisplayName(Metods.msgC(section.getString("name")));
        if (section.isList("lore"))
        {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            section.getStringList("lore").forEach(line -> lore.add(Metods.msgC(line)));
            meta.setLore(lore);
        }
        stack.setItemMeta(meta);
    }

    /** DIAMOND_PICKAXE, PICKAXE -> Diamond */
    private static String tierName(Material material, String tool)
    {
        String name = material.name();
        return prettyName(name.substring(0, name.length() - tool.length() - 1));
    }

    /** GOLDEN_APPLE -> Golden Apple */
    static String prettyName(String enumName)
    {
        StringBuilder sb = new StringBuilder();
        for (String word : enumName.toLowerCase(Locale.ROOT).split("_"))
        {
            if (word.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }

    /**
     * Rolls the items this entry gives, without giving them. Empty for command-only entries.
     */
    public List<ItemStack> createItems()
    {
        List<ItemStack> items = new ArrayList<>();
        if (_item == null) return items;

        int amount = ThreadLocalRandom.current().nextInt(_minAmount, _maxAmount + 1);
        while (amount > 0)
        {
            ItemStack stack = _item.get();
            if (stack == null) break;

            stack.setAmount(Math.min(amount, stack.getMaxStackSize()));
            amount -= stack.getAmount();
            items.add(stack);
        }
        return items;
    }

    /** True when this entry runs commands, which a preview can't show as items. */
    public boolean hasCommands() {return !_commands.isEmpty();}

    public void give(Player player)
    {
        for (ItemStack stack : createItems())
        {
            InvUtil.AddItemToInventoryOrDrop(player, stack);
        }

        for (String command : _commands)
        {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
        }

        if (_message != null) player.sendMessage(Metods.msgC(_message));
    }
}
