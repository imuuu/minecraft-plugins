package me.imu.imusminiquests.Quests;

import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.InvUtil;
import me.imu.imusminiquests.Hooks.ImusEnchantsHook;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Managers.ManagerEconomy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
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
    // False while everything this entry can give is locked (see ManagerUnlocks)
    private final BooleanSupplier _available;

    private QuestReward(DoubleFunction<ItemStack> item, int minAmount, int maxAmount, List<String> commands, String message,
                        String description, boolean hasDisplay, MoneyRange money, int weight,
                        BooleanSupplier available)
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
        _available = available;
    }

    public int weight() {return _weight;}

    /**
     * False while every item this entry could give is locked, so rolls skip it. An entry that
     * pays money or runs commands is always available.
     */
    public boolean isAvailable() {return _available.getAsBoolean();}

    private static boolean locked(Material material)
    {
        return ImusMiniQuests.getInstance().getUnlocks().isLocked(material);
    }

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
     * Reads a list of reward entries, such as rewards.pool of a quest or a tier in rewards.yml.
     * Entries that can't be read are logged and left out.
     */
    public static List<QuestReward> listFromConfig(ConfigurationSection section, String path, Logger log, String context)
    {
        List<QuestReward> rewards = new ArrayList<>();
        List<Map<?, ?>> entries = section.getMapList(path);
        for (int i = 0; i < entries.size(); i++)
        {
            YamlConfiguration entry = new YamlConfiguration();
            for (Map.Entry<?, ?> e : entries.get(i).entrySet())
            {
                // A nested map (tiers: {IRON: 50, DIAMOND: 50}) has to become a section, or
                // getConfigurationSection doesn't see it
                if (e.getValue() instanceof Map<?, ?> map) entry.createSection(String.valueOf(e.getKey()), map);
                else entry.set(String.valueOf(e.getKey()), e.getValue());
            }

            QuestReward reward = fromConfig(entry, log, context + " " + path + "[" + i + "]");
            if (reward != null) rewards.add(reward);
        }
        return rewards;
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
        BooleanSupplier available = () -> true;
        String description = null;

        if (section.contains("item") || section.contains("tool"))
        {
            DoubleFunction<Material> material;
            if (section.contains("item"))
            {
                // item: DIAMOND, or item: [DIAMOND, EMERALD, GOLD_INGOT] for a random one of them
                List<Material> materials = new ArrayList<>();
                for (String name : stringOrList(section, "item"))
                {
                    Material itemMaterial = Material.matchMaterial(name);
                    if (itemMaterial == null || !itemMaterial.isItem()) log.warning(context + ": unknown item " + name);
                    else materials.add(itemMaterial);
                }
                if (materials.isEmpty()) return null;

                material = luck ->
                {
                    List<Material> open = materials.stream().filter(m -> !locked(m)).toList();
                    return open.isEmpty() ? null : open.get(ThreadLocalRandom.current().nextInt(open.size()));
                };
                available = () -> materials.stream().anyMatch(m -> !locked(m));
                description = amountText + (section.isString("name") ? section.getString("name") : describeChoice(materials));
            }
            else
            {
                // tool: PICKAXE, a list of them, or RANDOM (any tool), ARMOR (any piece) or GEAR (either)
                List<List<Tier>> toolTiers = new ArrayList<>();
                List<String> tools = expandTools(stringOrList(section, "tool"));
                for (String tool : tools)
                {
                    List<Tier> tiers = new ArrayList<>();
                    String toolDescription = parseToolTiers(section, tool, tiers, log, context);
                    if (toolDescription == null) continue;
                    toolTiers.add(tiers);
                    if (description == null) description = toolDescription;
                }
                if (toolTiers.isEmpty()) return null;

                // Only tool types and tiers that aren't locked, e.g. no netherite before it is unlocked
                material = luck ->
                {
                    List<List<Tier>> open = toolTiers.stream()
                            .map(tiers -> tiers.stream().filter(t -> !locked(t.material())).toList())
                            .filter(tiers -> !tiers.isEmpty())
                            .toList();
                    if (open.isEmpty()) return null;
                    return Luck.pick(open.get(ThreadLocalRandom.current().nextInt(open.size())), Tier::weight, luck).material();
                };
                available = () -> toolTiers.stream().flatMap(List::stream).anyMatch(t -> !locked(t.material()));
                if (toolTiers.size() > 1) description = describeToolChoice(description, tools);
                if (section.isString("name")) description = section.getString("name");
            }

            boolean slots = section.getBoolean("slots", false);
            if (slots && !ImusEnchantsHook.isEnabled())
            {
                log.warning(context + ": slots needs ImusEnchants, the item is given without them");
                slots = false;
            }
            int enchantLevels = Math.max(0, section.getInt("enchant-levels", 0));
            EnchantSet enchants = EnchantSet.fromConfig(section, log, context);
            if (enchants != null && slots)
            {
                // Giving an item slots wipes its enchants, so the two can't go together
                log.warning(context + ": an item with enchants can't also have slots, slots ignored");
                slots = false;
            }
            item = gear(material, slots, enchantLevels, enchants, section);
            description += slots ? " &d(with slots)"
                    : enchants != null ? " &b(" + enchants.describe() + ")"
                    : enchantLevels > 0 ? " &b(enchanted)" : "";
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
                description == null ? null : Metods.msgC("&f" + description), hasDisplay, money, weight,
                money != null || !commands.isEmpty() ? () -> true : available);
    }

    /**
     * A new item of the rolled material, with ImusEnchants slots, exact enchants or random
     * enchanting-table enchants when asked.
     */
    private static DoubleFunction<ItemStack> gear(DoubleFunction<Material> material, boolean slots, int enchantLevels,
                                                  EnchantSet enchants, ConfigurationSection section)
    {
        return luck ->
        {
            Material rolled = material.apply(luck);
            if (rolled == null) return null;
            ItemStack stack = new ItemStack(rolled);
            if (slots) stack = ImusEnchantsHook.addSlots(stack);
            else
            {
                if (enchantLevels > 0)
                    stack = Bukkit.getItemFactory().enchantWithLevels(stack, enchantLevels, false, ThreadLocalRandom.current());
                if (enchants != null) stack = enchants.apply(stack, luck);
                // The enchanting table roll doesn't know which enchants the server switched off
                if (ImusEnchantsHook.isEnabled()) ImusEnchantsHook.stripDisabledEnchants(stack);
            }
            applyNameAndLore(stack, section);
            return stack;
        };
    }

    private static final List<String> ALL_TOOLS = List.of("PICKAXE", "AXE", "SHOVEL", "HOE", "SWORD");
    private static final List<String> ALL_ARMOR = List.of("HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS");

    /** A key that can be one value or a list of them. */
    private static List<String> stringOrList(ConfigurationSection section, String key)
    {
        return section.isList(key) ? section.getStringList(key) : List.of(section.getString(key, ""));
    }

    /** RANDOM, ARMOR and GEAR become the tool types they stand for. */
    private static List<String> expandTools(List<String> names)
    {
        List<String> tools = new ArrayList<>();
        for (String name : names)
        {
            switch (name.toUpperCase(Locale.ROOT))
            {
                case "RANDOM" -> tools.addAll(ALL_TOOLS);
                case "ARMOR" -> tools.addAll(ALL_ARMOR);
                case "GEAR" ->
                {
                    tools.addAll(ALL_TOOLS);
                    tools.addAll(ALL_ARMOR);
                }
                default -> tools.add(name.toUpperCase(Locale.ROOT));
            }
        }
        return tools;
    }

    /** "Diamond / Emerald / Gold Ingot", or "Diamond / Emerald / ..." for longer lists. */
    private static String describeChoice(List<Material> materials)
    {
        List<String> names = materials.stream().limit(3).map(m -> prettyName(m.name())).toList();
        return String.join(" / ", names) + (materials.size() > 3 ? " / ..." : "");
    }

    /**
     * "Wooden-Diamond Pickaxe" of the first tool becomes "Wooden-Diamond Tool", "... Armor" or
     * "... Gear" when several tool types can come out.
     */
    private static String describeToolChoice(String firstDescription, List<String> tools)
    {
        boolean anyArmor = tools.stream().anyMatch(ARMOR_PIECES::contains);
        boolean anyTool = tools.stream().anyMatch(t -> !ARMOR_PIECES.contains(t));
        String kind = anyArmor && anyTool ? "Gear" : anyArmor ? "Armor" : "Tool";
        int lastSpace = firstDescription.lastIndexOf(' ');
        String tierRange = lastSpace > 0 ? firstDescription.substring(0, lastSpace) : firstDescription;
        return tierRange + " " + kind;
    }

    /** One tier of a tool reward and how likely it is. */
    private record Tier(Material material, int weight) {}

    /**
     * Fills the table with the tiers of tool: PICKAXE, tiers: {WOODEN: 40, ..., DIAMOND: 5} and
     * returns its name for the reward list, like "Wooden-Diamond Pickaxe". Null when no tier is
     * valid.
     */
    private static String parseToolTiers(ConfigurationSection section, String tool, List<Tier> table, Logger log, String context)
    {
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

    /**
     * Gives the entry and returns what was given, like "3 x Diamond, $1,200", for messages.
     * Empty when nothing visible was given (only commands without a display text).
     */
    public String give(Player player, double luck)
    {
        List<String> given = new ArrayList<>();
        Map<String, Integer> counted = new LinkedHashMap<>();
        for (ItemStack stack : createItems(luck))
        {
            InvUtil.AddItemToInventoryOrDrop(player, stack);
            counted.merge(itemName(stack), stack.getAmount(), Integer::sum);
        }
        counted.forEach((name, amount) -> given.add(amount > 1 ? amount + " x " + name : name));

        double money = rollMoney(luck);
        if (money > 0 && ManagerEconomy.isAvailable())
        {
            ManagerEconomy.deposit(player, money);
            player.sendMessage(ImusMiniQuests.getInstance().getMessage("money")
                    .replace("%money%", ManagerEconomy.format(money)));
            given.add(Metods.msgC("&6" + ManagerEconomy.format(money)));
        }

        for (String command : _commands)
        {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
        }
        if (given.isEmpty() && _hasDisplay) given.add(_description);

        if (_message != null) player.sendMessage(Metods.msgC(_message));
        return String.join(Metods.msgC("&7, &f"), given);
    }

    /** The item's custom name, or its material written out: DIAMOND_PICKAXE -> Diamond Pickaxe */
    private static String itemName(ItemStack stack)
    {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null && meta.hasDisplayName()) return meta.getDisplayName();
        return Metods.msgC("&f") + prettyName(stack.getType().name());
    }
}
