package imu.iAPI.Menus.Paper;

import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * A menu that adds rows to the tooltips of the player's own items while it is open, on their screen only: the items
 * themselves never change, and the rows are gone when the menu closes. E.g. a shop shows what each item sells for.
 * <pre>
 * public class ShopMenu extends Menu implements OwnItemRows
 * {
 *     public List&lt;String&gt; ownItemRows(ItemStack item)
 *     {
 *         return List.of("&amp;7Sells here: &amp;e" + price(item));
 *     }
 * }
 * </pre>
 * The rows are shown when the menu opens, after clicks and every second, through
 * {@link imu.iAPI.Packets.ClientItems#showRows}; not for players in creative.
 */
public interface OwnItemRows
{
    /**
     * Rows added under the item's tooltip, with &amp; colour codes; empty or null for none. Runs often, so keep it
     * cheap.
     */
    List<String> ownItemRows(ItemStack item);
}
