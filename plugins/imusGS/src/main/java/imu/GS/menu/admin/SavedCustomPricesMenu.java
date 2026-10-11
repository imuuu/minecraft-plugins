package imu.GS.menu.admin;

import imu.GS.GsService;
import imu.GS.engine.pricing.PriceRule;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Shop;
import imu.GS.menu.Icons;
import imu.iAPI.Buttons.Button;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The custom prices an admin saved, to load one into the custom price editor.
 */
public class SavedCustomPricesMenu extends AdminMenu
{
    private final Listing _listing;
    private final ListingDraft _listingDraft;
    private final int _returnPage;
    private final CustomPriceDraft _current;
    private int _page = 0;

    public SavedCustomPricesMenu(GsService service, Shop shop, Listing listing, ListingDraft listingDraft, int returnPage, CustomPriceDraft current)
    {
        super(service, shop, "&9Load Custom Price", 36);
        _listing = listing;
        _listingDraft = listingDraft;
        _returnPage = returnPage;
        _current = current;
    }

    @Override
    protected void refresh()
    {
        clearButtons();
        for (int slot = 27; slot < 36; slot++)
            addButton(new Button(slot, Icons.filler(Material.BLACK_STAINED_GLASS_PANE)));

        List<Map.Entry<String, CustomPriceDraft>> saved = new ArrayList<>(CustomPriceDraft.saved(getPlayer().getUniqueId()).entrySet());
        int pages = Math.max(1, (saved.size() + 26) / 27);
        _page = Math.floorMod(_page, pages);
        int first = _page * 27;
        for (int i = first; i < Math.min(saved.size(), first + 27); i++)
        {
            String name = saved.get(i).getKey();
            CustomPriceDraft draft = saved.get(i).getValue();
            List<String> lore = new ArrayList<>();
            lore.add("&eMoney: &2" + Icons.money(draft.money) + " &7per " + draft.bundleSize);
            lore.add("&9==== Contains items below ====");
            for (PriceRule.ItemCost cost : draft.items)
                lore.add("&e" + cost.amount() + " &7" + imu.GS.bukkit.ItemKeys.template(cost.item()).getType());
            addButton(new Button(i - first, Icons.icon(Material.CHEST, "&b" + name + " &8(Click to load!)", lore),
                    e -> openNext(new CustomPriceEditorMenu(_service, _shop, _listing, _listingDraft, _returnPage, draft.copy()))));
        }

        addButton(new Button(27, Icons.icon(Material.RED_STAINED_GLASS_PANE, "&c<== BACK"),
                e -> openNext(new CustomPriceEditorMenu(_service, _shop, _listing, _listingDraft, _returnPage, _current))));
        addButton(new Button(30, Icons.icon(Material.BIRCH_SIGN, "&9<<"), e ->
        {
            _page--;
            refresh();
        }));
        addButton(new Button(32, Icons.icon(Material.BIRCH_SIGN, "&9>>"), e ->
        {
            _page++;
            refresh();
        }));
        updateButtons(true);
    }
}
