package imu.GS.prices;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * What enchants add to an item's value.
 *
 * <p>An enchant is worth {@code minPrice} at level 1 and {@code maxPrice} at its max level, growing by the same factor
 * every level in between. On gear it is multiplied by the gear's {@link GearTier} multiplier, on an enchanted book by
 * the enchant's book multiplier. Every enchant after the first adds {@code enchantCountPercent} to the total.</p>
 */
public class EnchantPrices
{
    /**
     * @param maxLevel       level at which the price reaches maxPrice
     * @param bookMultiplier the enchant on an enchanted book is worth this times its price
     */
    public record EnchantValue(int maxLevel, double minPrice, double maxPrice, double bookMultiplier)
    {
        public static final double DEFAULT_BOOK_MULTIPLIER = 0.3;

        public double price(int level)
        {
            if (level >= maxLevel || maxLevel <= 1)
                return maxPrice;
            if (level <= 1)
                return minPrice;
            if (minPrice <= 0 || maxPrice <= 0)
                return minPrice + (maxPrice - minPrice) * (level - 1) / (maxLevel - 1.0);
            double factor = Math.pow(maxPrice / minPrice, 1.0 / (maxLevel - 1.0));
            return minPrice * Math.pow(factor, level - 1);
        }
    }

    public static final double DEFAULT_ENCHANT_COUNT_PERCENT = 0.05;

    private final Map<NamespacedKey, EnchantValue> _values = new HashMap<>();
    private final Map<GearTier, Double> _gearMultipliers = new EnumMap<>(GearTier.class);
    private double _enchantCountPercent = DEFAULT_ENCHANT_COUNT_PERCENT;
    // "enchant:<key>" -> price per level, "item:<id>" -> price of one; set by admins, the rest go by tier
    private final Map<String, Double> _customPrices = new HashMap<>();

    public EnchantPrices()
    {
        for (GearTier tier : GearTier.values())
            _gearMultipliers.put(tier, tier.getDefaultMultiplier());
    }

    public static String customEnchantName(String key)
    {
        return "enchant:" + key.toLowerCase(java.util.Locale.ROOT);
    }

    public static String customItemName(String id)
    {
        return "item:" + id.toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * The price an admin set for a custom enchant (per level) or item, see {@link #customEnchantName} and
     * {@link #customItemName}; null when it goes by its tier.
     */
    public Double getCustomPrice(String name)
    {
        return _customPrices.get(name);
    }

    /**
     * Null goes back to the price by tier.
     */
    public void setCustomPrice(String name, Double price)
    {
        if (price == null)
            _customPrices.remove(name);
        else
            _customPrices.put(name, price);
    }

    public Map<String, Double> getCustomPrices()
    {
        return _customPrices;
    }

    /**
     * The enchant's value; an enchant nobody priced yet is worth nothing.
     */
    public EnchantValue getValue(Enchantment enchantment)
    {
        EnchantValue value = _values.get(enchantment.getKey());
        return value != null ? value : new EnchantValue(enchantment.getMaxLevel(), 0, 0, EnchantValue.DEFAULT_BOOK_MULTIPLIER);
    }

    public void setValue(Enchantment enchantment, EnchantValue value)
    {
        _values.put(enchantment.getKey(), value);
    }

    public Map<NamespacedKey, EnchantValue> getValues()
    {
        return _values;
    }

    public double getGearMultiplier(GearTier tier)
    {
        return _gearMultipliers.get(tier);
    }

    public void setGearMultiplier(GearTier tier, double multiplier)
    {
        _gearMultipliers.put(tier, multiplier);
    }

    public double getEnchantCountPercent()
    {
        return _enchantCountPercent;
    }

    public void setEnchantCountPercent(double percent)
    {
        _enchantCountPercent = percent;
    }

    /**
     * What the enchants of the item are worth.
     */
    public double price(ItemStack stack)
    {
        Map<Enchantment, Integer> enchants = enchants(stack);
        if (enchants.isEmpty())
            return 0;

        boolean book = stack.getType() == Material.ENCHANTED_BOOK;
        double price = 0;
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet())
        {
            EnchantValue value = getValue(entry.getKey());
            double enchantPrice = value.price(entry.getValue());
            price += book ? enchantPrice * value.bookMultiplier() : enchantPrice;
        }
        if (price <= 0)
            return 0;

        double multiplier = 1.0;
        if (!book)
        {
            GearTier tier = GearTier.of(stack);
            multiplier = tier == null ? 0 : getGearMultiplier(tier);
        }
        return price * multiplier * (1.0 + _enchantCountPercent * (enchants.size() - 1));
    }

    private static Map<Enchantment, Integer> enchants(ItemStack stack)
    {
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta book)
            return book.getStoredEnchants();
        return meta == null ? Map.of() : meta.getEnchants();
    }
}
