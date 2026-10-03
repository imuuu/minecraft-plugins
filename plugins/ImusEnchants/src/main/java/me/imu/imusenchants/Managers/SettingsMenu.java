package me.imu.imusenchants.Managers;

import imu.iAPI.Config.ConfigMenu;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

// The values of config.yml that can be changed in game with /ien config, /ia config or the
// button in /ien admin. Every change is read into CONSTANTS right away.
public class SettingsMenu
{
	public static final String PERMISSION = "imusenchants.admin";

	public static ConfigMenu Create(Plugin plugin, Runnable apply)
	{
		ConfigMenu menu = new ConfigMenu(plugin, "ImusEnchants", PERMISSION, apply);

		// Loot
		menu.addDouble("loot.slot-core-chance", "Loot: Slot Core chance", Material.AMETHYST_SHARD, 0, 1)
				.description("&7Per unopened loot chest")
				.note("now");
		menu.addDouble("loot.custom-book-chance", "Loot: custom book chance", Material.ENCHANTED_BOOK, 0, 1)
				.description("&7Per unopened loot chest, level I")
				.note("now");
		menu.addDouble("loot.cursed-book-chance", "Loot: cursed book chance", Material.WITHER_ROSE, 0, 1)
				.description("&7Share of found custom books")
				.description("&7that also carry a curse")
				.note("now");
		menu.addBoolean("loot.found-books-level-one", "Loot: found books level I", Material.BOOK)
				.description("&7Vanilla books in loot are level I")
				.note("now");
		menu.addBoolean("loot.mending-only-in-end", "Mending only in the End", Material.END_STONE)
				.description("&7Mending isn't found outside the End")
				.description("&7and isn't sold in the shop")
				.note("now");

		// Shop
		menu.addDouble("shop.custom-book-chance", "Shop: custom book chance", Material.KNOWLEDGE_BOOK, 0, 1)
				.description("&7Chance a bought book is a")
				.description("&7custom enchant (level I)")
				.note("now");
		for (int tier = 1; tier <= 3; tier++)
		{
			menu.addInt("shop.books.tier-" + tier + ".level", "Shop: tier " + tier + " book level", Material.EXPERIENCE_BOTTLE, 0, 1000)
					.description("&7XP level needed to buy")
					.note("now");
			menu.addInt("shop.books.tier-" + tier + ".cost", "Shop: tier " + tier + " book cost", Material.LAPIS_LAZULI, 0, 1000)
					.description("&7Levels taken")
					.note("now");
		}
		for (int tier = 1; tier <= 3; tier++)
		{
			menu.addInt("shop.boosters.tier-" + tier + ".level", "Shop: tier " + tier + " booster level", Material.EXPERIENCE_BOTTLE, 0, 1000)
					.description("&7XP level needed to buy")
					.note("now");
			menu.addInt("shop.boosters.tier-" + tier + ".cost", "Shop: tier " + tier + " booster cost", Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 0, 1000)
					.description("&7Levels taken")
					.note("now");
		}
		menu.addDouble("shop.tier-up.tier-1-book", "Shop: tier 1 book -> tier 2", Material.GOLD_NUGGET, 0, 1)
				.description("&7Chance the enchant is tier 2")
				.note("now");
		menu.addDouble("shop.tier-up.tier-2-book", "Shop: tier 2 book -> tier 2", Material.GOLD_INGOT, 0, 1)
				.description("&7Chance the enchant is tier 2")
				.description("&7instead of tier 1")
				.note("now");
		menu.addDouble("shop.tier-up.tier-3-book", "Shop: tier 3 book -> tier 3", Material.GOLD_BLOCK, 0, 1)
				.description("&7Chance the enchant is tier 3")
				.description("&7instead of tier 2")
				.note("now");
		menu.addDouble("shop.mixed.tool-side-chance", "Shop: \"more like tool\"", Material.IRON_PICKAXE, 0, 1)
				.description("&7Chance it gives a tool or weapon")
				.description("&7book, otherwise armor")
				.note("now");
		menu.addDouble("shop.mixed.tool-not-weapon-chance", "Shop: tool vs weapon", Material.IRON_SWORD, 0, 1)
				.description("&7Of those, chance for a tool")
				.description("&7book, otherwise weapon")
				.note("now");
		menu.addDouble("shop.mixed.armor-side-chance", "Shop: \"more like armor\"", Material.IRON_CHESTPLATE, 0, 1)
				.description("&7Chance it gives an armor book")
				.note("now");

		// Slots
		menu.addDouble("slots.unlock.start-chance", "Slots: neighbour chance", Material.GLASS_PANE, 0, 1)
				.description("&7Chance an open slot opens")
				.description("&7a neighbour on a new item")
				.note("new slotted items");
		menu.addDouble("slots.unlock.chance-multiplier", "Slots: chance per step", Material.GLASS, 0, 1)
				.description("&7The chance is multiplied by")
				.description("&7this for every step further")
				.note("new slotted items");
		menu.addInt("slots.unlock.max-depth", "Slots: max steps", Material.TINTED_GLASS, 0, 20)
				.note("new slotted items");
		menu.addBoolean("slots.swapper-animation", "Swapper animation", Material.RED_STAINED_GLASS_PANE)
				.note("now");
		menu.addInt("slots.swapper-animation-ticks", "Swapper animation ticks", Material.CLOCK, 1, 100)
				.note("now");

		// Items
		menu.addDouble("items.mending-per-level", "Mending per level", Material.EXPERIENCE_BOTTLE, 0, 10)
				.description("&7Part of a normal Mending")
				.description("&7repair, per Mending level")
				.note("now");
		menu.addBoolean("items.crossbow-infinity-uses-arrows", "Crossbow Infinity uses arrows", Material.CROSSBOW)
				.note("now");

		// Villagers
		menu.addBoolean("villagers.no-enchanted-books", "Villagers: no books", Material.LECTERN)
				.description("&7Villagers don't offer")
				.description("&7enchanted books")
				.note("new trades");
		menu.addBoolean("villagers.sell-boosters", "Villagers: sell boosters", Material.EMERALD)
				.description("&7A book trade also offers")
				.description("&7a booster for diamonds")
				.note("new trades");
		menu.addBoolean("villagers.gear-costs-boosters", "Villagers: gear for boosters", Material.SMITHING_TABLE)
				.description("&7Tool and armor trades cost")
				.description("&7a booster and a diamond")
				.note("new trades");

		return menu;
	}
}
