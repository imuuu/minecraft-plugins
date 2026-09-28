package me.imu.imusenchants.Enchants;

import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.CustomEnchantBook;
import me.imu.imusenchants.CustomEnchants.CustomEnchantRegistry;
import me.imu.imusenchants.CustomEnchants.EnchantSettings;
import me.imu.imusenchants.Enums.TOUCH_TYPE;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

// Custom enchant book placed in the grid. Works like NodeEnchant: boosters raise its level
// and it is capped to the enchant's max level.
public class NodeCustomEnchant extends Node
{
	private CustomEnchant _enchant;
	private int _level = 1;
	private CustomEnchant _curse;

	public NodeCustomEnchant()
	{
	}

	public NodeCustomEnchant(ItemStack stack)
	{
		SetLock(false);
		Map.Entry<CustomEnchant, Integer> entry = CustomEnchantBook.Read(stack);
		if (entry != null)
		{
			_enchant = entry.getKey();
			_level = entry.getValue();
			_curse = CustomEnchantBook.ReadCurse(stack);
		}
	}

	public CustomEnchant GetCurse()
	{
		return _curse;
	}

	public CustomEnchant GetEnchant()
	{
		return _enchant;
	}

	public int GetLevel()
	{
		return _level;
	}

	@Override
	public boolean IsValidGUIitem(TOUCH_TYPE touchType, EnchantedItem enchantedItem, ItemStack stack)
	{
		Map.Entry<CustomEnchant, Integer> entry = CustomEnchantBook.Read(stack);
		if (entry == null)
			return false;

		if (touchType != TOUCH_TYPE.DROP || enchantedItem == null)
			return true;

		CustomEnchant enchant = entry.getKey();
		if (!EnchantSettings.IsEnabled(enchant) || !enchant.CanApplyToItem(enchantedItem.GetItemStack()))
			return false;

		for (Enchantment vanilla : enchantedItem.GetVanillaEnchantNodes())
		{
			if (enchant.GetVanillaConflicts().contains(vanilla))
				return false;
		}

		// Only one of each custom enchant and nothing it conflicts with
		for (CustomEnchant placed : enchantedItem.GetCustomEnchantNodes())
		{
			if (placed == enchant || CustomEnchantRegistry.AreConflicting(placed, enchant))
				return false;
		}
		return true;
	}

	@Override
	public ItemStack GetGUIitemLoad(EnchantedItem enchantedItem)
	{
		if (_enchant == null)
			return new ItemStack(Material.AIR);

		return CustomEnchantBook.Create(_enchant, _level, _curse);
	}

	@Override
	public ItemStack GetGUIitemUnLoad(EnchantedItem enchantedItem, ItemStack stack)
	{
		return stack;
	}

	@Override
	public String Serialize()
	{
		String data = this.getClass().getSimpleName() +
				":" + GetX() +
				":" + GetY() +
				":" + IsFrozen();

		if (_enchant != null)
		{
			data += ":" + _enchant.GetKey() + "," + _level;
			if (_curse != null)
				data += "," + _curse.GetKey();
		}

		return data;
	}

	@Override
	public void Deserialize(String data)
	{
		String[] parts = data.split(":");
		_x = Integer.parseInt(parts[1]);
		_y = Integer.parseInt(parts[2]);
		SetFrozen(Boolean.parseBoolean(parts[3]));

		if (parts.length < 5)
			return;

		String[] enchantParts = parts[4].split(",");
		_enchant = CustomEnchantRegistry.Get(enchantParts[0]);
		if (enchantParts.length > 1)
			_level = Integer.parseInt(enchantParts[1]);
		if (enchantParts.length > 2)
			_curse = CustomEnchantRegistry.Get(enchantParts[2]);
	}
}
