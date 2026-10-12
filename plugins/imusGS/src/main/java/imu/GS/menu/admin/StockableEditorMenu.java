package imu.GS.menu.admin;

import imu.iAPI.Utilities.TextUtils;
import imu.GS.GsService;
import imu.GS.bukkit.ItemKeys;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Restrictions;
import imu.GS.engine.shop.Shop;
import imu.GS.menu.Icons;
import imu.iAPI.Buttons.Button;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Settings of one stockable listing. Changes are kept in a {@link ListingDraft} until CONFIRM.
 *
 * <pre>
 *  0 back   2 max stock   4 permission   6 fill delay   8 remove item
 *  9 item   10 tags       12 fill amount 14 price       17 copy to every stockable
 *  18 confirm             22 area        24 sell time   26 copy item to your inventory
 * </pre>
 * Left click sets a value, right click removes it.
 */
public class StockableEditorMenu extends AdminMenu
{
    private final Listing _listing;
    private final ListingDraft _draft;
    private final int _returnPage;

    public StockableEditorMenu(GsService service, Shop shop, Listing listing, ListingDraft draft, int returnPage)
    {
        super(service, shop, "&3Modify Stockable Item", 27);
        _listing = listing;
        _draft = draft;
        _returnPage = returnPage;
    }

    @Override
    protected void refresh()
    {
        clearButtons();
        for (int slot = 0; slot < 27; slot++)
            addButton(new Button(slot, Icons.filler(Material.BLACK_STAINED_GLASS_PANE)));

        addButton(new Button(0, Icons.icon(Material.RED_STAINED_GLASS_PANE, "&c<< BACK"), e -> returnToEditor()));
        addButton(new Button(18, Icons.icon(Material.GREEN_STAINED_GLASS_PANE, "&a<< CONFIRM"), e ->
        {
            if (_shop.positionOf(_listing) != null)
            {
                _draft.applyTo(_shop, _listing);
                _service.warnAboutBuyBackProfit(getPlayer(), _shop);
            }
            returnToEditor();
        }));
        addButton(new Button(9, Icons.withLore(ItemKeys.template(_listing.getItem()), List.of())));
        addButton(new Button(8, Icons.icon(Material.LAVA_BUCKET, "&c&lRemove this item from shop", "", "&bShift click &7to remove"), e ->
        {
            if (!e.isShiftClick())
                return;
            _shop.removeListing(_listing);
            _shop.compact(true);
            returnToEditor();
        }));
        addButton(new Button(26, Icons.icon(Material.SLIME_BALL, "&b&lClone to your inv"),
                e -> getPlayer().getInventory().addItem(ItemKeys.stack(_listing.getItem(), 1))));
        addButton(new Button(17, Icons.glow(Icons.icon(Material.PAPER, "&b&lOverride all items in shop with this data!",
                "&7Every stockable item of the shop gets", "&7these settings", "", "&bShift click &7to do it")), e ->
        {
            if (!e.isShiftClick())
                return;
            int count = 0;
            for (Listing listing : _shop.getListings())
            {
                if (!listing.isStockable())
                    continue;
                _draft.copy().applyTo(_shop, listing);
                count++;
            }
            getPlayer().sendMessage(TextUtils.Color("&3Data has been changed total of &2" + count + " &3items"));
            _service.warnAboutBuyBackProfit(getPlayer(), _shop);
        }));

        addButton(valueButton(2, "Max stock amount", _draft.maxAmount < 0 ? null : String.valueOf(_draft.maxAmount), List.of(), e ->
        {
            if (e.isRightClick())
            {
                _draft.maxAmount = -1;
                refresh();
                return;
            }
            Dialogs.wholeNumber(this, "Max stock amount", List.of("The stock refills up to this"), Math.max(0, _draft.maxAmount), 0, 1_000_000,
                    value -> _draft.maxAmount = value);
        }));

        addButton(valueButton(6, "Set Stock FillDelay(min)", _draft.fillDelayMinutes < 0 ? null : _draft.fillDelayMinutes + " min",
                List.of("&7How often the stock is filled"), e ->
        {
            if (e.isRightClick())
            {
                _draft.fillDelayMinutes = -1;
                refresh();
                return;
            }
            Dialogs.wholeNumber(this, "Fill delay", List.of("Minutes between refills"), Math.max(0, _draft.fillDelayMinutes), 0, 1_000_000,
                    value -> _draft.fillDelayMinutes = value);
        }));

        addButton(valueButton(12, "Set Stock FillAmount", _draft.fillAmount < 0 ? null : String.valueOf(_draft.fillAmount),
                List.of("&7How many it fills, up to the max.", "&7None: the stock never runs out"), e ->
        {
            if (e.isRightClick())
            {
                _draft.fillAmount = -1;
                refresh();
                return;
            }
            Dialogs.wholeNumber(this, "Fill amount", List.of("Units added every refill"), Math.max(0, _draft.fillAmount), 0, 1_000_000,
                    value -> _draft.fillAmount = value);
        }));

        addButton(valueButton(14, "Set Price", ShopEditorMenu.priceText(_draft.price),
                List.of("&7Own price, custom price or the material's price.", "&7Be careful, it can't be too cheap!"), e ->
        {
            if (e.isRightClick())
            {
                _draft.price = imu.GS.engine.pricing.PriceRule.MATERIAL;
                refresh();
                return;
            }
            openNext(new PriceTypeMenu(_service, _shop, _listing, _draft, _returnPage));
        }));

        addButton(valueButton(4, "Set permission", _draft.permissions.isEmpty() ? null : String.join("; ", _draft.permissions),
                List.of("&7Only players with one of these see it"), e ->
        {
            if (e.isRightClick())
            {
                _draft.permissions.clear();
                refresh();
                return;
            }
            Dialogs.text(this, "Add permission", List.of("Players need one of the item's permissions"), "", text ->
            {
                if (text.isBlank())
                    return "Write a permission";
                _draft.permissions.add(text.trim());
                return null;
            });
        }));

        List<String> areaLines = new ArrayList<>();
        for (Restrictions.Area area : _draft.areas)
            areaLines.add("&e" + ShopEditorMenu.areaText(area));
        areaLines.add("&7Item is shown inside the given radius");
        addButton(valueButton(22, "Set Distance radius", _draft.areas.isEmpty() ? null : _draft.areas.size() + " place(s)", areaLines, e ->
        {
            if (e.isRightClick())
            {
                _draft.areas.clear();
                refresh();
                return;
            }
            Dialogs.text(this, "Add area", List.of("distance world x y z  (e.g. 50 world 1 55 23)", "or: distance this  (where you stand)"), "", this::addArea);
        }));

        addButton(valueButton(24, "Set sell time", _draft.sellTime == null ? null : _draft.sellTime.start() + " - " + _draft.sellTime.end(),
                List.of("&7World time when the item is sold"), e ->
        {
            if (e.isRightClick())
            {
                _draft.sellTime = null;
                refresh();
                return;
            }
            Dialogs.text(this, "Sell time", List.of("start end, separated by a space (0-24000)", "e.g. 0 13000 = the whole day"), "", text ->
            {
                String[] parts = text.trim().split("\\s+");
                try
                {
                    int start = Integer.parseInt(parts[0]);
                    int end = Integer.parseInt(parts[1]);
                    if (parts.length != 2 || start < 0 || end < 0 || start > 24000 || end > 24000)
                        return "Two whole numbers between 0 and 24000";
                    _draft.sellTime = new Restrictions.SellTime(start, end);
                    return null;
                } catch (RuntimeException ex)
                {
                    return "Two whole numbers between 0 and 24000";
                }
            });
        }));

        addButton(valueButton(10, "Add Tags", _draft.tags.isEmpty() ? null : String.join(", ", _draft.tags),
                List.of("&7For /gs tag shopitems"), e ->
        {
            if (e.isRightClick())
            {
                _draft.tags.clear();
                refresh();
                return;
            }
            Dialogs.text(this, "Add tag", List.of("One word"), "", text ->
            {
                if (text.isBlank() || text.trim().contains(" "))
                    return "One word";
                _draft.tags.add(text.trim().toLowerCase(Locale.ROOT));
                return null;
            });
        }));

        updateButtons(true);
    }

    private String addArea(String text)
    {
        String[] parts = text.trim().split("\\s+");
        try
        {
            int radius = Integer.parseInt(parts[0]);
            if (parts.length == 2 && parts[1].equalsIgnoreCase("this"))
            {
                Location loc = getPlayer().getLocation();
                _draft.areas.add(new Restrictions.Area(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), radius));
                return null;
            }
            if (parts.length != 5)
                return "Write: distance world x y z, or: distance this";
            if (Bukkit.getWorld(parts[1]) == null)
                return "No world named " + parts[1];
            _draft.areas.add(new Restrictions.Area(parts[1], Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Double.parseDouble(parts[4]), radius));
            return null;
        } catch (NumberFormatException e)
        {
            return "Distance and coordinates must be numbers";
        }
    }

    private Button valueButton(int slot, String name, String value, List<String> help, java.util.function.Consumer<InventoryClickEvent> click)
    {
        List<String> lore = new ArrayList<>(help);
        lore.add(value == null ? "&aSet To &cNONE" : "&aSet To &1" + value);
        lore.add("&bM1: &aSet &bM2: &cRemove");
        ItemStack stack = Icons.icon(value == null ? Material.GLASS_PANE : Material.BLUE_STAINED_GLASS_PANE, "&5" + name, lore);
        return new Button(slot, stack, click);
    }

    private void returnToEditor()
    {
        openNext(new ShopEditorMenu(_service, _shop).page(_returnPage));
    }
}
