package me.imu.imusenchants.CustomEnchants;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Server wide on/off switches for vanilla and custom enchants, edited with /ien admin and
// saved to plugins/ImusEnchants/enchants.yml.
//
// Disabled custom enchant: does nothing on existing items and can't be obtained or applied.
// Disabled vanilla enchant: can't be obtained any more (shop, grid, loot, tables, anvils, fishing,
// villagers). Items that already have it keep it.
public class EnchantSettings
{
	private static final String PATH_VANILLA = "disabled-vanilla";
	private static final String PATH_CUSTOM = "disabled-custom";

	private static final Set<String> _disabledVanilla = new HashSet<>();
	private static final Set<String> _disabledCustom = new HashSet<>();
	private static File _file;

	public static void Load(Plugin plugin)
	{
		_file = new File(plugin.getDataFolder(), "enchants.yml");
		_disabledVanilla.clear();
		_disabledCustom.clear();

		if (!_file.exists())
			return;

		YamlConfiguration config = YamlConfiguration.loadConfiguration(_file);
		_disabledVanilla.addAll(config.getStringList(PATH_VANILLA));
		_disabledCustom.addAll(config.getStringList(PATH_CUSTOM));
	}

	public static void Save()
	{
		if (_file == null)
			return;

		YamlConfiguration config = new YamlConfiguration();
		config.set(PATH_VANILLA, new ArrayList<>(_disabledVanilla));
		config.set(PATH_CUSTOM, new ArrayList<>(_disabledCustom));
		try
		{
			_file.getParentFile().mkdirs();
			config.save(_file);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static boolean IsEnabled(Enchantment enchantment)
	{
		return enchantment != null && !_disabledVanilla.contains(enchantment.getKey().toString());
	}

	public static boolean IsEnabled(CustomEnchant enchant)
	{
		return enchant != null && !_disabledCustom.contains(enchant.GetKey());
	}

	public static void SetEnabled(Enchantment enchantment, boolean enabled)
	{
		if (enabled)
			_disabledVanilla.remove(enchantment.getKey().toString());
		else
			_disabledVanilla.add(enchantment.getKey().toString());
		Save();
	}

	public static void SetEnabled(CustomEnchant enchant, boolean enabled)
	{
		if (enabled)
			_disabledCustom.remove(enchant.GetKey());
		else
			_disabledCustom.add(enchant.GetKey());
		Save();
	}

	public static Set<Enchantment> GetDisabledVanilla()
	{
		Set<Enchantment> disabled = new HashSet<>();
		for (String key : _disabledVanilla)
		{
			Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString(key));
			if (enchantment != null)
				disabled.add(enchantment);
		}
		return disabled;
	}

	// Removes disabled vanilla enchants from an item or enchanted book. A book left without
	// enchants turns into a normal book. Returns true if something was removed.
	public static boolean StripDisabled(ItemStack stack)
	{
		if (stack == null || stack.getType().isAir() || _disabledVanilla.isEmpty())
			return false;

		ItemMeta meta = stack.getItemMeta();
		if (meta == null)
			return false;

		boolean changed = false;
		for (Enchantment enchantment : new ArrayList<>(meta.getEnchants().keySet()))
		{
			if (IsEnabled(enchantment))
				continue;
			meta.removeEnchant(enchantment);
			changed = true;
		}

		if (meta instanceof EnchantmentStorageMeta)
		{
			EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) meta;
			for (Enchantment enchantment : new ArrayList<>(bookMeta.getStoredEnchants().keySet()))
			{
				if (IsEnabled(enchantment))
					continue;
				bookMeta.removeStoredEnchant(enchantment);
				changed = true;
			}
		}

		if (!changed)
			return false;

		stack.setItemMeta(meta);

		if (stack.getType() == Material.ENCHANTED_BOOK && !CustomEnchantBook.IsBook(stack)
				&& ((EnchantmentStorageMeta) stack.getItemMeta()).getStoredEnchants().isEmpty())
		{
			stack.setType(Material.BOOK);
		}
		return true;
	}

	public static List<Enchantment> GetAllVanilla()
	{
		List<Enchantment> all = new ArrayList<>();
		for (Enchantment enchantment : Enchantment.values())
			all.add(enchantment);
		all.sort((a, b) -> a.getKey().getKey().compareTo(b.getKey().getKey()));
		return all;
	}
}
