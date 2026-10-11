package imu.GS.menu.admin;

import imu.GS.engine.ItemKey;
import imu.GS.engine.pricing.PriceRule;
import imu.GS.engine.shop.Listing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A custom price being built: items and money paid for every {@code bundleSize} units.
 */
class CustomPriceDraft
{
    final List<PriceRule.ItemCost> items = new ArrayList<>();
    double money;
    int bundleSize = 1;

    static CustomPriceDraft of(PriceRule price, Listing listing)
    {
        CustomPriceDraft draft = new CustomPriceDraft();
        if (price instanceof PriceRule.Custom custom)
        {
            draft.items.addAll(custom.items());
            draft.money = custom.money();
            draft.bundleSize = custom.bundleSize();
        }
        return draft;
    }

    CustomPriceDraft copy()
    {
        CustomPriceDraft draft = new CustomPriceDraft();
        draft.items.addAll(items);
        draft.money = money;
        draft.bundleSize = bundleSize;
        return draft;
    }

    PriceRule.Custom toPrice()
    {
        return new PriceRule.Custom(items, Math.round(money * 100) / 100.0, Math.max(1, bundleSize));
    }

    int indexOf(ItemKey item)
    {
        for (int i = 0; i < items.size(); i++)
        {
            if (items.get(i).item().equals(item))
                return i;
        }
        return -1;
    }

    // Saved drafts, per admin, until the server restarts =============================================================

    private static final Map<UUID, Map<String, CustomPriceDraft>> SAVED = new HashMap<>();

    static Map<String, CustomPriceDraft> saved(UUID admin)
    {
        return SAVED.computeIfAbsent(admin, id -> new LinkedHashMap<>());
    }
}
