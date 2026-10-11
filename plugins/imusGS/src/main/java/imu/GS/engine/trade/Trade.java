package imu.GS.engine.trade;

import imu.GS.engine.ItemKey;

import java.util.UUID;

/**
 * One item that changed hands, for the transaction log. Selling a shulker box makes one trade per item in it.
 *
 * @param type    BUY = the customer bought from the shop
 * @param listing the listing bought from or sold to
 * @param price   money the customer paid (BUY) or got (SELL)
 */
public record Trade(Type type, UUID shopId, UUID customerId, String customerName, UUID listing, ItemKey item, int amount, double price)
{
    public enum Type
    {
        BUY,
        SELL
    }
}
