package imu.GS.engine.customer;

import java.util.UUID;

/**
 * The customers' money. Implemented over Vault on the server; tests use a fake.
 */
public interface Wallet
{
    double balance(UUID player);

    /**
     * Returns false, and takes nothing, when the payment fails.
     */
    boolean withdraw(UUID player, double amount);

    /**
     * Returns false when the payment fails.
     */
    boolean deposit(UUID player, double amount);
}
