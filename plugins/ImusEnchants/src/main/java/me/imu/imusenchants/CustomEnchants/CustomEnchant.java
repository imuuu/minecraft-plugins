package me.imu.imusenchants.CustomEnchants;

import imu.iAPI.Enums.ITEM_CATEGORY;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Set;

// Base for enchants that don't exist in vanilla. They are stored in the item's persistent data
// (CustomEnchantData) and shown as lore. To add one: extend this, override the event hooks it
// needs and register it in CustomEnchantRegistry.RegisterDefaults.
public abstract class CustomEnchant
{
	// Unique id stored on items and books, never change it once items exist with it
	public abstract String GetKey();

	public abstract String GetName();

	public abstract int GetMaxLevel();

	public abstract boolean CanApplyTo(Material material);

	// Human readable, shown in help and book lore, e.g. "Pickaxes, Shovels"
	public abstract String GetAppliesToText();

	public abstract String GetDescription(int level);

	// Which shop category may roll this enchant, null = not sold in the shop
	public ITEM_CATEGORY GetShopCategory()
	{
		return null;
	}

	// Keys of custom enchants that can't be on the same item as this one.
	// Checked both ways, so it's enough that one of the two lists the other.
	public Set<String> GetConflicts()
	{
		return Collections.emptySet();
	}

	// Higher priority enchants get the event first
	public int GetPriority()
	{
		return 0;
	}

	// Return true when the event was fully handled so lower priority enchants skip it
	public boolean OnBlockBreak(BlockBreakEvent event, ItemStack tool, int level)
	{
		return false;
	}

	public final boolean CanApplyToItem(ItemStack stack)
	{
		return stack != null && CanApplyTo(stack.getType());
	}

	public final String GetDisplayName(int level)
	{
		if (GetMaxLevel() <= 1)
			return GetName();

		return GetName() + " " + ToRoman(level);
	}

	public static String ToRoman(int number)
	{
		final int[] values = {10, 9, 5, 4, 1};
		final String[] numerals = {"X", "IX", "V", "IV", "I"};

		if (number <= 0)
			return String.valueOf(number);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++)
		{
			while (number >= values[i])
			{
				number -= values[i];
				sb.append(numerals[i]);
			}
		}
		return sb.toString();
	}
}
