package imu.GS.engine.live;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The flash deal of each shop, at most one per shop. Not thread safe; on the server it is only used on the main
 * thread.
 */
public class FlashDeals
{
    public interface Listener
    {
        /**
         * A deal started, ended, or one of its offers was bought.
         */
        void changed(UUID shopId);
    }

    private final Map<UUID, FlashDeal> _deals = new HashMap<>();
    private final List<Listener> _listeners = new CopyOnWriteArrayList<>();

    public void addListener(Listener listener)
    {
        _listeners.add(listener);
    }

    public void removeListener(Listener listener)
    {
        _listeners.remove(listener);
    }

    /**
     * The shop's deal while it is on, otherwise null.
     */
    public FlashDeal active(UUID shopId, long nowMillis)
    {
        FlashDeal deal = _deals.get(shopId);
        return deal != null && deal.isActive(nowMillis) ? deal : null;
    }

    /**
     * Every stored deal, ended ones too until {@link #removeEnded} drops them.
     */
    public Collection<FlashDeal> all()
    {
        return List.copyOf(_deals.values());
    }

    /**
     * Starts the deal, replacing the shop's earlier one.
     */
    public void start(FlashDeal deal)
    {
        _deals.put(deal.shopId(), deal);
        changed(deal.shopId());
    }

    public void remove(UUID shopId)
    {
        if (_deals.remove(shopId) != null)
            changed(shopId);
    }

    /**
     * Drops the deals whose time is over. Returns the shops whose deal ended.
     */
    public List<UUID> removeEnded(long nowMillis)
    {
        List<UUID> ended = new ArrayList<>();
        for (FlashDeal deal : List.copyOf(_deals.values()))
        {
            if (nowMillis >= deal.endMillis())
            {
                _deals.remove(deal.shopId());
                ended.add(deal.shopId());
            }
        }
        ended.forEach(this::changed);
        return ended;
    }

    /**
     * The player bought units of the offer. Call it only after checking {@link FlashOffer#canBuy}.
     */
    public void recordPurchase(FlashDeal deal, FlashOffer offer, UUID player, int amount)
    {
        offer.sell(player, amount);
        changed(deal.shopId());
    }

    private void changed(UUID shopId)
    {
        for (Listener listener : _listeners)
            listener.changed(shopId);
    }
}
