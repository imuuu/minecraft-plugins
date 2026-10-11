package imu.GS.menu;

import imu.GS.GsService;
import imu.GS.bukkit.ItemKeys;
import imu.GS.bukkit.PlayerInventoryAdapter;
import imu.GS.engine.customer.Customer;
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
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Buying a listing whose price is paid with items and money. The customer picks an amount, sees what it costs
 * against what they have, and buys.
 *
 * <pre>
 *  0-26  cost: every item (have / need) and the money
 *  28-34 amount buttons, 37-43 marker under the selected one
 *  45 back   48 -&gt;   49 the item   50 &lt;-   53 buy
 * </pre>
 */
public class CustomPriceBuyMenu extends CustomInventory implements ShopListener
{
    private static final Material[] BLOCKS = { Material.IRON_BLOCK, Material.COPPER_BLOCK, Material.GOLD_BLOCK,
            Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK, Material.NETHERITE_BLOCK, Material.CRYING_OBSIDIAN };
    private static final Material[] WOOLS = { Material.WHITE_WOOL, Material.YELLOW_WOOL, Material.GREEN_WOOL,
            Material.BLUE_WOOL, Material.PURPLE_WOOL, Material.BROWN_WOOL, Material.BLACK_WOOL };
    private static final int[] SINGLE_AMOUNTS = { 1, 4, 8, 16, 32, 64 };

    private final GsService _service;
    private final Shop _shop;
    private final Listing _listing;
    private Customer _customer;
    private int _selected;

    public CustomPriceBuyMenu(GsService service, Shop shop, Listing listing)
    {
        super(service.getPlugin(), "Buying " + displayName(listing), 54);
        _service = service;
        _shop = shop;
        _listing = listing;
        _selected = bundle();
    }

    private static String displayName(Listing listing)
    {
        return Metods._ins.GetItemDisplayName(ItemKeys.template(listing.getItem()));
    }

    private int bundle()
    {
        return _listing.getPrice() instanceof PriceRule.Custom custom ? custom.bundleSize() : 1;
    }

    @Override
    public INVENTORY_AREA setInventoryLock()
    {
        return INVENTORY_AREA.UPPER_LOWER_INV;
    }

    @Override
    public void onAwake()
    {
        _customer = _service.customer(getPlayer(), new PlayerInventoryAdapter(getPlayer()));
    }

    @Override
    public void onOpen()
    {
        super.onOpen();
        _shop.addListener(this);
        refresh();
    }

    @Override
    public void onClose()
    {
        super.onClose();
        _shop.removeListener(this);
    }

    @Override
    public void listingChanged(Shop shop, Listing listing)
    {
        if (listing == _listing)
            refresh();
    }

    @Override
    public void layoutChanged(Shop shop)
    {
        refresh();
    }

    // Drawing ========================================================================================================

    private void refresh()
    {
        if (getPlayer() == null)
            return;
        if (!(_listing.getPrice() instanceof PriceRule.Custom))
        {
            backToShop();
            return;
        }

        clearButtons();
        ItemStack black = Icons.filler(Material.BLACK_STAINED_GLASS_PANE);
        for (int slot = 27; slot < 54; slot++)
            addButton(new Button(slot, black));

        drawCost();
        drawAmounts();

        addButton(new Button(45, Icons.icon(Material.RED_STAINED_GLASS_PANE, "&c<= BACK"), e -> backToShop()));
        addButton(new Button(48, Icons.icon(Material.PURPLE_STAINED_GLASS_PANE, "&eYou are buying this &b==>")));
        addButton(new Button(50, Icons.icon(Material.PURPLE_STAINED_GLASS_PANE, "&b<== &eYou are buying this")));
        addButton(new Button(49, Icons.withLore(ItemKeys.template(_listing.getItem()),
                List.of("&9Minimum amount to buy: &a" + bundle(), "&9Stock Amount: &a" + _listing.getAmount()))));
        addButton(new Button(53, Icons.icon(Material.GREEN_STAINED_GLASS_PANE, "&9BUY",
                "&bM1: &aBuying &9and going &cback &9to Shop menu!", "&bM2: &aBuying &9and staying &6this &9menu"),
                e -> buy(e.isLeftClick())));
        updateButtons(true);
    }

    private void drawCost()
    {
        PriceCalculator.CustomCost cost = _service.getPrices().customCost(_listing, _selected);
        PriceRule.Custom custom = (PriceRule.Custom) _listing.getPrice();
        int slot = 0;
        for (int i = 0; i < cost.items().size() && slot < 27; i++, slot++)
        {
            PriceRule.ItemCost need = cost.items().get(i);
            int have = _customer.inventory().count(need.item());
            int perBundle = custom.items().get(i).amount();
            ItemStack stack = Icons.withLore(ItemKeys.template(need.item()), List.of(
                    "&eYou have: " + (have >= need.amount() ? "&a" : "&c") + have,
                    "&9Cost you:&6 " + need.amount() + " &e(&8" + perBundle + " &3x &8" + _selected / custom.bundleSize() + "&e)"));
            addButton(new Button(slot, stack));
        }
        if (cost.money() > 0 && slot < 27)
        {
            double balance = _service.getWallet().balance(getPlayer().getUniqueId());
            addButton(new Button(slot, Icons.glow(Icons.icon(Material.PAPER, "&6Extra values",
                    "&9Money&6: &a" + Icons.money(cost.money()),
                    "&eYou have: " + (balance >= cost.money() ? "&a" : "&c") + Icons.money(balance)))));
        }
    }

    private void drawAmounts()
    {
        int bundle = bundle();
        List<Integer> amounts = new ArrayList<>();
        Material[] icons = bundle <= 1 ? BLOCKS : WOOLS;
        for (int i = 0; i < 6; i++)
            amounts.add(bundle <= 1 ? SINGLE_AMOUNTS[i] : bundle * (i + 1));
        amounts.add(_listing.getAmount());

        for (int i = 0; i < amounts.size(); i++)
        {
            int amount = amounts.get(i);
            int slot = 28 + i;
            if (amount > _listing.getAmount() || amount < bundle)
            {
                addButton(new Button(slot, Icons.icon(Material.BARRIER, "&cUnable to Buy: &4" + amount,
                        "&c&lNot Enough in Stock! &8(&7In Stock:&3 " + _listing.getAmount() + "&8)")));
                continue;
            }

            List<String> lore = new ArrayList<>();
            boolean canPay = canPay(amount);
            if (!canPay)
                lore.add("&c&lYou dont have enough items!");
            ItemStack stack = Icons.icon(icons[i], "&9BUY&6 " + amount, lore);
            if (!canPay)
                Icons.glow(stack);
            addButton(new Button(slot, stack, e ->
            {
                if (_selected == amount)
                    return;
                _selected = amount;
                refresh();
            }));

            if (amount == _selected)
                addButton(new Button(slot + 9, Icons.icon(Material.REDSTONE_TORCH, "&a^SELECTED^")));
        }
    }

    private boolean canPay(int amount)
    {
        PriceCalculator.CustomCost cost = _service.getPrices().customCost(_listing, amount);
        for (PriceRule.ItemCost need : cost.items())
        {
            if (_customer.inventory().count(need.item()) < need.amount())
                return false;
        }
        return _service.getWallet().balance(getPlayer().getUniqueId()) >= cost.money();
    }

    // Clicks =========================================================================================================

    private void buy(boolean backAfter)
    {
        Player player = getPlayer();
        int amount = Math.min(_selected, _listing.getAmount());
        if (amount != _selected)
        {
            player.sendMessage(Metods.msgC("&cYou were able to buy only &2" + amount + " &cbecause there weren't enough items in stock!"));
            refresh();
            return;
        }

        TradeResult result = _service.getTrades().buyCustom(_shop, _customer, _listing, amount);
        double balance = _service.getWallet().balance(player.getUniqueId());
        switch (result.status())
        {
            case SUCCESS -> player.sendMessage(Metods.msgC("&4Buy &9confirmed! Purchase value: &e " + Metods.Round(result.price())
                    + " &2$&9. &9Balance: &e" + Metods.Round(balance) + "&2$&9."));
            case NOT_ENOUGH_MONEY -> player.sendMessage(Metods.msgC("&4Buy &cCanceled! &9Balance isn't enough! &9Balance: &e" + Metods.Round(balance) + "&2$&9."));
            case NOT_ENOUGH_ITEMS -> player.sendMessage(Metods.msgC("&4Buy &cCanceled! &9You don't have the items to pay with."));
            case PAYMENT_FAILED -> player.sendMessage(Metods.msgC("&4Buy &cCanceled! &9Payment failed."));
            case NO_LISTING, WRONG_PRICE_TYPE -> player.sendMessage(Metods.msgC("&cThe shop has changed, please open it again."));
            default ->
            {
            }
        }

        if (result.isSuccess() && backAfter)
            backToShop();
        else
            refresh();
    }

    private void backToShop()
    {
        Player player = getPlayer();
        Bukkit.getScheduler().runTask(_service.getPlugin(), () -> _service.openShop(player, _shop));
    }
}
