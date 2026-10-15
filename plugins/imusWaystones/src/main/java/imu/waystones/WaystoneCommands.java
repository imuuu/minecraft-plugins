package imu.waystones;

import imu.iAPI.Menus.Paper.PaperMenus;
import imu.iAPI.Permissions.PaperPermissions;
import imu.waystones.engine.Destinations;
import imu.waystones.engine.Visibility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * /waystones (/iw): confirm a new waystone; admins: list, reload.
 */
final class WaystoneCommands implements TabExecutor
{
    private final ImusWaystones _plugin;
    /** Finished but not yet confirmed waystones: player to base block */
    private final Map<UUID, BlockSpot> _pending = new HashMap<>();

    WaystoneCommands(ImusWaystones plugin)
    {
        _plugin = plugin;
    }

    void setPending(Player player, BlockSpot base)
    {
        _pending.put(player.getUniqueId(), base);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        String sub = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        switch (sub)
        {
            case "confirm" ->
            {
                if (sender instanceof Player player)
                    confirm(player);
            }
            case "list" ->
            {
                if (admin(sender) && sender instanceof Player player)
                    PaperMenus.open(player, new ListMenu(_plugin.menuGame()));
            }
            case "reload" ->
            {
                if (admin(sender))
                {
                    _plugin.reload();
                    _plugin.send(sender, "reloaded");
                }
            }
            case "permissions" ->
            {
                if (sender instanceof Player player)
                    PaperPermissions.open(player, _plugin.getName());
            }
            default -> _plugin.send(sender, sender.hasPermission(ImusWaystones.ADMIN) ? "usage-admin" : "usage");
        }
        return true;
    }

    private boolean admin(CommandSender sender)
    {
        if (sender.hasPermission(ImusWaystones.ADMIN))
            return true;
        _plugin.send(sender, "no-permission");
        return false;
    }

    private void confirm(Player player)
    {
        BlockSpot spot = _pending.remove(player.getUniqueId());
        if (spot == null)
        {
            _plugin.send(player, "nothing-to-confirm");
            return;
        }
        if (!_plugin.waystones().isLoaded())
        {
            _plugin.send(player, "not-loaded");
            return;
        }
        World world = _plugin.getServer().getWorld(spot.world());
        Block base = world == null ? null : world.getBlockAt(spot.x(), spot.y(), spot.z());
        if (base == null || !_plugin.structure().isWhole(base))
        {
            _plugin.send(player, "not-whole");
            return;
        }
        if (_plugin.waystones().at(base) != null)
        {
            _plugin.send(player, "already");
            return;
        }
        Location center = base.getLocation().add(0.5, 0.5, 0.5);
        if (!player.getWorld().equals(world) || player.getLocation().distance(center) > 16)
        {
            _plugin.send(player, "too-far");
            return;
        }
        double min = _plugin.getConfig().getDouble("min-distance-between-own", 300);
        if (!player.hasPermission("imuswaystones.build-anywhere"))
        {
            double nearest = Destinations.tooCloseToOwn(player.getUniqueId(), spot.world(), spot.x(), spot.y(), spot.z(),
                    _plugin.waystones().stones(), min);
            if (nearest >= 0)
            {
                _plugin.send(player, "too-close", "min", (long) min, "distance", Math.round(nearest));
                return;
            }
        }

        Waystone ws = new Waystone(UUID.randomUUID(), spot.world(), spot.x(), spot.y(), spot.z());
        ws.owner = player.getUniqueId();
        ws.ownerName = player.getName();
        ws.name = _plugin.text("default-name", "player", player.getName());
        Material wall = base.getRelative(BlockFace.UP).getType();
        ws.icon = new ItemStack(wall.isItem() ? wall : Material.BLACKSTONE_WALL);
        ws.visibility = Visibility.BY_TOUCH;
        _plugin.waystones().add(ws);

        _plugin.send(player, "created", "name", ws.name);
        player.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.3f, 1.2f);
        world.spawnParticle(Particle.ENCHANT, center.clone().add(0, 1.5, 0), 120, 0.6, 1.2, 0.6, 0.5);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        List<String> options = new ArrayList<>();
        if (args.length == 1 && sender.hasPermission(ImusWaystones.ADMIN))
            options.addAll(List.of("list", "reload", "permissions"));
        String typed = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        return options.stream().filter(o -> o.startsWith(typed)).toList();
    }
}
