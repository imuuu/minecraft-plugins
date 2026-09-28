package me.imu.imusenchants.CustomEnchants;

import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Reads and writes the custom enchants of an item.
// Stored as "key:level,key:level" in the item's persistent data and shown as lore lines at the top.
public class CustomEnchantData
{
	private static final String PD_CUSTOM_ENCHANTS = "ie_custom_enchants";
	private static final String LORE_COLOR = "&7";

	public static Map<CustomEnchant, Integer> Get(ItemStack stack)
	{
		Map<CustomEnchant, Integer> enchants = new LinkedHashMap<>();

		String data = ItemUtils.GetPersistenData(stack, PD_CUSTOM_ENCHANTS, PersistentDataType.STRING);
		if (data == null || data.isEmpty())
			return enchants;

		for (String entry : data.split(","))
		{
			String[] parts = entry.split(":");
			if (parts.length < 2)
				continue;

			CustomEnchant enchant = CustomEnchantRegistry.Get(parts[0]);
			if (enchant == null)
				continue;

			try
			{
				enchants.put(enchant, Integer.parseInt(parts[1]));
			}
			catch (NumberFormatException ignored)
			{
			}
		}
		return enchants;
	}

	public static int GetLevel(ItemStack stack, CustomEnchant enchant)
	{
		Integer level = Get(stack).get(enchant);
		return level != null ? level : 0;
	}

	public static boolean HasAny(ItemStack stack)
	{
		String data = ItemUtils.GetPersistenData(stack, PD_CUSTOM_ENCHANTS, PersistentDataType.STRING);
		return data != null && !data.isEmpty();
	}

	// Replaces all custom enchants of the item and refreshes the lore
	public static void Set(ItemStack stack, Map<CustomEnchant, Integer> enchants)
	{
		if (!ItemUtils.IsValid(stack))
			return;

		RemoveLore(stack);

		if (enchants.isEmpty())
		{
			ItemUtils.RemovePersistenData(stack, PD_CUSTOM_ENCHANTS);
			return;
		}

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<CustomEnchant, Integer> entry : enchants.entrySet())
		{
			if (sb.length() > 0)
				sb.append(",");
			sb.append(entry.getKey().GetKey()).append(":").append(entry.getValue());
		}
		ItemUtils.SetPersistenData(stack, PD_CUSTOM_ENCHANTS, PersistentDataType.STRING, sb.toString());

		AddLore(stack);
	}

	public static void Add(ItemStack stack, CustomEnchant enchant, int level)
	{
		Map<CustomEnchant, Integer> enchants = Get(stack);
		enchants.put(enchant, level);
		Set(stack, enchants);
	}

	// Adds the enchant lines to the top of the lore, call RemoveLore first if they may already be there
	public static void AddLore(ItemStack stack)
	{
		Map<CustomEnchant, Integer> enchants = Get(stack);
		if (enchants.isEmpty())
			return;

		ItemMeta meta = stack.getItemMeta();
		List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

		List<String> enchantLines = new ArrayList<>();
		for (Map.Entry<CustomEnchant, Integer> entry : enchants.entrySet())
		{
			enchantLines.add(Metods.msgC(LORE_COLOR + entry.getKey().GetDisplayName(entry.getValue())));
		}

		lore.addAll(0, enchantLines);
		meta.setLore(lore);
		stack.setItemMeta(meta);
	}

	public static void RemoveLore(ItemStack stack)
	{
		ItemMeta meta = stack.getItemMeta();
		if (meta == null || !meta.hasLore())
			return;

		List<String> lore = meta.getLore();
		lore.removeIf(CustomEnchantData::IsEnchantLoreLine);
		meta.setLore(lore);
		stack.setItemMeta(meta);
	}

	private static boolean IsEnchantLoreLine(String line)
	{
		String text = ChatColor.stripColor(line);
		if (text == null)
			return false;

		for (CustomEnchant enchant : CustomEnchantRegistry.GetAll())
		{
			if (text.equals(enchant.GetName()))
				return true;

			if (!text.startsWith(enchant.GetName() + " "))
				continue;

			String level = text.substring(enchant.GetName().length() + 1);
			if (level.matches("[IVX]+"))
				return true;
		}
		return false;
	}
}
