package imu.imusEnchants.Enchants;

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
import imu.imusEnchants.Enums.TOUCH_TYPE;
import imu.imusEnchants.main.CONSTANTS;

public class NodeEnchant extends Node
{
	private Map<Enchantment, Integer> _enchants = new HashMap<>();

	public NodeEnchant()
	{
	};

	public NodeEnchant(ItemStack stack)
	{
		SetLock(false);
		LoadEnchantsFromStack(stack);
	}

	@Override
	public boolean IsValidGUIitem(TOUCH_TYPE touchType, EnchantedItem enchantedItem, ItemStack stack)
	{
		if (CONSTANTS.ENCHANT_MATERIAL != stack.getType())
		{
			return false;
		}
		
		int count = enchantedItem.ContainsEnchant(stack);
		if (!CONSTANTS.ENABLE_MULTIPLE_SAME_ENCHANTS && count > 1 && touchType == TOUCH_TYPE.PICK_UP)
		{

			return false;
		}
		
		if (!CONSTANTS.ENABLE_MULTIPLE_SAME_ENCHANTS && count > 0 && touchType == TOUCH_TYPE.DROP)
		{

			return false;
		}

		return true;
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
			sb.append(":").append(entry.getKey().getKey().toString()).append(",").append(entry.getValue());
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
		for (int i = 4; i < parts.length; i++)
		{
			String[] enchantParts = parts[i].split(",");
			if (enchantParts.length < 2)
			{
				// Handle the case where enchantParts does not have 2 elements
				// This might involve logging an error or skipping this entry
				continue;
			}

			Enchantment enchant = Enchantment.getByKey(NamespacedKey.fromString(enchantParts[0]));
			int level = Integer.parseInt(enchantParts[1]);
			_enchants.put(enchant, level);
		}
	}
}
