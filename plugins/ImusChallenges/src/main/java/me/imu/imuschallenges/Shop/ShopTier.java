package me.imu.imuschallenges.Shop;

import imu.iAPI.LootTables.LootTableItemStack;
import me.imu.imuschallenges.Database.Tables.TablePlayerShopStats;
import me.imu.imuschallenges.Factories.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * One rotating section of the challenge shop (normal or special): its settings from config.yml,
 * the items and prices of the current rotation, and who has already bought from it.
 */
public class ShopTier
{
    public static final String GADGED_MENU = "GadgedMenu";

    // Placeholder stacks in the loot table, swapped for a real item when rolled
    private static final String TAG_RANDOM = "RANDOM";
    private static final String TAG_MYSTERY_BOX = "MYSTERY_BOX";
    private static final String TAG_MYSTERY_DUST = "MYSTERY_DUST";

    private final String _name;
    private final boolean _special;
    private final int _slotCount;

    // Settings from config.yml, replaced by applyConfig on a reload
    private long _refreshMillis;
    private int _defaultSlots;
    private int _itemCostMin;
    private int _itemCostMax;
    private double _slotPriceFirst;
    private double _slotPriceMultiplier;
    private LootTableItemStack _lootTable = new LootTableItemStack();
    private final Map<Material, String> _placeholders = new HashMap<>();

    private final List<ItemStack> _items = new ArrayList<>();
    private final List<Integer> _costs = new ArrayList<>();
    private final Set<UUID> _buyers = new HashSet<>();
    private long _nextRefresh = 0;
    private int _generation = 0;

    public ShopTier(String name, boolean special, int slotCount, ConfigurationSection config)
    {
        _name = name;
        _special = special;
        _slotCount = slotCount;
        applyConfig(config);
    }

    /**
     * Takes new settings. Prices and loot apply from the next rotation; a shorter refresh time also cuts the
     * current rotation short, a longer one waits for the next.
     */
    public void applyConfig(ConfigurationSection config)
    {
        _refreshMillis = Math.max(1, config.getLong("refresh-minutes")) * 60_000L;
        _defaultSlots = config.getInt("default-slots");
        _itemCostMin = config.getInt("item-cost-min");
        _itemCostMax = Math.max(_itemCostMin, config.getInt("item-cost-max"));
        _slotPriceFirst = config.getDouble("slot-price-first");
        _slotPriceMultiplier = config.getDouble("slot-price-multiplier");
        loadLoot(config.getMapList("loot"));

        if (_generation > 0)
            _nextRefresh = Math.min(_nextRefresh, System.currentTimeMillis() + _refreshMillis);
    }

    private void loadLoot(List<Map<?, ?>> entries)
    {
        _lootTable = new LootTableItemStack();
        _placeholders.clear();
        // Placeholders need a unique material each so getLoot()'s clone can be mapped back
        Map<String, Material> placeholderMaterials = Map.of(
                TAG_RANDOM, Material.BARRIER,
                TAG_MYSTERY_BOX, Material.STRUCTURE_VOID,
                TAG_MYSTERY_DUST, Material.JIGSAW);
        for (Map<?, ?> entry : entries)
        {
            String itemName = String.valueOf(entry.get("item")).toUpperCase(Locale.ROOT);
            int weight = entry.get("weight") instanceof Number n ? n.intValue() : 1;
            int max = entry.get("max") instanceof Number n ? n.intValue() : 1;

            Material material = placeholderMaterials.get(itemName);
            if (material != null)
            {
                _placeholders.put(material, itemName);
            }
            else
            {
                material = Material.matchMaterial(itemName);
                if (material == null || !material.isItem() || placeholderMaterials.containsValue(material))
                {
                    Bukkit.getLogger().warning("[ImusChallenges] Unknown shop loot item '" + itemName + "' in " + _name + ", skipped");
                    continue;
                }
            }
            _lootTable.add(new ItemStack(material), weight, max);
        }
    }

    // Rotation ======================================================================================================

    public boolean isRefreshDue()
    {
        return System.currentTimeMillis() >= _nextRefresh;
    }

    public long getMillisUntilRefresh()
    {
        return Math.max(0, _nextRefresh - System.currentTimeMillis());
    }

    /**
     * Rolls a new set of items and prices and forgets who bought from the previous rotation.
     */
    public void generate(Supplier<ItemStack> randomItem)
    {
        _items.clear();
        _costs.clear();
        _buyers.clear();
        _generation++;
        _nextRefresh = System.currentTimeMillis() + _refreshMillis;

        boolean mysteryAllowed = Bukkit.getPluginManager().isPluginEnabled(GADGED_MENU);
        Set<String> usedKeys = new HashSet<>();
        int attemptsLeft = _slotCount * 50;
        while (_items.size() < _slotCount && attemptsLeft-- > 0)
        {
            ItemStack item = _lootTable.getLoot();
            if (item == null)
                continue;

            String placeholder = _placeholders.get(item.getType());
            if (placeholder != null)
            {
                if (!placeholder.equals(TAG_RANDOM) && !mysteryAllowed)
                    continue;

                int amount = item.getAmount();
                item = switch (placeholder)
                {
                    case TAG_MYSTERY_BOX -> ItemFactory.createMysteryBox();
                    case TAG_MYSTERY_DUST -> ItemFactory.createMysteryDust();
                    default -> randomItem.get();
                };
                if (!placeholder.equals(TAG_RANDOM))
                    item.setAmount(Math.max(1, Math.min(amount, item.getMaxStackSize())));
            }

            if (!usedKeys.add(itemKey(item)))
                continue;

            _items.add(item);
            _costs.add(ThreadLocalRandom.current().nextInt(_itemCostMin, _itemCostMax + 1));
        }
    }

    private static String itemKey(ItemStack item)
    {
        if (ItemFactory.isMysteryBox(item))
            return TAG_MYSTERY_BOX;
        if (ItemFactory.isMysteryDust(item))
            return TAG_MYSTERY_DUST;
        return item.getType().name();
    }

    // Purchases =====================================================================================================

    public boolean hasBought(UUID uuid)
    {
        return _buyers.contains(uuid);
    }

    public void setBought(UUID uuid, boolean value)
    {
        if (value)
            _buyers.add(uuid);
        else
            _buyers.remove(uuid);
    }

    public int getBoughtSlots(TablePlayerShopStats stats)
    {
        return _special ? stats.getBought_special_slots() : stats.getBought_normal_slots();
    }

    public void addBoughtSlots(TablePlayerShopStats stats, int amount)
    {
        if (_special)
            stats.setBought_special_slots(stats.getBought_special_slots() + amount);
        else
            stats.setBought_normal_slots(stats.getBought_normal_slots() + amount);
    }

    /**
     * @param slotNumber how many slots the player has bought on top of the default ones
     */
    public int getSlotPrice(int slotNumber)
    {
        return (int) (_slotPriceFirst * Math.pow(_slotPriceMultiplier, slotNumber));
    }

    // Persistence ===================================================================================================

    public void save(ConfigurationSection section)
    {
        section.set("generation", _generation);
        section.set("next-refresh", _nextRefresh);
        section.set("items", _items.stream().map(item -> Base64.getEncoder().encodeToString(item.serializeAsBytes())).toList());
        section.set("costs", new ArrayList<>(_costs));
        section.set("buyers", _buyers.stream().map(UUID::toString).toList());
    }

    public void load(ConfigurationSection section)
    {
        if (section == null)
            return;

        List<String> items = section.getStringList("items");
        List<Integer> costs = section.getIntegerList("costs");
        if (items.size() != costs.size())
            return;

        try
        {
            List<ItemStack> loadedItems = new ArrayList<>();
            for (String item : items)
                loadedItems.add(ItemStack.deserializeBytes(Base64.getDecoder().decode(item)));

            _items.clear();
            _items.addAll(loadedItems);
            _costs.clear();
            _costs.addAll(costs);
            _buyers.clear();
            for (String uuid : section.getStringList("buyers"))
                _buyers.add(UUID.fromString(uuid));
            _generation = section.getInt("generation");
            _nextRefresh = section.getLong("next-refresh");
        }
        catch (IllegalArgumentException e)
        {
            Bukkit.getLogger().warning("[ImusChallenges] Could not load saved " + _name + " shop, it will be regenerated: " + e.getMessage());
        }
    }

    // Getters =======================================================================================================

    public String getName()
    {
        return _name;
    }

    public boolean isSpecial()
    {
        return _special;
    }

    public int getSlotCount()
    {
        return _slotCount;
    }

    public int getDefaultSlots()
    {
        return _defaultSlots;
    }

    public int getGeneration()
    {
        return _generation;
    }

    public List<ItemStack> getItems()
    {
        return _items;
    }

    public List<Integer> getCosts()
    {
        return _costs;
    }
}
