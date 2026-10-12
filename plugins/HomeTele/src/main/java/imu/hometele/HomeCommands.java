package imu.hometele;

import imu.hometele.engine.Cooldowns;
import imu.hometele.engine.HomeBook;
import imu.hometele.engine.Spot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * /sethome, /home, /delhome, /homes, and for admins /homelist and /homeof.
 */
public class HomeCommands implements TabExecutor
{
    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_-]{1,16}");
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final HomeTele _plugin;
    private final HomeBook _book;
    private final Cooldowns _cooldowns;
    private final Teleports _teleports;

    public HomeCommands(HomeTele plugin, HomeBook book, Cooldowns cooldowns, Teleports teleports)
    {
        _plugin = plugin;
        _book = book;
        _cooldowns = cooldowns;
        _teleports = teleports;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player player))
        {
            sender.sendMessage("Only players can use this command");
            return true;
        }
        switch (command.getName().toLowerCase(Locale.ROOT))
        {
            case "sethome" -> setHome(player, args.length > 0 ? args[0] : null);
            case "home" -> home(player, args.length > 0 ? args[0] : null);
            case "delhome" -> delHome(player, args);
            case "homes" -> homes(player);
            case "homelist" -> homeList(player);
            case "homeof" -> homeOf(player, args);
            default ->
            {
                return false;
            }
        }
        return true;
    }

    /**
     * True when the player has to wait; tells them how long.
     */
    private boolean onCooldown(Player player, String action)
    {
        if (player.hasPermission("hometele.bypass"))
            return false;
        long now = System.currentTimeMillis();
        long left = action.equals(Teleports.HOME)
                ? Math.max(_cooldowns.remaining(player.getUniqueId(), Teleports.HOME, setting("teleport-cooldown-seconds", 300), now),
                           _cooldowns.remaining(player.getUniqueId(), Teleports.CANCELLED, setting("cancel-cooldown-seconds", 15), now))
                : _cooldowns.remaining(player.getUniqueId(), action, setting("sethome-cooldown-seconds", 1800), now);
        if (left <= 0)
            return false;
        _plugin.send(player, "cooldown", "seconds", (left + 999) / 1000);
        return true;
    }

    private void setHome(Player player, String name)
    {
        if (name != null && !NAME.matcher(name).matches())
        {
            _plugin.send(player, "bad-name");
            return;
        }
        if (onCooldown(player, "sethome"))
            return;
        int max = Math.max(1, _plugin.getConfig().getInt("max-homes", 1));
        String key = HomeBook.key(name);
        _book.setPlayerName(player.getUniqueId(), player.getName());
        switch (_book.set(player.getUniqueId(), key, HomeStore.spot(player.getLocation()), max))
        {
            case SET -> _plugin.send(player, "home-set", "name", key);
            case REPLACED -> _plugin.send(player, "home-moved", "name", key);
            case LIMIT_REACHED ->
            {
                _plugin.send(player, "limit", "max", max, "homes", String.join(", ", _book.names(player.getUniqueId())));
                return;
            }
        }
        _plugin.saveHomes();
        _cooldowns.used(player.getUniqueId(), "sethome", System.currentTimeMillis());
    }

    private double setting(String path, double fallback)
    {
        return _plugin.getConfig().getDouble(path, fallback);
    }

    private void home(Player player, String name)
    {
        if (_teleports.isWaiting(player))
        {
            _plugin.send(player, "already-waiting");
            return;
        }
        Spot spot = _book.get(player.getUniqueId(), name);
        if (spot == null)
        {
            List<String> names = _book.names(player.getUniqueId());
            if (names.isEmpty())
                _plugin.send(player, "no-home");
            else
                _plugin.send(player, "unknown-home", "name", name == null ? HomeBook.DEFAULT_NAME : name, "homes", String.join(", ", names));
            return;
        }
        if (onCooldown(player, Teleports.HOME))
            return;
        _teleports.begin(player, spot);
    }

    private void delHome(Player player, String[] args)
    {
        String name = args.length > 0 ? args[0] : null;
        if (name == null && _book.names(player.getUniqueId()).size() == 1)
            name = _book.names(player.getUniqueId()).getFirst();
        if (name == null || !_book.delete(player.getUniqueId(), name))
        {
            _plugin.send(player, "unknown-home", "name", name == null ? "?" : name, "homes", String.join(", ", _book.names(player.getUniqueId())));
            return;
        }
        _plugin.saveHomes();
        _plugin.send(player, "home-deleted", "name", HomeBook.key(name));
    }

    private void homes(Player player)
    {
        List<String> names = _book.names(player.getUniqueId());
        if (names.isEmpty())
        {
            _plugin.send(player, "no-home");
            return;
        }
        Component line = LEGACY.deserialize(_plugin.text("homes-title", "count", names.size(), "max", _plugin.getConfig().getInt("max-homes", 1)));
        for (String name : names)
        {
            line = line.append(Component.text(" "))
                    .append(LEGACY.deserialize("&b[" + name + "]")
                            .clickEvent(ClickEvent.runCommand("/home " + name))
                            .hoverEvent(HoverEvent.showText(LEGACY.deserialize(_plugin.text("click-to-go")))));
        }
        player.sendMessage(line);
    }

    private void homeList(Player player)
    {
        if (!player.hasPermission("hometele.admin"))
        {
            _plugin.send(player, "no-permission");
            return;
        }
        if (_book.owners().isEmpty())
        {
            _plugin.send(player, "nobody-has-homes");
            return;
        }
        _plugin.send(player, "homelist-title", "count", _book.owners().size());
        for (UUID owner : _book.owners())
        {
            Component line = LEGACY.deserialize("&7" + _book.playerName(owner) + ":");
            for (String name : _book.names(owner))
            {
                line = line.append(Component.text(" "))
                        .append(LEGACY.deserialize("&b[" + name + "]")
                                .clickEvent(ClickEvent.runCommand("/homeof " + owner + " " + name))
                                .hoverEvent(HoverEvent.showText(LEGACY.deserialize(_plugin.text("click-to-go")))));
            }
            player.sendMessage(line);
        }
    }

    /**
     * /homeof &lt;player or uuid&gt; [home]: an admin goes to someone's home right away.
     */
    private void homeOf(Player player, String[] args)
    {
        if (!player.hasPermission("hometele.admin"))
        {
            _plugin.send(player, "no-permission");
            return;
        }
        if (args.length == 0)
        {
            _plugin.send(player, "homeof-usage");
            return;
        }
        UUID owner = owner(args[0]);
        Spot spot = owner == null ? null : _book.get(owner, args.length > 1 ? args[1] : null);
        Location target = spot == null ? null : HomeStore.location(spot);
        if (target == null)
        {
            _plugin.send(player, "unknown-home", "name", args.length > 1 ? args[1] : args[0],
                    "homes", owner == null ? "" : String.join(", ", _book.names(owner)));
            return;
        }
        player.teleport(target);
        _plugin.send(player, "teleported");
    }

    /**
     * A player by uuid, online name or the name their homes were saved with.
     */
    private UUID owner(String text)
    {
        try
        {
            return UUID.fromString(text);
        } catch (IllegalArgumentException ignored)
        {
        }
        Player online = Bukkit.getPlayerExact(text);
        if (online != null)
            return online.getUniqueId();
        for (UUID owner : _book.owners())
        {
            if (_book.playerName(owner).equalsIgnoreCase(text))
                return owner;
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player player) || args.length == 0)
            return List.of();
        String typed = args[args.length - 1].toLowerCase(Locale.ROOT);
        List<String> options = new ArrayList<>();
        switch (command.getName().toLowerCase(Locale.ROOT))
        {
            case "home", "delhome" ->
            {
                if (args.length == 1)
                    options.addAll(_book.names(player.getUniqueId()));
            }
            case "homeof" ->
            {
                if (!player.hasPermission("hometele.admin"))
                    break;
                if (args.length == 1)
                    _book.owners().forEach(owner -> options.add(_book.playerName(owner)));
                else if (args.length == 2)
                {
                    UUID owner = owner(args[0]);
                    if (owner != null)
                        options.addAll(_book.names(owner));
                }
            }
            default ->
            {
            }
        }
        return options.stream().filter(o -> o.toLowerCase(Locale.ROOT).startsWith(typed)).toList();
    }
}
