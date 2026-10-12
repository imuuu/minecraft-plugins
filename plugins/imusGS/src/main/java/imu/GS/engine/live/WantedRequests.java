package imu.GS.engine.live;

import imu.GS.engine.ItemKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Items shops want for a while: they pay a bonus for them. Not thread safe; on the server it is only used on the
 * main thread.
 */
public class WantedRequests
{
    /**
     * @param bonus 0.5 = the shop pays 50% more
     */
    public record Wanted(UUID shopId, ItemKey item, double bonus, long endMillis) {}

    private final Map<UUID, List<Wanted>> _wanted = new HashMap<>();

    /**
     * The shop's requests that are still on.
     */
    public List<Wanted> active(UUID shopId, long nowMillis)
    {
        List<Wanted> active = new ArrayList<>();
        for (Wanted wanted : _wanted.getOrDefault(shopId, List.of()))
        {
            if (nowMillis < wanted.endMillis())
                active.add(wanted);
        }
        return active;
    }

    /**
     * The bonus the shop pays for the item now, 0 when it doesn't want it.
     */
    public double bonus(UUID shopId, ItemKey item, long nowMillis)
    {
        double bonus = 0;
        for (Wanted wanted : active(shopId, nowMillis))
        {
            if (wanted.item().equals(item))
                bonus = Math.max(bonus, wanted.bonus());
        }
        return bonus;
    }

    public void add(Wanted wanted)
    {
        _wanted.computeIfAbsent(wanted.shopId(), id -> new ArrayList<>()).add(wanted);
    }

    public void clear(UUID shopId)
    {
        _wanted.remove(shopId);
    }

    /**
     * Drops requests whose time is over. Returns true when any ended.
     */
    public boolean removeEnded(long nowMillis)
    {
        boolean removed = false;
        for (List<Wanted> list : _wanted.values())
            removed |= list.removeIf(wanted -> nowMillis >= wanted.endMillis());
        _wanted.values().removeIf(List::isEmpty);
        return removed;
    }

    public Collection<Wanted> all()
    {
        List<Wanted> all = new ArrayList<>();
        _wanted.values().forEach(all::addAll);
        return all;
    }
}
