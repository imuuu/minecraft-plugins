package imu.iAPI.CustomItems;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Every plugin's custom items in one place. A plugin registers its {@link CustomItemProvider} on enable and
 * unregisters it on disable; others read the items from here. Empty when no plugin has registered.
 */
public final class CustomItems
{
    private static final List<CustomItemProvider> PROVIDERS = new CopyOnWriteArrayList<>();
    private static final Logger LOGGER = Logger.getLogger("imusAPI");

    private CustomItems() {}

    public static void register(CustomItemProvider provider)
    {
        if (!PROVIDERS.contains(provider))
            PROVIDERS.add(provider);
    }

    public static void unregister(CustomItemProvider provider)
    {
        PROVIDERS.remove(provider);
    }

    public static boolean isEmpty()
    {
        return PROVIDERS.isEmpty();
    }

    public static List<CustomItemProvider.CustomItem> items()
    {
        List<CustomItemProvider.CustomItem> items = new ArrayList<>();
        for (CustomItemProvider provider : PROVIDERS)
        {
            try
            {
                items.addAll(provider.items());
            } catch (RuntimeException e)
            {
                LOGGER.log(Level.WARNING, "A custom item provider failed", e);
            }
        }
        return items;
    }

    /**
     * The item with this id, or null.
     */
    public static CustomItemProvider.CustomItem item(String id)
    {
        for (CustomItemProvider.CustomItem item : items())
        {
            if (item.id().equalsIgnoreCase(id))
                return item;
        }
        return null;
    }

    /**
     * Items that have one of these tags.
     */
    public static List<CustomItemProvider.CustomItem> withTags(List<String> tags)
    {
        List<CustomItemProvider.CustomItem> found = new ArrayList<>();
        for (CustomItemProvider.CustomItem item : items())
        {
            for (String tag : item.tags())
            {
                if (tags.stream().anyMatch(t -> t.equalsIgnoreCase(tag)))
                {
                    found.add(item);
                    break;
                }
            }
        }
        return found;
    }

    /**
     * The id of the item's kind when a plugin made it, otherwise null.
     */
    public static String idOf(ItemStack item)
    {
        if (item == null || item.getType().isAir() || PROVIDERS.isEmpty())
            return null;
        for (CustomItemProvider provider : PROVIDERS)
        {
            try
            {
                String id = provider.idOf(item);
                if (id != null)
                    return id.toLowerCase(Locale.ROOT);
            } catch (RuntimeException e)
            {
                LOGGER.log(Level.WARNING, "A custom item provider failed", e);
            }
        }
        return null;
    }
}
