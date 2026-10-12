package imu.iAPI.CustomEnchants;

import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;

/**
 * A plugin's custom enchants, for other plugins: a shop can price them, sell their books and see who has them,
 * without knowing the plugin. The enchants themselves (what they do, how they level up) stay in the plugin.
 * Register one with {@link CustomEnchants#register}.
 *
 * <p>Keys are namespaced, "plugin:enchant", e.g. "imusenchants:tunnel".</p>
 */
public interface CustomEnchantProvider
{
    /**
     * @param tier how rare and strong it is, 1 (common) to 3 (rare); a shop prices it by this until an admin sets a price
     */
    record Info(String key, String name, int maxLevel, int tier) {}

    /**
     * The enchants that are on: they do something on this server and may be sold.
     */
    Collection<Info> enchants();

    /**
     * The custom enchants on the item, or stored in it if it is a book: key -> level. Empty when none.
     */
    Map<String, Integer> read(ItemStack item);

    /**
     * Whether the enchant can be put on the item at level I as it is, by the plugin's own rules (fits the item,
     * no conflicts, ...).
     */
    boolean canApply(ItemStack item, String key);

    /**
     * Puts the enchant on the item at level I. Returns false, and changes nothing, when it can't.
     */
    boolean apply(ItemStack item, String key);

    /**
     * A book of the enchant that a player can put on an item, or null for an unknown key.
     */
    ItemStack book(String key, int level);
}
