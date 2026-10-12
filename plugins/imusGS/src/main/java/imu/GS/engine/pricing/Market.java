package imu.GS.engine.pricing;

import imu.GS.engine.ItemKey;

import java.util.UUID;

/**
 * What the market does to prices today: the day's mood of an item's kind, and a shop's wanted requests.
 */
public interface Market
{
    Market NONE = new Market()
    {
        @Override
        public double moodFactor(ItemKey item)
        {
            return 1.0;
        }

        @Override
        public double wantedBonus(UUID shopId, ItemKey item)
        {
            return 0;
        }
    };

    /**
     * Today's price factor of the item, e.g. 1.07 = 7% up. Changes both buying and selling.
     */
    double moodFactor(ItemKey item);

    /**
     * How much more the shop pays for the item while it wants it, 0.5 = 50% more. 0 when it doesn't.
     */
    double wantedBonus(UUID shopId, ItemKey item);
}
