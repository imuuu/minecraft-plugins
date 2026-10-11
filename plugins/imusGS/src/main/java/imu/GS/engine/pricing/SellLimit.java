package imu.GS.engine.pricing;

/**
 * How much of a group of farmable items (crops, iron...) one player can sell at the full price.
 *
 * <p>Every sale adds its full-price value to the player's pressure on the group, and the pressure goes down by
 * {@code refillPerHour} every hour. While the pressure is within {@code allowance} the player gets the full price;
 * above it the price falls in a straight line, reaching {@code minFraction} of the full price when the pressure is
 * twice the allowance. Values are money, so a hay bale uses up the allowance of nine wheat.</p>
 *
 * @param allowance     value a player can sell at the full price before prices drop
 * @param refillPerHour value of the allowance that comes back every hour
 * @param minFraction   the lowest share of the full price, 0.2 = 20%
 */
public record SellLimit(double allowance, double refillPerHour, double minFraction)
{
    public SellLimit
    {
        if (allowance <= 0)
            throw new IllegalArgumentException("allowance must be over 0");
        if (minFraction < 0 || minFraction > 1)
            throw new IllegalArgumentException("minFraction must be 0-1");
    }

    /**
     * Share of the full price a unit sells for at this pressure.
     */
    public double multiplier(double pressure)
    {
        if (pressure <= allowance)
            return 1.0;
        double over = (pressure - allowance) / allowance;
        return Math.max(minFraction, 1.0 - over * (1.0 - minFraction));
    }

    /**
     * Pressure after {@code millis} of refill.
     */
    public double refill(double pressure, long millis)
    {
        if (millis <= 0)
            return pressure;
        return Math.max(0, pressure - refillPerHour * millis / 3_600_000.0);
    }
}
