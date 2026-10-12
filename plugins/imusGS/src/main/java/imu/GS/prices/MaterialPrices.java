package imu.GS.prices;

import imu.iAPI.Utilities.RecipeUtils;
import imu.GS.engine.pricing.Overflow;
import imu.GS.engine.pricing.SellLimit;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Tuple;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base price of every material, with its tags, overflow and smart price.
 *
 * <p>A smart price follows the material's crafting recipe: the price of each ingredient per crafted unit, added up and
 * multiplied by the smart multiplier. A 1.2 multiplier makes crafted items worth 20% more than their ingredients.</p>
 */
public class MaterialPrices
{
    public static final class Entry
    {
        private double _price;
        private Double _smartMultiplier;
        private Overflow _overflow;
        private final Set<String> _tags = new HashSet<>();
        private String _limitGroup;

        /**
         * The sell limit group (crops, iron...), null when selling the material isn't limited.
         */
        public String getLimitGroup()
        {
            return _limitGroup;
        }

        public double getPrice()
        {
            return _price;
        }

        /**
         * Null when the material has no smart price.
         */
        public Double getSmartMultiplier()
        {
            return _smartMultiplier;
        }

        /**
         * Null when the price never drops.
         */
        public Overflow getOverflow()
        {
            return _overflow;
        }

        public Set<String> getTags()
        {
            return Collections.unmodifiableSet(_tags);
        }
    }

    private final Map<Material, Entry> _entries = new EnumMap<>(Material.class);
    // Sell limit group -> its limit in every shop
    private final Map<String, SellLimit> _limits = new HashMap<>();
    // Ingredient -> units per crafted unit, looked up once per material
    private final Map<Material, Map<Material, Double>> _recipes = new HashMap<>();

    public Entry get(Material material)
    {
        return _entries.computeIfAbsent(material, m -> new Entry());
    }

    public Map<Material, Entry> getEntries()
    {
        return _entries;
    }

    /**
     * The material's price: its smart price when it has one, otherwise its base price.
     */
    public double price(Material material)
    {
        Entry entry = get(material);
        if (entry._smartMultiplier == null)
            return entry._price;
        return smartPrice(material, entry._smartMultiplier);
    }

    private double smartPrice(Material material, double multiplier)
    {
        double price = 0;
        for (Map.Entry<Material, Double> ingredient : recipe(material).entrySet())
            price += get(ingredient.getKey())._price * ingredient.getValue();
        return price * multiplier;
    }

    /**
     * Ingredients of the material's first crafting recipe, per crafted unit. Empty when it has no recipe.
     */
    public Map<Material, Double> recipe(Material material)
    {
        return _recipes.computeIfAbsent(material, m ->
        {
            Map<Material, Double> ingredients = new HashMap<>();
            if (!m.isItem())
                return ingredients;
            List<Tuple<ItemStack, Double>> recipe = RecipeUtils.GetRecipe(new ItemStack(m));
            for (Tuple<ItemStack, Double> part : recipe)
                ingredients.merge(part.GetKey().getType(), part.GetValue(), Double::sum);
            return ingredients;
        });
    }

    public void setPrice(Material material, double price)
    {
        get(material)._price = Math.max(0, price);
    }

    /**
     * Null removes the smart price.
     */
    public void setSmartMultiplier(Material material, Double multiplier)
    {
        get(material)._smartMultiplier = multiplier;
    }

    /**
     * Null removes the overflow.
     */
    public void setOverflow(Material material, Overflow overflow)
    {
        get(material)._overflow = overflow;
    }

    // Sell limits ====================================================================================================

    /**
     * Null takes the material out of every sell limit group.
     */
    public void setLimitGroup(Material material, String group)
    {
        get(material)._limitGroup = group == null || group.isBlank() ? null : group.toLowerCase(Locale.ROOT);
    }

    public Map<String, SellLimit> getLimits()
    {
        return _limits;
    }

    public void setLimits(Map<String, SellLimit> limits)
    {
        _limits.clear();
        limits.forEach((group, limit) -> _limits.put(group.toLowerCase(Locale.ROOT), limit));
    }

    // Tags ===========================================================================================================

    public boolean addTag(Material material, String tag)
    {
        return get(material)._tags.add(tag.toLowerCase(Locale.ROOT));
    }

    public boolean removeTag(Material material, String tag)
    {
        return get(material)._tags.remove(tag.toLowerCase(Locale.ROOT));
    }

    public void clearTags(Material material)
    {
        get(material)._tags.clear();
    }

    public void setTags(Material material, Collection<String> tags)
    {
        Entry entry = get(material);
        entry._tags.clear();
        for (String tag : tags)
            entry._tags.add(tag.toLowerCase(Locale.ROOT));
    }

    public Set<Material> withTag(String tag)
    {
        String lower = tag.toLowerCase(Locale.ROOT);
        return _entries.entrySet().stream()
                .filter(e -> e.getValue()._tags.contains(lower))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Set<String> allTags()
    {
        Set<String> tags = new HashSet<>();
        for (Entry entry : _entries.values())
            tags.addAll(entry._tags);
        return tags;
    }
}
