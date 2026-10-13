package imu.iAPI.LootTables;

import imu.iAPI.Menus.Button;
import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Click;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.Pager;
import imu.iAPI.Menus.Paper.PaperIcons;
import imu.iAPI.Menus.Prompt;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * /ia loottables: every drop table; click one to see its items. On an item, left click sets the weight, right
 * click the least and shift click the most dropped at once.
 */
public class Inventory_LootTables extends Menu
{
    private final Pager<String> _pager = Pager.rows(0, 4);

    public Inventory_LootTables()
    {
        super("&9Drop Tables", 5);
    }

    @Override
    protected void render(Canvas canvas)
    {
        Map<String, ImusLootTable> tables = Manager_ImusLootTable.getInstance().get_lootTables();
        _pager.render(canvas, new ArrayList<>(tables.keySet()), key -> Button.of(Icon.of(Material.CHEST, key, "Click to edit"),
                click -> click.open(new TableMenu(key))), 36, 44);
    }

    /**
     * The items of one table.
     */
    private static final class TableMenu extends Menu
    {
        private final String _name;
        private final Pager<ILootTableItem<?>> _pager = Pager.rows(0, 4);

        private TableMenu(String name)
        {
            super("&9Drop Table " + name, 5);
            _name = name;
        }

        @Override
        protected void render(Canvas canvas)
        {
            ImusLootTable table = Manager_ImusLootTable.getInstance().get_lootTable(_name);
            if (table == null)
            {
                canvas.set(22, Icon.of(Material.BARRIER, "&cThe table is gone"));
                return;
            }
            List<ILootTableItem<?>> items = new ArrayList<>(table.getItems());
            _pager.render(canvas, items, item -> Button.of(icon(item), click -> edit(click, item)), 36, 44);
            canvas.set(40, Icon.of(Material.RED_STAINED_GLASS_PANE, "&bGo Back"), Click::back);
        }

        private static Icon icon(ILootTableItem<?> item)
        {
            Icon icon = item instanceof LootItemStack && item.get_value() instanceof ItemStack stack
                    ? PaperIcons.of(stack)
                    : Icon.of(Material.BARRIER, "Not ItemStack");
            return icon.addLore("&9Weight: &e" + item.get_weight(), "&9MinAmount: &e" + item.get_minAmount(),
                    "&9MaxAmount: &e" + item.get_maxAmount(), " ",
                    "&bLeft &7weight, &bright &7min, &bshift &7max");
        }

        private void edit(Click click, ILootTableItem<?> item)
        {
            if (!(item.get_value() instanceof ItemStack stack))
                return;
            String field = click.isShift() ? "MaxAmount" : click.isRight() ? "MinAmount" : "Weight";
            int current = click.isShift() ? item.get_maxAmount() : click.isRight() ? item.get_minAmount() : item.get_weight();
            click.prompt(Prompt.of(field, List.of("A whole number, 0 or more"), String.valueOf(current), 10, text ->
            {
                int value;
                try
                {
                    value = Integer.parseInt(text.trim());
                } catch (NumberFormatException e)
                {
                    return "'" + text + "' is not a whole number";
                }
                if (value < 0)
                    return "Can't be below 0";
                Integer min = field.equals("MinAmount") ? value : null;
                Integer max = field.equals("MaxAmount") ? value : null;
                Integer weight = field.equals("Weight") ? value : null;
                Manager_ImusLootTable.getInstance().updateItemStackAsync(stack, min, max, weight, success ->
                        click.message(success ? "&6" + field + " &9updated successfully" : "&f" + field + " update &cfailed"));
                return null;
            }));
        }
    }
}
