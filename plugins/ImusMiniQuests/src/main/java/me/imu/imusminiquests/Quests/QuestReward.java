package me.imu.imusminiquests.Quests;

import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.InvUtil;
import me.imu.imusminiquests.Hooks.ImusEnchantsHook;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Managers.ManagerEconomy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleFunction;
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

    // Luck in, item out: tool tiers and other rolls lean on the player's luck
    private final DoubleFunction<ItemStack> _item;
    private final int _minAmount;
    private final int _maxAmount;
    private final List<String> _commands;
    private final String _message;
    private final String _description;
    private final boolean _hasDisplay;
    private final MoneyRange _money;
    private final int _weight;

    private QuestReward(DoubleFunction<ItemStack> item, int minAmount, int maxAmount, List<String> commands, String message,
                        String description, boolean hasDisplay, MoneyRange money, int weight)
    {
        _item = item;
        _minAmount = minAmount;
        _maxAmount = maxAmount;
        _commands = List.copyOf(commands);
        _message = message;
        _description = description;
        _hasDisplay = hasDisplay;
        _money = money;
        _weight = weight;
    }

    public int weight() {return _weight;}

    /**
     * The reward line on the quest panel, or null when the entry shouldn't be listed (only
     * commands and no display text). Money is worked out each time, since percent rewards follow
     * the economy.
     */
    public String describe()
    {
        if (_money == null || _hasDisplay) return _description;

        String money = Metods.msgC("&6" + _money.describe());
        return _description == null ? money : _description + Metods.msgC(" &7+ ") + money;
    }

    /**
     * money: 3-10%  (a share of the server's wealth, see ManagerEconomy)
     * money: 200-500 or money: 500  (a fixed amount)
     */
    record MoneyRange(double min, double max, boolean percent)
    {
        static MoneyRange parse(String text, Logger log, String context)
        {
            String value = text.trim().replace(" ", "");
            boolean percent = value.endsWith("%");
            if (percent) value = value.substring(0, value.length() - 1);

            try
            {
                String[] parts = value.split("-", 2);
                double min = Double.parseDouble(parts[0]);
                double max = parts.length > 1 ? Double.parseDouble(parts[1]) : min;
                if (min < 0 || max < min) throw new NumberFormatException();
                return new MoneyRange(min, max, percent);
            }
            catch (NumberFormatException e)
            {
                log.warning(context + ": money must look like 500, 200-500 or 3-10%, not " + text);
                return null;
            }
        }

        private static ManagerEconomy economy() {return ImusMiniQuests.getInstance().getEconomy();}

        double roll(double luck)
        {
            double value = Luck.between(min, max, luck);
            return economy().clamp(percent ? economy().getBasisAmount() * value / 100 : value);
        }

        String describe()
        {
            ManagerEconomy economy = economy();
            if (!percent)
                return min == max ? ManagerEconomy.format(min) : ManagerEconomy.format(min) + "-" + ManagerEconomy.format(max);

            String range = (min == max ? fmt(min) : fmt(min) + "-" + fmt(max)) + "%";
            String of = switch (economy.getBasis())
            {
                case TOTAL -> "of all players' money";
                case AVERAGE -> "of the average balance";
                case MEDIAN -> "of the median balance";
            };
            if (economy.getSurveyedAt() == 0) return range + " " + of;

            double low = economy.clamp(economy.getBasisAmount() * min / 100);
            double high = economy.clamp(economy.getBasisAmount() * max / 100);
            return ManagerEconomy.format(low) + "-" + ManagerEconomy.format(high) + " &8(" + range + " " + of + ")";
        }

        private static String fmt(double value)
        {
            return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
        }
    }

    /**
     * Reads one entry of rewards.pool or rewards.guaranteed. Returns null and logs a warning when
     * the entry gives nothing.
     */
    public static QuestReward fromConfig(ConfigurationSection section, Logger log, String context)
    {
        int min = Math.max(1, section.getInt("min", 1));
        int max = Math.max(min, section.getInt("max", min));
        String amountText = min == max ? (min == 1 ? "" : min + " x ") : min + "-" + max + " x ";

        DoubleFunction<ItemStack> item = null;
        String description = null;

        if (section.isString("item") || section.isString("tool"))
        {
            DoubleFunction<Material> material;
            if (section.isString("item"))
            {
                Material itemMaterial = Material.matchMaterial(section.getString("item"));
                if (itemMaterial == null || !itemMaterial.isItem())
                {
                    log.warning(context + ": unknown item " + section.getString("item"));
                    return null;
                }
                material = luck -> itemMaterial;
                description = amountText + (section.isString("name") ? section.getString("name") : prettyName(itemMaterial.name()));
            }
            else
            {
                List<Tier> tiers = new ArrayList<>();
                description = parseToolTiers(section, tiers, log, context);
                if (description == null) return null;
                material = luck -> Luck.pick(tiers, Tier::weight, luck).material();
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

            item = luck -> ImusEnchantsHook.createBook(key, level);
            description = amountText + "&dCustom Enchant Book" + (random ? "" : " &7(" + enchantName + ")");
        }
        else if (section.contains("slot-core"))
        {
            if (!ImusEnchantsHook.isEnabled())
            {
                log.warning(context + ": slot-core needs ImusEnchants, skipped");
                return null;
            }

            item = luck -> ImusEnchantsHook.createSlotCore(1);
            description = amountText + "&dSlot Core";
        }

        MoneyRange money = null;
        if (section.contains("money"))
        {
            money = MoneyRange.parse(String.valueOf(section.get("money")), log, context);
            if (money == null) return null;
            if (!ManagerEconomy.isAvailable())
                log.warning(context + ": money needs Vault and an economy plugin, nothing is paid without one");
        }

        List<String> commands = section.getStringList("commands");
        String message = section.getString("message");
        int weight = Math.max(1, section.getInt("weight", 1));
        boolean hasDisplay = section.isString("display");
        if (hasDisplay) description = section.getString("display");

        if (item == null && money == null && commands.isEmpty() && message == null)
        {
            log.warning(context + ": reward gives nothing, skipped");
            return null;
        }
        return new QuestReward(item, min, max, commands, message,
                description == null ? null : Metods.msgC("&f" + description), hasDisplay, money, weight);
    }

    /**
     * A new item of the rolled material, with ImusEnchants slots or vanilla enchants when asked.
     */
    private static DoubleFunction<ItemStack> gear(DoubleFunction<Material> material, boolean slots, int enchantLevels, ConfigurationSection section)
    {
        return luck ->
        {
            ItemStack stack = new ItemStack(material.apply(luck));
            if (slots) stack = ImusEnchantsHook.addSlots(stack);
            else if (enchantLevels > 0)
                stack = Bukkit.getItemFactory().enchantWithLevels(stack, enchantLevels, false, ThreadLocalRandom.current());
            applyNameAndLore(stack, section);
            return stack;
        };
    }

    /** One tier of a tool reward and how likely it is. */
    private record Tier(Material material, int weight) {}

    /**
     * Fills the table with the tiers of tool: PICKAXE, tiers: {WOODEN: 40, ..., DIAMOND: 5} and
     * returns its name for the reward list, like "Wooden-Diamond Pickaxe". Null when no tier is
     * valid.
     */
    private static String parseToolTiers(ConfigurationSection section, List<Tier> table, Logger log, String context)
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

            table.add(new Tier(material, tier.getValue()));
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
     * Luck leans the amount towards max and tool tiers towards the rare ones.
     */
    public List<ItemStack> createItems(double luck)
    {
        List<ItemStack> items = new ArrayList<>();
        if (_item == null) return items;

        int amount = Luck.between(_minAmount, _maxAmount, luck);
        while (amount > 0)
        {
            ItemStack stack = _item.apply(luck);
            if (stack == null) break;

            stack.setAmount(Math.min(amount, stack.getMaxStackSize()));
            amount -= stack.getAmount();
            items.add(stack);
        }
        return items;
    }

    /** True when this entry runs commands, which a preview can't show as items. */
    public boolean hasCommands() {return !_commands.isEmpty();}

    /** Rolls the money this entry pays, 0 when it pays none. Luck leans it towards the top. */
    public double rollMoney(double luck)
    {
        return _money == null ? 0 : _money.roll(luck);
    }

    public void give(Player player, double luck)
    {
        for (ItemStack stack : createItems(luck))
        {
            InvUtil.AddItemToInventoryOrDrop(player, stack);
        }

        double money = rollMoney(luck);
        if (money > 0 && ManagerEconomy.isAvailable())
        {
            ManagerEconomy.deposit(player, money);
            player.sendMessage(ImusMiniQuests.getInstance().getMessage("money")
                    .replace("%money%", ManagerEconomy.format(money)));
        }

        for (String command : _commands)
        {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
        }

        if (_message != null) player.sendMessage(Metods.msgC(_message));
    }
}
