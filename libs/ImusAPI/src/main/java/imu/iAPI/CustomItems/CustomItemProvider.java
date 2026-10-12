package imu.iAPI.CustomItems;

import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * A plugin's custom items (special TNT, boosters...), for other plugins: a shop can price, sell and track them
 * without knowing the plugin. Register one with {@link CustomItems#register}.
 */
public interface CustomItemProvider
{
    /**
     * @param id   namespaced, "plugin:item", e.g. "imustnt:chunk_tnt"
     * @param item a fresh stack of one
     * @param tier how valuable it is, 1 (common) to 3 (rare); a shop prices it by this until an admin sets a price
     * @param tags kinds of shop that sell it, like the material tags of imusGS: "redstone", "explosives", "tools"...
     */
    record CustomItem(String id, String name, ItemStack item, int tier, List<String> tags)
    {
        public CustomItem
        {
            tags = List.copyOf(tags);
        }

        public ItemStack stack()
        {
            return item.clone();
        }
    }

    /**
     * The plugin's items that are on.
     */
    List<CustomItem> items();

    /**
     * The id of the item's kind when it is one of this plugin's items, otherwise null.
     */
    String idOf(ItemStack item);
}
