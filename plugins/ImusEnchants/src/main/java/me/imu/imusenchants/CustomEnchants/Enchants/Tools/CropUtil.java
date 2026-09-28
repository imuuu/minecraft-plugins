package me.imu.imusenchants.CustomEnchants.Enchants.Tools;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;

import java.util.EnumMap;
import java.util.Map;

// Crops Replanter and Harvest work on, and the item each one is planted with
class CropUtil
{
	private static final Map<Material, Material> SEEDS = new EnumMap<>(Material.class);

	static
	{
		SEEDS.put(Material.WHEAT, Material.WHEAT_SEEDS);
		SEEDS.put(Material.CARROTS, Material.CARROT);
		SEEDS.put(Material.POTATOES, Material.POTATO);
		SEEDS.put(Material.BEETROOTS, Material.BEETROOT_SEEDS);
		SEEDS.put(Material.NETHER_WART, Material.NETHER_WART);
	}

	static Material GetSeed(Material crop)
	{
		return SEEDS.get(crop);
	}

	static boolean IsMatureCrop(Block block)
	{
		return IsMatureCrop(block.getType(), block.getBlockData());
	}

	static boolean IsMatureCrop(BlockState state)
	{
		return IsMatureCrop(state.getType(), state.getBlockData());
	}

	private static boolean IsMatureCrop(Material type, BlockData data)
	{
		if (!SEEDS.containsKey(type) || !(data instanceof Ageable))
			return false;

		Ageable ageable = (Ageable) data;
		return ageable.getAge() >= ageable.getMaximumAge();
	}

	static boolean CanPlant(Material crop, Block ground)
	{
		if (crop == Material.NETHER_WART)
			return ground.getType() == Material.SOUL_SAND;

		return ground.getType() == Material.FARMLAND;
	}
}
