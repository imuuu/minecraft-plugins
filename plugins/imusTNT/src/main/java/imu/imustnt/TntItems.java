package imu.imustnt;

import imu.iAPI.CustomItems.CustomItemProvider;
import imu.iAPI.Utilities.TextUtils;
import imu.imustnt.engine.TntType;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * The special TNTs as ImusAPI custom items, so shops (imusGS) can price and sell them. Ids are
 * "imustnt:chunk_tnt" and "imustnt:chunk_stone_tnt".
 */
class TntItems implements CustomItemProvider
{
    private final ImusTNT _plugin;

    TntItems(ImusTNT plugin)
    {
        _plugin = plugin;
    }

    @Override
    public List<CustomItem> items()
    {
        List<CustomItem> items = new ArrayList<>();
        for (TntType type : TntType.values())
        {
            String name = ChatColor.stripColor(TextUtils.Color(_plugin.getConfig().getString("items." + type.key() + ".name", type.key())));
            items.add(new CustomItem(type.id(), name, _plugin.item(type, 1), type.tier(), List.of("redstone", "explosives")));
        }
        return items;
    }

    @Override
    public String idOf(ItemStack item)
    {
        TntType type = _plugin.typeOf(item);
        return type == null ? null : type.id();
    }
}
