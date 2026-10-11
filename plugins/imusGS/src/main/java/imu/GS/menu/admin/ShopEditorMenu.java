package imu.GS.menu.admin;

import imu.GS.GsService;
import imu.GS.bukkit.ItemKeys;
import imu.GS.engine.pricing.PriceRule;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Restrictions;
import imu.GS.engine.shop.Shop;
import imu.GS.menu.Icons;
import imu.iAPI.Buttons.Button;
import imu.iAPI.Other.Metods;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * /gs modify shop: the shop's pages, where stockable items are edited, and the shop's tools.
 *
 * <pre>
 *  0-26 shop page: click a stockable to edit it
 *  39 remove normal items   45 &lt;&lt;   47 save   49 settings   51 generate items   53 &gt;&gt;
 * </pre>
 */
public class ShopEditorMenu extends AdminMenu
{
    private int _page = 0;

    public ShopEditorMenu(GsService service, Shop shop)
    {
        super(service, shop, "Modding: " + shop.getDisplayName(), 54);
    }

    public ShopEditorMenu page(int page)
    {
        _page = page;
        return this;
    }

    @Override
    protected void refresh()
    {
        clearButtons();
        _page = Math.max(0, Math.min(_page, _shop.getPageCount() - 1));

        for (int slot = 0; slot < Shop.PAGE_SIZE; slot++)
        {
            Listing listing = _shop.getListing(_page, slot);
            if (listing == null)
            {
                addButton(new Button(slot, Icons.filler(Material.BLACK_STAINED_GLASS_PANE)));
                continue;
            }
            if (!listing.isStockable())
            {
                addButton(new Button(slot, Icons.withLore(ItemKeys.template(listing.getItem()),
                        List.of("&9Amount: &a" + listing.getAmount(), "&cCan't be modified!", "&7Sold to the shop by players"))));
                continue;
            }
            addButton(new Button(slot, Icons.withLore(ItemKeys.template(listing.getItem()), describe(listing)),
                    e -> openNext(new StockableEditorMenu(_service, _shop, listing, ListingDraft.of(listing), _page))));
        }

        ItemStack orange = Icons.filler(Material.ORANGE_STAINED_GLASS_PANE);
        for (int slot = 27; slot < 54; slot++)
            addButton(new Button(slot, orange));

        addButton(new Button(39, Icons.icon(Material.RED_DYE, "&bClear shop from normal items",
                "&9Removes all items except &estockable &9items!"), e ->
        {
            for (Listing listing : new ArrayList<>(_shop.getListings()))
            {
                if (!listing.isStockable())
                    _shop.removeListing(listing);
            }
            _shop.compact(true);
            getPlayer().sendMessage(Metods.msgC("&3Shop &2cleared &3from normal items!"));
            refresh();
        }));
        addButton(new Button(45, Icons.icon(Material.BIRCH_SIGN, "&b<<"), e ->
        {
            _page--;
            refresh();
        }));
        addButton(new Button(53, Icons.icon(Material.BIRCH_SIGN, "&b>>"), e ->
        {
            _page++;
            refresh();
        }));
        addButton(new Button(47, Icons.icon(Material.GOLD_INGOT, "&bSave shop data to Database!",
                "&9Shops save on their own after changes", "&9and on shutdown; this saves right now"), e ->
        {
            _service.saveNow(_shop);
            getPlayer().sendMessage(Metods.msgC("&6(Shop) " + _shop.getDisplayName() + " &6has been saved!"));
            closeNext();
        }));
        addButton(new Button(49, Icons.icon(Material.BEACON, "&bChange shop data", "&9Name, multipliers, lock..."),
                e -> openNext(new ShopSettingsMenu(_service, _shop))));
        addButton(new Button(51, Icons.icon(Material.GOLD_ORE, "&6Generate Items", "&5Open item generator inventory"),
                e -> openNext(new ItemGeneratorMenu(_service, _shop))));

        updateButtons(true);
    }

    static List<String> describe(Listing listing)
    {
        List<String> lore = new ArrayList<>();
        lore.add("&e== Click to modify ==");
        lore.add("&9Amount now: &e" + listing.getAmount());
        lore.add("&9Max stock amount: " + (listing.getStock() == null ? "&cNone" : "&e" + listing.getStock().maxAmount()));
        lore.add("&9Fill Amount: " + value(listing.getStock() == null ? -1 : listing.getStock().fillAmount()));
        lore.add("&9Fill Delay: " + value(listing.getStock() == null ? -1 : listing.getStock().fillDelayMinutes()));
        lore.add("&9Price: &e" + priceText(listing.getPrice()));
        Restrictions restrictions = listing.getRestrictions();
        lore.add("&9Permission: " + (restrictions.permissions().isEmpty() ? "&cNone" : "&e" + String.join("; ", restrictions.permissions())));
        lore.add("&9Sold Distance&Loc: " + (restrictions.areas().isEmpty() ? "&cNone"
                : restrictions.areas().size() == 1 ? "&e" + areaText(restrictions.areas().get(0)) : "&eMore than one"));
        lore.add("&9Sell time: " + (restrictions.sellTime() == null ? "&cNone"
                : "&e" + restrictions.sellTime().start() + " - " + restrictions.sellTime().end()));
        lore.add("&9Tags: " + (listing.getTags().isEmpty() ? "&cNone" : "&e" + String.join(", ", listing.getTags())));
        return lore;
    }

    private static String value(int value)
    {
        return value < 0 ? "&cNone" : "&e" + value;
    }

    static String priceText(PriceRule price)
    {
        return switch (price)
        {
            case PriceRule.Material m -> "material price";
            case PriceRule.Fixed fixed -> Icons.money(fixed.price());
            case PriceRule.Unique unique -> "unique " + Icons.money(unique.price());
            case PriceRule.Custom custom -> "custom (" + custom.items().size() + " items, " + Icons.money(custom.money()) + " per " + custom.bundleSize() + ")";
        };
    }

    static String areaText(Restrictions.Area area)
    {
        return area.radius() + " " + area.world() + " " + (int) area.x() + " " + (int) area.y() + " " + (int) area.z();
    }
}
