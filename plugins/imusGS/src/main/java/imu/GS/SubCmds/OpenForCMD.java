package imu.GS.SubCmds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import imu.GS.Main.Main;
import imu.GS.ShopUtl.Shop;

/**
 * /gsopenfor &lt;player&gt; &lt;shop name&gt;: opens a shop for a player. NPC scripts run this as the server,
 * so players never need op or the gs.open.shop permission to use a shop.
 */
public class OpenForCMD implements CommandExecutor, TabCompleter
{
	private final Main _main;

	public OpenForCMD(Main main)
	{
		_main = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(args.length < 2)
		{
			sender.sendMessage(ChatColor.RED + "/" + label + " <player> <shop name>");
			return true;
		}

		Player target = Bukkit.getPlayerExact(args[0]);
		if(target == null)
		{
			sender.sendMessage(ChatColor.RED + "Player " + args[0] + " is not online");
			return true;
		}

		String shopName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		Shop shop = _main.get_shopManager().GetShop(shopName);
		if(shop == null)
		{
			sender.sendMessage(ChatColor.RED + "Shop not found: " + shopName);
			return true;
		}

		shop.Open(target);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		List<String> result = new ArrayList<>();
		if(args.length == 1)
		{
			for(Player player : Bukkit.getOnlinePlayers())
			{
				if(player.getName().toLowerCase().startsWith(args[0].toLowerCase())) result.add(player.getName());
			}
			return result;
		}

		// Shop names can have spaces: suggest the next word of every shop name that matches so far
		String typed = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).toLowerCase();
		int wordIndex = args.length - 2;
		for(Shop shop : _main.get_shopManager().GetShops())
		{
			if(!shop.GetName().toLowerCase().startsWith(typed)) continue;
			String[] words = shop.GetName().split(" ");
			if(wordIndex < words.length) result.add(words[wordIndex]);
		}
		return result;
	}
}
