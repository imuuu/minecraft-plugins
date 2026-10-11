package imu.GS.engine.pricing;

import imu.GS.engine.ItemKey;

import java.util.List;

/**
 * How a shop listing is priced for players buying it.
 */
public sealed interface PriceRule
{
    /**
     * The item's value from {@link PriceRules}, times the shop's sell multiplier.
     */
    record Material() implements PriceRule {}

    /**
     * A price the admin set for this listing. The shop's multipliers don't apply.
     */
    record Fixed(double price) implements PriceRule {}

    /**
     * A unique item's price, times the shop's sell multiplier.
     */
    record Unique(double price) implements PriceRule {}

    /**
     * Paid with items and money. Every {@code bundleSize} units cost {@code items} and {@code money}.
     */
    record Custom(List<ItemCost> items, double money, int bundleSize) implements PriceRule
    {
        public Custom
        {
            items = List.copyOf(items);
            if (bundleSize < 1)
                throw new IllegalArgumentException("bundleSize must be at least 1");
        }
    }

    record ItemCost(ItemKey item, int amount) {}

    PriceRule MATERIAL = new Material();
}
