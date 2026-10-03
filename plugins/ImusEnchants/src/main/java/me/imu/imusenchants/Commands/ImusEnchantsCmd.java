package me.imu.imusenchants.Commands;

import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.InvUtil;
import me.imu.imusenchants.CONSTANTS;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.CustomEnchantBook;
import me.imu.imusenchants.CustomEnchants.CustomEnchantRegistry;
import me.imu.imusenchants.CustomEnchants.EnchantSettings;
import me.imu.imusenchants.Enums.MATERIAL_SLOT_RANGE;
import me.imu.imusenchants.Inventories.InventoryEnchantAdmin;
import me.imu.imusenchants.Items.SlotCore;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ImusEnchantsCmd implements CommandExecutor, TabCompleter
{
	private static final String PERMISSION_ADMIN = "imusenchants.admin";

	private static final String HEADER = "&5&m          &r &d&lImus Enchants &7- &e%s &5&m          ";
	private static final String FOOTER = "&5&m                                                  ";

	private static final class HelpTopic
	{
		final String Title;
		final String Summary;
		final List<String> Lines;

		HelpTopic(String title, String summary, String... lines)
		{
			this(title, summary, Arrays.asList(lines));
		}

		HelpTopic(String title, String summary, List<String> lines)
		{
			Title = title;
			Summary = summary;
			Lines = lines;
		}
	}

	private final Map<String, HelpTopic> _topics = new LinkedHashMap<>();

	public ImusEnchantsCmd()
	{
		_topics.put("core", new HelpTopic("Slot Core", "How items get enchant slots",
				"&7Tools and armor are &fvanilla &7until a &dSlot Core &7is used on them.",
				"&6Where to find: &7unopened loot chests &8(&e" + Percent(CONSTANTS.SLOT_CORE_CHEST_CHANCE) + "&8 chance each)",
				"&6How to use: &7pick the core up with your cursor and",
				"&7click it on a tool or armor piece in your inventory.",
				"&c! &7The item is reset: &call enchants, name and lore are removed&7.",
				"&7Durability is kept. An item can only be cored once.",
				"&7The number of slots is random and depends on the material:",
				SlotRange("Netherite", MATERIAL_SLOT_RANGE.NETHERITE) + "  " + SlotRange("Elytra", MATERIAL_SLOT_RANGE.ELYTRA),
				SlotRange("Diamond", MATERIAL_SLOT_RANGE.DIAMOND) + "  " + SlotRange("Trident", MATERIAL_SLOT_RANGE.TRIDEN),
				SlotRange("Bow/Crossbow", MATERIAL_SLOT_RANGE.BOW) + "  " + SlotRange("Fishing rod", MATERIAL_SLOT_RANGE.FISH_ROD),
				SlotRange("Gold", MATERIAL_SLOT_RANGE.GOLD) + "  " + SlotRange("Iron", MATERIAL_SLOT_RANGE.IRON)
						+ "  " + SlotRange("Shield", MATERIAL_SLOT_RANGE.SHIELD),
				SlotRange("Stone", MATERIAL_SLOT_RANGE.STONE) + "  " + SlotRange("Leather", MATERIAL_SLOT_RANGE.LEATHER)
						+ "  " + SlotRange("Wood", MATERIAL_SLOT_RANGE.WOOD),
				"&7The amount stays hidden &a&k##&r &7until the item is opened in a table."));

		_topics.put("table", new HelpTopic("Enchanting Table", "Opening and using the slot grid",
				"&7The enchanting table works &fnormally&7, unless you",
				"&eright-click it while holding a slotted item&7.",
				"&7That opens the &5slot grid &7with your item already in place.",
				"&8■ &7Black glass &8= &7closed slot, it can't be used.",
				"&7Empty spots are your &aopen slots&7. Put pieces there:",
				"  &b▪ Enchanted books  &6▪ Boosters  &9▪ Swapper",
				"&7Press &eEnchant &7(enchanting table icon) to apply them.",
				"&7After enchanting, placed pieces become &cLOCKED &7and stay.",
				"&7Right-click a &cLOCKED &7piece to &cdestroy &7it and free the slot.",
				"&7Unlocked pieces can be taken back out before enchanting.",
				"&7Closing the table gives your item back."));

		_topics.put("enchants", new HelpTopic("Enchant Books", "How enchant levels are calculated",
				"&7Place an &benchanted book &7in an open slot to add its enchant.",
				"&7Every book gives &eLevel " + CONSTANTS.ENCHANT_FORCE_LEVEL + "&7, the book's own level doesn't matter.",
				"&7Raise the level with &6Boosters &7pointing at the book.",
				CONSTANTS.ENABLE_MULTIPLE_SAME_ENCHANTS
						? "&7The same enchant can be placed multiple times, levels add up."
						: "&7The same enchant can only be placed once per item.",
				"&7Level caps: &fFortune, Looting, Unbreaking, Protections &8→ &e4",
				"              &fSilk Touch, Infinity &8→ &e1",
				"&7Get books from the table's &eBuy Enchants &7shop or from loot."));

		_topics.put("custom", new HelpTopic("Custom Enchants", "New enchants: Tunnel, Timber, Lifesteal...", CustomEnchantLines()));

		_topics.put("boosters", new HelpTopic("Boosters", "Raising enchant levels",
				"&7A &6Booster &7has &5arrows &8(&5↑ ↓ ← →&8) &7and a &6⚡ power&7.",
				"&7Put it next to a book: each arrow pointing at a book",
				"&7adds the booster's &6⚡ power &7to that enchant's level.",
				"&7One booster with several arrows can boost several books,",
				"&7and one book can be boosted by several boosters.",
				"&7Tip: plan the layout &ebefore &7pressing Enchant, pieces lock after.",
				"&7Get boosters from the &eBuy Enchants &7shop or villagers.",
				"&7Villagers sell boosters instead of enchanted books,",
				"&7and some tool/armor trades cost a booster."));

		_topics.put("swapper", new HelpTopic("Swapper", "Moving a slot to a better spot",
				"&7Place your tool's &9main material &7in an open slot",
				"&8(&7e.g. a &bdiamond &7for a diamond pickaxe&8)&7. It becomes a &9Swapper&7.",
				"&eRight-click &7it to activate: its slot closes and the closed",
				"&7slot next to it &8(&7random direction&8) &7opens instead.",
				"&c! &7If that spot is the edge, a used slot or an open slot,",
				"&7the swap fails and &cthe slot is lost&7.",
				"&7Use it to connect slots so boosters can reach more books."));

		_topics.put("shop", new HelpTopic("Buy Enchants", "Buying books and boosters with XP",
				"&7Open it with the &ebookshelf &7button in the slot grid.",
				"&7Pay with &aXP levels&7. You need the &erequired level&7, but",
				"&7only the &ccost &7is taken.",
				"&bBooks &7(tool / weapon / armor):",
				ShopLine("Tier 1", CONSTANTS.CAP_FIRST_1_ENCHANTS, CONSTANTS.COST_FIRST_1_ENCHANTS,
						Percent(CONSTANTS.NORMAL_CHANCE_1_TO_BE_TIER_2) + " chance for tier 2"),
				ShopLine("Tier 2", CONSTANTS.CAP_FIRST_2_ENCHANTS, CONSTANTS.COST_FIRST_2_ENCHANTS,
						Percent(CONSTANTS.HIGH_CHANCE_1_TO_BE_TIER_2) + " chance for tier 2"),
				ShopLine("Tier 3", CONSTANTS.CAP_FIRST_3_ENCHANTS, CONSTANTS.COST_FIRST_3_ENCHANTS,
						Percent(CONSTANTS.HIGH_CHANCE_2_TO_BE_TIER_3) + " chance for tier 3"),
				"&6Boosters&7:",
				ShopLine("Tier 1", CONSTANTS.CAP_FIRST_1_BOOSTER, CONSTANTS.COST_FIRST_1_BOOSTER, "1 arrow"),
				ShopLine("Tier 2", CONSTANTS.CAP_FIRST_2_BOOSTER, CONSTANTS.COST_FIRST_2_BOOSTER, "2+ arrows"),
				ShopLine("Tier 3", CONSTANTS.CAP_FIRST_3_BOOSTER, CONSTANTS.COST_FIRST_3_BOOSTER, "4 arrows"),
				CONSTANTS.ENABLE_MENDING_FOUND_ONLY_END
						? "&7Mending can't be bought, it's only found in &5The End&7."
						: "&7Curses can't be bought."));

		_topics.put("items", new HelpTopic("Slotted Items", "Rules for items with slots",
				"&7Slotted items &ccan't &7be enchanted in a vanilla table,",
				"&7get books in an anvil or be used in a grindstone.",
				"&6Netherite upgrade: &7upgrading a slotted diamond item gives",
				"&7it more slots, rolled the next time it's opened in a table.",
				"&6Combining: &73 slotted items of the same type in a crafting",
				"&7grid make a new one with the " + CONSTANTS.CRAFTING_SLOT_CALCULATION.name().toLowerCase() + " slot amount.",
				"&6Repair: &7anvil + &bdiamonds&7, the cost rises with every repair.",
				"&6Mending: &7repairs &e" + Percent(CONSTANTS.MENDING_INCREASE_BY_LEVEL) + " &7of normal per level,",
				"&7boost it higher for faster repairs."));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (args.length == 0 || args[0].equalsIgnoreCase("help"))
		{
			if (args.length >= 2)
				SendTopic(sender, label, args[1].toLowerCase());
			else
				SendOverview(sender, label);
			return true;
		}

		// "core" is also a help topic: admins get the item, everyone else the topic
		// (admins can still read it with /ien help core)
		if (args[0].equalsIgnoreCase("core") && sender.hasPermission(PERMISSION_ADMIN))
		{
			GiveCore(sender, label, args);
			return true;
		}

		if (_topics.containsKey(args[0].toLowerCase()))
		{
			SendTopic(sender, label, args[0].toLowerCase());
			return true;
		}

		if (args[0].equalsIgnoreCase("core"))
		{
			GiveCore(sender, label, args);
			return true;
		}

		if (args[0].equalsIgnoreCase("admin"))
		{
			if (!sender.hasPermission(PERMISSION_ADMIN))
				sender.sendMessage(Metods.msgC("&cNo permission!"));
			else if (!(sender instanceof Player))
				sender.sendMessage(Metods.msgC("&cOnly players can open the admin menu"));
			else
				new InventoryEnchantAdmin().open((Player) sender);
			return true;
		}

		if (args[0].equalsIgnoreCase("book"))
		{
			GiveBook(sender, label, args);
			return true;
		}

		sender.sendMessage(Metods.msgC("&cUnknown command. Use &e/" + label + " help"));
		return true;
	}

	private static List<String> CustomEnchantLines()
	{
		List<String> lines = new ArrayList<>();
		lines.add("&7Custom enchants give tools new abilities.");
		lines.add("&6Get books: &7the &eBuy Enchants &7shop &8(&7small chance&8) &7and");
		lines.add("&7unopened loot chests &8(&e" + Percent(CONSTANTS.CUSTOM_BOOK_CHEST_CHANCE) + "&8)&7. Books are always &eLevel I&7.");
		lines.add("&6Normal tool: &7click the book on it in your inventory &8→ &eLevel I&7.");
		lines.add("&6Slotted tool: &7put the book in the table grid,");
		lines.add("&6Boosters &7raise it up to the enchant's max level.");
		lines.add("&cCursed books &7(" + Percent(CONSTANTS.CURSED_BOOK_CHANCE) + " of found ones) also give a curse.");
		lines.add("&7Hover an enchant for its levels:");
		return lines;
	}

	// Built when shown so enchants switched off in /ien admin drop out of the list
	private void SendCustomEnchantList(CommandSender sender, String label)
	{
		for (CustomEnchant enchant : CustomEnchantRegistry.GetAll())
		{
			if (!EnchantSettings.IsEnabled(enchant))
				continue;

			StringBuilder hover = new StringBuilder((enchant.IsCurse() ? "&c" : "&d") + enchant.GetName());
			for (int level = 1; level <= enchant.GetMaxLevel(); level++)
				hover.append("\n&e").append(CustomEnchant.ToRoman(level)).append(" &7").append(enchant.GetDescription(level));
			hover.append("\n&7For: &f").append(enchant.GetAppliesToText());

			SendClickable(sender,
					"  " + (enchant.IsCurse() ? "&c" : "&d") + "▸ " + enchant.GetName()
							+ " &8(&7max &e" + CustomEnchant.ToRoman(enchant.GetMaxLevel()) + "&8) &7" + enchant.GetAppliesToText(),
					"/" + label + " help custom",
					hover.toString());
		}
	}

	private void SendOverview(CommandSender sender, String label)
	{
		sender.sendMessage(Metods.msgC(String.format(HEADER, "Help")));
		sender.sendMessage(Metods.msgC("&7Give your gear &6enchant slots &7and build enchants"));
		sender.sendMessage(Metods.msgC("&7from books and boosters on a grid."));
		sender.sendMessage(Metods.msgC("&dQuick start: &7find a &dSlot Core &8→ &7click it on a tool"));
		sender.sendMessage(Metods.msgC("&8→ &7right-click an &eenchanting table &7while holding the tool."));
		sender.sendMessage("");
		sender.sendMessage(Metods.msgC("&7Topics &8(&7click or type &e/" + label + " help <topic>&8)&7:"));

		for (Map.Entry<String, HelpTopic> entry : _topics.entrySet())
		{
			SendClickable(sender,
					"  &e▸ &6" + entry.getKey() + " &8- &7" + entry.getValue().Summary,
					"/" + label + " help " + entry.getKey(),
					"&7Show &e" + entry.getValue().Title);
		}

		if (sender.hasPermission(PERMISSION_ADMIN))
		{
			sender.sendMessage("");
			sender.sendMessage(Metods.msgC("&cAdmin: &e/" + label + " core [player] [amount] &8- &7give Slot Cores"));
			sender.sendMessage(Metods.msgC("&cAdmin: &e/" + label + " book <enchant> [level] [player] &8- &7give a custom book"));
			sender.sendMessage(Metods.msgC("&cAdmin: &e/" + label + " admin &8- &7switch enchants on/off server wide"));
		}
		sender.sendMessage(Metods.msgC(FOOTER));
	}

	private void SendTopic(CommandSender sender, String label, String key)
	{
		HelpTopic topic = _topics.get(key);
		if (topic == null)
		{
			sender.sendMessage(Metods.msgC("&cUnknown topic &e" + key + "&c. Topics: &e" + String.join(", ", _topics.keySet())));
			return;
		}

		sender.sendMessage(Metods.msgC(String.format(HEADER, topic.Title)));
		for (String line : topic.Lines)
		{
			sender.sendMessage(Metods.msgC(line));
		}

		if (key.equals("custom"))
			SendCustomEnchantList(sender, label);

		SendClickable(sender, "&8« &7Back to all topics", "/" + label + " help", "&7Show all topics");
		sender.sendMessage(Metods.msgC(FOOTER));
	}

	private void SendClickable(CommandSender sender, String text, String command, String hover)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage(Metods.msgC(text));
			return;
		}

		TextComponent component = new TextComponent(TextComponent.fromLegacyText(Metods.msgC(text)));
		component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(Metods.msgC(hover))));
		((Player) sender).spigot().sendMessage(component);
	}

	private void GiveCore(CommandSender sender, String label, String[] args)
	{
		if (!sender.hasPermission(PERMISSION_ADMIN))
		{
			sender.sendMessage(Metods.msgC("&cNo permission!"));
			return;
		}

		Player target;
		if (args.length >= 2)
		{
			target = Bukkit.getPlayerExact(args[1]);
			if (target == null)
			{
				sender.sendMessage(Metods.msgC("&cPlayer not found: " + args[1]));
				return;
			}
		}
		else if (sender instanceof Player)
		{
			target = (Player) sender;
		}
		else
		{
			sender.sendMessage(Metods.msgC("&e/" + label + " core <player> [amount]"));
			return;
		}

		int amount = 1;
		if (args.length >= 3)
		{
			try
			{
				amount = Math.max(1, Math.min(64, Integer.parseInt(args[2])));
			}
			catch (NumberFormatException e)
			{
				sender.sendMessage(Metods.msgC("&cInvalid amount: " + args[2]));
				return;
			}
		}

		InvUtil.AddItemToInventoryOrDrop(target, SlotCore.Create(amount));
		sender.sendMessage(Metods.msgC("&dGave &6" + amount + " &dSlot Core(s) to &e" + target.getName()));
	}

	private void GiveBook(CommandSender sender, String label, String[] args)
	{
		if (!sender.hasPermission(PERMISSION_ADMIN))
		{
			sender.sendMessage(Metods.msgC("&cNo permission!"));
			return;
		}

		if (args.length < 2)
		{
			sender.sendMessage(Metods.msgC("&e/" + label + " book <enchant> [level] [player]"));
			return;
		}

		CustomEnchant enchant = CustomEnchantRegistry.Get(args[1]);
		if (enchant == null)
		{
			List<String> keys = new ArrayList<>();
			CustomEnchantRegistry.GetAll().forEach(e -> keys.add(e.GetKey()));
			sender.sendMessage(Metods.msgC("&cUnknown enchant &e" + args[1] + "&c. Enchants: &e" + String.join(", ", keys)));
			return;
		}

		int level = 1;
		if (args.length >= 3)
		{
			try
			{
				level = Math.max(1, Math.min(enchant.GetMaxLevel(), Integer.parseInt(args[2])));
			}
			catch (NumberFormatException e)
			{
				sender.sendMessage(Metods.msgC("&cInvalid level: " + args[2]));
				return;
			}
		}

		Player target;
		if (args.length >= 4)
		{
			target = Bukkit.getPlayerExact(args[3]);
			if (target == null)
			{
				sender.sendMessage(Metods.msgC("&cPlayer not found: " + args[3]));
				return;
			}
		}
		else if (sender instanceof Player)
		{
			target = (Player) sender;
		}
		else
		{
			sender.sendMessage(Metods.msgC("&e/" + label + " book <enchant> <level> <player>"));
			return;
		}

		InvUtil.AddItemToInventoryOrDrop(target, CustomEnchantBook.Create(enchant, level));
		sender.sendMessage(Metods.msgC("&dGave &e" + enchant.GetDisplayName(level) + " &dbook to &e" + target.getName()));
	}

	private static String SlotRange(String name, MATERIAL_SLOT_RANGE range)
	{
		return "&f" + name + " &e" + range.GetMinSlots() + "-" + range.GetMaxSlots();
	}

	private static String ShopLine(String tier, int requiredLevel, int cost, String info)
	{
		return "  &e" + tier + " &8| &7need &a" + requiredLevel + "L &8| &7cost &c" + cost + "L &8| &7" + info;
	}

	private static String Percent(double value)
	{
		return Math.round(value * 100) + "%";
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		List<String> options = new ArrayList<>();
		if (args.length == 1)
		{
			options.add("help");
			if (sender.hasPermission(PERMISSION_ADMIN))
			{
				options.add("core");
				options.add("book");
				options.add("admin");
			}
		}
		else if (args.length == 2 && args[0].equalsIgnoreCase("help"))
		{
			options.addAll(_topics.keySet());
		}
		else if (args.length == 2 && args[0].equalsIgnoreCase("core") && sender.hasPermission(PERMISSION_ADMIN))
		{
			for (Player player : Bukkit.getOnlinePlayers())
				options.add(player.getName());
		}
		else if (args.length == 2 && args[0].equalsIgnoreCase("book") && sender.hasPermission(PERMISSION_ADMIN))
		{
			CustomEnchantRegistry.GetAll().forEach(e -> options.add(e.GetKey()));
		}
		else if (args.length == 4 && args[0].equalsIgnoreCase("book") && sender.hasPermission(PERMISSION_ADMIN))
		{
			for (Player player : Bukkit.getOnlinePlayers())
				options.add(player.getName());
		}
		else
		{
			return Collections.emptyList();
		}

		String typed = args[args.length - 1].toLowerCase();
		options.removeIf(option -> !option.toLowerCase().startsWith(typed));
		return options;
	}
}
