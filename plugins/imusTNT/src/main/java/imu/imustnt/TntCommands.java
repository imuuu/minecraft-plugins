package imu.imustnt;

import imu.iAPI.Menus.Paper.PaperIcons;
import imu.iAPI.Menus.Paper.PaperMenus;
import imu.iAPI.Permissions.PaperPermissions;
import imu.iAPI.Utilities.InvUtil;
import imu.imustnt.engine.TntType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * /imustnt (/it): menu, give &lt;type&gt; [player] [amount], and permissions (who may use imusTNT, in imusAPI's menu).
 */
class TntCommands implements TabExecutor
{
    private final ImusTNT _plugin;

    TntCommands(ImusTNT plugin)
    {
        _plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        String sub = args.length == 0 ? "menu" : args[0].toLowerCase(Locale.ROOT);
        switch (sub)
        {
            // "inv" is the old imusTNT's name for the menu
            case "menu", "inv" ->
            {
                if (sender instanceof Player player)
                    PaperMenus.open(player, new TntMenu(_plugin.text("menu-title"), type -> PaperIcons.of(_plugin.item(type, 1)),
                            (viewer, type, amount) -> InvUtil.AddItemToInventoryOrDrop(player, _plugin.item(type, amount))));
                else
                    _plugin.send(sender, "usage");
            }
            case "give" -> give(sender, args);
            case "permissions", "perms" ->
            {
                if (sender instanceof Player player)
                    PaperPermissions.open(player, _plugin.getName());
                else
                    _plugin.send(sender, "usage");
            }
            default -> _plugin.send(sender, "usage");
        }
        return true;
    }

    private void give(CommandSender sender, String[] args)
    {
        TntType type = args.length > 1 ? TntType.parse(args[1]) : null;
        if (type == null)
        {
            _plugin.send(sender, "unknown-type", "types", String.join(", ", keys()));
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
        InvUtil.AddItemToInventoryOrDrop(to, _plugin.item(type, amount));
        _plugin.send(sender, "given", "amount", amount, "item", _plugin.getConfig().getString("items." + type.key() + ".name", type.key()), "player", to.getName());
    }

    private static List<String> keys()
    {
        return Arrays.stream(TntType.values()).map(TntType::key).toList();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        List<String> options = new ArrayList<>();
        if (args.length == 1)
            options.addAll(List.of("menu", "give", "permissions"));
        else if (args.length == 2 && args[0].equalsIgnoreCase("give"))
            options.addAll(keys());
        else if (args.length == 3 && args[0].equalsIgnoreCase("give"))
            Bukkit.getOnlinePlayers().forEach(p -> options.add(p.getName()));
        String typed = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        return options.stream().filter(o -> o.toLowerCase(Locale.ROOT).startsWith(typed)).toList();
    }
}
