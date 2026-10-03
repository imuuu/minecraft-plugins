package imu.iAPI.Config;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Utilities.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * /ia config: every registered {@link ConfigMenu} the player may edit, one item each.
 */
public class ConfigMenuListInventory extends CustomInventory
{
    public ConfigMenuListInventory()
    {
        super(ImusAPI._instance, "&9Plugin settings", 6 * 9);
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
        clearButtons();

        int slot = 0;
        for (ConfigMenu menu : ConfigMenu.getRegistered())
        {
            if (!menu.canEdit(getPlayer()) || slot >= getSize())
                continue;

            ItemStack stack = ItemUtils.SetDisplayName(new ItemStack(Material.COMPARATOR), "&6&l" + menu.getTitle());
            ItemUtils.AddLore(stack, "&7" + menu.getPlugin().getName(), true);
            ItemUtils.AddLore(stack, "&7" + menu.getEntries().size() + " settings", true);
            addButton(new Button(slot++, stack, event -> openPage(new ConfigMenuInventory(menu))));
        }
        updateButtons(true);
    }
}
