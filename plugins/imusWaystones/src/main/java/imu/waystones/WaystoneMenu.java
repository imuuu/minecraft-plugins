package imu.waystones;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Utilities.ItemUtils;
import imu.iAPI.Utilities.TextUtils;
import imu.waystones.engine.Destinations;
import imu.waystones.engine.Stone;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * A waystone's menu: the waystones the player can go to, the upgrades, and for admins its settings.
 */
final class WaystoneMenu extends CustomInventory
{
    private static final int PER_PAGE = 27;
    private static final int PREVIOUS = 27, VISIBILITY = 28, UNBREAKABLE = 29, MAX_OUT = 30, UPGRADES = 31, ENABLED = 32,
            REMOVE = 33, NEXT = 35;

    private final ImusWaystones _plugin;
    private final Waystone _ws;
    private int _page;

    WaystoneMenu(ImusWaystones plugin, Waystone ws)
    {
        super(plugin, ws.name, 36);
        _plugin = plugin;
        _ws = ws;
    }

    static ItemStack named(Material material, String name, List<String> lore)
    {
        ItemStack item = new ItemStack(material);
        ItemUtils.SetDisplayName(item, name);
        if (!lore.isEmpty())
            ItemUtils.SetLores(item, lore, false);
        return item;
    }

    @Override
    public INVENTORY_AREA setInventoryLock()
    {
        return INVENTORY_AREA.UPPER_INV;
    }

    @Override
    public void onAwake()
    {
    }

    @Override
    public void onOpen()
    {
        super.onOpen();
        render();
    }

    private void render()
    {
        Player player = getPlayer();
        clearButtons();
        ItemStack bar = ItemUtils.SetDisplayNameEmpty(new ItemStack(Material.CYAN_STAINED_GLASS_PANE));
        for (int i = PER_PAGE; i < getSize(); i++)
            addButton(new Button(i, bar.clone()));

        String closed = closedReason(player);
        if (closed != null)
            addButton(new Button(13, named(Material.BARRIER, closed, List.of())));
        else
            renderDestinations(player);

        addButton(new Button(UPGRADES, named(Material.BEACON, _plugin.text("menu-upgrades"), List.of(_plugin.text("menu-upgrades-lore"))),
                e -> Bukkit.getScheduler().runTask(_plugin, () -> new UpgradeMenu(_plugin, _ws).open(player))));
        if (player.hasPermission(ImusWaystones.ADMIN))
            renderAdmin();
        updateButtons(true);
    }

    /**
     * Why the player can't leave from here now, or null.
     */
    private String closedReason(Player player)
    {
        if (!_ws.enabled)
            return _plugin.text("closed-disabled");
        if (!_ws.visibility.canDepart())
            return _plugin.text("closed-arrival-only");
        long left = _plugin.teleports().cooldownLeft(player, _ws);
        if (left > 0)
            return _plugin.text("closed-cooldown", "time", TextUtils.FormatTime(left));
        return null;
    }

    private void renderDestinations(Player player)
    {
        List<Stone> list = Destinations.from(_ws.stone(), _plugin.waystones().stones(), _plugin.waystones().discovered(player.getUniqueId()),
                _ws.panel(player.getUniqueId()), _plugin.rules());
        if (list.isEmpty())
        {
            addButton(new Button(13, named(Material.MAP, _plugin.text("no-destinations"), List.of(_plugin.text("no-destinations-lore")))));
            return;
        }
        int pages = (list.size() + PER_PAGE - 1) / PER_PAGE;
        _page = Math.max(0, Math.min(_page, pages - 1));
        for (int i = 0; i < PER_PAGE && _page * PER_PAGE + i < list.size(); i++)
        {
            Waystone to = _plugin.waystones().get(list.get(_page * PER_PAGE + i).id());
            if (to == null)
                continue;
            ItemStack icon = to.icon.clone();
            ItemUtils.SetDisplayName(icon, to.name);
            List<String> lore = new ArrayList<>();
            lore.add(_plugin.text("destination-owner", "owner", to.ownerName));
            lore.add(_plugin.text("destination-world", "world", to.world));
            lore.add(" ");
            lore.add(_plugin.text("destination-click"));
            ItemUtils.SetLores(icon, lore, false);
            addButton(new Button(i, icon, e -> go(player, to)));
        }
        if (_page > 0)
            addButton(new Button(PREVIOUS, named(Material.ARROW, _plugin.text("menu-previous"), List.of()), e -> { _page--; render(); }));
        if (_page < pages - 1)
            addButton(new Button(NEXT, named(Material.ARROW, _plugin.text("menu-next"), List.of()), e -> { _page++; render(); }));
    }

    private void go(Player player, Waystone to)
    {
        player.closeInventory();
        if (!_plugin.structure().isWhole(to))
        {
            _plugin.send(player, "destination-gone");
            return;
        }
        int levels = Teleports.isFree(player, _ws) ? 0 : _plugin.rules().xpLevels(_ws.panel(player.getUniqueId()));
        if (player.getLevel() < levels)
        {
            _plugin.send(player, "not-enough-xp", "levels", levels);
            return;
        }
        _plugin.teleports().begin(player, _ws, to);
    }

    private void renderAdmin()
    {
        addButton(new Button(VISIBILITY, toggle(Material.SPYGLASS, "admin-visibility", _plugin.text("visibility." + _ws.visibility.name())), e ->
        {
            _ws.visibility = _ws.visibility.next();
            _plugin.waystones().save(_ws);
            render();
        }));
        addButton(new Button(UNBREAKABLE, toggle(Material.BEDROCK, "admin-unbreakable", onOff(_ws.unbreakable)), e ->
        {
            _ws.unbreakable = !_ws.unbreakable;
            _plugin.waystones().save(_ws);
            render();
        }));
        addButton(new Button(MAX_OUT, toggle(Material.NETHER_STAR, "admin-max-out", onOff(_ws.maxedOut)), e ->
        {
            _ws.maxedOut = !_ws.maxedOut;
            _plugin.waystones().save(_ws);
            render();
        }));
        addButton(new Button(ENABLED, toggle(Material.LEVER, "admin-enabled", onOff(_ws.enabled)), e ->
        {
            _ws.enabled = !_ws.enabled;
            _plugin.waystones().save(_ws);
            render();
        }));
        addButton(new Button(REMOVE, named(Material.LAVA_BUCKET, _plugin.text("admin-remove"), List.of(_plugin.text("admin-remove-lore"))), e ->
        {
            if (!e.isShiftClick())
                return;
            getPlayer().closeInventory();
            _plugin.waystones().remove(_ws);
            getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.3f, 1f);
            _plugin.send(getPlayer(), "removed", "name", _ws.name);
        }));
    }

    private ItemStack toggle(Material material, String key, String value)
    {
        return named(material, _plugin.text(key), List.of(_plugin.text("admin-current", "value", value), _plugin.text("admin-click")));
    }

    private String onOff(boolean value)
    {
        return _plugin.text(value ? "on" : "off");
    }
}
