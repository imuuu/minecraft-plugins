package imu.waystones.engine;

import java.util.EnumMap;
import java.util.Map;

/**
 * The upgrade tiers one player has on one waystone. Tier 0 is no upgrade.
 */
public final class Panel
{
    private final EnumMap<UpgradeKind, Integer> _tiers = new EnumMap<>(UpgradeKind.class);

    public int tier(UpgradeKind kind)
    {
        return _tiers.getOrDefault(kind, 0);
    }

    public void set(UpgradeKind kind, int tier)
    {
        if (tier <= 0)
            _tiers.remove(kind);
        else
            _tiers.put(kind, tier);
    }

    public boolean isEmpty()
    {
        return _tiers.isEmpty();
    }

    public void clear()
    {
        _tiers.clear();
    }

    /**
     * Raises every tier to the other panel's where that is higher, as when an upgrade item is used.
     *
     * @return true when something rose
     */
    public boolean raiseTo(Panel other)
    {
        boolean rose = false;
        for (Map.Entry<UpgradeKind, Integer> entry : other._tiers.entrySet())
        {
            if (entry.getValue() > tier(entry.getKey()))
            {
                set(entry.getKey(), entry.getValue());
                rose = true;
            }
        }
        return rose;
    }

    public Map<UpgradeKind, Integer> tiers()
    {
        return Map.copyOf(_tiers);
    }
}
