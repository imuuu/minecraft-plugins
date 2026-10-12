package imu.imusTNT.TNTs;

import imu.iAPI.CustomItems.CustomItemProvider;
import imu.imusTNT.enums.TNT_TYPE;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// The special TNTs as custom items through ImusAPI's CustomItems, so shops (imusGS) can price and sell them.
// Ids are "imustnt:<type>", e.g. "imustnt:chunk_tnt".
public class TntItems implements CustomItemProvider
{
	private static final String PREFIX = "imustnt:";

	@Override
	public List<CustomItem> items()
	{
		List<CustomItem> items = new ArrayList<>();
		for (TNT tnt : TNT_Mananger.Instance.GetTNTs())
		{
			// Clearing a whole chunk is worth a lot more than clearing its stone
			int tier = tnt.Type == TNT_TYPE.CHUNK_TNT ? 3 : 2;
			String name = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', tnt.DisplayName));
			items.add(new CustomItem(id(tnt.Type), name, tnt.GetItemStack(), tier, List.of("redstone", "explosives")));
		}
		return items;
	}

	@Override
	public String idOf(ItemStack item)
	{
		if (item == null || item.getType().isAir() || !item.hasItemMeta())
			return null;
		TNT_TYPE type = TNT_Mananger.Instance.GetTntType(item);
		return type == null || type == TNT_TYPE.NONE ? null : id(type);
	}

	private static String id(TNT_TYPE type)
	{
		return PREFIX + type.name().toLowerCase(Locale.ROOT);
	}
}
