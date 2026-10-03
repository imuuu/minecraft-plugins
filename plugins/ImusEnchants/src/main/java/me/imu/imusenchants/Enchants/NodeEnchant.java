package me.imu.imusenchants.Enchants;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ItemUtils;
import imu.iAPI.Utilities.ItemUtils.DisplayNamePosition;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.CustomEnchantBook;
import me.imu.imusenchants.CustomEnchants.EnchantSettings;
import me.imu.imusenchants.Enums.TOUCH_TYPE;
import  me.imu.imusenchants.CONSTANTS;

public class NodeEnchant extends Node
{
	private final Map<Enchantment, Integer> _enchants = new HashMap<>();

	public NodeEnchant()
	{
	}

	public NodeEnchant(ItemStack stack)
	{
		SetLock(false);
		LoadEnchantsFromStack(stack);
	}

	public NodeEnchant(Enchantment enchantment, int level)
	{
		SetLock(false);
		ItemStack enchantedBook = createEnchantedBook(enchantment, level);
		LoadEnchantsFromStack(enchantedBook);
	}

	private ItemStack createEnchantedBook(Enchantment enchantment, int level)
	{
		ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
		EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
		if (meta != null)
		{
			meta.addStoredEnchant(enchantment, level, true);
		}
		book.setItemMeta(meta);
		return book;
	}

	@Override
	public boolean IsValidGUIitem(TOUCH_TYPE touchType, EnchantedItem enchantedItem, ItemStack stack)
	{
		if (CONSTANTS.ENCHANT_MATERIAL != stack.getType())
		{
			return false;
		}

		// Custom enchant books are handled by NodeCustomEnchant
		if (CustomEnchantBook.IsBook(stack))
		{
			return false;
		}

		if (touchType == TOUCH_TYPE.DROP && enchantedItem != null)
		{
			for (Enchantment enchant : ItemUtils.GetEnchantsWithLevels(stack).keySet())
			{
				if (!EnchantSettings.IsEnabled(enchant))
					return false;

				// An enchant that does nothing on this item (Protection on a sword) would only use up
				// a slot. Conflicting enchants are allowed on purpose, this only checks the item type.
				if (!enchant.canEnchantItem(new ItemStack(enchantedItem.GetItemStack().getType())))
				{
					if (enchantedItem.GetPlayer() != null)
						enchantedItem.GetPlayer().sendMessage(Metods.msgC("&cThis enchant doesn't work on this item!"));
					return false;
				}

				for (CustomEnchant custom : enchantedItem.GetCustomEnchantNodes())
				{
					if (custom.GetVanillaConflicts().contains(enchant))
						return false;
				}
			}
		}
		
		int count = enchantedItem.ContainsEnchant(stack);
		if (!CONSTANTS.ENABLE_MULTIPLE_SAME_ENCHANTS && count > 1 && touchType == TOUCH_TYPE.PICK_UP)
		{

			return false;
		}

		return CONSTANTS.ENABLE_MULTIPLE_SAME_ENCHANTS || count <= 0 || touchType != TOUCH_TYPE.DROP;
	}

	@Override
	public ItemStack GetGUIitemLoad(EnchantedItem enchantedItem)
	{
		return GetItemStack();
	}
	
	@Override
	public ItemStack GetGUIitemUnLoad(EnchantedItem enchantedItem, ItemStack stack)
	{
		return stack;
	}

	public void LoadEnchantsFromStack(ItemStack stack)
	{
		_enchants.clear();
		Map<Enchantment, Integer> enchants = ItemUtils.GetEnchantsWithLevels(stack);

		if (enchants.isEmpty())
			return;

		for (Map.Entry<Enchantment, Integer> enchantEntry : enchants.entrySet())
		{
			AddEnchantment(enchantEntry.getKey(), enchantEntry.getValue());
		}
	}

	public void AddEnchantment(Enchantment enchantment, int level)
	{
		_enchants.put(enchantment, level);
	}

	public Map<Enchantment, Integer> GetEnchantments()
	{
		return _enchants;
	}

	public ItemStack GetItemStack()
	{
		ItemStack itemStack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
		ItemUtils.AddTextToDisplayName(itemStack, "&b", DisplayNamePosition.FRONT);
		EnchantmentStorageMeta meta = (EnchantmentStorageMeta) itemStack.getItemMeta();
		
		for (Map.Entry<Enchantment, Integer> enchantEntry : _enchants.entrySet())
		{
			meta.addStoredEnchant(enchantEntry.getKey(), enchantEntry.getValue(), true);
		}
		itemStack.setItemMeta(meta);

		return itemStack;
	}

	@Override
	public String Serialize()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append(":").append(GetX());
		sb.append(":").append(GetY());
		sb.append(":").append(IsFrozen());
		for (Map.Entry<Enchantment, Integer> entry : _enchants.entrySet())
		{
			sb.append(":").append(entry.getKey().getKey()).append(",").append(entry.getValue());
		}
		return sb.toString();
	}

	@Override
	public void Deserialize(String data)
	{
		String[] parts = data.split(":");
		_x = Integer.parseInt(parts[1]);
		_y = Integer.parseInt(parts[2]);
		SetFrozen(Boolean.parseBoolean(parts[3]));
		// Keys are saved as "namespace:key,level" in a ':' separated string, so the namespace
		// arrives as its own part. Joined back here; it used to be dropped, which only worked
		// for minecraft: enchants and lost datapack ones.
		String namespace = null;
		for (int i = 4; i < parts.length; i++)
		{
			String[] enchantParts = parts[i].split(",");
			if (enchantParts.length < 2)
			{
				namespace = parts[i];
				continue;
			}

			String key = namespace == null ? enchantParts[0] : namespace + ":" + enchantParts[0];
			namespace = null;

			Enchantment enchant = Enchantment.getByKey(NamespacedKey.fromString(key));
			if (enchant == null)
				continue; // datapack removed
			_enchants.put(enchant, Integer.parseInt(enchantParts[1]));
		}
	}
}
