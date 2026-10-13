package me.imu.imusenchants.Inventories;

import imu.iAPI.Enums.ENCHANTMENT_TIER;
import imu.iAPI.Enums.ITEM_CATEGORY;
import imu.iAPI.Menus.Viewer;
import imu.iAPI.Other.XpUtil;
import imu.iAPI.Utilities.EnchantUtil;
import imu.iAPI.Utilities.InvUtil;
import me.imu.imusenchants.CONSTANTS;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.CustomEnchantBook;
import me.imu.imusenchants.CustomEnchants.CustomEnchantRegistry;
import me.imu.imusenchants.CustomEnchants.EnchantSettings;
import me.imu.imusenchants.Enchants.NodeBooster;
import me.imu.imusenchants.Managers.ManagerEnchants;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * The enchant shop's {@link EnchantShopGame} on the server.
 */
public final class PaperEnchantShopGame implements EnchantShopGame
{
	private static final Random _random = new Random();

	private static Player Player(Viewer viewer)
	{
		return Objects.requireNonNull(Bukkit.getPlayer(viewer.id()), "The player is offline");
	}

	@Override
	public int level(Viewer viewer)
	{
		return Player(viewer).getLevel();
	}

	@Override
	public boolean isCreative(Viewer viewer)
	{
		return Player(viewer).getGameMode() == GameMode.CREATIVE;
	}

	@Override
	public void setLevel(Viewer viewer, int level)
	{
		XpUtil.SetPlayerLevel(Player(viewer), level);
	}

	@Override
	public double roll()
	{
		return _random.nextDouble();
	}

	@Override
	public boolean giveBook(Viewer viewer, ENCHANTMENT_TIER tier, ITEM_CATEGORY category)
	{
		ItemStack book = PickBook(tier, category);
		if (book == null)
			return false;
		InvUtil.AddItemToInventoryOrDrop(Player(viewer), book);
		return true;
	}

	// Sometimes the bought book is a custom enchant of the category instead, always level I.
	// If no vanilla enchant of the category is left, a custom one is given if there is any.
	private ItemStack PickBook(ENCHANTMENT_TIER tier, ITEM_CATEGORY category)
	{
		CustomEnchant custom = roll() < CONSTANTS.CUSTOM_BOOK_SHOP_CHANCE ? CustomEnchantRegistry.GetRandomForShop(category) : null;
		if (custom != null)
			return CustomEnchantBook.Create(custom, 1);

		Enchantment enchant = EnchantUtil.GetRandomEnchantment(category, tier, ExcludedEnchants());
		if (enchant != null)
			return EnchantUtil.GetEnchantedBook(enchant);

		custom = CustomEnchantRegistry.GetRandomForShop(category);
		return custom == null ? null : CustomEnchantBook.Create(custom, 1);
	}

	private static Set<Enchantment> ExcludedEnchants()
	{
		Set<Enchantment> excluded = new HashSet<>();
		if (CONSTANTS.ENABLE_MENDING_FOUND_ONLY_END)
			excluded.add(Enchantment.MENDING);
		excluded.add(Enchantment.VANISHING_CURSE);
		excluded.add(Enchantment.BINDING_CURSE);
		excluded.addAll(EnchantSettings.GetDisabledVanilla());
		return excluded;
	}

	@Override
	public void giveBooster(Viewer viewer, int directions)
	{
		InvUtil.AddItemToInventoryOrDrop(Player(viewer), new NodeBooster(1, directions).GetItemStack());
	}

	@Override
	public void openTable(Viewer viewer)
	{
		ManagerEnchants.Instance.OpenEnchantingInventory(Player(viewer));
	}
}
