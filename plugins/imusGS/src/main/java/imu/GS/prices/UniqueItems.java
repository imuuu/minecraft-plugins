package imu.GS.prices;

import imu.GS.bukkit.ItemKeys;
import imu.GS.engine.ItemKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Items with a price of their own, e.g. a named sword. Damage doesn't matter: a damaged copy is the same unique,
 * it just sells for less.
 */
public class UniqueItems
{
    /**
     * @param item the item undamaged, amount one
     */
    public record Unique(ItemKey key, ItemStack item, double price) {}

    private final Map<ItemKey, Unique> _uniques = new LinkedHashMap<>();

    /**
     * The key every copy of the item shares, whatever its damage.
     */
    public static ItemKey keyOf(ItemStack stack)
    {
        ItemStack undamaged = stack.asOne();
        if (undamaged.getItemMeta() instanceof Damageable meta && meta.hasDamage())
        {
            meta.setDamage(0);
            undamaged.setItemMeta(meta);
        }
        return ItemKeys.of(undamaged);
    }

    public Unique get(ItemStack stack)
    {
        return _uniques.get(keyOf(stack));
    }

    public Unique add(ItemStack stack, double price)
    {
        ItemKey key = keyOf(stack);
        Unique unique = new Unique(key, ItemKeys.template(key), price);
        _uniques.put(key, unique);
        return unique;
    }

    public void put(Unique unique)
    {
        _uniques.put(unique.key(), unique);
    }

    public Unique setPrice(ItemKey key, double price)
    {
        Unique old = _uniques.get(key);
        if (old == null)
            return null;
        Unique unique = new Unique(key, old.item(), price);
        _uniques.put(key, unique);
        return unique;
    }

    public boolean remove(ItemKey key)
    {
        return _uniques.remove(key) != null;
    }

    public Collection<Unique> getAll()
    {
        return Collections.unmodifiableCollection(_uniques.values());
    }
}
