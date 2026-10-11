package imu.GS.engine.pricing;

/**
 * Lowers what a shop pays for an item once it has plenty of it. Below the soft cap every unit sells for the full
 * price; each unit above it is worth dropFactor times the one before, but never less than minPrice.
 *
 * @param softCap    units the shop takes at the full price
 * @param dropFactor 0.9 = every unit over the cap is worth 10% less than the one before
 * @param minPrice   lowest price of one unit
 */
public record Overflow(int softCap, double dropFactor, double minPrice)
{
    /**
     * Price of one unit sold to a shop that already has {@code stockBefore} of it.
     */
    public double unitPrice(double fullPrice, int stockBefore)
    {
        if (stockBefore < softCap)
            return fullPrice;
        double price = fullPrice * Math.pow(dropFactor, stockBefore - softCap);
        return Math.max(minPrice, price);
    }

    /**
     * Total for {@code amount} units sold one after another; each unit raises the stock of the next.
     */
    public double totalPrice(double fullPrice, int stockBefore, int amount)
    {
        double total = 0;
        for (int i = 0; i < amount; i++)
            total += unitPrice(fullPrice, stockBefore + i);
        return total;
    }
}
