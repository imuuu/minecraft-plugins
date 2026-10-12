package imu.GS.engine.live;

import java.util.List;
import java.util.UUID;

/**
 * Limited-time offers of a shop. Items bought from a flash deal can't be sold back to a shop.
 *
 * @param offers in the order they are shown; the first ones are the teasers a peek shows
 */
public record FlashDeal(UUID id, UUID shopId, long startMillis, long endMillis, List<FlashOffer> offers)
{
    public FlashDeal
    {
        offers = List.copyOf(offers);
    }

    public boolean isActive(long nowMillis)
    {
        return nowMillis >= startMillis && nowMillis < endMillis;
    }

    public long millisLeft(long nowMillis)
    {
        return Math.max(0, endMillis - nowMillis);
    }

    public FlashOffer getOffer(UUID offerId)
    {
        for (FlashOffer offer : offers)
        {
            if (offer.getId().equals(offerId))
                return offer;
        }
        return null;
    }

    /**
     * True when every offer is sold out.
     */
    public boolean isSoldOut()
    {
        return offers.stream().allMatch(offer -> offer.getStock() <= 0);
    }
}
