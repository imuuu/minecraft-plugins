package imu.GS.menu;

import imu.iAPI.Utilities.InvUtil;
import imu.iAPI.Utilities.TextUtils;
import imu.GS.text.Messages;
import imu.GS.GsService;
import imu.GS.bukkit.ItemKeys;
import imu.GS.bukkit.PlayerInventoryAdapter;
import imu.GS.engine.customer.Customer;
import imu.GS.engine.live.FlashDeal;
import imu.GS.engine.live.FlashDeals;
import imu.GS.engine.live.FlashOffer;
import imu.GS.engine.shop.Shop;
import imu.GS.engine.trade.TradeResult;
import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.InvUtil.CustomInventory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A shop's flash deal. Opened from the shop it can be bought from; as a peek, from anywhere, it shows only the
 * first few offers and nothing can be bought.
 *
 * <pre>
 *   4      time left
 *   9-17   the offers, centred
 *   18     back to the shop (not in a peek)
 * </pre>
 */
public class FlashDealMenu extends CustomInventory implements FlashDeals.Listener
{
    private final GsService _service;
    private final Shop _shop;
    private final boolean _peek;
    private Customer _customer;
    private BukkitTask _timer;
    private long _lastClick;

    /**
     * @param peek only look at a few offers, from anywhere
     */
    public FlashDealMenu(GsService service, Shop shop, boolean peek)
    {
        super(service.getPlugin(), Messages.get(peek ? "flash.peek-title" : "flash.title"), 27);
        _service = service;
        _shop = shop;
        _peek = peek;
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
        _service.getLive().getDeals().addListener(this);
        _timer = Bukkit.getScheduler().runTaskTimer(_service.getPlugin(), this::refresh, 20, 20);
        refresh();
    }

    @Override
    public void onClose()
    {
        super.onClose();
        _service.getLive().getDeals().removeListener(this);
        if (_timer != null)
            _timer.cancel();
    }

    @Override
    public void changed(UUID shopId)
    {
        if (shopId.equals(_shop.getId()))
            refresh();
    }

    public void refresh()
    {
        if (getPlayer() == null)
            return;
        clearButtons();
        for (int slot = 0; slot < 27; slot++)
            addButton(new Button(slot, Icons.filler(Material.BLACK_STAINED_GLASS_PANE)));

        long now = System.currentTimeMillis();
        FlashDeal deal = _service.getLive().getDeals().active(_shop.getId(), now);
        if (!_peek)
            addButton(new Button(18, Icons.icon(Material.RED_STAINED_GLASS_PANE, Messages.get("flash.back")), e -> backToShop()));
        if (deal == null)
        {
            addButton(new Button(13, Icons.icon(Material.BARRIER, Messages.get("flash.over"), Messages.lines("flash.over-lore"))));
            updateButtons(true);
            return;
        }

        List<String> header = new ArrayList<>();
        header.add(Messages.get("flash.ends-in", "time", timeLeft(deal.millisLeft(now))));
        header.add(Messages.get("flash.no-sell-back"));
        if (_peek)
            header.add(Messages.get("flash.visit", "shop", _shop.getDisplayName()));
        addButton(new Button(4, Icons.glow(Icons.icon(Material.CLOCK, Messages.get("flash.header"), header))));

        List<FlashOffer> offers = deal.offers();
        int shown = Math.min(offers.size(), 9);
        int first = 9 + (9 - shown) / 2;
        int peekCount = _service.getPlugin().getConfig().getInt("live.flash-deals.peek-offers", 2);
        for (int i = 0; i < shown; i++)
        {
            FlashOffer offer = offers.get(i);
            if (_peek && i >= peekCount)
                addButton(new Button(first + i, Icons.icon(Material.GRAY_DYE, Messages.get("flash.hidden"), Messages.lines("flash.hidden-lore"))));
            else
                addButton(offerButton(first + i, offer));
        }
        if (_peek)
            addButton(new Button(22, Icons.icon(Material.OAK_SIGN, Messages.get("flash.go-to-shop"), "&7" + _shop.getDisplayName())));
        updateButtons(true);
    }

    private Button offerButton(int slot, FlashOffer offer)
    {
        ItemStack item = ItemKeys.template(offer.getItem());
        double normal = _service.getRules().value(item) * _shop.getSettings().sellMultiplier();
        int left = offer.getStock();
        int canBuy = offer.canBuy(_customer.id());

        List<String> lore = new ArrayList<>();
        lore.add(" ");
        lore.add(Messages.get("flash.price", "price", Icons.money(offer.getUnitPrice())));
        if (normal > offer.getUnitPrice())
            lore.add(Messages.get("flash.normally", "normal", TextUtils.Round(normal), "percent", Math.round((1 - offer.getUnitPrice() / normal) * 100)));
        lore.add(left <= 0 ? Messages.get("flash.sold-out") : Messages.get("flash.left", "amount", left));
        lore.add(Messages.get("flash.can-buy", "amount", canBuy, "limit", offer.getPerPlayerLimit()));
        if (!_peek && canBuy > 0)
        {
            lore.add(" ");
            lore.add(Messages.get("flash.clicks"));
        }
        ItemStack icon = Icons.withLore(item, lore);
        icon.setAmount(Math.max(1, Math.min(left, icon.getMaxStackSize())));
        if (_peek || canBuy <= 0)
            return new Button(slot, icon);
        return new Button(slot, icon, e -> buy(offer, e.getClick()));
    }

    private void buy(FlashOffer offer, ClickType click)
    {
        long now = System.currentTimeMillis();
        long cooldown = (long) (_service.getPlugin().getConfig().getDouble("shop.click-cooldown-seconds") * 1000);
        if (now - _lastClick < cooldown)
            return;
        _lastClick = now;

        int canBuy = offer.canBuy(_customer.id());
        int amount = Math.min(canBuy, switch (click)
        {
            case LEFT -> 1;
            case RIGHT -> 8;
            case SHIFT_LEFT -> 64;
            case SHIFT_RIGHT -> canBuy;
            default -> 0;
        });
        if (amount <= 0)
            return;

        Player player = getPlayer();
        TradeResult result = _service.getTrades().buyFlash(_shop, _customer, _service.getLive().getDeals(), offer.getId(), amount, now);
        switch (result.status())
        {
            case SUCCESS -> Messages.send(player, "flash.bought", "price", TextUtils.Round(result.price()),
                    "balance", TextUtils.Round(_service.getWallet().balance(player.getUniqueId())));
            case NOT_ENOUGH_MONEY -> Messages.send(player, "flash.not-enough-money", "price", TextUtils.Round(offer.getUnitPrice() * amount));
            case DEAL_OVER -> Messages.send(player, "flash.ended");
            case PAYMENT_FAILED -> Messages.send(player, "flash.payment-failed");
            default ->
            {
                // Someone bought the last ones first; the redraw shows what is left
            }
        }
        refresh();
    }

    private void backToShop()
    {
        Player player = getPlayer();
        Bukkit.getScheduler().runTask(_service.getPlugin(), () -> _service.openShop(player, _shop));
    }

    static String timeLeft(long millis)
    {
        long seconds = millis / 1000;
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }
}
