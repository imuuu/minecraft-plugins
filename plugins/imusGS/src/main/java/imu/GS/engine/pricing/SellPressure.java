package imu.GS.engine.pricing;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * How much of each group of farmable items has lately been sold to each shop, see {@link SellLimit}.
 * Not thread safe; on the server it is only used on the main thread.
 */
public class SellPressure
{
    public record Key(UUID owner, String group) {}

    /**
     * @param pressure value sold, as of {@code updatedMillis}
     */
    public record Entry(UUID owner, String group, double pressure, long updatedMillis) {}

    private record State(double pressure, long updatedMillis) {}

    private final Map<Key, State> _states = new HashMap<>();

    /**
     * The owner's (a shop's) pressure on the group now, with the refill since it last changed.
     */
    public double get(UUID owner, String group, SellLimit limit, long nowMillis)
    {
        State state = _states.get(new Key(owner, group));
        if (state == null)
            return 0;
        return limit.refill(state.pressure(), nowMillis - state.updatedMillis());
    }

    public void add(UUID owner, String group, SellLimit limit, double value, long nowMillis)
    {
        double pressure = get(owner, group, limit, nowMillis) + value;
        _states.put(new Key(owner, group), new State(pressure, nowMillis));
    }

    /**
     * Every stored pressure, to save it.
     */
    public List<Entry> entries()
    {
        List<Entry> entries = new ArrayList<>();
        for (Map.Entry<Key, State> entry : _states.entrySet())
            entries.add(new Entry(entry.getKey().owner(), entry.getKey().group(), entry.getValue().pressure(), entry.getValue().updatedMillis()));
        return entries;
    }

    public void restore(Entry entry)
    {
        _states.put(new Key(entry.owner(), entry.group()), new State(entry.pressure(), entry.updatedMillis()));
    }

    /**
     * Forgets everything sold to the owner (a shop): every group is back to its full allowance.
     */
    public void reset(UUID owner)
    {
        _states.keySet().removeIf(key -> key.owner().equals(owner));
    }

    /**
     * Drops entries that have refilled completely, so the saved data doesn't grow forever.
     */
    public void forgetRefilled(Map<String, SellLimit> limits, long nowMillis)
    {
        _states.entrySet().removeIf(entry ->
        {
            SellLimit limit = limits.get(entry.getKey().group());
            return limit == null || limit.refill(entry.getValue().pressure(), nowMillis - entry.getValue().updatedMillis()) <= 0;
        });
    }
}
