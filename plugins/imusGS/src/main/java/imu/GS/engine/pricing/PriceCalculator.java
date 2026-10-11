package imu.GS.engine.pricing;

import imu.GS.engine.ItemKey;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Prices of buying from and selling to a shop. Only reads; prices are rounded to cents.
 */
public class PriceCalculator
{
    private final PriceRules _rules;

    public PriceCalculator(PriceRules rules)
    {
        _rules = rules;
    }

    public PriceRules getRules()
    {
        return _rules;
    }

    // Players buying =================================================================================================

    /**
     * Price of one unit of the listing. Not for {@link PriceRule.Custom}, see {@link #customCost}.
     */
    public double buyUnitPrice(Shop shop, Listing listing)
    {
        double sellMultiplier = shop.getSettings().sellMultiplier();
        return switch (listing.getPrice())
        {
            case PriceRule.Material m -> _rules.value(listing.getItem()) * sellMultiplier;
            case PriceRule.Fixed fixed -> fixed.price();
            case PriceRule.Unique unique -> unique.price() * sellMultiplier;
            case PriceRule.Custom custom -> throw new IllegalArgumentException("A custom price is paid with customCost");
        };
    }

    public double buyPrice(Shop shop, Listing listing, int amount)
    {
        return round(buyUnitPrice(shop, listing) * amount);
    }

    /**
     * What {@code amount} units of a custom priced listing cost. Items are counted per bundle, rounding down.
     */
    public CustomCost customCost(Listing listing, int amount)
    {
        if (!(listing.getPrice() instanceof PriceRule.Custom custom))
            throw new IllegalArgumentException("Not a custom price: " + listing.getPrice());

        List<PriceRule.ItemCost> items = new ArrayList<>();
        for (PriceRule.ItemCost cost : custom.items())
            items.add(new PriceRule.ItemCost(cost.item(), cost.amount() * amount / custom.bundleSize()));
        return new CustomCost(items, round(custom.money() * amount / custom.bundleSize()));
    }

    public record CustomCost(List<PriceRule.ItemCost> items, double money) {}

    // Players selling ================================================================================================

    /**
     * What the shop pays for one unit before any overflow.
     */
    public double sellUnitPrice(Shop shop, ItemKey item)
    {
        double buyMultiplier = shop.getSettings().buyMultiplier();
        Double unique = _rules.uniquePrice(item);
        if (unique != null)
            return unique * _rules.condition(item) * buyMultiplier;
        return _rules.value(item) * buyMultiplier;
    }

    /**
     * What the shop pays for {@code amount} units. With an overflow every unit over the soft cap is worth less;
     * unique items never drop.
     */
    public double sellPrice(Shop shop, ItemKey item, int amount)
    {
        double fullPrice = sellUnitPrice(shop, item);
        Overflow overflow = _rules.uniquePrice(item) == null ? _rules.overflow(item) : null;
        if (overflow == null)
            return round(fullPrice * amount);
        return round(overflow.totalPrice(fullPrice, overflowStock(shop, item), amount));
    }

    /**
     * What the shop pays for everything in a shulker box.
     */
    public double shulkerPrice(Shop shop, Map<ItemKey, Integer> contents)
    {
        double total = 0;
        for (Map.Entry<ItemKey, Integer> entry : contents.entrySet())
            total += sellPrice(shop, entry.getKey(), entry.getValue());
        return round(total);
    }

    /**
     * The stock the overflow of this item counts: the whole group, or the first listing of exactly this item.
     */
    public int overflowStock(Shop shop, ItemKey item)
    {
        String group = _rules.overflowGroup(item);
        if (group != null)
            return shop.countItems(other -> group.equals(_rules.overflowGroup(other)));

        Listing listing = shop.findSameKind(item);
        return listing == null ? 0 : listing.getAmount();
    }

    /**
     * How a listing made from items sold to the shop is priced.
     */
    public PriceRule priceForSoldItem(ItemKey item)
    {
        Double unique = _rules.uniquePrice(item);
        return unique != null ? new PriceRule.Unique(unique) : PriceRule.MATERIAL;
    }

    public static double round(double price)
    {
        return Math.round(price * 100.0) / 100.0;
    }
}
