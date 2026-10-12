package imu.GS.engine.trade;

/**
 * How a buy or sell went.
 *
 * @param amount units that changed hands, 0 when it failed
 * @param price  money paid or received, 0 when it failed
 */
public record TradeResult(Status status, int amount, double price)
{
    public enum Status
    {
        SUCCESS,
        /** Amount was 0 or negative, or under a custom price's bundle size */
        INVALID_AMOUNT,
        /** The listing is no longer in the shop */
        NO_LISTING,
        /** The listing has a custom price and must be bought with buyCustom, or the other way round */
        WRONG_PRICE_TYPE,
        /** The listing's restrictions (time, area, permission) don't allow this customer */
        NOT_AVAILABLE,
        NOT_ENOUGH_STOCK,
        NOT_ENOUGH_MONEY,
        /** The customer doesn't have the items to sell, or to pay a custom price with */
        NOT_ENOUGH_ITEMS,
        /** The shop doesn't buy from customers */
        SHOP_DOESNT_BUY,
        /** A shulker box that is empty, or holds nothing the shop buys */
        NOTHING_TO_SELL,
        /** The shop pays nothing for the item, and selling for nothing is off */
        ZERO_PRICE,
        /** The economy plugin refused the payment */
        PAYMENT_FAILED
    }

    public static TradeResult success(int amount, double price)
    {
        return new TradeResult(Status.SUCCESS, amount, price);
    }

    public static TradeResult fail(Status status)
    {
        return new TradeResult(status, 0, 0);
    }

    public boolean isSuccess()
    {
        return status == Status.SUCCESS;
    }
}
