package imu.GS.menu;

import imu.GS.GsService;
import imu.GS.bukkit.ItemKeys;
import imu.GS.bukkit.PlayerInventoryAdapter;
import imu.GS.engine.ItemCategory;
import imu.GS.engine.ItemKey;
import imu.GS.engine.customer.Customer;
import imu.GS.engine.pricing.Overflow;
import imu.GS.engine.pricing.PriceCalculator;
import imu.GS.engine.pricing.PriceRule;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Shop;
import imu.GS.engine.shop.ShopListener;
import imu.GS.engine.trade.TradeResult;
import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Other.Metods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The shop as a customer sees it: the shop's listings on top, their own sellable items at the bottom.
 *
 * <pre>
 *  0-26  shop page (27 listings)
 *  27 &lt;&lt; Shop   28,29 -   30 &lt;&lt; Inv   31 tab   32 Inv &gt;&gt;   33 hotbar/armor   34 -   35 Shop &gt;&gt;
 *  36-53 customer's items (18 per page)
 * </pre>
 *
 * Left click buys or sells 1, right click 8, shift + left 64, shift + right all of it.
 */
public class ShopMenu extends CustomInventory implements ShopListener
{
    private static final int SHOP_SLOTS = Shop.PAGE_SIZE;
    private static final int CUSTOMER_START = 36;
    private static final int CUSTOMER_SLOTS = 18;
    private static final ItemCategory[] TABS = { ItemCategory.BLOCKS, ItemCategory.GEAR, ItemCategory.SHULKER_BOX };

    private final GsService _service;
    private final Shop _shop;
    private PlayerInventoryAdapter _inventory;
    private Customer _customer;
    private int _shopPage = 0;
    private int _customerPage = 0;
    private int _tab = 0;
    private long _lastClick;

    public ShopMenu(GsService service, Shop shop)
    {
        super(service.getPlugin(), shop.getDisplayName(), 54);
        _service = service;
        _shop = shop;
    }

    public Shop getShop()
    {
        return _shop;
    }

    @Override
    public INVENTORY_AREA setInventoryLock()
    {
        return INVENTORY_AREA.UPPER_LOWER_INV;
    }

    @Override
    public void onAwake()
    {
        _inventory = new PlayerInventoryAdapter(getPlayer());
        _customer = _service.customer(getPlayer(), _inventory);
    }

    @Override
    public void onOpen()
    {
        super.onOpen();
        _shop.addViewer();
        _shop.addListener(this);
        _service.menuOpened(this);
        refresh();
    }

    @Override
    public void onClose()
    {
        super.onClose();
        _shop.removeListener(this);
        _service.menuClosed(this);
        _shop.removeViewer();
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

    public void refresh()
    {
        if (getPlayer() == null)
            return;
        clearButtons();
        drawShop();
        drawControls();
        drawCustomer();
        updateButtons(true);
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
            return new Button(slot, Icons.icon(Material.BLACK_STAINED_GLASS_PANE, "&9-"));

        ItemStack item = ItemKeys.template(listing.getItem());
        if (!_service.getTrades().isAvailable(listing, _customer))
            return new Button(slot, Icons.withLore(item, List.of("&cNOT AVAILABLE")));

        if (listing.getPrice() instanceof PriceRule.Custom)
        {
            return new Button(slot, Icons.withLore(item, List.of(amountLine(listing.getAmount()), "&9(&l&eClick&r&9) to see price!", "")),
                    e -> Bukkit.getScheduler().runTask(_service.getPlugin(),
                            () -> new CustomPriceBuyMenu(_service, _shop, listing).open(getPlayer())));
        }

        List<String> lore = priceLines("&2BUY ", listing.getAmount(), amount -> prices.buyPrice(_shop, listing, amount));
        return new Button(slot, Icons.withLore(item, lore), e -> buy(listing, e));
    }

    private void drawControls()
    {
        ItemStack red = Icons.filler(Material.RED_STAINED_GLASS_PANE);
        addButton(new Button(28, red));
        addButton(new Button(29, red));
        addButton(new Button(34, red));

        addButton(new Button(27, Icons.icon(Material.DARK_OAK_SIGN, "&9<< Shop"), e -> changeShopPage(-1)));
        addButton(new Button(35, Icons.icon(Material.DARK_OAK_SIGN, "&9Shop >>"), e -> changeShopPage(1)));
        addButton(new Button(30, Icons.icon(Material.BIRCH_SIGN, "&9<< Inv"), e -> changeCustomerPage(-1)));
        addButton(new Button(32, Icons.icon(Material.BIRCH_SIGN, "&9Inv >>"), e -> changeCustomerPage(1)));

        ItemStack tab = switch (TABS[_tab])
        {
            case BLOCKS -> Icons.icon(Material.STONE, "&9Blocks, Ores...");
            case GEAR -> Icons.icon(Material.NETHERITE_CHESTPLATE, "&9Tools, Armor...");
            case SHULKER_BOX -> Icons.icon(Material.CYAN_SHULKER_BOX, "&9CONTENT OF SHULKER BOXES");
        };
        addButton(new Button(31, tab, e ->
        {
            _tab = (_tab + 1) % TABS.length;
            _customerPage = 0;
            refresh();
        }));

        ItemStack hotbar = _inventory.isIncludeHotbarAndArmor()
                ? Icons.icon(Material.SHIELD, "&9Hotbar and Armor slots &2Included!")
                : Icons.icon(Material.CHEST, "&9Hotbar and Armor slots &cExcluded!");
        addButton(new Button(33, hotbar, e ->
        {
            _inventory.setIncludeHotbarAndArmor(!_inventory.isIncludeHotbarAndArmor());
            refresh();
        }));
    }

    private void drawCustomer()
    {
        if (_shop.getSettings().customersCanOnlyBuy())
        {
            ItemStack blue = Icons.filler(Material.BLUE_STAINED_GLASS_PANE);
            for (int i = 0; i < CUSTOMER_SLOTS; i++)
                addButton(new Button(CUSTOMER_START + i, blue));
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
            Map<ItemKey, Integer> contents = _inventory.shulkerContents(entry.getShulkerSlot());
            double price = prices.shulkerPrice(_shop, contents);
            if (price <= 0)
                return new Button(slot, Icons.icon(Material.PURPLE_STAINED_GLASS, "&6Shulker Content &aCleared!"));
            return new Button(slot, Icons.withLore(item, List.of(amountLine(1), "&9Sell Content with &5" + Icons.money(price), "")),
                    e -> sellShulker(entry.getShulkerSlot()));
        }

        List<String> lore = priceLines("&3SELL ", entry.getAmount(), amount -> prices.sellPrice(_shop, entry.getItem(), amount));
        Overflow overflow = _service.getRules().uniquePrice(entry.getItem()) == null ? _service.getRules().overflow(entry.getItem()) : null;
        if (overflow != null)
        {
            int dropsIn = overflow.softCap() - prices.overflowStock(_shop, entry.getItem());
            lore.add(" ");
            lore.add(dropsIn <= 0 ? "&b&k# &e&nThe Price Drops&b&k #" : "&e&nThe Price Drops in:&2&l " + dropsIn);
        }
        PriceCalculator.LimitStatus limit = prices.limitStatus(_shop, entry.getItem());
        if (limit.unitsLeft() > 0)
        {
            lore.add(" ");
            lore.add("&e&nFull Price for:&2&l " + limit.unitsLeft());
        } else if (limit.unitsLeft() == 0)
        {
            lore.add(" ");
            lore.add("&e&nShop is Full:&c&l " + Math.round(limit.multiplier() * 100) + "% &eprice");
            lore.add("&7The price comes back over time");
        }
        return new Button(slot, Icons.withLore(item, lore), e -> sell(entry, e));
    }

    private static String amountLine(int amount)
    {
        return "&9____ &6Amount: &a" + amount + " &9____";
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
            lore.add(" &bM&21  " + action + " :  &e1  : &5" + Icons.money(price.price(1)));
        if (amount >= 8)
            lore.add(" &bM&42  " + action + " :  &e8  : &5" + Icons.money(price.price(8)));
        if (amount >= 64)
            lore.add("&9S&bM&21  " + action + " : &e64 : &5" + Icons.money(price.price(64)));
        if (amount != 1 && amount != 8 && amount != 64)
            lore.add("&9S&bM&42  " + action + " : &eAll : &5" + Icons.money(price.price(amount)));
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
    private static int clickAmount(ClickType click, int available)
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

    private void buy(Listing listing, InventoryClickEvent e)
    {
        if (cooldown())
            return;
        int amount = clickAmount(e.getClick(), listing.getAmount());
        if (amount <= 0)
            return;

        Player player = getPlayer();
        double price = _service.getPrices().buyPrice(_shop, listing, amount);
        TradeResult result = _service.getTrades().buy(_shop, _customer, listing, amount);
        switch (result.status())
        {
            case SUCCESS -> player.sendMessage(Metods.msgC("&4Buy &9confirmed! Purchase value: &e " + Metods.Round(result.price())
                    + " &2$&9. &9Balance: &e" + Metods.Round(balance()) + "&2$&9."));
            case NOT_ENOUGH_MONEY -> player.sendMessage(Metods.msgC("&4Buy &cCanceled! &9Balance isn't enough! Purchase value: &e "
                    + Metods.Round(price) + " &2$&9. &9Balance: &e" + Metods.Round(balance()) + "&2$&9."));
            case PAYMENT_FAILED -> player.sendMessage(Metods.msgC("&4Buy &cCanceled! &9Payment failed."));
            default ->
            {
                // Stock or availability changed under the click; the menu redraws with the current state
            }
        }
        refresh();
    }

    private void sell(Customer.SellEntry entry, InventoryClickEvent e)
    {
        if (cooldown())
            return;
        int amount = clickAmount(e.getClick(), entry.getAmount());
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
            case SUCCESS -> player.sendMessage(Metods.msgC("&3Sell &9confirmed! Sold value: &e " + Metods.Round(result.price())
                    + " &2$&9. &9Balance: &e" + Metods.Round(balance()) + "&2$&9."));
            case NOT_ENOUGH_ITEMS -> player.sendMessage(Metods.msgC("&cYour inventory changed, the shop was refreshed. Please try again."));
            case PAYMENT_FAILED -> player.sendMessage(Metods.msgC("&3Sell &cCanceled! &9Payment failed."));
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
