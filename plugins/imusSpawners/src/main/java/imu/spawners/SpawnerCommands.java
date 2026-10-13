package imu.spawners;

import imu.iAPI.Utilities.InvUtil;
import imu.spawners.engine.Luck;
import imu.spawners.engine.Names;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * /spawners: your chance. Admins: give &lt;mob&gt; [player] [amount], stats &lt;player&gt;.
 */
class SpawnerCommands implements TabExecutor
{
    private static final String ADMIN = "imusspawners.admin";

    private final ImusSpawners _plugin;
    private final SpawnerItems _items;
    private final LuckStore _stats;

    SpawnerCommands(ImusSpawners plugin, SpawnerItems items, LuckStore stats)
    {
        _plugin = plugin;
        _items = items;
        _stats = stats;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        String sub = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        switch (sub)
        {
            case "" ->
            {
                if (sender instanceof Player player)
                    showStats(sender, player);
                else
                    _plugin.send(sender, "usage");
            }
            case "give" ->
            {
                if (admin(sender))
                    give(sender, args);
            }
            case "stats" ->
            {
                if (!admin(sender))
                    return true;
                OfflinePlayer target = args.length > 1 ? Bukkit.getOfflinePlayerIfCached(args[1]) : null;
                if (target == null)
                    _plugin.send(sender, "unknown-player", "player", args.length > 1 ? args[1] : "?");
                else
                    showStats(sender, target);
            }
            default -> _plugin.send(sender, "usage");
        }
        return true;
    }

    private boolean admin(CommandSender sender)
    {
        if (sender.hasPermission(ADMIN))
            return true;
        _plugin.send(sender, "no-permission");
        return false;
    }

    private void showStats(CommandSender to, OfflinePlayer player)
    {
        Luck.Stats stats = _stats.get(player.getUniqueId());
        _plugin.send(to, "stats", "player", player.getName(), "chance", ImusSpawners.percent(_plugin.luck().chance(stats)),
                "tries", stats.tries(), "got", stats.got());
    }

    private void give(CommandSender sender, String[] args)
    {
        EntityType type = args.length > 1 ? SpawnerItems.parse(args[1]) : null;
        if (type == null)
        {
            _plugin.send(sender, "unknown-type", "type", args.length > 1 ? args[1] : "?");
            return;
        }
        Player to = args.length > 2 ? Bukkit.getPlayerExact(args[2]) : sender instanceof Player p ? p : null;
        if (to == null)
        {
            _plugin.send(sender, "unknown-player", "player", args.length > 2 ? args[2] : "?");
            return;
        }
        int amount;
        try
        {
            amount = args.length > 3 ? Math.max(1, Math.min(64, Integer.parseInt(args[3]))) : 1;
        } catch (NumberFormatException e)
        {
            _plugin.send(sender, "usage");
            return;
        }
        InvUtil.AddItemToInventoryOrDrop(to, _items.create(type, null, amount));
        _plugin.send(sender, "given", "amount", amount, "type", Names.pretty(type.name()), "player", to.getName());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        List<String> options = new ArrayList<>();
        boolean admin = sender.hasPermission(ADMIN);
        if (args.length == 1 && admin)
            options.addAll(List.of("give", "stats"));
        else if (args.length == 2 && admin && args[0].equalsIgnoreCase("give"))
            SpawnerItems.spawnable().forEach(type -> options.add(type.name().toLowerCase(Locale.ROOT)));
        else if ((args.length == 3 && args[0].equalsIgnoreCase("give") || args.length == 2 && args[0].equalsIgnoreCase("stats")) && admin)
            Bukkit.getOnlinePlayers().forEach(p -> options.add(p.getName()));
        String typed = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        return options.stream().filter(o -> o.toLowerCase(Locale.ROOT).startsWith(typed)).toList();
    }
}
