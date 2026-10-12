package imu.hometele;

import imu.hometele.engine.Cooldowns;
import imu.hometele.engine.Spot;
import imu.hometele.engine.Warmup;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Teleports that are getting ready: the player stands still while lava drips around them, then they are home.
 * Moving away or taking damage cancels it.
 */
public class Teleports implements Listener
{
    public static final String HOME = "home";
    /** A cancelled teleport: a short wait before trying again */
    public static final String CANCELLED = "home-cancelled";

    private final HomeTele _plugin;
    private final Cooldowns _cooldowns;
    private final Map<UUID, Warmup> _warmups = new HashMap<>();
    private BukkitTask _task;

    public Teleports(HomeTele plugin, Cooldowns cooldowns)
    {
        _plugin = plugin;
        _cooldowns = cooldowns;
    }

    public void start()
    {
        _task = Bukkit.getScheduler().runTaskTimer(_plugin, this::tick, 1, 1);
    }

    public void stop()
    {
        if (_task != null)
            _task.cancel();
        _warmups.clear();
    }

    public boolean isWaiting(Player player)
    {
        return _warmups.containsKey(player.getUniqueId());
    }

    /**
     * Starts the teleport to the destination; with no warmup it happens at once.
     */
    public void begin(Player player, Spot destination)
    {
        long now = System.currentTimeMillis();
        double seconds = Math.max(0, _plugin.getConfig().getDouble("warmup-seconds", 10));
        Warmup warmup = new Warmup(HomeStore.spot(player.getLocation()), destination, now, (long) (seconds * 1000),
                _plugin.getConfig().getDouble("cancel-distance", 1.0));
        if (seconds <= 0)
        {
            arrive(player, warmup);
            return;
        }
        _warmups.put(player.getUniqueId(), warmup);
        _plugin.send(player, "warmup", "seconds", trim(seconds));
    }

    public void cancel(Player player, String messageKey)
    {
        if (_warmups.remove(player.getUniqueId()) == null)
            return;
        _plugin.send(player, messageKey);
        _cooldowns.used(player.getUniqueId(), CANCELLED, System.currentTimeMillis());
    }

    private void tick()
    {
        if (_warmups.isEmpty())
            return;
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, Warmup> entry : List.copyOf(_warmups.entrySet()))
        {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null)
            {
                _warmups.remove(entry.getKey());
                continue;
            }
            Warmup warmup = entry.getValue();
            switch (warmup.check(HomeStore.spot(player.getLocation()), now))
            {
                case MOVED -> cancel(player, "cancelled-moved");
                case DONE ->
                {
                    _warmups.remove(entry.getKey());
                    arrive(player, warmup);
                }
                case WAITING -> animate(player, warmup, now);
            }
        }
    }

    private void arrive(Player player, Warmup warmup)
    {
        Location target = HomeStore.location(warmup.destination());
        if (target == null)
        {
            _plugin.send(player, "world-missing", "world", warmup.destination().world());
            return;
        }
        player.teleport(target);
        player.getWorld().spawnParticle(Particle.PORTAL, target.clone().add(0, 1, 0), 60, 0.4, 0.8, 0.4, 0.2);
        player.playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.1f);
        _plugin.send(player, "teleported");
        _cooldowns.used(player.getUniqueId(), HOME, System.currentTimeMillis());
    }

    /**
     * The old HomeTele's animation: lava spiralling up, then a ring spreading out.
     */
    private void animate(Player player, Warmup warmup, long now)
    {
        Location start = HomeStore.location(warmup.start());
        if (start == null)
            return;
        double progress = warmup.progress(now);
        double ticks = (now - warmup.startMillis()) / 50.0;
        if (progress < 0.3)
        {
            for (int i = 0; i < 2; i++)
            {
                double angle = Math.PI / 10 * ticks + i * 10;
                Location spot = start.clone().add(Math.cos(angle), Math.PI / 10 * ticks * 0.1, Math.sin(angle));
                player.getWorld().spawnParticle(Particle.DRIPPING_LAVA, spot, 1, 0, 0, 0, 0);
            }
        } else if (progress < 0.6 && ((long) ticks) % 4 == 0)
        {
            double radius = (progress - 0.3) / 0.3 * 2;
            for (int angle = 0; angle < 360; angle += 12)
            {
                double rad = Math.toRadians(angle);
                player.getWorld().spawnParticle(Particle.DRIPPING_LAVA, start.clone().add(radius * Math.cos(rad), 0.1, radius * Math.sin(rad)), 1, 0, 0, 0, 0);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e)
    {
        if (e.getEntity() instanceof Player player && isWaiting(player) && _plugin.getConfig().getBoolean("cancel-on-damage", true))
            cancel(player, "cancelled-damage");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e)
    {
        _warmups.remove(e.getPlayer().getUniqueId());
    }

    static String trim(double value)
    {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }
}
