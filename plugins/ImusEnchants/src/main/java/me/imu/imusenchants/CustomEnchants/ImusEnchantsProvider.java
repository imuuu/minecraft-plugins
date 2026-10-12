package me.imu.imusenchants.CustomEnchants;

import imu.iAPI.CustomEnchants.CustomEnchantProvider;
import imu.iAPI.CustomItems.CustomItemProvider;
import me.imu.imusenchants.Enchants.EnchantedItem;
import me.imu.imusenchants.Enchants.NodeBooster;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// ImusEnchants' custom enchants for other plugins through ImusAPI's CustomEnchants, and its boosters through
// CustomItems, e.g. imusGS pricing and selling them. Keys are "imusenchants:<enchant key>".
// Only level I goes on items here, like a book on an item without slots; higher levels come from the
// slot grid with boosters.
public class ImusEnchantsProvider implements CustomEnchantProvider, CustomItemProvider
{
	private static final String PREFIX = "imusenchants:";

	// The strong ones are rare in shops, the small helpers common; the rest are in between
	private static final Set<String> TIER_3 = Set.of("tunnel", "vein_miner", "timber", "telekinesis", "lifesteal",
			"soulbound", "execute", "beheading", "double_catch");
	private static final Set<String> TIER_1 = Set.of("night_vision", "aquatic", "glass_breaker", "replanter",
			"auto_reel", "survivalist", "ender_arrow", "smelting");

	private static CustomEnchant enchant(String key)
	{
		if (key == null)
			return null;
		String lower = key.toLowerCase(Locale.ROOT);
		return CustomEnchantRegistry.Get(lower.startsWith(PREFIX) ? lower.substring(PREFIX.length()) : lower);
	}

	private static int tier(CustomEnchant enchant)
	{
		if (TIER_3.contains(enchant.GetKey()))
			return 3;
		return TIER_1.contains(enchant.GetKey()) ? 1 : 2;
	}

	@Override
	public Collection<Info> enchants()
	{
		List<Info> infos = new ArrayList<>();
		for (CustomEnchant enchant : CustomEnchantRegistry.GetAll())
		{
			if (!enchant.IsCurse() && EnchantSettings.IsEnabled(enchant))
				infos.add(new Info(PREFIX + enchant.GetKey(), enchant.GetName(), enchant.GetMaxLevel(), tier(enchant)));
		}
		return infos;
	}

	@Override
	public Map<String, Integer> read(ItemStack item)
	{
		Map<String, Integer> enchants = new HashMap<>();
		Map.Entry<CustomEnchant, Integer> book = CustomEnchantBook.Read(item);
		if (book != null)
		{
			enchants.put(PREFIX + book.getKey().GetKey(), book.getValue());
			return enchants;
		}
		if (item == null || !CustomEnchantData.HasAny(item))
			return enchants;
		for (Map.Entry<CustomEnchant, Integer> entry : CustomEnchantData.Get(item).entrySet())
			enchants.put(PREFIX + entry.getKey().GetKey(), entry.getValue());
		return enchants;
	}

	// The same rules as clicking a book on an item without slots
	@Override
	public boolean canApply(ItemStack item, String key)
	{
		CustomEnchant enchant = enchant(key);
		if (enchant == null || item == null || enchant.IsCurse() || !EnchantSettings.IsEnabled(enchant) || !enchant.CanApplyToItem(item))
			return false;
		if (EnchantedItem.HasSlots(item))
			return false;
		Map<CustomEnchant, Integer> current = CustomEnchantData.Get(item);
		if (current.containsKey(enchant) || CustomEnchantRegistry.FindConflict(enchant, current.keySet()) != null)
			return false;
		for (Enchantment vanilla : item.getEnchantments().keySet())
		{
			if (enchant.GetVanillaConflicts().contains(vanilla))
				return false;
		}
		return true;
	}

	@Override
	public boolean apply(ItemStack item, String key)
	{
		if (!canApply(item, key))
			return false;
		Map<CustomEnchant, Integer> current = CustomEnchantData.Get(item);
		current.put(enchant(key), 1);
		CustomEnchantData.Set(item, current);
		return true;
	}

	@Override
	public ItemStack book(String key, int level)
	{
		CustomEnchant enchant = enchant(key);
		return enchant == null ? null : CustomEnchantBook.Create(enchant, Math.max(1, Math.min(level, enchant.GetMaxLevel())));
	}

	// ===== Boosters as custom items =====

	@Override
	public List<CustomItem> items()
	{
		List<String> tags = List.of("enchanting");
		return List.of(
				new CustomItem(PREFIX + "booster_1", "Booster (one direction)", new NodeBooster(1, 1).GetItemStack(), 1, tags),
				new CustomItem(PREFIX + "booster_2", "Booster (three directions)", new NodeBooster(1, 3).GetItemStack(), 2, tags),
				new CustomItem(PREFIX + "booster_3", "Booster (four directions)", new NodeBooster(1, 4).GetItemStack(), 3, tags));
	}

	// Boosters by how many directions they power, like the tiers of the enchant shop
	@Override
	public String idOf(ItemStack item)
	{
		if (!NodeBooster.IsBooster(item))
			return null;
		int directions = new NodeBooster(item).GetDirectionAmount();
		return PREFIX + "booster_" + (directions >= 4 ? 3 : directions >= 2 ? 2 : 1);
	}
}
