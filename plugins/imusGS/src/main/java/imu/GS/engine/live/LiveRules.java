package imu.GS.engine.live;

import imu.GS.engine.ItemKey;
import imu.GS.engine.pricing.PriceCalculator;
import imu.GS.engine.pricing.PriceRule;
import imu.GS.engine.shop.Shop;

/**
 * The maths of the live events: how likely a found item is, how rotating stock is picked, what a flash deal
 * costs, and adding stock to a shop.
 */
public final class LiveRules
{
    private LiveRules() {}

    /**
     * How often a rarer item is found or rotated in: 1 for something worth nothing, falling with the square root
     * of the value. With a scale of 5, cobblestone (0.5) is 0.95, iron (80) 0.24 and diamond (1000) 0.07.
     */
    public static double rarity(double value, double valueScale)
    {
        return 1.0 / Math.sqrt(1.0 + Math.max(0, value) / Math.max(1e-9, valueScale));
    }

    /**
     * Chance that an item a player gathered also turns up in the shop.
     */
    public static double foundChance(double value, double baseChance, double valueScale)
    {
        return Math.min(1.0, Math.max(0, baseChance) * rarity(value, valueScale));
    }

    /**
     * How many of an item a restock brings: about {@code budget} worth, at least 1 and at most {@code maxAmount}.
     */
    public static int restockAmount(double value, double budget, int maxAmount)
    {
        if (value <= 0)
            return maxAmount;
        return (int) Math.max(1, Math.min(maxAmount, Math.round(budget / value)));
    }

    /**
     * Adds units to the shop as if a player sold them, but never past {@code cap} units of the item in the shop.
     *
     * @return units added
     */
    public static int restock(Shop shop, ItemKey item, int amount, int cap, PriceRule newListingPrice)
    {
        int has = shop.countItems(item::equals);
        int added = Math.min(amount, cap - has);
        if (added <= 0)
            return 0;
        shop.receive(item, added, newListingPrice);
        return added;
    }

    /**
     * A flash deal's unit price: the normal price less the discount, but never so low that selling the item
     * back would pay more than it cost (in case the flash mark is lost, e.g. by placing a block).
     *
     * @param normalPrice   what the shop normally sells one unit for
     * @param sellBackPrice what the shop pays for one unit
     * @param discount      0.3 = 30% off
     */
    public static double flashPrice(double normalPrice, double sellBackPrice, double discount)
    {
        double discounted = normalPrice * (1.0 - Math.max(0, Math.min(1, discount)));
        double floor = sellBackPrice * 1.1;
        return PriceCalculator.round(Math.max(discounted, Math.max(floor, 0.01)));
    }
}
