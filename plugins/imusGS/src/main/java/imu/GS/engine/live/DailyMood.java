package imu.GS.engine.live;

import java.time.LocalDate;
import java.util.Locale;

/**
 * The market's mood: every day each kind of item (a material tag) is a little up or down in price. The factor
 * comes from the day, the tag and a seed, so it is the same all day and across restarts, and needs no saving.
 */
public final class DailyMood
{
    private final long _seed;
    private final double _maxSwing;

    /**
     * @param maxSwing 0.1 = prices move at most 10% up or down
     */
    public DailyMood(long seed, double maxSwing)
    {
        _seed = seed;
        _maxSwing = Math.max(0, Math.min(0.9, maxSwing));
    }

    /**
     * The tag's price factor on that day, between 1 - maxSwing and 1 + maxSwing, rounded to whole percents.
     */
    public double factor(String tag, LocalDate day)
    {
        long hash = _seed;
        hash = mix(hash ^ day.toEpochDay());
        hash = mix(hash ^ tag.toLowerCase(Locale.ROOT).hashCode());
        // 53 random bits -> [0, 1)
        double random = (hash >>> 11) * 0x1.0p-53;
        double factor = 1.0 + _maxSwing * (2 * random - 1);
        return Math.round(factor * 100) / 100.0;
    }

    // SplitMix64's finaliser: neighbouring days and tags get unrelated numbers
    private static long mix(long z)
    {
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }
}
