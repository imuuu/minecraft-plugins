package imu.GS.engine.customer;

/**
 * Where the customer is and what they may do, for listing restrictions.
 */
public interface CustomerContext
{
    /**
     * Time of the customer's world, 0-24000.
     */
    long worldTime();

    String world();

    double x();

    double y();

    double z();

    boolean hasPermission(String permission);
}
