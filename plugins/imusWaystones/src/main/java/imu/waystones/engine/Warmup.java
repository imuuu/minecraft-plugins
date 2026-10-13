package imu.waystones.engine;

import java.util.UUID;

/**
 * A teleport getting ready: the player has to stand still until it is done.
 *
 * @param from    the waystone they leave from
 * @param to      the waystone they go to
 * @param x       where the player stood when it began
 * @param maxMove how far they may move (blocks) before it is cancelled
 */
public record Warmup(UUID from, UUID to, String world, double x, double y, double z, long startMillis, long durationMillis,
                     double maxMove)
{
    public enum State
    {
        WAITING, DONE, MOVED
    }

    public State check(String nowWorld, double nx, double ny, double nz, long nowMillis)
    {
        double dx = nx - x, dy = ny - y, dz = nz - z;
        if (!world.equals(nowWorld) || Math.sqrt(dx * dx + dy * dy + dz * dz) > maxMove)
            return State.MOVED;
        return nowMillis - startMillis >= durationMillis ? State.DONE : State.WAITING;
    }

    /**
     * Whole seconds left, rounded up, for the countdown.
     */
    public long secondsLeft(long nowMillis)
    {
        return Math.max(0, (startMillis + durationMillis - nowMillis + 999) / 1000);
    }
}
