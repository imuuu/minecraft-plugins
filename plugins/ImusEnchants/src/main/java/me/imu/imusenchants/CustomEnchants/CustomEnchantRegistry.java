package me.imu.imusenchants.CustomEnchants;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.Enchants.TunnelEnchant;
import me.imu.imusenchants.CustomEnchants.Enchants.VeinMinerEnchant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CustomEnchantRegistry
{
	private static final Map<String, CustomEnchant> _enchants = new LinkedHashMap<>();
	private static final Random _random = new Random();

	public static void RegisterDefaults()
	{
		Register(new TunnelEnchant());
		Register(new VeinMinerEnchant());
	}

	public static void Register(CustomEnchant enchant)
	{
		_enchants.put(enchant.GetKey(), enchant);
	}

	public static CustomEnchant Get(String key)
	{
		if (key == null)
			return null;

		return _enchants.get(key.toLowerCase());
	}

	public static Collection<CustomEnchant> GetAll()
	{
		return Collections.unmodifiableCollection(_enchants.values());
	}

	public static boolean AreConflicting(CustomEnchant a, CustomEnchant b)
	{
		if (a == null || b == null || a == b)
			return false;

		return a.GetConflicts().contains(b.GetKey()) || b.GetConflicts().contains(a.GetKey());
	}

	// First enchant in the collection that conflicts with the given one, or null
	public static CustomEnchant FindConflict(CustomEnchant enchant, Collection<CustomEnchant> others)
	{
		for (CustomEnchant other : others)
		{
			if (AreConflicting(enchant, other))
				return other;
		}
		return null;
	}

	public static CustomEnchant GetRandom()
	{
		return GetRandom(new ArrayList<>(_enchants.values()));
	}

	// Random enchant sold in the given shop category, or null if there are none
	public static CustomEnchant GetRandomForShop(ITEM_CATEGORY category)
	{
		List<CustomEnchant> options = new ArrayList<>();
		for (CustomEnchant enchant : _enchants.values())
		{
			if (enchant.GetShopCategory() == category)
				options.add(enchant);
		}
		return GetRandom(options);
	}

	private static CustomEnchant GetRandom(List<CustomEnchant> options)
	{
		if (options.isEmpty())
			return null;

		return options.get(_random.nextInt(options.size()));
	}
}
