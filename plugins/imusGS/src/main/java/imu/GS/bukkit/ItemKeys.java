package imu.GS.bukkit;

import imu.GS.engine.ItemKey;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Turns ItemStacks into engine {@link ItemKey}s and back with Paper's own item formats, so nothing of the item is
 * lost: name, lore, enchants, custom model data, and plugins' persistent data (minecraft:custom_data).
 *
 * <p>The key is normally the item as /give writes it, e.g.
 * {@code minecraft:diamond_sword[minecraft:enchantments={sharpness:5}]}, readable in the database. Every new key is
 * turned back into an item once and compared; an item that doesn't come back exactly the same is keyed by Paper's
 * binary form instead ({@code bytes:...}). Two stacks get the same key exactly when they are similar.</p>
 *
 * <p>Main thread only.</p>
 */
public final class ItemKeys
{
    private static final String BYTES_PREFIX = "bytes:";
    private static final Map<ItemKey, ItemStack> TEMPLATES = new HashMap<>();
    // Text keys of items that didn't round trip -> their binary key
    private static final Map<ItemKey, ItemKey> BINARY_KEYS = new HashMap<>();
    private static Logger _logger = Logger.getLogger("imusGS");

    private ItemKeys() {}

    public static void setLogger(Logger logger)
    {
        _logger = logger;
    }

    public static ItemKey of(ItemStack stack)
    {
        ItemStack one = stack.asOne();
        ItemKey key = new ItemKey(componentString(one));
        ItemKey known = BINARY_KEYS.get(key);
        if (known != null)
            return known;
        if (TEMPLATES.containsKey(key))
            return key;

        if (roundTrips(key, one))
        {
            TEMPLATES.put(key, one);
            return key;
        }

        ItemKey bytesKey = new ItemKey(BYTES_PREFIX + Base64.getEncoder().encodeToString(one.serializeAsBytes()));
        TEMPLATES.putIfAbsent(bytesKey, one);
        BINARY_KEYS.put(key, bytesKey);
        return bytesKey;
    }

    /**
     * A new stack of the item. Throws IllegalArgumentException when the key isn't a valid item.
     */
    public static ItemStack stack(ItemKey key, int amount)
    {
        ItemStack stack = template(key).clone();
        stack.setAmount(amount);
        return stack;
    }

    /**
     * The item with an amount of one, shared; don't change it.
     */
    public static ItemStack template(ItemKey key)
    {
        ItemStack template = TEMPLATES.get(key);
        if (template == null)
        {
            template = parse(key).asOne();
            TEMPLATES.put(key, template);
        }
        return template;
    }

    public static boolean isValid(ItemKey key)
    {
        try
        {
            template(key);
            return true;
        } catch (RuntimeException e)
        {
            return false;
        }
    }

    private static String componentString(ItemStack one)
    {
        ItemMeta meta = one.getItemMeta();
        return one.getType().getKey() + (meta == null ? "" : meta.getAsComponentString());
    }

    private static ItemStack parse(ItemKey key)
    {
        String id = key.id();
        if (id.startsWith(BYTES_PREFIX))
            return ItemStack.deserializeBytes(Base64.getDecoder().decode(id.substring(BYTES_PREFIX.length())));
        return Bukkit.getItemFactory().createItemStack(id);
    }

    private static boolean roundTrips(ItemKey key, ItemStack original)
    {
        try
        {
            if (parse(key).isSimilar(original))
                return true;
        } catch (RuntimeException e)
        {
            // falls through to the warning
        }
        _logger.warning("Item " + key + " doesn't come back the same from its text form, storing it in binary form");
        return false;
    }
}
