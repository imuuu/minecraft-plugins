package me.imu.imusenchants.CustomEnchants.Enchants.Tools;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// Block drops come out smelted, using the server's own furnace recipes. Keeps the drop amount so Fortune still works.
public class SmeltingEnchant extends CustomEnchant
{
	private Map<Material, ItemStack> _results;

	@Override public String GetKey() { return "smelting"; }
	@Override public String GetName() { return "Smelting"; }
	@Override public int GetMaxLevel() { return 1; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.TOOL; }
	@Override public int GetPriority() { return 200; } // before Telekinesis picks the drops up

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.SetOf(ItemTarget.PICKAXE, ItemTarget.SHOVEL, ItemTarget.AXE);
	}

	@Override
	public Set<Enchantment> GetVanillaConflicts()
	{
		return Collections.singleton(Enchantment.SILK_TOUCH);
	}

	@Override
	public String GetDescription(int level)
	{
		return "Mined blocks drop smelted";
	}

	@Override
	public void OnBlockDrop(BlockDropItemEvent event, ItemStack tool, int level)
	{
		if (event.getBlockState() instanceof Container)
			return;

		for (Item drop : event.getItems())
		{
			ItemStack stack = drop.getItemStack();
			ItemStack result = GetResults().get(stack.getType());
			if (result == null)
				continue;

			ItemStack smelted = result.clone();
			smelted.setAmount(stack.getAmount());
			drop.setItemStack(smelted);
		}
	}

	private Map<Material, ItemStack> GetResults()
	{
		if (_results != null)
			return _results;

		_results = new EnumMap<>(Material.class);
		Iterator<Recipe> recipes = Bukkit.recipeIterator();
		while (recipes.hasNext())
		{
			Recipe recipe = recipes.next();
			if (!(recipe instanceof FurnaceRecipe))
				continue;

			RecipeChoice choice = ((FurnaceRecipe) recipe).getInputChoice();
			if (!(choice instanceof RecipeChoice.MaterialChoice))
				continue;

			for (Material input : ((RecipeChoice.MaterialChoice) choice).getChoices())
			{
				if (!IsExcluded(input))
					_results.putIfAbsent(input, recipe.getResult());
			}
		}
		return _results;
	}

	// Stone should still drop cobblestone and trees logs, not charcoal
	private static boolean IsExcluded(Material material)
	{
		return material == Material.COBBLESTONE
				|| material == Material.COBBLED_DEEPSLATE
				|| Tag.LOGS.isTagged(material);
	}
}
