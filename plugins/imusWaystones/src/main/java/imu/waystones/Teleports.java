package imu.waystones;

import imu.waystones.engine.Panel;
import imu.waystones.engine.Rules;
import imu.waystones.engine.Warmup;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Teleports getting ready: the player stands still while a countdown runs, then they are at the other waystone.
 */
final class Teleports implements Listener
{
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final int[][] AROUND = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }, { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 },
            { 2, 0 }, { -2, 0 }, { 0, 2 }, { 0, -2 } };

    private final ImusWaystones _plugin;
    private final Waystones _waystones;
    private final Map<UUID, Warmup> _warmups = new HashMap<>();
    private final Map<UUID, Long> _shownSeconds = new HashMap<>();
    private BukkitTask _task;

    Teleports(ImusWaystones plugin, Waystones waystones)
    {
        _plugin = plugin;
        _waystones = waystones;
    }

    void start()
    {
        _task = Bukkit.getScheduler().runTaskTimer(_plugin, this::tick, 1, 2);
    }

    void stop()
    {
        if (_task != null)
            _task.cancel();
        _warmups.clear();
    }

    boolean isWaiting(Player player)
    {
        return _warmups.containsKey(player.getUniqueId());
    }

    /**
     * Admins and maxed out waystones go fast, without cooldowns or XP.
     */
    static boolean isFree(Player player, Waystone from)
    {
        return from.maxedOut || player.hasPermission("imuswaystones.bypass");
    }

    /**
     * How long until the player can leave from the waystone again, in milliseconds.
     */
    long cooldownLeft(Player player, Waystone from)
    {
        if (isFree(player, from))
            return 0;
        double seconds = _plugin.rules().cooldownSeconds(from.panel(player.getUniqueId()));
        return _waystones.cooldowns().remaining(player.getUniqueId(), from.id, seconds, System.currentTimeMillis());
    }

    void begin(Player player, Waystone from, Waystone to)
    {
        if (isWaiting(player))
            return;
        Rules rules = _plugin.rules();
        double seconds = isFree(player, from) ? 1 : rules.castSeconds(from.panel(player.getUniqueId()));
        Location at = player.getLocation();
        Warmup warmup = new Warmup(from.id, to.id, at.getWorld().getName(), at.getX(), at.getY(), at.getZ(),
                System.currentTimeMillis(), (long) (seconds * 1000), _plugin.getConfig().getDouble("cancel-distance", 0.5));
        _warmups.put(player.getUniqueId(), warmup);
        _shownSeconds.remove(player.getUniqueId());
    }

    private void tick()
    {
        if (_warmups.isEmpty())
            return;
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, Warmup> entry : List.copyOf(_warmups.entrySet()))
        {
            Player player = Bukkit.getPlayer(entry.getKey());
            Warmup warmup = entry.getValue();
            if (player == null)
            {
                _warmups.remove(entry.getKey());
                continue;
            }
            Location at = player.getLocation();
            switch (warmup.check(at.getWorld().getName(), at.getX(), at.getY(), at.getZ(), now))
            {
                case MOVED ->
                {
                    _warmups.remove(entry.getKey());
                    title(player, _plugin.text("cancelled"), "");
                }
                case DONE ->
                {
                    _warmups.remove(entry.getKey());
                    arrive(player, warmup);
                }
                case WAITING ->
                {
                    long left = warmup.secondsLeft(now);
                    if (!Long.valueOf(left).equals(_shownSeconds.put(entry.getKey(), left)))
                    {
                        title(player, _plugin.text("warmup-title", "seconds", left), _plugin.text("warmup-subtitle"));
                        player.playSound(at, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f, 0.6f + 0.1f * Math.min(10, left));
                    }
                }
            }
        }
    }

    private void arrive(Player player, Warmup warmup)
    {
        Waystone from = _waystones.get(warmup.from());
        Waystone to = _waystones.get(warmup.to());
        Structure structure = _plugin.structure();
        if (from == null || to == null || !to.enabled || !structure.isWhole(to) || !structure.isWhole(from))
        {
            _plugin.send(player, "destination-gone");
            return;
        }
        boolean free = isFree(player, from);
        Panel panel = from.panel(player.getUniqueId());
        int levels = free ? 0 : _plugin.rules().xpLevels(panel);
        if (player.getLevel() < levels)
        {
            _plugin.send(player, "not-enough-xp", "levels", levels);
            return;
        }
        Location target = landing(to);
        if (target == null)
        {
            _plugin.send(player, "destination-gone");
            return;
        }
        player.teleport(target);
        if (levels > 0)
            player.setLevel(player.getLevel() - levels);
        if (!free)
            _waystones.cooldowns().used(player.getUniqueId(), from.id, System.currentTimeMillis());
        target.getWorld().spawnParticle(Particle.PORTAL, target.clone().add(0, 1, 0), 60, 0.4, 0.8, 0.4, 0.2);
        player.playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.1f);
        title(player, to.name, "");
    }

    /**
     * A free spot next to the waystone to stand on, facing it; on top of it when there is none.
     */
    static Location landing(Waystone ws)
    {
        Block base = ws.base();
        if (base == null)
            return null;
        Location center = ws.center();
        for (int[] offset : AROUND)
        {
            for (int dy : new int[] { 0, 1, -1, 2 })
            {
                Block feet = base.getRelative(offset[0], dy, offset[1]);
                if (feet.isPassable() && !feet.isLiquid() && feet.getRelative(0, 1, 0).isPassable()
                        && feet.getRelative(0, -1, 0).getType().isSolid())
                {
                    Location spot = feet.getLocation().add(0.5, 0, 0.5);
                    spot.setDirection(center.toVector().subtract(spot.toVector()).setY(0));
                    return spot;
                }
            }
        }
        return base.getLocation().add(0.5, 3, 0.5);
    }

    private static void title(Player player, String title, String subtitle)
    {
        player.showTitle(Title.title(LEGACY.deserialize(title), LEGACY.deserialize(subtitle),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(1200), Duration.ofMillis(200))));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e)
    {
        _warmups.remove(e.getPlayer().getUniqueId());
        _shownSeconds.remove(e.getPlayer().getUniqueId());
    }
}
