package imu.waystones;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Utilities.ItemUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * /waystones list: every waystone for admins. Click goes there at once, shift right click removes it.
 */
final class ListMenu extends CustomInventory
{
    private static final int PER_PAGE = 45, PREVIOUS = 45, NEXT = 53;

    private final ImusWaystones _plugin;
    private int _page;

    ListMenu(ImusWaystones plugin)
    {
        super(plugin, plugin.text("list-title", "count", plugin.waystones().all().size()), 54);
        _plugin = plugin;
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
        clearButtons();
        List<Waystone> all = new ArrayList<>(_plugin.waystones().all());
        int pages = Math.max(1, (all.size() + PER_PAGE - 1) / PER_PAGE);
        _page = Math.max(0, Math.min(_page, pages - 1));
        ItemStack bar = ItemUtils.SetDisplayNameEmpty(new ItemStack(Material.CYAN_STAINED_GLASS_PANE));
        for (int i = PER_PAGE; i < getSize(); i++)
            addButton(new Button(i, bar.clone()));
        for (int i = 0; i < PER_PAGE && _page * PER_PAGE + i < all.size(); i++)
        {
            Waystone ws = all.get(_page * PER_PAGE + i);
            ItemStack icon = ws.icon.clone();
            ItemUtils.SetDisplayName(icon, ws.name);
            ItemUtils.SetLores(icon, List.of(
                    _plugin.text("destination-owner", "owner", ws.ownerName),
                    _plugin.text("list-location", "world", ws.world, "x", ws.x, "y", ws.y, "z", ws.z),
                    _plugin.text("list-visibility", "visibility", _plugin.text("visibility." + ws.visibility.name())),
                    " ",
                    _plugin.text("list-click")), false);
            addButton(new Button(i, icon, e -> click(ws, e.getClick())));
        }
        if (_page > 0)
            addButton(new Button(PREVIOUS, WaystoneMenu.named(Material.ARROW, _plugin.text("menu-previous"), List.of()), e -> { _page--; render(); }));
        if (_page < pages - 1)
            addButton(new Button(NEXT, WaystoneMenu.named(Material.ARROW, _plugin.text("menu-next"), List.of()), e -> { _page++; render(); }));
        updateButtons(true);
    }

    private void click(Waystone ws, ClickType click)
    {
        Player player = getPlayer();
        if (click == ClickType.SHIFT_RIGHT)
        {
            _plugin.waystones().remove(ws);
            _plugin.send(player, "removed", "name", ws.name);
            render();
            return;
        }
        Location to = Teleports.landing(ws);
        if (to == null)
        {
            _plugin.send(player, "world-not-loaded", "world", ws.world);
            return;
        }
        player.closeInventory();
        player.teleport(to);
    }
}
