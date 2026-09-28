package me.imu.imusenchants.CustomEnchants;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.Enchants.Armor.*;
import me.imu.imusenchants.CustomEnchants.Enchants.Bow.*;
import me.imu.imusenchants.CustomEnchants.Enchants.Combat.*;
import me.imu.imusenchants.CustomEnchants.Enchants.Fishing.*;
import me.imu.imusenchants.CustomEnchants.Enchants.Tools.*;
import me.imu.imusenchants.CustomEnchants.Enchants.Universal.*;

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
		// Tools
		Register(new TunnelEnchant());
		Register(new VeinMinerEnchant());
		Register(new TimberEnchant());
		Register(new SmeltingEnchant());
		Register(new TelekinesisEnchant());
		Register(new ReplanterEnchant());
		Register(new HarvestEnchant());
		Register(new GlassBreakerEnchant());
		Register(new HasteEnchant());
		Register(new WisdomEnchant());

		// Melee
		Register(new LifestealEnchant());
		Register(new VenomEnchant());
		Register(new FrostEnchant());
		Register(new WitheringEnchant());
		Register(new BeheadingEnchant());
		Register(new CleaveEnchant());
		Register(new ExecuteEnchant());
		Register(new ScavengerEnchant());

		// Bows
		Register(new SniperEnchant());
		Register(new PoisonArrowEnchant());
		Register(new EnderArrowEnchant());
		Register(new HunterEnchant());
		Register(new RetrievalEnchant());

		// Armor
		Register(new NightVisionEnchant());
		Register(new SaturationEnchant());
		Register(new AquaticEnchant());
		Register(new RegrowthEnchant());
		Register(new ReflectEnchant());
		Register(new StoppingForceEnchant());
		Register(new JumpBoostEnchant());
		Register(new MagmaWalkerEnchant());
		Register(new FallGuardEnchant());

		// Fishing
		Register(new AutoReelEnchant());
		Register(new DoubleCatchEnchant());
		Register(new SurvivalistEnchant());
		Register(new SeasonedAnglerEnchant());
		Register(new LongCastEnchant());

		// Any item
		Register(new SoulboundEnchant());
		Register(new CurseOfFragilityEnchant());
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

	// Random enabled, non-curse enchant for loot books
	public static CustomEnchant GetRandom()
	{
		List<CustomEnchant> options = new ArrayList<>();
		for (CustomEnchant enchant : _enchants.values())
		{
			if (!enchant.IsCurse() && EnchantSettings.IsEnabled(enchant))
				options.add(enchant);
		}
		return GetRandom(options);
	}

	public static CustomEnchant GetRandomCurse()
	{
		List<CustomEnchant> options = new ArrayList<>();
		for (CustomEnchant enchant : _enchants.values())
		{
			if (enchant.IsCurse() && EnchantSettings.IsEnabled(enchant))
				options.add(enchant);
		}
		return GetRandom(options);
	}

	// Random enabled enchant sold in the given shop category, or null if there are none
	public static CustomEnchant GetRandomForShop(ITEM_CATEGORY category)
	{
		List<CustomEnchant> options = new ArrayList<>();
		for (CustomEnchant enchant : _enchants.values())
		{
			if (enchant.GetShopCategory() == category && !enchant.IsCurse() && EnchantSettings.IsEnabled(enchant))
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
