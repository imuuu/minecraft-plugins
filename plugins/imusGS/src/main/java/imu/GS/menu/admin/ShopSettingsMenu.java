package imu.GS.menu.admin;

import imu.GS.GsService;
import imu.GS.engine.shop.Shop;
import imu.GS.engine.shop.ShopSettings;
import imu.GS.menu.Icons;
import imu.iAPI.Buttons.Button;
import imu.iAPI.Other.Metods;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

/**
 * A shop's name and settings. Nothing changes until CONFIRM. Left click sets a value, right click resets it to
 * what new shops get from config.yml.
 *
 * <pre>
 *  0 name   2 sell multiplier   4 buy multiplier   6 absolute positions   8 lock
 *  10 expire percent   12 expire cooldown   14 customers can't sell
 *  18 back   20 remove shop   26 confirm
 * </pre>
 */
public class ShopSettingsMenu extends AdminMenu
{
    private String _displayName;
    private double _sellMultiplier;
    private double _buyMultiplier;
    private double _expirePercent;
    private int _expireCooldown;
    private boolean _locked;
    private boolean _customersCanOnlyBuy;
    private boolean _absolutePositions;
    private boolean _removing;

    public ShopSettingsMenu(GsService service, Shop shop)
    {
        super(service, shop, "&6Modifying Shop: " + shop.getDisplayName(), 27);
        ShopSettings settings = shop.getSettings();
        _displayName = shop.getDisplayName();
        _sellMultiplier = settings.sellMultiplier();
        _buyMultiplier = settings.buyMultiplier();
        _expirePercent = settings.expirePercent();
        _expireCooldown = settings.expireCooldownMinutes();
        _locked = settings.locked();
        _customersCanOnlyBuy = settings.customersCanOnlyBuy();
        _absolutePositions = settings.absolutePositions();
    }

    @Override
    protected void refresh()
    {
        clearButtons();
        if (_removing)
        {
            drawRemoveWarning();
            updateButtons(true);
            return;
        }

        for (int slot = 0; slot < 27; slot++)
            addButton(new Button(slot, Icons.filler(Material.BLACK_STAINED_GLASS_PANE)));

        FileConfiguration config = _service.getPlugin().getConfig();

        addButton(value(0, Material.PAPER, "&6Rename shop", _displayName, e ->
                Dialogs.text(this, "Shop name", List.of("Colour codes with & work"), _displayName, text ->
                {
                    if (GsService.plainName(text).isEmpty())
                        return "The name can't be empty";
                    Shop other = _service.getShop(text);
                    if (other != null && other != _shop)
                        return "A shop with that name already exists";
                    _displayName = text.trim();
                    return null;
                })));

        addButton(value(2, Material.GOLD_INGOT, "&6Sell Multiplier", Dialogs.format(_sellMultiplier) + " &7(players buy for price x this)", e ->
        {
            if (e.isRightClick())
            {
                _sellMultiplier = config.getDouble("new-shop.sell-multiplier");
                refresh();
                return;
            }
            Dialogs.number(this, "Sell multiplier", List.of("Players buy from the shop for price x this"), _sellMultiplier, 0, 1000, v -> _sellMultiplier = v);
        }));

        addButton(value(4, Material.IRON_INGOT, "&6Buy Multiplier", Dialogs.format(_buyMultiplier) + " &7(players sell for price x this)", e ->
        {
            if (e.isRightClick())
            {
                _buyMultiplier = config.getDouble("new-shop.buy-multiplier");
                refresh();
                return;
            }
            Dialogs.number(this, "Buy multiplier", List.of("Players sell to the shop for price x this"), _buyMultiplier, 0, 1000, v -> _buyMultiplier = v);
        }));

        addButton(toggle(6, Material.STICKY_PISTON, "&6Absolute position", _absolutePositions,
                "&7On: items a player can't buy show as NOT AVAILABLE", "&7Off: they are left out", e ->
                {
                    _absolutePositions = e.isRightClick() || !_absolutePositions;
                    refresh();
                }));

        addButton(toggle(8, Material.TRIPWIRE_HOOK, "&6Lock", _locked, "&7Locked: only admins can open the shop", "", e ->
        {
            _locked = !e.isRightClick() && !_locked;
            refresh();
        }));

        addButton(value(10, Material.COMPASS, "&6Expire percent", Dialogs.format(_expirePercent * 100) + " %", e ->
        {
            if (e.isRightClick())
            {
                _expirePercent = config.getDouble("new-shop.expire-percent");
                refresh();
                return;
            }
            Dialogs.number(this, "Expire percent", List.of("Share of the stock that disappears every cooldown", "0.1 = 10%"),
                    _expirePercent, 0, 1, v -> _expirePercent = v);
        }));

        addButton(value(12, Material.CLOCK, "&6Expire Cooldown", _expireCooldown + " min", e ->
        {
            if (e.isRightClick())
            {
                _expireCooldown = config.getInt("new-shop.expire-cooldown-minutes");
                refresh();
                return;
            }
            Dialogs.wholeNumber(this, "Expire cooldown", List.of("Minutes between expiries, 0 = never"), _expireCooldown, 0, 100_000, v -> _expireCooldown = v);
        }));

        addButton(toggle(14, Material.IRON_BARS, "&6Block Customer Sell", _customersCanOnlyBuy, "&7On: players can only buy", "", e ->
        {
            _customersCanOnlyBuy = !e.isRightClick() && !_customersCanOnlyBuy;
            refresh();
        }));

        addButton(new Button(20, Icons.icon(Material.BARRIER, "&l&6Remove This Shop", "&eConfirmation Needed!"), e ->
        {
            _removing = true;
            refresh();
        }));
        addButton(new Button(18, Icons.icon(Material.RED_STAINED_GLASS_PANE, "&b<= &cBACK"),
                e -> openNext(new ShopEditorMenu(_service, _shop))));
        addButton(new Button(26, Icons.icon(Material.GREEN_STAINED_GLASS_PANE, "&bCONFIRM"), e -> confirm()));
        updateButtons(true);
    }

    private void drawRemoveWarning()
    {
        ItemStack warning = Icons.glow(Icons.icon(Material.BARRIER, "&l&cWARNING!", "&eAll items of the shop will be &clost!", "&eYou are about to remove the shop!"));
        for (int slot = 0; slot < 27; slot++)
            addButton(new Button(slot, warning));
        for (int slot : new int[] { 8, 17, 26 })
        {
            addButton(new Button(slot, Icons.icon(Material.GREEN_STAINED_GLASS_PANE, "&bCONFIRM", "&cRemoves the shop"), e ->
            {
                String name = _shop.getDisplayName();
                _service.deleteShop(_shop);
                getPlayer().sendMessage(Metods.msgC("&9Shop named " + name + " &9has been &cremoved"));
                closeNext();
            }));
        }
        for (int slot : new int[] { 0, 9, 18 })
        {
            addButton(new Button(slot, Icons.icon(Material.RED_STAINED_GLASS_PANE, "&bCANCEL"), e ->
            {
                _removing = false;
                refresh();
            }));
        }
    }

    private void confirm()
    {
        if (_service.getShop(_shop.getId()) == null)
        {
            getPlayer().sendMessage(Metods.msgC("&cThe shop doesn't exist any more"));
            closeNext();
            return;
        }

        _shop.setSettings(new ShopSettings(_sellMultiplier, _buyMultiplier, _expirePercent, _expireCooldown,
                _locked, _customersCanOnlyBuy, _absolutePositions));
        if (!_displayName.equals(_shop.getDisplayName()))
        {
            Shop other = _service.getShop(_displayName);
            if (other != null && other != _shop)
                getPlayer().sendMessage(Metods.msgC("&cA shop named " + _displayName + " &calready exists, the name wasn't changed"));
            else
            {
                getPlayer().sendMessage(Metods.msgC("&2Shop name has been changed from " + _shop.getDisplayName() + "&2 to " + _displayName));
                _service.renameShop(_shop, _displayName);
            }
        }
        _service.saveNow(_shop);
        getPlayer().sendMessage(Metods.msgC("&eShop data has been saved!"));
        _service.warnAboutBuyBackProfit(getPlayer(), _shop);
        openNext(new ShopEditorMenu(_service, _shop));
    }

    private Button value(int slot, Material material, String name, String value, Consumer<InventoryClickEvent> click)
    {
        return new Button(slot, Icons.icon(material, name, "&6Set To &9" + value, "&bM1: &aSet &bM2: &cDefault"), click);
    }

    private Button toggle(int slot, Material material, String name, boolean value, String help1, String help2, Consumer<InventoryClickEvent> click)
    {
        return new Button(slot, Icons.icon(material, name, "&6Set To " + (value ? "&aTrue" : "&cFalse"), help1, help2,
                "&bM1: &aSwitch &bM2: &cDefault"), click);
    }
}
