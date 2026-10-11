package imu.GS.engine.trade;

import imu.GS.engine.customer.Customer;
import imu.GS.engine.shop.Shop;

/**
 * Told about every finished trade, e.g. to write the transaction log.
 */
public interface TradeListener
{
    void traded(Shop shop, Customer customer, Trade trade);
}
