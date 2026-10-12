package imu.iAPI.CustomEnchants;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Every plugin's custom enchants in one place. A plugin with custom enchants registers its
 * {@link CustomEnchantProvider} on enable and unregisters it on disable; others read them from here.
 * Nothing registered: everything here is empty, so users of it work without any custom enchant plugin.
 */
public final class CustomEnchants
{
    private static final List<CustomEnchantProvider> PROVIDERS = new CopyOnWriteArrayList<>();
    private static final Logger LOGGER = Logger.getLogger("imusAPI");

    private CustomEnchants() {}

    public static void register(CustomEnchantProvider provider)
    {
        if (!PROVIDERS.contains(provider))
            PROVIDERS.add(provider);
    }

    public static void unregister(CustomEnchantProvider provider)
    {
        PROVIDERS.remove(provider);
    }

    public static List<CustomEnchantProvider> providers()
    {
        return List.copyOf(PROVIDERS);
    }

    public static boolean isEmpty()
    {
        return PROVIDERS.isEmpty();
    }

    /**
     * Every enchant of every provider that is on.
     */
    public static List<CustomEnchantProvider.Info> enchants()
    {
        List<CustomEnchantProvider.Info> all = new ArrayList<>();
        for (CustomEnchantProvider provider : PROVIDERS)
        {
            try
            {
                all.addAll(provider.enchants());
            } catch (RuntimeException e)
            {
                LOGGER.log(Level.WARNING, "A custom enchant provider failed", e);
            }
        }
        return all;
    }

    /**
     * The enchant with this key, or null.
     */
    public static CustomEnchantProvider.Info info(String key)
    {
        for (CustomEnchantProvider.Info info : enchants())
        {
            if (info.key().equalsIgnoreCase(key))
                return info;
        }
        return null;
    }

    /**
     * Every custom enchant on (or in) the item, of every provider: key -> level.
     */
    public static Map<String, Integer> read(ItemStack item)
    {
        Map<String, Integer> enchants = new HashMap<>();
        if (item == null || item.getType().isAir() || PROVIDERS.isEmpty())
            return enchants;
        for (CustomEnchantProvider provider : PROVIDERS)
        {
            try
            {
                enchants.putAll(provider.read(item));
            } catch (RuntimeException e)
            {
                LOGGER.log(Level.WARNING, "A custom enchant provider failed", e);
            }
        }
        return enchants;
    }

    /**
     * The provider of the enchant with this key, or null.
     */
    public static CustomEnchantProvider providerOf(String key)
    {
        for (CustomEnchantProvider provider : PROVIDERS)
        {
            for (CustomEnchantProvider.Info info : provider.enchants())
            {
                if (info.key().equalsIgnoreCase(key))
                    return provider;
            }
        }
        return null;
    }

    public static boolean canApply(ItemStack item, String key)
    {
        CustomEnchantProvider provider = providerOf(key);
        return provider != null && provider.canApply(item, key);
    }

    /**
     * Puts the enchant on the item at level I, if its provider allows it.
     */
    public static boolean apply(ItemStack item, String key)
    {
        CustomEnchantProvider provider = providerOf(key);
        return provider != null && provider.apply(item, key);
    }

    public static ItemStack book(String key, int level)
    {
        CustomEnchantProvider provider = providerOf(key);
        return provider == null ? null : provider.book(key, level);
    }
}
