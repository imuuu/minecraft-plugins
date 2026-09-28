package me.imu.imusenchants.CustomEnchants;

import imu.iAPI.Utilities.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.AbstractMap;
import java.util.Map;

// Enchanted book carrying one custom enchant. It has no vanilla stored enchants,
// so vanilla anvils and tables can't use it. A cursed book also gives its curse to the item.
public class CustomEnchantBook
{
	private static final String PD_CUSTOM_BOOK = "ie_custom_book";
	private static final String PD_CUSTOM_BOOK_CURSE = "ie_custom_book_curse";

	public static ItemStack Create(CustomEnchant enchant, int level)
	{
		return Create(enchant, level, null);
	}

	public static ItemStack Create(CustomEnchant enchant, int level, CustomEnchant curse)
	{
		ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
		String color = enchant.IsCurse() ? "&c" : "&d";
		ItemUtils.SetDisplayName(book, color + enchant.GetDisplayName(level));
		ItemUtils.AddLore(book, enchant.IsCurse() ? "&4Curse" : "&5Custom Enchant", true);
		ItemUtils.AddLore(book, "&7" + enchant.GetDescription(level), true);
		ItemUtils.AddLore(book, "&7Max level: &e" + CustomEnchant.ToRoman(enchant.GetMaxLevel()), true);
		ItemUtils.AddLore(book, "&7For: &f" + enchant.GetAppliesToText(), true);

		if (curse != null && curse != enchant)
		{
			ItemUtils.AddLore(book, "&cCursed: &4" + curse.GetName() + " &8(" + curse.GetDescription(1) + ")", true);
			ItemUtils.SetPersistenData(book, PD_CUSTOM_BOOK_CURSE, PersistentDataType.STRING, curse.GetKey());
		}

		ItemUtils.AddLore(book, "&3▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
		ItemUtils.AddLore(book, "&3Click it on an item to apply &eLevel I", true);
		ItemUtils.AddLore(book, "&3Higher levels: &6slots &3+ &6boosters", true);
		ItemUtils.SetPersistenData(book, PD_CUSTOM_BOOK, PersistentDataType.STRING, enchant.GetKey() + ":" + level);
		return book;
	}

	public static boolean IsBook(ItemStack stack)
	{
		if (stack == null || stack.getType() != Material.ENCHANTED_BOOK)
			return false;

		return ItemUtils.GetPersistenData(stack, PD_CUSTOM_BOOK, PersistentDataType.STRING) != null;
	}

	// Enchant and level of the book, or null if it isn't a (known) custom book
	public static Map.Entry<CustomEnchant, Integer> Read(ItemStack stack)
	{
		if (!IsBook(stack))
			return null;

		String data = ItemUtils.GetPersistenData(stack, PD_CUSTOM_BOOK, PersistentDataType.STRING);
		String[] parts = data.split(":");

		CustomEnchant enchant = CustomEnchantRegistry.Get(parts[0]);
		if (enchant == null)
			return null;

		int level = 1;
		if (parts.length > 1)
		{
			try
			{
				level = Integer.parseInt(parts[1]);
			}
			catch (NumberFormatException ignored)
			{
			}
		}
		return new AbstractMap.SimpleEntry<>(enchant, level);
	}

	// Curse attached to the book, or null
	public static CustomEnchant ReadCurse(ItemStack stack)
	{
		if (!IsBook(stack))
			return null;

		return CustomEnchantRegistry.Get(ItemUtils.GetPersistenData(stack, PD_CUSTOM_BOOK_CURSE, PersistentDataType.STRING));
	}
}
