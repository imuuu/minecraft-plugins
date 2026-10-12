package imu.GS.menu.admin;

import imu.GS.GsService;
import imu.GS.menu.Icons;
import imu.GS.prices.UniqueItems;
import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.Interfaces.IMenuButton;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * /gs modify uniques: every unique item. Click one to change its price; put an item from your inventory on the
 * middle slot of the bottom row to make it a unique (you keep the item).
 */
public class UniquesMenu extends AdminMenu
{
    private static final int PAGE_SIZE = 45;
    private static final int ADD_SLOT = 49;
    private int _page = 0;

    public UniquesMenu(GsService service)
    {
        super(service, null, "&5========== Uniques =========", 54);
    }

    @Override
    public INVENTORY_AREA setInventoryLock()
    {
        return INVENTORY_AREA.UPPER_INV;
    }

    @Override
    protected void refresh()
    {
        clearButtons();
        List<UniqueItems.Unique> uniques = new ArrayList<>(_service.getUniques().getAll());
        int pages = Math.max(1, (uniques.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        _page = Math.max(0, Math.min(_page, pages - 1));

        int first = _page * PAGE_SIZE;
        for (int i = first; i < Math.min(uniques.size(), first + PAGE_SIZE); i++)
        {
            UniqueItems.Unique unique = uniques.get(i);
            addButton(new Button(i - first, Icons.withLore(unique.item(), List.of(
                    "&b======Unique======", "&5price : &6" + Icons.money(unique.price()), "&e== Click to modify ==", "&b==================")),
                    e -> openNext(new UniqueEditorMenu(_service, unique))));
        }

        ItemStack orange = Icons.filler(Material.ORANGE_STAINED_GLASS_PANE);
        for (int slot = PAGE_SIZE; slot < 54; slot++)
        {
            if (slot != ADD_SLOT)
                addButton(new Button(slot, orange));
        }
        addButton(new Button(45, Icons.icon(Material.BIRCH_SIGN, "&b<<"), e ->
        {
            _page--;
            refresh();
        }));
        addButton(new Button(53, Icons.icon(Material.BIRCH_SIGN, "&b>>"), e ->
        {
            _page++;
            refresh();
        }));
        addButton(new Button(48, Icons.icon(Material.PURPLE_STAINED_GLASS_PANE, "&ePut an item here &b==>",
                "&7Pick up an item in your inventory and", "&7put it in the next slot to make it unique")));
        updateButtons(true);
    }

    @Override
    public boolean onDropItem(ItemStack stack, int slot)
    {
        if (slot != ADD_SLOT || stack == null || stack.getType() == Material.AIR)
            return false;
        UniqueItems.Unique existing = _service.getUniques().get(stack);
        UniqueItems.Unique unique = existing != null ? existing : _service.getUniques().add(stack, 0);
        if (existing == null)
            _service.getDatabase().saveUnique(unique.key(), 0);
        openNext(new UniqueEditorMenu(_service, unique));
        return false;
    }

    @Override
    public IMenuButton onDropItemSet(ItemStack stack, int slot)
    {
        return null;
    }
}
