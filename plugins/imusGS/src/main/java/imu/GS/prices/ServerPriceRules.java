package imu.GS.prices;

import imu.GS.bukkit.FlashItems;
import imu.GS.bukkit.ItemKeys;
import imu.GS.engine.ItemKey;
import imu.GS.engine.pricing.Overflow;
import imu.GS.engine.pricing.PriceRules;
import imu.GS.engine.pricing.SellLimit;
import imu.iAPI.CustomEnchants.CustomEnchantProvider;
import imu.iAPI.CustomEnchants.CustomEnchants;
import imu.iAPI.CustomItems.CustomItemProvider;
import imu.iAPI.CustomItems.CustomItems;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;

/**
 * The engine's prices from the server's material prices, enchant values and unique items.
 */
public class ServerPriceRules implements PriceRules
{
    private final MaterialPrices _materials;
    private final EnchantPrices _enchants;
    private final UniqueItems _uniques;
    private final DoubleSupplier _durabilityPenalty;
    // pricing.custom-enchant-tiers / custom-item-tiers of config.yml
    private java.util.function.Function<String, List<Double>> _tierFactors = setting -> null;
    private final ItemKey _bucket;

    /**
     * @param durabilityPenalty how much less than its durability a damaged item is worth, from config.yml
     */
    public ServerPriceRules(MaterialPrices materials, EnchantPrices enchants, UniqueItems uniques, DoubleSupplier durabilityPenalty)
    {
        _materials = materials;
        _enchants = enchants;
        _uniques = uniques;
        _durabilityPenalty = durabilityPenalty;
        _bucket = ItemKeys.of(new ItemStack(Material.BUCKET));
    }

    public void setTierFactors(java.util.function.Function<String, List<Double>> tierFactors)
    {
        _tierFactors = tierFactors;
    }

    public MaterialPrices getMaterials()
    {
        return _materials;
    }

    public EnchantPrices getEnchants()
    {
        return _enchants;
    }

    public UniqueItems getUniques()
    {
        return _uniques;
    }

    /**
     * Value of one unit of the stack, see {@link #value(ItemKey)}.
     */
    public double value(ItemStack stack)
    {
        return value(stack, CustomItems.idOf(stack), CustomEnchants.read(stack));
    }

    private double value(ItemStack stack, String customItem, Map<String, Integer> customEnchants)
    {
        double base = customItem != null ? customItemPrice(customItem, stack.getType()) : _materials.price(stack.getType());
        double value = base + _enchants.price(stack);
        for (Map.Entry<String, Integer> enchant : customEnchants.entrySet())
            value += customEnchantPrice(enchant.getKey()) * enchant.getValue();
        return Math.max(0, value) * condition(stack);
    }

    @Override
    public double value(ItemKey item)
    {
        ItemStack stack = ItemKeys.template(item);
        CustomParts parts = _customParts.computeIfAbsent(item, key -> new CustomParts(CustomItems.idOf(stack), CustomEnchants.read(stack)));
        return value(stack, parts.item(), parts.enchants());
    }

    // What other plugins made of an item never changes, so it is looked up once per item
    private record CustomParts(String item, Map<String, Integer> enchants) {}

    private final Map<ItemKey, CustomParts> _customParts = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Forgets which items are custom, e.g. after plugins were reloaded.
     */
    public void clearCustomCache()
    {
        _customParts.clear();
        _tiers.clear();
    }

    // "enchant:<key>" / "item:<id>" -> tier, 0 when no plugin knows it
    private final Map<String, Integer> _tiers = new java.util.concurrent.ConcurrentHashMap<>();

    private double diamond()
    {
        double diamond = _materials.price(Material.DIAMOND);
        return diamond > 0 ? diamond : 1000;
    }

    /**
     * What a custom enchant adds per level: the admin's price, or its tier times the diamond price.
     */
    public double customEnchantPrice(String key)
    {
        Double set = _enchants.getCustomPrice(EnchantPrices.customEnchantName(key));
        if (set != null)
            return set;
        int tier = _tiers.computeIfAbsent(EnchantPrices.customEnchantName(key), name ->
        {
            CustomEnchantProvider.Info info = CustomEnchants.info(key);
            return info == null ? 0 : info.tier();
        });
        return tier == 0 ? 0 : tierFactor("custom-enchant-tiers", tier, List.of(0.3, 0.8, 2.0)) * diamond();
    }

    /**
     * One custom item: the admin's price, or its tier times the diamond price.
     */
    public double customItemPrice(String id, Material material)
    {
        Double set = _enchants.getCustomPrice(EnchantPrices.customItemName(id));
        if (set != null)
            return set;
        int tier = _tiers.computeIfAbsent(EnchantPrices.customItemName(id), name ->
        {
            CustomItemProvider.CustomItem item = CustomItems.item(id);
            return item == null ? 0 : item.tier();
        });
        if (tier == 0)
            return _materials.price(material);
        return tierFactor("custom-item-tiers", tier, List.of(0.5, 1.5, 4.0)) * diamond();
    }

    private double tierFactor(String setting, int tier, List<Double> defaults)
    {
        List<Double> factors = _tierFactors.apply(setting);
        if (factors == null || factors.size() < 3)
            factors = defaults;
        return factors.get(Math.max(1, Math.min(3, tier)) - 1);
    }

    @Override
    public Overflow overflow(ItemKey item)
    {
        return _materials.get(ItemKeys.template(item).getType()).getOverflow();
    }

    @Override
    public String overflowGroup(ItemKey item)
    {
        // Tools and armor differ by damage and enchants; their overflow counts every piece of the material
        Material type = ItemKeys.template(item).getType();
        return type.getMaxDurability() > 0 ? type.name() : null;
    }

    @Override
    public Double uniquePrice(ItemKey item)
    {
        UniqueItems.Unique unique = _uniques.get(ItemKeys.template(item));
        return unique == null ? null : unique.price();
    }

    @Override
    public double condition(ItemKey item)
    {
        return condition(ItemKeys.template(item));
    }

    /**
     * 1.0 for an undamaged item; a damaged one is worth its durability left minus the penalty.
     */
    public double condition(ItemStack stack)
    {
        int maxDurability = stack.getType().getMaxDurability();
        if (maxDurability <= 0 || !(stack.getItemMeta() instanceof Damageable meta) || !meta.hasDamage())
            return 1.0;
        double left = 1.0 - (double) meta.getDamage() / maxDurability;
        if (left >= 1.0)
            return 1.0;
        return Math.max(0, Math.min(1, left - _durabilityPenalty.getAsDouble()));
    }

    @Override
    public boolean neverBuys(ItemKey item)
    {
        return FlashItems.isMarked(ItemKeys.template(item));
    }

    @Override
    public String sellLimitGroup(ItemKey item)
    {
        return _materials.get(ItemKeys.template(item).getType()).getLimitGroup();
    }

    @Override
    public SellLimit sellLimit(String group)
    {
        return _materials.getLimits().get(group);
    }

    @Override
    public ItemKey leftover(ItemKey item)
    {
        return switch (ItemKeys.template(item).getType())
        {
            case WATER_BUCKET, LAVA_BUCKET, MILK_BUCKET -> _bucket;
            default -> null;
        };
    }
}
