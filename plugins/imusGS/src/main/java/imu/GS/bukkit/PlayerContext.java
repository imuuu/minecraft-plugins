package imu.GS.bukkit;

import imu.GS.engine.customer.CustomerContext;
import org.bukkit.entity.Player;

/**
 * Where a player is and what they may do, read live from the player.
 */
public record PlayerContext(Player player) implements CustomerContext
{
    @Override
    public long worldTime()
    {
        return player.getWorld().getTime();
    }

    @Override
    public String world()
    {
        return player.getWorld().getName();
    }

    @Override
    public double x()
    {
        return player.getLocation().getX();
    }

    @Override
    public double y()
    {
        return player.getLocation().getY();
    }

    @Override
    public double z()
    {
        return player.getLocation().getZ();
    }

    @Override
    public boolean hasPermission(String permission)
    {
        return player.hasPermission(permission);
    }
}
