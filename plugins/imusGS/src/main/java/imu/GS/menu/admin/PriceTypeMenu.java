package imu.GS.menu.admin;

import imu.GS.GsService;
import imu.GS.engine.pricing.PriceRule;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Shop;
import imu.GS.menu.Icons;
import org.bukkit.Material;

import java.util.List;

/**
 * How a stockable listing is priced: the material's price, an own price, or a custom price of items and money.
 */
public class PriceTypeMenu extends AdminMenu
{
    private final Listing _listing;
    private final ListingDraft _draft;
    private final int _returnPage;

    public PriceTypeMenu(GsService service, Shop shop, Listing listing, ListingDraft draft, int returnPage)
    {
        super(service, shop, "&1Change item price", 9);
        _listing = listing;
        _draft = draft;
        _returnPage = returnPage;
    }

    @Override
    protected void draw()
    {
        for (int slot = 0; slot < 9; slot++)
            button(slot, Icons.filler(Material.BLACK_STAINED_GLASS_PANE));

        button(0, Icons.icon(Material.RED_STAINED_GLASS_PANE, "&cBACK"), e -> returnToEditor());
        button(2, Icons.icon(Material.GRASS_BLOCK, "&bMaterial price",
                "&7The item's value from material prices", "&7and enchants, times the shop's sell multiplier"), e ->
        {
            _draft.price = PriceRule.MATERIAL;
            returnToEditor();
        });
        double current = _draft.price instanceof PriceRule.Fixed fixed ? fixed.price() : 0;
        button(4, Icons.icon(Material.PAPER, "&bWrite the price",
                "&7A price of one item. The shop's", "&7multipliers don't change it"), e ->
                Dialogs.number(this, "Own price", List.of("Price of one item"), current, 0, 1e12, value ->
                {
                    _draft.price = new PriceRule.Fixed(Math.round(value * 100) / 100.0);
                    returnToEditor();
                }));
        button(6, Icons.icon(Material.BEACON, "&bSet custom price", "&7Paid with items and money"),
                e -> openNext(new CustomPriceEditorMenu(_service, _shop, _listing, _draft, _returnPage, CustomPriceDraft.of(_draft.price, _listing))));
    }

    private void returnToEditor()
    {
        openNext(new StockableEditorMenu(_service, _shop, _listing, _draft, _returnPage));
    }
}
