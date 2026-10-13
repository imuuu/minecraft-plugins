package imu.spawners.engine;

/**
 * The chance of getting a natural spawner with silk touch. Every miss raises the player's own chance, and
 * getting one sets it back.
 *
 * @param baseChance      % everyone has
 * @param increasePerMiss % a miss adds to the player's chance
 * @param maxChance       the chance never goes above this %
 */
public record Luck(double baseChance, double increasePerMiss, double maxChance)
{
    /**
     * What a player has done so far.
     *
     * @param bonus % added to the base chance by misses since the last spawner
     * @param tries natural spawners broken with silk touch
     * @param got   spawners got from them
     */
    public record Stats(double bonus, int tries, int got)
    {
        public static final Stats NEW = new Stats(0, 0, 0);
    }

    /**
     * @param roll  what was rolled, 0 to 100
     * @param chance the player's chance when rolling
     * @param after the player's stats after the try
     */
    public record Try(boolean success, double roll, double chance, Stats after)
    {
    }

    public double chance(Stats stats)
    {
        return Math.max(0, Math.min(maxChance, baseChance + stats.bonus()));
    }

    /**
     * One try with a roll from 0 (inclusive) to 100 (exclusive): it succeeds when the roll is below the chance.
     */
    public Try roll(Stats stats, double roll)
    {
        double chance = chance(stats);
        if (roll < chance)
            return new Try(true, roll, chance, new Stats(0, stats.tries() + 1, stats.got() + 1));
        double bonus = Math.min(Math.max(0, maxChance - baseChance), stats.bonus() + increasePerMiss);
        return new Try(false, roll, chance, new Stats(bonus, stats.tries() + 1, stats.got()));
    }
}
