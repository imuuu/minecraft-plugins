package me.imu.imusminiquests.Quests;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToIntFunction;

/**
 * Random rolls that a player's luck (0 = none, 1 = the most there is) tilts in their favour.
 * Luck comes from quest points, see ManagerQuestPoints.
 */
public final class Luck
{
    private Luck() {}

    /**
     * Picks one entry by weight. Luck flattens the weights towards the biggest one, so rare
     * entries come up more often: at luck 0 the weights are used as they are, at luck 1 every
     * entry is equally likely.
     */
    public static <T> T pick(List<T> entries, ToIntFunction<T> weight, double luck)
    {
        if (entries.isEmpty()) return null;

        double max = 0;
        for (T entry : entries) max = Math.max(max, weight.applyAsInt(entry));

        double total = 0;
        double[] effective = new double[entries.size()];
        for (int i = 0; i < entries.size(); i++)
        {
            double w = weight.applyAsInt(entries.get(i));
            effective[i] = w + luck * (max - w);
            total += effective[i];
        }

        double roll = ThreadLocalRandom.current().nextDouble(total);
        for (int i = 0; i < effective.length; i++)
        {
            roll -= effective[i];
            if (roll < 0) return entries.get(i);
        }
        return entries.getLast();
    }

    /**
     * A random number from 0 to 1 that leans towards 1 with luck. Its average is 0.5 at luck 0,
     * about 0.67 at luck 0.5 and 0.75 at luck 1.
     */
    public static double lean(double luck)
    {
        double u = ThreadLocalRandom.current().nextDouble();
        return 1 - Math.pow(1 - u, 1 + 2 * luck);
    }

    /** A whole number from min to max (both included), leaning towards max with luck. */
    public static int between(int min, int max, double luck)
    {
        if (max <= min) return min;
        return min + (int) Math.min(max - min, Math.floor(lean(luck) * (max - min + 1)));
    }

    /** A number from min to max, leaning towards max with luck. */
    public static double between(double min, double max, double luck)
    {
        if (max <= min) return min;
        return min + lean(luck) * (max - min);
    }
}
