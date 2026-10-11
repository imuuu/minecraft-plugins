package imu.GS.menu.admin;

import imu.GS.GsService;
import imu.GS.engine.shop.Shop;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.InvUtil.CustomInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Base of the menus that edit a shop. While one is open the shop is closed to customers, like a locked shop.
 */
abstract class AdminMenu extends CustomInventory
{
    protected final GsService _service;
    protected final Shop _shop;

    AdminMenu(GsService service, Shop shop, String title, int size)
    {
        super(service.getPlugin(), title, size);
        _service = service;
        _shop = shop;
    }

    @Override
    public INVENTORY_AREA setInventoryLock()
    {
        return INVENTORY_AREA.UPPER_LOWER_INV;
    }

    @Override
    public void onAwake()
    {
    }

    @Override
    public void onOpen()
    {
        super.onOpen();
        if (_shop != null)
            _service.startEditing(_shop);
        refresh();
    }

    @Override
    public void onClose()
    {
        super.onClose();
        if (_shop != null)
            _service.stopEditing(_shop);
    }

    protected abstract void refresh();

    /**
     * Opens another menu next tick; opening one inside a click event isn't safe.
     */
    protected void openNext(CustomInventory menu)
    {
        Player player = getPlayer();
        Bukkit.getScheduler().runTask(getPlugin(), () -> menu.open(player));
    }

    protected void closeNext()
    {
        Player player = getPlayer();
        Bukkit.getScheduler().runTask(getPlugin(), () -> player.closeInventory());
    }
}
