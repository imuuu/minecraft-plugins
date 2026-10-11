package imu.GS.engine.shop;

import imu.GS.engine.ItemKey;
import imu.GS.engine.pricing.PriceRule;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * An item a shop sells: how many it has and how it is priced. Changed only through its {@link Shop}, which
 * tells the shop's listeners.
 */
public class Listing
{
    private final UUID _id;
    private final ItemKey _item;
    private int _amount;
    private PriceRule _price;
    private StockRules _stock;
    private Restrictions _restrictions = Restrictions.NONE;
    private final Set<String> _tags = new HashSet<>();
    private long _nextFillMillis;

    public Listing(UUID id, ItemKey item, int amount, PriceRule price)
    {
        _id = id;
        _item = item;
        _amount = Math.max(0, amount);
        _price = price;
    }

    public UUID getId()
    {
        return _id;
    }

    public ItemKey getItem()
    {
        return _item;
    }

    public int getAmount()
    {
        return _amount;
    }

    public PriceRule getPrice()
    {
        return _price;
    }

    /**
     * Null for a normal listing.
     */
    public StockRules getStock()
    {
        return _stock;
    }

    public boolean isStockable()
    {
        return _stock != null;
    }

    public Restrictions getRestrictions()
    {
        return _restrictions;
    }

    public Set<String> getTags()
    {
        return Collections.unmodifiableSet(_tags);
    }

    public boolean hasTag(String tag)
    {
        return _tags.contains(tag.toLowerCase(Locale.ROOT));
    }

    /**
     * True when buying never lowers the amount (a stockable without refills).
     */
    public boolean isUnlimited()
    {
        return _stock != null && _stock.isUnlimited();
    }

    // Changes, only through Shop =====================================================================================

    void setAmount(int amount)
    {
        _amount = Math.max(0, amount);
        if (_stock != null && _amount > _stock.maxAmount())
            _amount = _stock.maxAmount();
    }

    /**
     * Units taken by a buyer.
     */
    void take(int amount)
    {
        if (!isUnlimited())
            setAmount(_amount - amount);
    }

    /**
     * Units sold to the shop. A stockable only takes them while it has room, up to its max.
     */
    void receive(int amount)
    {
        if (_stock == null)
            _amount += amount;
        else if (!_stock.isUnlimited())
            setAmount(_amount + amount);
    }

    void setPrice(PriceRule price)
    {
        _price = price;
    }

    void setStock(StockRules stock)
    {
        _stock = stock;
        _nextFillMillis = 0;
        setAmount(_amount);
    }

    void setRestrictions(Restrictions restrictions)
    {
        _restrictions = restrictions == null ? Restrictions.NONE : restrictions;
    }

    void setTags(Set<String> tags)
    {
        _tags.clear();
        for (String tag : tags)
            _tags.add(tag.toLowerCase(Locale.ROOT));
    }

    /**
     * Refills the stockable when its delay has passed. Returns true when the amount changed.
     */
    boolean refill(long nowMillis)
    {
        if (_stock == null || !_stock.refills())
            return false;
        if (_nextFillMillis == 0)
        {
            // The first delay starts when the shop starts ticking
            _nextFillMillis = nowMillis + _stock.fillDelayMinutes() * 60_000L;
            return false;
        }
        if (nowMillis < _nextFillMillis)
            return false;

        _nextFillMillis = nowMillis + _stock.fillDelayMinutes() * 60_000L;
        int before = _amount;
        setAmount(_amount + _stock.fillAmount());
        return _amount != before;
    }

    @Override
    public String toString()
    {
        return "Listing{" + _item + " x" + _amount + ", " + _price + (_stock != null ? ", " + _stock : "") + "}";
    }
}
