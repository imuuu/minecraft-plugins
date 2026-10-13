package imu.GS.menu.admin;

import imu.GS.GsService;
import imu.GS.engine.shop.Shop;
import imu.GS.menu.GsMenu;
import imu.iAPI.Menus.Viewer;

/**
 * Base of the menus that edit a shop. While one is open the shop is closed to customers, like a locked shop.
 */
abstract class AdminMenu extends GsMenu
{
    protected final Shop _shop;
    private boolean _editing;

    AdminMenu(GsService service, Shop shop, String title, int size)
    {
        super(service, title, size);
        _shop = shop;
    }

    // Also when the menu comes back after a dialog, so it counts once
    @Override
    protected void onOpen(Viewer viewer)
    {
        if (_shop != null && !_editing)
        {
            _editing = true;
            _service.startEditing(_shop);
        }
    }

    @Override
    protected void onClose(Viewer viewer)
    {
        if (_shop != null && _editing)
        {
            _editing = false;
            _service.stopEditing(_shop);
        }
    }
}
