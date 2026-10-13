package imu.waystones.engine;

/**
 * The kind of world a waystone is in.
 */
public enum Dimension
{
    NORMAL, NETHER, END;

    /**
     * The Dimension upgrade tier it takes to travel between the two: the Nether needs 1, the End 2.
     */
    public static int tierNeeded(Dimension from, Dimension to)
    {
        if (from == to)
            return 0;
        if (from == END || to == END)
            return 2;
        return 1;
    }
}
