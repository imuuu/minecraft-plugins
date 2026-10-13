package imu.imustnt;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Utilities.InvUtil;
import imu.iAPI.Utilities.ItemUtils;
import imu.imustnt.engine.TntType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * The special TNTs for admins to take: click for one, right or shift click for a stack.
 */
class TntMenu extends CustomInventory
{
    private final ImusTNT _plugin;

    TntMenu(ImusTNT plugin)
    {
        super(plugin, plugin.text("menu-title"), 9);
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
        clearButtons();
        ItemStack background = ItemUtils.SetDisplayNameEmpty(new ItemStack(Material.PURPLE_STAINED_GLASS_PANE));
        for (int i = 0; i < getSize(); i++)
            addButton(new Button(i, background.clone()));
        TntType[] types = TntType.values();
        for (int i = 0; i < types.length; i++)
        {
            TntType type = types[i];
            addButton(new Button(i, _plugin.item(type, 1), e ->
                    InvUtil.AddItemToInventoryOrDrop(getPlayer(), _plugin.item(type, e.isRightClick() || e.isShiftClick() ? 64 : 1))));
        }
        updateButtons(true);
    }
}
