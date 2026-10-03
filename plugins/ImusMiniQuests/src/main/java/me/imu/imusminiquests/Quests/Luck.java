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
     * The weight an entry counts with at this luck: weight ^ (1 - luck). Rare entries gain the
     * most, but gently: weights 600 and 5 (120 to 1) become about 24 and 2.2 (11 to 1) at luck
     * 0.5. At luck 0 the weights are used as they are, at luck 1 every entry is equally likely.
     */
    public static double effectiveWeight(int weight, double luck)
    {
        return weight <= 0 ? 0 : Math.pow(weight, 1 - Math.max(0, Math.min(1, luck)));
    }

    /**
     * Picks one entry by weight, with luck making the rare ones likelier (see effectiveWeight).
     */
    public static <T> T pick(List<T> entries, ToIntFunction<T> weight, double luck)
    {
        if (entries.isEmpty()) return null;

        double total = 0;
        double[] effective = new double[entries.size()];
        for (int i = 0; i < entries.size(); i++)
        {
            effective[i] = effectiveWeight(weight.applyAsInt(entries.get(i)), luck);
            total += effective[i];
        }
        if (total <= 0) return entries.getFirst();

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
