package imu.GS.menu;

import imu.iAPI.Menus.Viewer;
import imu.iAPI.Utilities.InvUtil;
import imu.iAPI.Utilities.ItemUtils;
import imu.iAPI.Utilities.TextUtils;
import imu.GS.text.Messages;
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
public class CustomPriceBuyMenu extends GsMenu implements ShopListener
{
    private static final Material[] BLOCKS = { Material.IRON_BLOCK, Material.COPPER_BLOCK, Material.GOLD_BLOCK,
            Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK, Material.NETHERITE_BLOCK, Material.CRYING_OBSIDIAN };
    private static final Material[] WOOLS = { Material.WHITE_WOOL, Material.YELLOW_WOOL, Material.GREEN_WOOL,
            Material.BLUE_WOOL, Material.PURPLE_WOOL, Material.BROWN_WOOL, Material.BLACK_WOOL };
    private static final int[] SINGLE_AMOUNTS = { 1, 4, 8, 16, 32, 64 };

    private final Shop _shop;
    private final Listing _listing;
    private Customer _customer;
    private int _selected;

    public CustomPriceBuyMenu(GsService service, Shop shop, Listing listing)
    {
        super(service, Messages.get("custom-price.title", "item", displayName(listing)), 54);
        _shop = shop;
        _listing = listing;
        _selected = bundle();
    }

    private static String displayName(Listing listing)
    {
        return ItemUtils.GetNameOrMaterial(ItemKeys.template(listing.getItem()));
    }

    private int bundle()
    {
        return _listing.getPrice() instanceof PriceRule.Custom custom ? custom.bundleSize() : 1;
    }

    @Override
    protected void onOpen(Viewer viewer)
    {
        if (_customer != null)
            return;
        // A custom price is paid from the whole inventory, hotbar included, like before
        PlayerInventoryAdapter inventory = new PlayerInventoryAdapter(getPlayer());
        inventory.setIncludeHotbarAndArmor(true);
        _customer = _service.customer(getPlayer(), inventory);
        _shop.addListener(this);
    }

    @Override
    protected void onClose(Viewer viewer)
    {
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

    protected void draw()
    {
        if (getPlayer() == null)
            return;
        if (!(_listing.getPrice() instanceof PriceRule.Custom))
        {
            backToShop();
            return;
        }

        ItemStack black = Icons.filler(Material.BLACK_STAINED_GLASS_PANE);
        for (int slot = 27; slot < 54; slot++)
            button(slot, black);

        drawCost();
        drawAmounts();

        button(45, Icons.icon(Material.RED_STAINED_GLASS_PANE, Messages.get("custom-price.back")), e -> backToShop());
        button(48, Icons.icon(Material.PURPLE_STAINED_GLASS_PANE, Messages.get("custom-price.buying-right")));
        button(50, Icons.icon(Material.PURPLE_STAINED_GLASS_PANE, Messages.get("custom-price.buying-left")));
        button(49, Icons.withLore(ItemKeys.template(_listing.getItem()),
                Messages.lines("custom-price.info", "bundle", bundle(), "stock", _listing.getAmount())));
        button(53, Icons.icon(Material.GREEN_STAINED_GLASS_PANE, Messages.get("custom-price.buy"),
                Messages.lines("custom-price.buy-lore")),
                e -> buy(e.isLeft()));
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
                    Messages.get(have >= need.amount() ? "custom-price.you-have-enough" : "custom-price.you-have-too-few", "amount", have),
                    Messages.get("custom-price.costs", "amount", need.amount(), "per-bundle", perBundle, "bundles", _selected / custom.bundleSize())));
            button(slot, stack);
        }
        if (cost.money() > 0 && slot < 27)
        {
            double balance = _service.getWallet().balance(getPlayer().getUniqueId());
            button(slot, Icons.glow(Icons.icon(Material.PAPER, Messages.get("custom-price.extra"),
                    Messages.get("custom-price.money", "price", Icons.money(cost.money())),
                    Messages.get(balance >= cost.money() ? "custom-price.you-have-enough" : "custom-price.you-have-too-few", "amount", Icons.money(balance)))));
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
                button(slot, Icons.icon(Material.BARRIER, Messages.get("custom-price.not-in-stock", "amount", amount),
                        Messages.get("custom-price.not-in-stock-lore", "stock", _listing.getAmount())));
                continue;
            }

            List<String> lore = new ArrayList<>();
            boolean canPay = canPay(amount);
            if (!canPay)
                lore.add(Messages.get("custom-price.missing-items"));
            ItemStack stack = Icons.icon(icons[i], Messages.get("custom-price.buy-amount", "amount", amount), lore);
            if (!canPay)
                Icons.glow(stack);
            button(slot, stack, e ->
            {
                if (_selected == amount)
                    return;
                _selected = amount;
                refresh();
            });

            if (amount == _selected)
                button(slot + 9, Icons.icon(Material.REDSTONE_TORCH, Messages.get("custom-price.selected")));
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
            Messages.send(player, "custom-price.only-some", "amount", amount);
            refresh();
            return;
        }

        TradeResult result = _service.getTrades().buyCustom(_shop, _customer, _listing, amount);
        double balance = _service.getWallet().balance(player.getUniqueId());
        switch (result.status())
        {
            case SUCCESS -> Messages.send(player, "custom-price.bought", "price", TextUtils.Round(result.price()), "balance", TextUtils.Round(balance));
            case NOT_ENOUGH_MONEY -> Messages.send(player, "custom-price.not-enough-money", "balance", TextUtils.Round(balance));
            case NOT_ENOUGH_ITEMS -> Messages.send(player, "custom-price.not-enough-items");
            case PAYMENT_FAILED -> Messages.send(player, "custom-price.payment-failed");
            case NO_LISTING, WRONG_PRICE_TYPE -> Messages.send(player, "custom-price.shop-changed");
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
