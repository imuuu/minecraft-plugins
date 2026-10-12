package imu.hometele.engine;

/**
 * A teleport getting ready: the player has to stand still until it is done.
 *
 * @param start       where the player stood when it began
 * @param destination where they go
 * @param maxMove     how far they may move (blocks) before it is cancelled
 */
public record Warmup(Spot start, Spot destination, long startMillis, long durationMillis, double maxMove)
{
    public enum State
    {
        WAITING, DONE, MOVED
    }

    public State check(Spot now, long nowMillis)
    {
        if (start.distance(now) > maxMove)
            return State.MOVED;
        return nowMillis - startMillis >= durationMillis ? State.DONE : State.WAITING;
    }

    /**
     * How far along it is, 0 to 1, for the animation.
     */
    public double progress(long nowMillis)
    {
        if (durationMillis <= 0)
            return 1;
        return Math.min(1, Math.max(0, (nowMillis - startMillis) / (double) durationMillis));
    }
}
