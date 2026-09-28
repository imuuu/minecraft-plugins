package me.imu.imusenchants.CustomEnchants;

import org.bukkit.Material;

public enum ToolType
{
	PICKAXE("_PICKAXE"),
	SHOVEL("_SHOVEL"),
	AXE("_AXE"),
	HOE("_HOE"),
	SWORD("_SWORD"),
	NONE(null);

	private final String _suffix;

	ToolType(String suffix)
	{
		_suffix = suffix;
	}

	public static ToolType Of(Material material)
	{
		if (material == null)
			return NONE;

		String name = material.name();
		for (ToolType type : values())
		{
			// "_AXE" would also match "_PICKAXE", PICKAXE is checked first
			if (type._suffix != null && name.endsWith(type._suffix))
				return type;
		}
		return NONE;
	}
}
