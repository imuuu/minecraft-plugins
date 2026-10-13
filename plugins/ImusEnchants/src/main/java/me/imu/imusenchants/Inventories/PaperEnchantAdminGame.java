package me.imu.imusenchants.Inventories;

import imu.iAPI.Config.ConfigMenu;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Paper.PaperIcons;
import imu.iAPI.Menus.Viewer;
import imu.iAPI.Utilities.InvUtil;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.CustomEnchantBook;
import me.imu.imusenchants.CustomEnchants.CustomEnchantRegistry;
import me.imu.imusenchants.CustomEnchants.EnchantSettings;
import me.imu.imusenchants.ImusEnchants;
import me.imu.imusenchants.Items.SlotCore;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * /ien admin's {@link EnchantAdminGame} on the server.
 */
public final class PaperEnchantAdminGame implements EnchantAdminGame
{
	private static final String VANILLA = "vanilla:";
	private static final String CUSTOM = "custom:";

	private static Enchantment Vanilla(String key)
	{
		if (!key.startsWith(VANILLA))
			return null;
		String name = key.substring(VANILLA.length());
		return EnchantSettings.GetAllVanilla().stream().filter(e -> e.getKey().getKey().equals(name)).findFirst().orElse(null);
	}

	private static CustomEnchant Custom(String key)
	{
		return key.startsWith(CUSTOM) ? CustomEnchantRegistry.Get(key.substring(CUSTOM.length())) : null;
	}

	private static void Give(Viewer viewer, ItemStack stack)
	{
		Player player = Bukkit.getPlayer(viewer.id());
		if (player != null)
			InvUtil.AddItemToInventoryOrDrop(player, stack);
	}

	@Override
	public List<Entry> vanilla()
	{
		List<Entry> entries = new ArrayList<>();
		for (Enchantment enchant : EnchantSettings.GetAllVanilla())
		{
			entries.add(new Entry(VANILLA + enchant.getKey().getKey(), PaperIcons.of(VanillaBook(enchant)), PrettyName(enchant), "&e",
					List.of("&7Max level: &f" + CustomEnchant.ToRoman(enchant.getMaxLevel()))));
		}
		return entries;
	}

	@Override
	public List<Entry> custom()
	{
		List<Entry> entries = new ArrayList<>();
		for (CustomEnchant enchant : CustomEnchantRegistry.GetAll())
		{
			List<String> details = new ArrayList<>(List.of(
					"&7" + enchant.GetDescription(enchant.GetMaxLevel()),
					"&7Max level: &f" + CustomEnchant.ToRoman(enchant.GetMaxLevel()),
					"&7For: &f" + enchant.GetAppliesToText()));
			if (enchant.IsCurse())
				details.add("&4Curse");
			entries.add(new Entry(CUSTOM + enchant.GetKey(), Icon.of(Material.ENCHANTED_BOOK), enchant.GetName(), enchant.IsCurse() ? "&c" : "&d", details));
		}
		return entries;
	}

	@Override
	public boolean isEnabled(String key)
	{
		Enchantment vanilla = Vanilla(key);
		if (vanilla != null)
			return EnchantSettings.IsEnabled(vanilla);
		CustomEnchant custom = Custom(key);
		return custom != null && EnchantSettings.IsEnabled(custom);
	}

	@Override
	public void setEnabled(String key, boolean enabled)
	{
		Enchantment vanilla = Vanilla(key);
		if (vanilla != null)
			EnchantSettings.SetEnabled(vanilla, enabled);
		CustomEnchant custom = Custom(key);
		if (custom != null)
			EnchantSettings.SetEnabled(custom, enabled);
	}

	@Override
	public void giveBook(Viewer viewer, String key)
	{
		Enchantment vanilla = Vanilla(key);
		if (vanilla != null)
			Give(viewer, VanillaBook(vanilla));
		CustomEnchant custom = Custom(key);
		if (custom != null)
			Give(viewer, CustomEnchantBook.Create(custom, custom.GetMaxLevel()));
	}

	@Override
	public Icon coreIcon()
	{
		return PaperIcons.of(SlotCore.Create(1));
	}

	@Override
	public void giveCores(Viewer viewer, int amount)
	{
		Give(viewer, SlotCore.Create(amount));
	}

	@Override
	public ConfigMenu settings()
	{
		return ImusEnchants.Instance.GetSettingsMenu();
	}

	private static ItemStack VanillaBook(Enchantment enchant)
	{
		ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
		EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
		meta.addStoredEnchant(enchant, enchant.getMaxLevel(), true);
		book.setItemMeta(meta);
		return book;
	}

	private static String PrettyName(Enchantment enchant)
	{
		StringBuilder sb = new StringBuilder();
		for (String word : enchant.getKey().getKey().split("_"))
		{
			if (sb.length() > 0)
				sb.append(' ');
			sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		}
		return sb.toString();
	}
}
