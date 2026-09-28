package me.imu.imusenchants.CustomEnchants;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

// What kind of item a custom enchant can be put on
public enum ItemTarget
{
	PICKAXE("Pickaxes"),
	SHOVEL("Shovels"),
	AXE("Axes"),
	HOE("Hoes"),
	SWORD("Swords"),
	BOW("Bows"),
	CROSSBOW("Crossbows"),
	TRIDENT("Tridents"),
	FISHING_ROD("Fishing rods"),
	HELMET("Helmets"),
	CHESTPLATE("Chestplates"),
	LEGGINGS("Leggings"),
	BOOTS("Boots"),
	ELYTRA("Elytras"),
	SHIELD("Shields");

	public static final Set<ItemTarget> MINING_TOOLS = EnumSet.of(PICKAXE, SHOVEL, AXE, HOE);
	public static final Set<ItemTarget> MELEE = EnumSet.of(SWORD, AXE);
	public static final Set<ItemTarget> RANGED = EnumSet.of(BOW, CROSSBOW);
	public static final Set<ItemTarget> ARMOR = EnumSet.of(HELMET, CHESTPLATE, LEGGINGS, BOOTS);
	public static final Set<ItemTarget> ALL = EnumSet.allOf(ItemTarget.class);

	private final String _displayName;

	ItemTarget(String displayName)
	{
		_displayName = displayName;
	}

	public String GetDisplayName()
	{
		return _displayName;
	}

	public static ItemTarget Of(Material material)
	{
		if (material == null)
			return null;

		String name = material.name();

		// PICKAXE has to be checked before AXE
		if (name.endsWith("_PICKAXE")) return PICKAXE;
		if (name.endsWith("_SHOVEL")) return SHOVEL;
		if (name.endsWith("_AXE")) return AXE;
		if (name.endsWith("_HOE")) return HOE;
		if (name.endsWith("_SWORD")) return SWORD;
		if (name.endsWith("_HELMET") || material == Material.TURTLE_HELMET) return HELMET;
		if (name.endsWith("_CHESTPLATE")) return CHESTPLATE;
		if (name.endsWith("_LEGGINGS")) return LEGGINGS;
		if (name.endsWith("_BOOTS")) return BOOTS;

		switch (material)
		{
			case BOW: return BOW;
			case CROSSBOW: return CROSSBOW;
			case TRIDENT: return TRIDENT;
			case FISHING_ROD: return FISHING_ROD;
			case ELYTRA: return ELYTRA;
			case SHIELD: return SHIELD;
			default: return null;
		}
	}

	public static Set<ItemTarget> SetOf(ItemTarget... targets)
	{
		return EnumSet.copyOf(Arrays.asList(targets));
	}

	@SafeVarargs
	public static Set<ItemTarget> Union(Collection<ItemTarget>... groups)
	{
		Set<ItemTarget> result = EnumSet.noneOf(ItemTarget.class);
		for (Collection<ItemTarget> group : groups)
			result.addAll(group);
		return result;
	}

	public static String ToText(Set<ItemTarget> targets)
	{
		if (targets.containsAll(ALL))
			return "All items";

		List<String> names = new ArrayList<>();
		for (ItemTarget target : targets)
			names.add(target.GetDisplayName());
		return String.join(", ", names);
	}
}
