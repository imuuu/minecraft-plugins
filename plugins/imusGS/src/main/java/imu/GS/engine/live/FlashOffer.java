package imu.GS.engine.live;

import imu.GS.engine.ItemKey;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * One item of a flash deal: a limited stock at a set price, and at most {@code perPlayerLimit} units per player.
 */
public class FlashOffer
{
    private final UUID _id;
    private final ItemKey _item;
    private final double _unitPrice;
    private final int _perPlayerLimit;
    private int _stock;
    private final Map<UUID, Integer> _bought = new HashMap<>();

    /**
     * @param item the item as the buyer gets it, marked as a flash deal item
     */
    public FlashOffer(UUID id, ItemKey item, int stock, double unitPrice, int perPlayerLimit)
    {
        _id = id;
        _item = item;
        _stock = Math.max(0, stock);
        _unitPrice = unitPrice;
        _perPlayerLimit = Math.max(1, perPlayerLimit);
    }

    public UUID getId()
    {
        return _id;
    }

    public ItemKey getItem()
    {
        return _item;
    }

    public int getStock()
    {
        return _stock;
    }

    public double getUnitPrice()
    {
        return _unitPrice;
    }

    public int getPerPlayerLimit()
    {
        return _perPlayerLimit;
    }

    public int boughtBy(UUID player)
    {
        return _bought.getOrDefault(player, 0);
    }

    /**
     * Units the player can still buy: what's left of their limit, and of the stock.
     */
    public int canBuy(UUID player)
    {
        return Math.max(0, Math.min(_stock, _perPlayerLimit - boughtBy(player)));
    }

    public Map<UUID, Integer> getBought()
    {
        return Collections.unmodifiableMap(_bought);
    }

    void sell(UUID player, int amount)
    {
        _stock -= amount;
        _bought.merge(player, amount, Integer::sum);
    }

    /**
     * For loading a saved deal.
     */
    public void restoreBought(UUID player, int amount)
    {
        _bought.put(player, amount);
    }
}
