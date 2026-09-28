package me.imu.imusenchants.Commands;

import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.InvUtil;
import me.imu.imusenchants.Items.SlotCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImusEnchantsCmd implements CommandExecutor, TabCompleter
{
	private static final String PERMISSION_ADMIN = "imusenchants.admin";

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (!sender.hasPermission(PERMISSION_ADMIN))
		{
			sender.sendMessage(Metods.msgC("&cNo permission!"));
			return true;
		}

		if (args.length == 0 || !args[0].equalsIgnoreCase("core"))
		{
			sender.sendMessage(Metods.msgC("&e/" + label + " core [player] [amount]"));
			return true;
		}

		Player target;
		if (args.length >= 2)
		{
			target = Bukkit.getPlayerExact(args[1]);
			if (target == null)
			{
				sender.sendMessage(Metods.msgC("&cPlayer not found: " + args[1]));
				return true;
			}
		}
		else if (sender instanceof Player)
		{
			target = (Player) sender;
		}
		else
		{
			sender.sendMessage(Metods.msgC("&e/" + label + " core <player> [amount]"));
			return true;
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
				return true;
			}
		}

		InvUtil.AddItemToInventoryOrDrop(target, SlotCore.Create(amount));
		sender.sendMessage(Metods.msgC("&dGave &6" + amount + " &dSlot Core(s) to &e" + target.getName()));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (!sender.hasPermission(PERMISSION_ADMIN))
			return Collections.emptyList();

		List<String> result = new ArrayList<>();
		if (args.length == 1)
		{
			if ("core".startsWith(args[0].toLowerCase()))
				result.add("core");
		}
		else if (args.length == 2 && args[0].equalsIgnoreCase("core"))
		{
			for (Player player : Bukkit.getOnlinePlayers())
			{
				if (player.getName().toLowerCase().startsWith(args[1].toLowerCase()))
					result.add(player.getName());
			}
		}
		return result;
	}
}
