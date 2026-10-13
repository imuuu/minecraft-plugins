package imu.iAPI.SubCommands;

import imu.iAPI.Utilities.TextUtils;
import imu.iAPI.CmdUtil.CmdData;
import imu.iAPI.Config.ConfigEntry;
import imu.iAPI.Config.ConfigMenu;
import imu.iAPI.Config.ConfigListMenu;
import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Menus.Paper.PaperMenus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * /ia config [plugin]: opens the settings menu of one plugin, or the list of all of them.
 * /ia config &lt;plugin&gt; list and /ia config &lt;plugin&gt; set &lt;setting&gt; &lt;value&gt; work from the console too.
 */
public class Sub_Cmd_OpenConfigMenus implements CommandInterface
{
	CmdData _data;

	public Sub_Cmd_OpenConfigMenus(CmdData data)
	{
		_data = data;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		ConfigMenu menu = args.length >= 2 ? ConfigMenu.getRegistered(args[1]) : null;
		if (args.length >= 2 && menu == null)
		{
			sender.sendMessage(TextUtils.Color("&cNo settings menu for '" + args[1] + "'"));
			return true;
		}

		if (args.length >= 3 && args[2].equalsIgnoreCase("list"))
		{
			list(sender, menu);
			return true;
		}
		if (args.length >= 3 && args[2].equalsIgnoreCase("set"))
		{
			set(sender, menu, args);
			return true;
		}

		if (!(sender instanceof Player player))
		{
			sender.sendMessage("Only players can open the settings menu. Use /ia config <plugin> list|set <setting> <value>");
			return true;
		}
		if (menu != null)
			menu.open(player);
		else
			PaperMenus.open(player, new ConfigListMenu());
		return true;
	}

	private static void list(CommandSender sender, ConfigMenu menu)
	{
		sender.sendMessage(TextUtils.Color("&9Settings of " + menu.getTitle() + ":"));
		for (ConfigEntry entry : menu.getEntries())
		{
			sender.sendMessage(TextUtils.Color("&7  " + entry.getPath() + " &e" + entry.format(entry.get(menu.getConfig()))
					+ (entry.hasRange() ? " &7(" + entry.rangeText() + ")" : "")));
		}
	}

	private static void set(CommandSender sender, ConfigMenu menu, String[] args)
	{
		if (!menu.canEdit(sender))
		{
			sender.sendMessage(TextUtils.Color("&cYou don't have permission to change these settings!"));
			return;
		}
		if (args.length < 5)
		{
			sender.sendMessage(TextUtils.Color("&c/ia config " + args[1] + " set <setting> <value>"));
			return;
		}
		ConfigEntry entry = menu.getEntry(args[3]);
		if (entry == null)
		{
			sender.sendMessage(TextUtils.Color("&cNo setting '" + args[3] + "', see /ia config " + args[1] + " list"));
			return;
		}
		try
		{
			Object value = entry.parse(String.join(" ", Arrays.copyOfRange(args, 4, args.length)));
			if (!menu.setValue(sender, entry, value))
				sender.sendMessage(TextUtils.Color("&7" + entry.getPath() + " is already " + entry.format(value)));
		} catch (IllegalArgumentException e)
		{
			sender.sendMessage(TextUtils.Color("&c" + e.getMessage()));
		}
	}

	@Override
	public void FailedMsg(CommandSender arg0, String arg1) {

	}
}
