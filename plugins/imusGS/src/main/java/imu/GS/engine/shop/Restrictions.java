package imu.GS.engine.shop;

import imu.GS.engine.customer.CustomerContext;

import java.util.List;

/**
 * Who can buy a listing, and when. Every restriction that is set must pass.
 *
 * @param sellTime    world times it is sold, null = always
 * @param areas       the player must be in one of these, empty = anywhere
 * @param permissions the player needs one of these, empty = anyone
 */
public record Restrictions(SellTime sellTime, List<Area> areas, List<String> permissions)
{
    public static final Restrictions NONE = new Restrictions(null, List.of(), List.of());

    public Restrictions
    {
        areas = List.copyOf(areas);
        permissions = List.copyOf(permissions);
    }

    public boolean allows(CustomerContext context)
    {
        if (sellTime != null && !sellTime.contains(context.worldTime()))
            return false;
        if (!areas.isEmpty() && areas.stream().noneMatch(area -> area.contains(context)))
            return false;
        return permissions.isEmpty() || permissions.stream().anyMatch(context::hasPermission);
    }

    public boolean isEmpty()
    {
        return sellTime == null && areas.isEmpty() && permissions.isEmpty();
    }

    /**
     * World ticks (0-24000) from start to end. A start after the end wraps over midnight: 13000-1000 is the night.
     */
    public record SellTime(int start, int end)
    {
        public boolean contains(long worldTime)
        {
            if (start == end)
                return true;
            if (start < end)
                return worldTime >= start && worldTime < end;
            return worldTime >= start || worldTime < end;
        }
    }

    /**
     * Within {@code radius} blocks of a point.
     */
    public record Area(String world, double x, double y, double z, int radius)
    {
        public boolean contains(CustomerContext context)
        {
            if (!world.equals(context.world()))
                return false;
            double dx = context.x() - x, dy = context.y() - y, dz = context.z() - z;
            return dx * dx + dy * dy + dz * dz < (double) radius * radius;
        }
    }
}
