package imu.iAPI.Config;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Utilities.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Lists the entries of a {@link ConfigMenu}. Left click edits a value, right click resets it to the default.
 */
public class ConfigMenuInventory extends CustomInventory
{
    private static final int PAGE_SIZE = 45;

    private final ConfigMenu _menu;
    private int _page = 0;

    public ConfigMenuInventory(ConfigMenu menu)
    {
        super(menu.getPlugin(), "&9" + menu.getTitle(), 6 * 9);
        _menu = menu;
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
        refresh();
    }

    private void refresh()
    {
        clearButtons();
        for (int i = PAGE_SIZE; i < getSize(); i++)
        {
            addButton(getEmptyButton(i, Material.BLACK_STAINED_GLASS_PANE));
        }

        List<ConfigEntry> entries = _menu.getEntries();
        int pageCount = Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        _page = Math.min(_page, pageCount - 1);

        FileConfiguration config = _menu.getPlugin().getConfig();
        int first = _page * PAGE_SIZE;
        for (int i = first; i < Math.min(entries.size(), first + PAGE_SIZE); i++)
        {
            ConfigEntry entry = entries.get(i);
            addButton(new Button(i - first, createEntryStack(entry, config), event ->
            {
                if (event.isRightClick())
                    resetToDefault(entry);
                else
                    edit(entry);
            }));
        }

        if (_page > 0)
            addButton(navigationButton(PAGE_SIZE, "&e<- Previous page", -1));
        if (_page < pageCount - 1)
            addButton(navigationButton(getSize() - 1, "&eNext page ->", 1));
        if (!getPageStack().isEmpty())
        {
            ItemStack back = ItemUtils.SetDisplayName(new ItemStack(Material.RED_STAINED_GLASS_PANE), "&bGo Back");
            addButton(new Button(PAGE_SIZE + 4, back, event -> back()));
        }

        updateButtons(true);
    }

    private Button navigationButton(int slot, String name, int pageChange)
    {
        ItemStack stack = ItemUtils.SetDisplayName(new ItemStack(Material.ARROW), name);
        return new Button(slot, stack, event ->
        {
            _page += pageChange;
            refresh();
        });
    }

    private ItemStack createEntryStack(ConfigEntry entry, FileConfiguration config)
    {
        Object value = entry.get(config);
        Object defaultValue = entry.getDefault(config);

        Material icon = entry.getIcon();
        if (entry.getType() == ConfigEntry.Type.BOOLEAN)
            icon = Boolean.TRUE.equals(value) ? Material.LIME_DYE : Material.GRAY_DYE;

        ItemStack stack = new ItemStack(icon);
        ItemUtils.SetDisplayName(stack, "&6&l" + entry.getName());
        for (String line : entry.getDescription())
            ItemUtils.AddLore(stack, "&7" + line, true);
        ItemUtils.AddLore(stack, " ", true);
        ItemUtils.AddLore(stack, "&9Value: &a" + entry.format(value), true);
        if (defaultValue != null)
            ItemUtils.AddLore(stack, "&9Default: &7" + entry.format(defaultValue), true);
        if (entry.hasRange())
            ItemUtils.AddLore(stack, "&9Allowed: &7" + entry.rangeText(), true);
        if (entry.getType() == ConfigEntry.Type.CHOICE)
            ItemUtils.AddLore(stack, "&9Options: &7" + String.join(", ", entry.getOptions()), true);
        if (entry.getNote() != null)
            ItemUtils.AddLore(stack, "&9Takes effect: &e" + entry.getNote(), true);
        ItemUtils.AddLore(stack, " ", true);
        String leftClick = switch (entry.getType())
        {
            case BOOLEAN -> "&bLeft click &7to toggle";
            case CHOICE -> "&bLeft click &7for the next option";
            default -> "&bLeft click &7to change";
        };
        ItemUtils.AddLore(stack, leftClick, true);
        if (defaultValue != null)
            ItemUtils.AddLore(stack, "&bRight click &7to reset to default", true);
        ItemUtils.AddLore(stack, "&8" + entry.getPath(), true);
        ItemUtils.HideFlag(stack, ItemFlag.HIDE_ATTRIBUTES);
        return stack;
    }

    private void edit(ConfigEntry entry)
    {
        FileConfiguration config = _menu.getPlugin().getConfig();
        if (entry.getType() == ConfigEntry.Type.BOOLEAN)
        {
            _menu.setValue(getPlayer(), entry, !Boolean.TRUE.equals(entry.get(config)));
            refresh();
            return;
        }
        if (entry.getType() == ConfigEntry.Type.CHOICE)
        {
            _menu.setValue(getPlayer(), entry, entry.nextOption(config));
            refresh();
            return;
        }

        // The dialog replaces the inventory; this same inventory is reopened when it closes
        ConfigValueDialog.show(getPlayer(), _menu, entry, () -> open(getPlayer()));
    }

    private void resetToDefault(ConfigEntry entry)
    {
        Object defaultValue = entry.getDefault(_menu.getPlugin().getConfig());
        if (defaultValue == null)
            return;

        _menu.setValue(getPlayer(), entry, defaultValue);
        refresh();
    }
}
