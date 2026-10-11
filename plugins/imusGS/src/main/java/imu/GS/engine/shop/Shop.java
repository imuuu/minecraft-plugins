package imu.GS.engine.shop;

import imu.GS.engine.ItemKey;
import imu.GS.engine.pricing.PriceRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * A shop: pages of listings and its settings. Every change goes through here and is told to the listeners.
 * Not thread safe; on the server it is only used on the main thread.
 *
 * <p>While customers have the shop open, listings never move: one that sells out stays as an empty slot. When
 * the last customer leaves, empty listings are removed and the rest are packed together again
 * ({@link #compact(boolean)}). Stockable listings always keep their slot.</p>
 */
public class Shop
{
    public static final int PAGE_SIZE = 27;

    private final UUID _id;
    private String _name;
    private String _displayName;
    private ShopSettings _settings;
    private final List<Listing[]> _pages = new ArrayList<>();
    private final List<ShopListener> _listeners = new CopyOnWriteArrayList<>();
    private int _viewers;
    private long _nextExpireMillis;

    /**
     * @param name        plain name, used to find the shop
     * @param displayName name with colour codes, shown to players
     */
    public Shop(UUID id, String name, String displayName, ShopSettings settings)
    {
        _id = id;
        _name = name;
        _displayName = displayName;
        _settings = settings;
        _pages.add(new Listing[PAGE_SIZE]);
    }

    public UUID getId()
    {
        return _id;
    }

    public String getName()
    {
        return _name;
    }

    public String getDisplayName()
    {
        return _displayName;
    }

    public ShopSettings getSettings()
    {
        return _settings;
    }

    public void rename(String name, String displayName)
    {
        _name = name;
        _displayName = displayName;
        _listeners.forEach(l -> l.settingsChanged(this));
    }

    public void setSettings(ShopSettings settings)
    {
        _settings = settings;
        _listeners.forEach(l -> l.settingsChanged(this));
    }

    // Listeners and viewers ==========================================================================================

    public void addListener(ShopListener listener)
    {
        _listeners.add(listener);
    }

    public void removeListener(ShopListener listener)
    {
        _listeners.remove(listener);
    }

    /**
     * A customer opened the shop. Listings stay in place until every customer has left.
     */
    public void addViewer()
    {
        _viewers++;
    }

    /**
     * A customer left. When it was the last one, sold out listings are removed and the rest packed together.
     */
    public void removeViewer()
    {
        _viewers = Math.max(0, _viewers - 1);
        if (_viewers == 0)
            compact(true);
    }

    public boolean hasViewers()
    {
        return _viewers > 0;
    }

    // Reading ========================================================================================================

    public int getPageCount()
    {
        return _pages.size();
    }

    /**
     * Null for an empty slot or a page that doesn't exist.
     */
    public Listing getListing(int page, int slot)
    {
        if (page < 0 || page >= _pages.size() || slot < 0 || slot >= PAGE_SIZE)
            return null;
        return _pages.get(page)[slot];
    }

    /**
     * Page and slot of the listing, or null when it isn't in this shop.
     */
    public int[] positionOf(Listing listing)
    {
        for (int page = 0; page < _pages.size(); page++)
        {
            Listing[] slots = _pages.get(page);
            for (int slot = 0; slot < PAGE_SIZE; slot++)
            {
                if (slots[slot] == listing)
                    return new int[] { page, slot };
            }
        }
        return null;
    }

    public List<Listing> getListings()
    {
        List<Listing> listings = new ArrayList<>();
        for (Listing[] page : _pages)
        {
            for (Listing listing : page)
            {
                if (listing != null)
                    listings.add(listing);
            }
        }
        return Collections.unmodifiableList(listings);
    }

    public Listing getListing(UUID id)
    {
        for (Listing listing : getListings())
        {
            if (listing.getId().equals(id))
                return listing;
        }
        return null;
    }

    /**
     * The first listing of exactly this item, in page and slot order.
     */
    public Listing findSameKind(ItemKey item)
    {
        for (Listing listing : getListings())
        {
            if (listing.getItem().equals(item))
                return listing;
        }
        return null;
    }

    /**
     * Units of every listing whose item matches.
     */
    public int countItems(Predicate<ItemKey> items)
    {
        int count = 0;
        for (Listing listing : getListings())
        {
            if (items.test(listing.getItem()))
                count += listing.getAmount();
        }
        return count;
    }

    // Changing listings ==============================================================================================

    /**
     * Puts the listing in the first free slot, adding a page when every page is full.
     */
    public void addListing(Listing listing)
    {
        int[] free = findFreeSlot();
        placeListing(listing, free[0], free[1]);
    }

    /**
     * Puts the listing in this slot, adding pages up to it. Whatever was in the slot is replaced.
     */
    public void placeListing(Listing listing, int page, int slot)
    {
        if (slot < 0 || slot >= PAGE_SIZE || page < 0)
            throw new IllegalArgumentException("No slot " + page + ":" + slot);
        while (_pages.size() <= page)
            _pages.add(new Listing[PAGE_SIZE]);
        _pages.get(page)[slot] = listing;
        layoutChanged();
    }

    public boolean removeListing(Listing listing)
    {
        int[] position = positionOf(listing);
        if (position == null)
            return false;
        _pages.get(position[0])[position[1]] = null;
        layoutChanged();
        return true;
    }

    /**
     * Units sold to the shop. They go to the first listing of the same item, or to a new listing priced with
     * {@code newListingPrice}. Returns the listing that got them.
     */
    public Listing receive(ItemKey item, int amount, PriceRule newListingPrice)
    {
        Listing listing = findSameKind(item);
        if (listing != null)
        {
            listing.receive(amount);
            listingChanged(listing);
            return listing;
        }

        listing = new Listing(UUID.randomUUID(), item, amount, newListingPrice);
        addListing(listing);
        return listing;
    }

    /**
     * Units bought from the listing. An unlimited stockable keeps its amount.
     */
    public void take(Listing listing, int amount)
    {
        listing.take(amount);
        listingChanged(listing);
    }

    public void setAmount(Listing listing, int amount)
    {
        listing.setAmount(amount);
        listingChanged(listing);
    }

    public void setPrice(Listing listing, PriceRule price)
    {
        listing.setPrice(price);
        listingChanged(listing);
    }

    /**
     * Null turns a stockable back into a normal listing.
     */
    public void setStock(Listing listing, StockRules stock)
    {
        listing.setStock(stock);
        listingChanged(listing);
    }

    public void setRestrictions(Listing listing, Restrictions restrictions)
    {
        listing.setRestrictions(restrictions);
        listingChanged(listing);
    }

    public void setTags(Listing listing, Set<String> tags)
    {
        listing.setTags(tags);
        listingChanged(listing);
    }

    // Time ===========================================================================================================

    /**
     * Refills stockables and expires stock when their time has come. Call it regularly.
     */
    public void tick(long nowMillis)
    {
        for (Listing listing : getListings())
        {
            if (listing.refill(nowMillis))
                listingChanged(listing);
        }

        if (_settings.expireCooldownMinutes() <= 0)
            return;
        long cooldown = _settings.expireCooldownMinutes() * 60_000L;
        if (_nextExpireMillis == 0)
        {
            _nextExpireMillis = nowMillis + cooldown;
            return;
        }
        if (nowMillis < _nextExpireMillis)
            return;

        _nextExpireMillis = nowMillis + cooldown;
        expire();
    }

    /**
     * Every normal listing loses the expire share of its amount, rounded down.
     */
    public void expire()
    {
        double keep = 1.0 - _settings.expirePercent();
        for (Listing listing : getListings())
        {
            if (listing.isStockable())
                continue;
            int amount = (int) Math.floor(listing.getAmount() * keep);
            if (amount != listing.getAmount())
            {
                listing.setAmount(amount);
                listingChanged(listing);
            }
        }
        if (!hasViewers())
            compact(true);
    }

    /**
     * Packs normal listings into the first slots of their page, around the stockables, which keep their slots.
     * Empty pages at the end are removed, except the first page.
     *
     * @param removeEmpty also remove normal listings that are sold out
     */
    public void compact(boolean removeEmpty)
    {
        boolean changed = false;
        for (int page = 0; page < _pages.size(); page++)
        {
            Listing[] old = _pages.get(page);
            Listing[] packed = new Listing[PAGE_SIZE];
            for (int slot = 0; slot < PAGE_SIZE; slot++)
            {
                if (old[slot] != null && old[slot].isStockable())
                    packed[slot] = old[slot];
            }

            int next = 0;
            for (int slot = 0; slot < PAGE_SIZE; slot++)
            {
                Listing listing = old[slot];
                if (listing == null || listing.isStockable())
                    continue;
                if (removeEmpty && listing.getAmount() <= 0)
                    continue;
                while (packed[next] != null)
                    next++;
                packed[next] = listing;
            }

            for (int slot = 0; slot < PAGE_SIZE && !changed; slot++)
                changed = packed[slot] != old[slot];
            _pages.set(page, packed);
        }

        while (_pages.size() > 1 && isEmpty(_pages.get(_pages.size() - 1)))
        {
            _pages.remove(_pages.size() - 1);
            changed = true;
        }

        if (changed)
            layoutChanged();
    }

    private static boolean isEmpty(Listing[] page)
    {
        for (Listing listing : page)
        {
            if (listing != null)
                return false;
        }
        return true;
    }

    private int[] findFreeSlot()
    {
        for (int page = 0; page < _pages.size(); page++)
        {
            Listing[] slots = _pages.get(page);
            for (int slot = 0; slot < PAGE_SIZE; slot++)
            {
                if (slots[slot] == null)
                    return new int[] { page, slot };
            }
        }
        return new int[] { _pages.size(), 0 };
    }

    private void listingChanged(Listing listing)
    {
        _listeners.forEach(l -> l.listingChanged(this, listing));
    }

    private void layoutChanged()
    {
        _listeners.forEach(l -> l.layoutChanged(this));
    }
}
