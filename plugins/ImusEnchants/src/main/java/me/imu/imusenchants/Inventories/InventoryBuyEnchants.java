package me.imu.imusenchants.Inventories;

import imu.iAPI.Enums.ENCHANTMENT_TIER;
import imu.iAPI.Enums.ITEM_CATEGORY;
import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Click;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.Viewer;
import me.imu.imusenchants.CONSTANTS;
import org.bukkit.Material;

/**
 * The enchanting table's shop: enchanted books ("more like tools" or "more like armor") and boosters in three
 * tiers each, paid with levels.
 */
public class InventoryBuyEnchants extends Menu
{
	static final int TOOLS = 2;
	static final int ARMOR = 6;
	static final int BOOSTERS = 4;
	static final int BACK = 45;

	private final EnchantShopGame _game;

	public InventoryBuyEnchants(EnchantShopGame game)
	{
		super("&3Buy &eEnchants", 6);
		_game = game;
	}

	@Override
	protected void render(Canvas canvas)
	{
		canvas.fill(Icon.filler(Material.BLACK_STAINED_GLASS_PANE));
		canvas.set(BACK, Icon.of(Material.RED_STAINED_GLASS_PANE, "&bGo Back"), this::Back);

		ENCHANTMENT_TIER[] tiers = { ENCHANTMENT_TIER.TIER_1, ENCHANTMENT_TIER.TIER_2, ENCHANTMENT_TIER.TIER_3 };
		for (int row = 0; row < 3; row++)
		{
			ENCHANTMENT_TIER tier = tiers[row];
			canvas.set(TOOLS + row * 9, BookIcon("More like Tools", tier), click -> BuyMoreLikeTool(click.viewer(), tier));
			canvas.set(ARMOR + row * 9, BookIcon("More like Armor", tier), click -> BuyMoreLikeArmor(click.viewer(), tier));
			canvas.set(BOOSTERS + row * 9, BoosterIcon(tier), click -> BuyBooster(click.viewer(), tier));
		}
	}

	private void Back(Click click)
	{
		if (click.session().canGoBack())
		{
			click.back();
			return;
		}
		click.close();
		_game.openTable(click.viewer());
	}

	private static Icon BookIcon(String kind, ENCHANTMENT_TIER tier)
	{
		String[] lore = switch (tier)
		{
			case TIER_1 -> new String[] { "&3Guaranteed &eTier &91 &3Enchant", "&9Low &3chance to &eTier &d2",
					"&3Buyable at &2" + CONSTANTS.CAP_FIRST_1_ENCHANTS + "L", "&3Small chance for a &dCustom Enchant" };
			case TIER_2 -> new String[] { "&3Guaranteed &eTier &91 &3Enchant", "&6High &3chance to &eTier &d2",
					"&3Buyable at &2" + CONSTANTS.CAP_FIRST_2_ENCHANTS + "L", "&3Small chance for a &dCustom Enchant" };
			default -> new String[] { "&3Guaranteed &eTier &d2 &3Enchant", "&6High &3chance to &eTier &63",
					"&3Buyable at &2" + CONSTANTS.CAP_FIRST_3_ENCHANTS + "L", "&3Small chance for a &dCustom Enchant" };
		};
		return Icon.of(CONSTANTS.ENCHANT_MATERIAL, "&eBuy Enchant (&b" + kind + "&e)", lore);
	}

	private static Icon BoosterIcon(ENCHANTMENT_TIER tier)
	{
		String directions = switch (tier)
		{
			case TIER_1 -> "&5one";
			case TIER_2 -> "&5two &3or &5more";
			default -> "&5four";
		};
		return Icon.of(CONSTANTS.BOOSTER_MATERIAL, "&eBuy Booster", "&3Guaranteed &6Booster",
				"&3Booster has " + directions + " &3power direcion",
				"&3Buyable at &2" + BoosterCap(tier) + "L",
				"&cCost &2" + CONSTANTS.GetCostBooster(tier) + "L").glow(true);
	}

	private static int BoosterCap(ENCHANTMENT_TIER tier)
	{
		return switch (tier)
		{
			case TIER_1 -> CONSTANTS.CAP_FIRST_1_BOOSTER;
			case TIER_2 -> CONSTANTS.CAP_FIRST_2_BOOSTER;
			default -> CONSTANTS.CAP_FIRST_3_BOOSTER;
		};
	}

	private void BuyBooster(Viewer viewer, ENCHANTMENT_TIER tier)
	{
		if (!HasLevels(viewer, Math.max(BoosterCap(tier), CONSTANTS.GetCostBooster(tier))))
			return;

		TakeLevels(viewer, CONSTANTS.GetCostBooster(tier));
		int directions = switch (tier)
		{
			case TIER_1 -> 1;
			case TIER_2 -> 2 + (int) (_game.roll() * 3);
			default -> 4;
		};
		_game.giveBooster(viewer, directions);
	}

	private boolean HasLevels(Viewer viewer, int required)
	{
		if (_game.isCreative(viewer))
			return true;
		if (_game.level(viewer) >= required)
			return true;
		viewer.message("&7Not Enough Levels");
		return false;
	}

	private void TakeLevels(Viewer viewer, int levels)
	{
		// Creative players skip the level check, so this can be more than they have
		_game.setLevel(viewer, Math.max(0, _game.level(viewer) - levels));
	}

	private ENCHANTMENT_TIER EnchantTier(ENCHANTMENT_TIER bookTier)
	{
		return switch (bookTier)
		{
			case TIER_1 -> RollChance(CONSTANTS.NORMAL_CHANCE_1_TO_BE_TIER_2) ? ENCHANTMENT_TIER.TIER_2 : ENCHANTMENT_TIER.TIER_1;
			case TIER_2 -> RollChance(CONSTANTS.HIGH_CHANCE_1_TO_BE_TIER_2) ? ENCHANTMENT_TIER.TIER_2 : ENCHANTMENT_TIER.TIER_1;
			case TIER_3 -> RollChance(CONSTANTS.HIGH_CHANCE_2_TO_BE_TIER_3) ? ENCHANTMENT_TIER.TIER_3 : ENCHANTMENT_TIER.TIER_2;
			default -> bookTier;
		};
	}

	private boolean RollChance(double chance)
	{
		return _game.roll() < chance;
	}

	private void BuyMoreLikeTool(Viewer viewer, ENCHANTMENT_TIER tier)
	{
		if (RollChance(CONSTANTS.BUY_MORE_LIKE_TOOL_CHANCE))
			BuyBook(viewer, tier, RollChance(CONSTANTS.BUY_MORE_LIKE_TOOL_TO_BE_TOOL_CHANCE) ? ITEM_CATEGORY.TOOL : ITEM_CATEGORY.WEAPON);
		else
			BuyBook(viewer, tier, ITEM_CATEGORY.ARMOR);
	}

	private void BuyMoreLikeArmor(Viewer viewer, ENCHANTMENT_TIER tier)
	{
		if (RollChance(CONSTANTS.BUY_MORE_LIKE_ARMOR_CHANCE))
			BuyBook(viewer, tier, ITEM_CATEGORY.ARMOR);
		else
			BuyBook(viewer, tier, RollChance(0.5) ? ITEM_CATEGORY.WEAPON : ITEM_CATEGORY.TOOL);
	}

	// The book is picked before the levels are taken: when every enchant of the category is
	// switched off in /ien admin there is nothing to give, and nothing is charged
	private void BuyBook(Viewer viewer, ENCHANTMENT_TIER bookTier, ITEM_CATEGORY category)
	{
		if (!HasLevels(viewer, Math.max(CONSTANTS.GetCapEnchant(bookTier), CONSTANTS.GetCostEnchant(bookTier))))
			return;

		if (!_game.giveBook(viewer, EnchantTier(bookTier), category))
		{
			viewer.message("&cNo enchants of this kind are available right now");
			return;
		}
		TakeLevels(viewer, CONSTANTS.GetCostEnchant(bookTier));
	}
}
