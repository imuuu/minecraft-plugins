package imu.GS.command;

import imu.GS.GsService;
import imu.GS.engine.shop.Shop;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * /gsopenfor &lt;player&gt; &lt;shop name&gt;: opens a shop for a player. For the console and command NPCs, so it doesn't
 * go through ImusAPI's CommandHandler, which only takes players.
 */
public class OpenForCommand implements CommandExecutor, TabCompleter
{
    private final GsService _service;

    public OpenForCommand(GsService service)
    {
        _service = service;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (args.length < 2)
        {
            sender.sendMessage(ChatColor.RED + "/" + label + " <player> <shop name>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null)
        {
            sender.sendMessage(ChatColor.RED + "Player " + args[0] + " is not online");
            return true;
        }
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Shop shop = _service.getShop(name);
        if (shop == null)
        {
            sender.sendMessage(ChatColor.RED + "Shop not found: " + name);
            return true;
        }
        _service.openShop(target, shop);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
    {
        List<String> result = new ArrayList<>();
        if (args.length == 1)
        {
            for (Player player : Bukkit.getOnlinePlayers())
            {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase()))
                    result.add(player.getName());
            }
            return result;
        }
        // Shop names can have spaces: the next word of every shop name that matches so far
        String typed = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).toLowerCase();
        int word = args.length - 2;
        for (Shop shop : _service.getShops())
        {
            if (!shop.getName().toLowerCase().startsWith(typed))
                continue;
            String[] words = shop.getName().split(" ");
            if (word < words.length)
                result.add(words[word]);
        }
        return result;
    }
}
