package imu.waystones.engine;

/**
 * Who sees a waystone as a destination. The names are stored in the database.
 */
public enum Visibility
{
    /** Players who have touched it */
    BY_TOUCH,
    /** Everyone */
    TO_ALL,
    /** Nobody: it only sends players away */
    ONE_WAY,
    /** Everyone, but nobody can leave from it */
    TO_ALL_ONE_WAY;

    public boolean forEveryone()
    {
        return this == TO_ALL || this == TO_ALL_ONE_WAY;
    }

    public boolean isDestination()
    {
        return this != ONE_WAY;
    }

    public boolean canDepart()
    {
        return this != TO_ALL_ONE_WAY;
    }

    public Visibility next()
    {
        return values()[(ordinal() + 1) % values().length];
    }

    public static Visibility parse(String name)
    {
        try
        {
            return name == null ? BY_TOUCH : valueOf(name);
        } catch (IllegalArgumentException e)
        {
            return BY_TOUCH;
        }
    }
}
