package imu.GS.menu.admin;

import imu.GS.engine.pricing.PriceRule;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Restrictions;
import imu.GS.engine.shop.Shop;
import imu.GS.engine.shop.StockRules;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Settings of a stockable listing being edited. Nothing changes in the shop until {@link #applyTo}.
 */
class ListingDraft
{
    /** -1 = not set */
    int maxAmount = -1;
    int fillAmount = -1;
    int fillDelayMinutes = -1;
    PriceRule price = PriceRule.MATERIAL;
    final List<String> permissions = new ArrayList<>();
    final List<Restrictions.Area> areas = new ArrayList<>();
    Restrictions.SellTime sellTime;
    final Set<String> tags = new LinkedHashSet<>();

    static ListingDraft of(Listing listing)
    {
        ListingDraft draft = new ListingDraft();
        StockRules stock = listing.getStock();
        if (stock != null)
        {
            draft.maxAmount = stock.maxAmount();
            draft.fillAmount = stock.fillAmount();
            draft.fillDelayMinutes = stock.fillDelayMinutes();
        }
        draft.price = listing.getPrice();
        draft.permissions.addAll(listing.getRestrictions().permissions());
        draft.areas.addAll(listing.getRestrictions().areas());
        draft.sellTime = listing.getRestrictions().sellTime();
        draft.tags.addAll(listing.getTags());
        return draft;
    }

    ListingDraft copy()
    {
        ListingDraft draft = new ListingDraft();
        draft.maxAmount = maxAmount;
        draft.fillAmount = fillAmount;
        draft.fillDelayMinutes = fillDelayMinutes;
        draft.price = price;
        draft.permissions.addAll(permissions);
        draft.areas.addAll(areas);
        draft.sellTime = sellTime;
        draft.tags.addAll(tags);
        return draft;
    }

    /**
     * Makes the listing stockable with these settings. Its stock is set to the max amount.
     */
    void applyTo(Shop shop, Listing listing)
    {
        int max = maxAmount >= 0 ? maxAmount : listing.getAmount();
        shop.setStock(listing, new StockRules(max, fillAmount, fillDelayMinutes));
        shop.setAmount(listing, max);
        shop.setPrice(listing, price);
        shop.setRestrictions(listing, new Restrictions(sellTime, areas, permissions));
        shop.setTags(listing, tags);
    }
}
