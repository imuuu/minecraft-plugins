package imu.GS.menu;

import imu.iAPI.Menus.Click;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Paper.OwnItemRows;
import imu.iAPI.Menus.Viewer;
import imu.iAPI.Utilities.InvUtil;
import imu.iAPI.Utilities.TextUtils;
import imu.GS.GsService;
import imu.GS.bukkit.ItemKeys;
import imu.GS.bukkit.PlayerInventoryAdapter;
import imu.GS.engine.ItemCategory;
import imu.GS.engine.ItemKey;
import imu.GS.engine.customer.Customer;
import imu.GS.engine.live.FlashDeal;
import imu.GS.engine.live.WantedRequests;
import imu.GS.live.MarketEvents;
import imu.GS.text.Messages;
import imu.GS.engine.pricing.Overflow;
import imu.GS.engine.pricing.PriceCalculator;
import imu.GS.engine.pricing.PriceRule;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Shop;
import imu.GS.engine.shop.ShopListener;
import imu.GS.engine.trade.TradeResult;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import imu.iAPI.Menus.ClickKind;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The shop as a customer sees it: the shop's listings on top, their own sellable items at the bottom.
 *
 * <pre>
 *  0-26  shop page (27 listings)
 *  27 &lt;&lt; Shop   28 market today   29 wanted   30 &lt;&lt; Inv   31 tab   32 Inv &gt;&gt;   33 hotbar/armor   34 flash deals   35 Shop &gt;&gt;
 *  36-53 customer's items (18 per page)
 * </pre>
 *
 * Left click buys or sells 1, right click 8, shift + left 64, shift + right all of it.
 */
public class ShopMenu extends GsMenu implements ShopListener, OwnItemRows
{
    private static final int SHOP_SLOTS = Shop.PAGE_SIZE;
    private static final int CUSTOMER_START = 36;
    private static final int CUSTOMER_SLOTS = 18;
    private static final ItemCategory[] TABS = { ItemCategory.BLOCKS, ItemCategory.GEAR, ItemCategory.SHULKER_BOX };

    private final Shop _shop;
    private PlayerInventoryAdapter _inventory;
    private Customer _customer;
    private int _shopPage = 0;
    private int _customerPage = 0;
    private int _tab = 0;
    private long _lastClick;
    private boolean _shown;
    private int _frame;
    private boolean _flashShown;

    public ShopMenu(GsService service, Shop shop)
    {
        super(service, shop.getDisplayName(), 54);
        _shop = shop;
    }

    public Shop getShop()
    {
        return _shop;
    }

    @Override
    protected void onOpen(Viewer viewer)
    {
        if (_shown)
            return;
        _shown = true;
        _inventory = new PlayerInventoryAdapter(getPlayer());
        _customer = _service.customer(getPlayer(), _inventory);
        _shop.addViewer();
        _shop.addListener(this);
        _service.menuOpened(this);
    }

    @Override
    protected void onClose(Viewer viewer)
    {
        _shop.removeListener(this);
        _service.menuClosed(this);
        _shop.removeViewer();
    }

    // The empty slots of the inventory below are black panes on the player's screen; their items show as they are
    @Override
    public Icon ownInventoryCover()
    {
        return Icon.filler(Material.BLACK_STAINED_GLASS_PANE);
    }

    // The player's own items tell what they sell for here, on the player's screen only
    @Override
    public List<String> ownItemRows(ItemStack item)
    {
        if (PlayerInventoryAdapter.category(item) == ItemCategory.SHULKER_BOX)
            return null;
        ItemKey key = ItemKeys.of(item);
        if (!_service.getTrades().buysItem(_shop, key))
            return List.of("", Messages.get("shop.doesnt-buy"));
        return List.of("", Messages.get("shop.own-item-sells", "price", Icons.money(_service.getPrices().sellPrice(_shop, key, 1))));
    }

    // The flash deal button blinks
    @Override
    public int refreshTicks()
    {
        return 10;
    }

    @Override
    protected boolean onTick()
    {
        boolean active = _service.getLive().getDeals().active(_shop.getId(), System.currentTimeMillis()) != null;
        if (!active && !_flashShown)
            return false;
        _frame++;
        return true;
    }

    // Shop changes, from any customer or the timer ===================================================================

    @Override
    public void listingChanged(Shop shop, Listing listing)
    {
        refresh();
    }

    @Override
    public void layoutChanged(Shop shop)
    {
        refresh();
    }

    @Override
    public void settingsChanged(Shop shop)
    {
        refresh();
    }

    // Drawing ========================================================================================================

    protected void draw()
    {
        if (getPlayer() == null)
            return;
        drawShop();
        drawControls();
        drawCustomer();
    }

    private void drawShop()
    {
        _shopPage = Math.max(0, Math.min(_shopPage, _shop.getPageCount() - 1));
        PriceCalculator prices = _service.getPrices();

        if (_shop.getSettings().absolutePositions())
        {
            for (int slot = 0; slot < SHOP_SLOTS; slot++)
            {
                Listing listing = _shop.getListing(_shopPage, slot);
                if (listing != null)
                    addButton(listingButton(slot, listing, prices));
            }
            return;
        }

        // Listings the customer can't buy are left out, the rest move up
        int next = 0;
        for (int slot = 0; slot < SHOP_SLOTS; slot++)
        {
            Listing listing = _shop.getListing(_shopPage, slot);
            if (listing != null && _service.getTrades().isAvailable(listing, _customer))
                addButton(listingButton(next++, listing, prices));
        }
    }

    private Button listingButton(int slot, Listing listing, PriceCalculator prices)
    {
        if (listing.getAmount() <= 0)
            return new Button(slot, Icons.icon(Material.BLACK_STAINED_GLASS_PANE, Messages.get("shop.sold-out")));

        ItemStack item = ItemKeys.template(listing.getItem());
        if (!_service.getTrades().isAvailable(listing, _customer))
            return new Button(slot, Icons.withLore(item, Messages.lines("shop.not-available")));

        if (listing.getPrice() instanceof PriceRule.Custom)
        {
            return new Button(slot, Icons.withLore(item, List.of(amountLine(listing.getAmount()), Messages.get("shop.custom-price"), "")),
                    e -> openNext(new CustomPriceBuyMenu(_service, _shop, listing)));
        }

        List<String> lore = priceLines(Messages.get("shop.buy-action"), listing.getAmount(), amount -> prices.buyPrice(_shop, listing, amount));
        if (listing.getPrice() instanceof PriceRule.Material)
            addMoodLine(lore, listing.getItem());
        return new Button(slot, Icons.withLore(item, lore), e -> buy(listing, e));
    }

    private void drawControls()
    {
        addButton(marketButton());
        addButton(wantedButton());
        addButton(flashButton());

        button(27, Icons.icon(Material.DARK_OAK_SIGN, Messages.get("shop.page-back")), e -> changeShopPage(-1));
        button(35, Icons.icon(Material.DARK_OAK_SIGN, Messages.get("shop.page-next")), e -> changeShopPage(1));
        button(30, Icons.icon(Material.BIRCH_SIGN, Messages.get("shop.inventory-back")), e -> changeCustomerPage(-1));
        button(32, Icons.icon(Material.BIRCH_SIGN, Messages.get("shop.inventory-next")), e -> changeCustomerPage(1));

        ItemStack tab = switch (TABS[_tab])
        {
            case BLOCKS -> Icons.icon(Material.STONE, Messages.get("shop.tab-blocks"));
            case GEAR -> Icons.icon(Material.NETHERITE_CHESTPLATE, Messages.get("shop.tab-gear"));
            case SHULKER_BOX -> Icons.icon(Material.CYAN_SHULKER_BOX, Messages.get("shop.tab-shulkers"));
        };
        button(31, tab, e ->
        {
            _tab = (_tab + 1) % TABS.length;
            _customerPage = 0;
            refresh();
        });

        ItemStack hotbar = _inventory.isIncludeHotbarAndArmor()
                ? Icons.icon(Material.SHIELD, Messages.get("shop.hotbar-included"))
                : Icons.icon(Material.CHEST, Messages.get("shop.hotbar-excluded"));
        button(33, hotbar, e ->
        {
            _inventory.setIncludeHotbarAndArmor(!_inventory.isIncludeHotbarAndArmor());
            refresh();
        });
    }

    // Market =========================================================================================================

    private void addMoodLine(List<String> lore, ItemKey item)
    {
        String mood = moodText(_service.getMarket().moodFactor(item));
        if (mood != null)
            lore.add(Messages.get("market.mood-line", "mood", mood));
    }

    /**
     * "&amp;a▲ +7%" for 1.07, null for no change.
     */
    private static String moodText(double factor)
    {
        long percent = Math.round((factor - 1) * 100);
        if (percent == 0)
            return null;
        return Messages.get(percent > 0 ? "market.mood-up" : "market.mood-down", "percent", Math.abs(percent));
    }

    /**
     * Slot 28: how each kind of item is priced today.
     */
    private Button marketButton()
    {
        Map<String, Double> moods = _service.getMarket().todaysMood();
        if (moods.isEmpty())
            return new Button(28, Icons.filler(Material.RED_STAINED_GLASS_PANE));
        List<String> lore = new ArrayList<>();
        for (Map.Entry<String, Double> mood : moods.entrySet())
        {
            String text = moodText(mood.getValue());
            lore.add(Messages.get("market.button-line", "tag", mood.getKey(), "mood", text == null ? Messages.get("market.mood-none") : text));
        }
        return new Button(28, Icons.icon(Material.FILLED_MAP, Messages.get("market.button"), lore));
    }

    /**
     * Slot 29: what the shop wants now, if anything.
     */
    private Button wantedButton()
    {
        List<WantedRequests.Wanted> wanted = _service.getMarket().activeWanted(_shop);
        if (wanted.isEmpty())
            return new Button(29, Icons.filler(Material.RED_STAINED_GLASS_PANE));
        List<String> lore = new ArrayList<>();
        for (WantedRequests.Wanted request : wanted)
            lore.add(Messages.get("market.wanted-line", "item", MarketEvents.itemName(ItemKeys.template(request.item()).getType()),
                    "percent", Math.round(request.bonus() * 100)));
        long left = wanted.getFirst().endMillis() - System.currentTimeMillis();
        lore.add(Messages.get("market.wanted-ends", "hours", left / 3_600_000, "minutes", left / 60_000 % 60));
        lore.add(Messages.get("market.wanted-hint"));
        ItemStack icon = Icons.glow(Icons.icon(ItemKeys.template(wanted.getFirst().item()).getType(), Messages.get("market.wanted"), lore));
        return new Button(29, icon);
    }

    // Flash deals ====================================================================================================

    private static final Material[] FLASH_FRAMES = { Material.NETHER_STAR, Material.YELLOW_STAINED_GLASS_PANE,
            Material.GOLD_BLOCK, Material.ORANGE_STAINED_GLASS_PANE };

    /**
     * Slot 34: a blinking button to the flash deals while they are on, otherwise a filler.
     */
    private Button flashButton()
    {
        FlashDeal deal = _service.getLive().getDeals().active(_shop.getId(), System.currentTimeMillis());
        _flashShown = deal != null;
        if (deal == null)
            return new Button(34, Icons.filler(Material.RED_STAINED_GLASS_PANE));

        List<String> names = Messages.lines("flash.button");
        ItemStack icon = Icons.glow(Icons.icon(FLASH_FRAMES[_frame % FLASH_FRAMES.length], names.get(_frame % names.size()),
                Messages.lines("flash.button-lore", "time", FlashDealMenu.timeLeft(deal.millisLeft(System.currentTimeMillis())))));
        return new Button(34, icon, e -> openNext(new FlashDealMenu(_service, _shop, false)));
    }

    private void drawCustomer()
    {
        if (_shop.getSettings().customersCanOnlyBuy())
        {
            ItemStack blue = Icons.filler(Material.BLUE_STAINED_GLASS_PANE);
            for (int i = 0; i < CUSTOMER_SLOTS; i++)
                button(CUSTOMER_START + i, blue);
            return;
        }

        List<Customer.SellEntry> entries = _customer.sellEntries(TABS[_tab], _inventory.isIncludeHotbarAndArmor());
        int pages = Math.max(1, (entries.size() + CUSTOMER_SLOTS - 1) / CUSTOMER_SLOTS);
        _customerPage = Math.max(0, Math.min(_customerPage, pages - 1));

        int first = _customerPage * CUSTOMER_SLOTS;
        for (int i = first; i < Math.min(entries.size(), first + CUSTOMER_SLOTS); i++)
            addButton(entryButton(CUSTOMER_START + i - first, entries.get(i)));
    }

    private Button entryButton(int slot, Customer.SellEntry entry)
    {
        PriceCalculator prices = _service.getPrices();
        ItemStack item = ItemKeys.template(entry.getItem());

        if (entry.isShulker())
        {
            Map<ItemKey, Integer> contents = new LinkedHashMap<>(_inventory.shulkerContents(entry.getShulkerSlot()));
            if (contents.isEmpty())
                return new Button(slot, Icons.icon(Material.PURPLE_STAINED_GLASS, Messages.get("shop.shulker-cleared")));
            contents.keySet().removeIf(content -> !_service.getTrades().buysItem(_shop, content));
            if (contents.isEmpty())
                return new Button(slot, Icons.withLore(item, List.of(amountLine(1), Messages.get("shop.shulker-nothing"))));
            double price = prices.shulkerPrice(_shop, contents);
            return new Button(slot, Icons.withLore(item, List.of(amountLine(1), Messages.get("shop.shulker-sell", "price", Icons.money(price)), "")),
                    e -> sellShulker(entry.getShulkerSlot()));
        }

        if (!_service.getTrades().buysItem(_shop, entry.getItem()))
            return new Button(slot, Icons.withLore(item, List.of(amountLine(entry.getAmount()), Messages.get("shop.doesnt-buy"))));

        List<String> lore = priceLines(Messages.get("shop.sell-action"), entry.getAmount(), amount -> prices.sellPrice(_shop, entry.getItem(), amount));
        double wanted = _service.getMarket().wantedBonus(_shop.getId(), entry.getItem());
        if (wanted > 0)
            lore.add(Messages.get("market.wanted-lore", "percent", Math.round(wanted * 100)));
        if (_service.getRules().uniquePrice(entry.getItem()) == null)
            addMoodLine(lore, entry.getItem());
        Overflow overflow = _service.getRules().uniquePrice(entry.getItem()) == null ? _service.getRules().overflow(entry.getItem()) : null;
        if (overflow != null)
        {
            int dropsIn = overflow.softCap() - prices.overflowStock(_shop, entry.getItem());
            lore.add(" ");
            lore.add(dropsIn <= 0 ? Messages.get("shop.price-drops") : Messages.get("shop.price-drops-in", "amount", dropsIn));
        }
        PriceCalculator.LimitStatus limit = prices.limitStatus(_shop, entry.getItem());
        if (limit.unitsLeft() > 0)
        {
            lore.add(" ");
            lore.add(Messages.get("shop.full-price-for", "amount", limit.unitsLeft()));
        } else if (limit.unitsLeft() == 0)
        {
            lore.add(" ");
            lore.add(Messages.get("shop.shop-full", "percent", Math.round(limit.multiplier() * 100)));
            lore.add(Messages.get("shop.shop-full-refills"));
        }
        return new Button(slot, Icons.withLore(item, lore), e -> sell(entry, e));
    }

    private static String amountLine(int amount)
    {
        return Messages.get("shop.amount", "amount", amount);
    }

    private interface PriceFor
    {
        double price(int amount);
    }

    /**
     * Amount line and the price of 1, 8, 64 and all of it, as far as there are that many.
     */
    private static List<String> priceLines(String action, int amount, PriceFor price)
    {
        List<String> lore = new ArrayList<>();
        lore.add(amountLine(amount));
        if (amount >= 1)
            lore.add(Messages.get("shop.price-1", "action", action, "price", Icons.money(price.price(1))));
        if (amount >= 8)
            lore.add(Messages.get("shop.price-8", "action", action, "price", Icons.money(price.price(8))));
        if (amount >= 64)
            lore.add(Messages.get("shop.price-64", "action", action, "price", Icons.money(price.price(64))));
        if (amount != 1 && amount != 8 && amount != 64)
            lore.add(Messages.get("shop.price-all", "action", action, "price", Icons.money(price.price(amount))));
        return lore;
    }

    // Clicks =========================================================================================================

    private boolean cooldown()
    {
        long now = System.currentTimeMillis();
        long cooldown = (long) (_service.getPlugin().getConfig().getDouble("shop.click-cooldown-seconds") * 1000);
        if (now - _lastClick < cooldown)
            return true;
        _lastClick = now;
        return false;
    }

    /**
     * 1, 8, 64 or everything there is, by click type; 0 for other clicks.
     */
    private static int clickAmount(ClickKind click, int available)
    {
        int amount = switch (click)
        {
            case LEFT -> 1;
            case RIGHT -> 8;
            case SHIFT_LEFT -> 64;
            case SHIFT_RIGHT -> available;
            default -> 0;
        };
        return Math.min(amount, available);
    }

    private void changeShopPage(int change)
    {
        _shopPage = Math.max(0, Math.min(_shopPage + change, _shop.getPageCount() - 1));
        refresh();
    }

    private void changeCustomerPage(int change)
    {
        _customerPage = Math.max(0, _customerPage + change);
        refresh();
    }

    private void buy(Listing listing, Click e)
    {
        if (cooldown())
            return;
        int amount = clickAmount(e.kind(), listing.getAmount());
        if (amount <= 0)
            return;

        Player player = getPlayer();
        double price = _service.getPrices().buyPrice(_shop, listing, amount);
        TradeResult result = _service.getTrades().buy(_shop, _customer, listing, amount);
        switch (result.status())
        {
            case SUCCESS -> Messages.send(player, "shop.bought", "price", TextUtils.Round(result.price()), "balance", TextUtils.Round(balance()));
            case NOT_ENOUGH_MONEY -> Messages.send(player, "shop.not-enough-money", "price", TextUtils.Round(price), "balance", TextUtils.Round(balance()));
            case PAYMENT_FAILED -> Messages.send(player, "shop.buy-payment-failed");
            default ->
            {
                // Stock or availability changed under the click; the menu redraws with the current state
            }
        }
        refresh();
    }

    private void sell(Customer.SellEntry entry, Click e)
    {
        if (cooldown())
            return;
        int amount = clickAmount(e.kind(), entry.getAmount());
        if (amount <= 0)
            return;

        TradeResult result = _service.getTrades().sell(_shop, _customer, entry.getItem(), amount);
        sellMessage(result);
        refresh();
    }

    private void sellShulker(int slot)
    {
        if (cooldown())
            return;
        sellMessage(_service.getTrades().sellShulker(_shop, _customer, slot));
        refresh();
    }

    private void sellMessage(TradeResult result)
    {
        Player player = getPlayer();
        switch (result.status())
        {
            case SUCCESS -> Messages.send(player, "shop.sold", "price", TextUtils.Round(result.price()), "balance", TextUtils.Round(balance()));
            case NOT_ENOUGH_ITEMS -> Messages.send(player, "shop.inventory-changed");
            case PAYMENT_FAILED -> Messages.send(player, "shop.sell-payment-failed");
            default ->
            {
            }
        }
    }

    private double balance()
    {
        return _service.getWallet().balance(getPlayer().getUniqueId());
    }
}
