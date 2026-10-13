package imu.GS.menu.admin;

import imu.iAPI.Menus.Click;
import imu.iAPI.Utilities.TextUtils;
import imu.GS.GsService;
import imu.GS.bukkit.ItemKeys;
import imu.GS.engine.ItemKey;
import imu.GS.engine.pricing.PriceRule;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Shop;
import imu.GS.menu.Icons;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Builds a custom price. Put an item from your inventory on an empty slot to make it part of the price (you keep
 * the item); click it to change how many are needed, right click to take it out.
 *
 * <pre>
 *  0-44 items of the price
 *  45 back   46 copy items to you   47 load saved   48 save   49 bundle size   51 money   53 confirm
 * </pre>
 */
public class CustomPriceEditorMenu extends AdminMenu
{
    private static final int ITEM_SLOTS = 45;

    private final Listing _listing;
    private final ListingDraft _listingDraft;
    private final int _returnPage;
    private final CustomPriceDraft _draft;

    public CustomPriceEditorMenu(GsService service, Shop shop, Listing listing, ListingDraft listingDraft, int returnPage, CustomPriceDraft draft)
    {
        super(service, shop, "&9=== &6Create Custom Price &9===", 54);
        _listing = listing;
        _listingDraft = listingDraft;
        _returnPage = returnPage;
        _draft = draft;
    }

    @Override
    protected void draw()
    {
        for (int i = 0; i < _draft.items.size() && i < ITEM_SLOTS; i++)
        {
            PriceRule.ItemCost cost = _draft.items.get(i);
            int index = i;
            button(i, Icons.withLore(ItemKeys.template(cost.item()), List.of(
                    "&bM1: &2Set new Amount  &bM2: &cRemove", "&9Amount: &1" + cost.amount())), e ->
            {
                if (e.isRight())
                {
                    _draft.items.remove(index);
                    refresh();
                    return;
                }
                Dialogs.wholeNumber(this, "Amount", List.of("How many are needed per bundle"), cost.amount(), 1, 100_000,
                        value -> _draft.items.set(index, new PriceRule.ItemCost(cost.item(), value)));
            });
        }

        for (int slot = Math.min(_draft.items.size(), ITEM_SLOTS); slot < ITEM_SLOTS; slot++)
            emptySlot(slot, this::addItem);
        for (int slot = ITEM_SLOTS; slot < 54; slot++)
            button(slot, Icons.filler(Material.BLACK_STAINED_GLASS_PANE));

        button(45, Icons.icon(Material.RED_STAINED_GLASS_PANE, "&cBACK"), e -> returnToEditor());
        button(46, Icons.icon(Material.SLIME_BALL, "&bClone Items to your inv"), e ->
        {
            for (PriceRule.ItemCost cost : _draft.items)
                getPlayer().getInventory().addItem(ItemKeys.stack(cost.item(), 1));
        });
        button(47, Icons.icon(Material.ANCIENT_DEBRIS, "&bLoad saved custom prices"),
                e -> openNext(new SavedCustomPricesMenu(_service, _shop, _listing, _listingDraft, _returnPage, _draft)));
        button(48, Icons.icon(Material.NETHERITE_SCRAP, "&bSave this custom price",
                "&7Kept until the server restarts"), e ->
                Dialogs.text(this, "Save as", List.of("A name for this custom price"), "", text ->
                {
                    if (text.isBlank())
                        return "Write a name";
                    CustomPriceDraft.saved(getPlayer().getUniqueId()).put(text.trim(), _draft.copy());
                    getPlayer().sendMessage(TextUtils.Color("&6Custom Price saved with name: &9" + text.trim()));
                    return null;
                }));
        button(49, Icons.withLore(ItemKeys.template(_listing.getItem()), List.of(
                "&6Minimum buy amount (bundle): &e" + _draft.bundleSize,
                "&7The price is paid for every this many", "&bClick &7to change")), e ->
                Dialogs.wholeNumber(this, "Bundle size", List.of("Items and money are paid for every this many bought"),
                        _draft.bundleSize, 1, 100_000, value -> _draft.bundleSize = value));
        button(51, Icons.icon(Material.PAPER, "&9Add Money", "&9Money: &e" + Icons.money(_draft.money),
                "&bM1: &2Set  &bM2: &cRemove"), e ->
        {
            if (e.isRight())
            {
                _draft.money = 0;
                refresh();
                return;
            }
            Dialogs.number(this, "Money", List.of("Money paid per bundle"), _draft.money, 0, 1e12, value -> _draft.money = value);
        });
        button(53, Icons.icon(Material.GREEN_STAINED_GLASS_PANE, "&1CONFIRM"), e ->
        {
            if (_draft.items.isEmpty() && _draft.money <= 0)
            {
                getPlayer().sendMessage(TextUtils.Color("&cAdd items or money to the price first"));
                return;
            }
            _listingDraft.price = _draft.toPrice();
            returnToEditor();
        });
    }

    // An item put in an empty cost slot is added with the amount on the cursor; the admin keeps the item
    private void addItem(Click click)
    {
        ItemStack stack = (ItemStack) click.cursor();
        if (stack == null)
            return;
        ItemKey item = ItemKeys.of(stack);
        int index = _draft.indexOf(item);
        if (index >= 0)
            _draft.items.set(index, new PriceRule.ItemCost(item, stack.getAmount()));
        else
            _draft.items.add(new PriceRule.ItemCost(item, stack.getAmount()));
    }

    private void returnToEditor()
    {
        openNext(new StockableEditorMenu(_service, _shop, _listing, _listingDraft, _returnPage));
    }
}
