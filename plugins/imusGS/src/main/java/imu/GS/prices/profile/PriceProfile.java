package imu.GS.prices.profile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A ready set of material prices and tags, read from a profile file.
 *
 * @param referencePrice    what diamond costs in this profile; applying it with another diamond price scales
 *                          every price by the same factor
 * @param craftedMultiplier an item priced from its recipe costs its ingredients times this
 * @param defaultPrice      price of anything the profile doesn't price and that has no crafting recipe
 * @param prices            material name or wildcard pattern (*) -> price, at the reference price
 * @param tags              tag -> material names or wildcard patterns
 * @param sellLimits        sell limit group -> its limit and materials
 */
public record PriceProfile(String name, double referencePrice, double craftedMultiplier, double defaultPrice,
                           Map<String, Double> prices, Map<String, List<String>> tags, Map<String, LimitGroup> sellLimits)
{
    /**
     * How much of a group of farmable items a shop buys at the full price, see {@link imu.GS.engine.pricing.SellLimit}.
     *
     * @param allowance     value sold at the full price, at the reference price
     * @param refillPerHour value of the allowance that comes back every hour, at the reference price
     * @param minFraction   the lowest the price drops to, 0.2 = 20% of the full price
     * @param items         material names or wildcard patterns
     */
    public record LimitGroup(double allowance, double refillPerHour, double minFraction, List<String> items)
    {
        public LimitGroup
        {
            items = List.copyOf(items);
            if (allowance <= 0)
                throw new IllegalArgumentException("A sell limit's allowance must be over 0");
        }
    }

    public PriceProfile
    {
        prices = new LinkedHashMap<>(prices);
        tags = new LinkedHashMap<>(tags);
        sellLimits = new LinkedHashMap<>(sellLimits);
        if (referencePrice <= 0)
            throw new IllegalArgumentException("reference-price must be over 0");
    }

    public PriceProfile(String name, double referencePrice, double craftedMultiplier, double defaultPrice,
                        Map<String, Double> prices, Map<String, List<String>> tags)
    {
        this(name, referencePrice, craftedMultiplier, defaultPrice, prices, tags, Map.of());
    }
}
