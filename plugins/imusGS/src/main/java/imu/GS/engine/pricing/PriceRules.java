package imu.GS.engine.pricing;

import imu.GS.engine.ItemKey;

/**
 * What items are worth, from the server's material prices, enchant values and unique items.
 * Implemented on the Bukkit side; tests use a fake.
 */
public interface PriceRules
{
    /**
     * Value of one unit before the shop's multipliers: material price (or smart price) plus enchants, lowered for
     * a damaged item. Never negative.
     */
    double value(ItemKey item);

    /**
     * The overflow of the item's material, or null when its price never drops.
     */
    Overflow overflow(ItemKey item);

    /**
     * Name of a group whose whole stock counts for the overflow, e.g. "DIAMOND_SWORD" for swords that differ only
     * by damage or enchants. Null when only exactly this item counts.
     */
    String overflowGroup(ItemKey item);

    /**
     * Price of the item as a unique item, or null when it isn't one.
     */
    Double uniquePrice(ItemKey item);

    /**
     * 1.0 for an undamaged item, lower for a damaged one. Unique prices are multiplied by this when a player sells.
     */
    double condition(ItemKey item);

    /**
     * What the player gets back for each unit they sell, e.g. an empty bucket for a water bucket. Null for nothing.
     */
    ItemKey leftover(ItemKey item);

    /**
     * True when no shop ever buys the item, e.g. one bought from a flash deal.
     */
    default boolean neverBuys(ItemKey item)
    {
        return false;
    }

    /**
     * The sell limit group of the item (crops, iron...), or null when selling it isn't limited.
     */
    default String sellLimitGroup(ItemKey item)
    {
        return null;
    }

    /**
     * The limit of a group in one shop, shared by everyone who sells to it, or null when the group has none.
     * Sellers compete for it: what one sells lowers the price for the next.
     */
    default SellLimit sellLimit(String group)
    {
        return null;
    }
}
