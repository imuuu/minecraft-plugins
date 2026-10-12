package imu.GS.bukkit;

import imu.GS.text.Messages;
import imu.iAPI.Utilities.ItemUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * The mark on items bought from a flash deal: no shop buys them back. Placing a marked block or crafting with a
 * marked item loses the mark, which is why a flash price never goes under what the shop pays back.
 */
public final class FlashItems
{
    public static final NamespacedKey KEY = new NamespacedKey("imusgs", "flash_deal");

    private FlashItems() {}

    /**
     * A copy of the item with the mark and a lore line telling about it.
     */
    public static ItemStack mark(ItemStack item)
    {
        ItemStack stack = item.clone();
        ItemMeta meta = stack.getItemMeta();
        if (meta == null)
            return stack;
        meta.getPersistentDataContainer().set(KEY, PersistentDataType.BYTE, (byte) 1);
        stack.setItemMeta(meta);
        ItemUtils.AddLore(stack, List.of(Messages.get("flash.item-mark")));
        return stack;
    }

    public static boolean isMarked(ItemStack item)
    {
        if (item == null || !item.hasItemMeta())
            return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY, PersistentDataType.BYTE);
    }
}
