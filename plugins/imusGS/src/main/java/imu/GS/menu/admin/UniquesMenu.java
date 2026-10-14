package imu.GS.menu.admin;

import imu.iAPI.Menus.Click;
import imu.GS.GsService;
import imu.GS.menu.Icons;
import imu.GS.prices.UniqueItems;
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

    // The player picks items up from their own inventory to put them in
    @Override
    public boolean locksOwnInventory()
    {
        return false;
    }

    @Override
    protected void draw()
    {
        List<UniqueItems.Unique> uniques = new ArrayList<>(_service.getUniques().getAll());
        int pages = Math.max(1, (uniques.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        _page = Math.max(0, Math.min(_page, pages - 1));

        int first = _page * PAGE_SIZE;
        for (int i = first; i < Math.min(uniques.size(), first + PAGE_SIZE); i++)
        {
            UniqueItems.Unique unique = uniques.get(i);
            button(i - first, Icons.withLore(unique.item(), List.of(
                    "&b======Unique======", "&5price : &6" + Icons.money(unique.price()), "&e== Click to modify ==", "&b==================")),
                    e -> openNext(new UniqueEditorMenu(_service, unique)));
        }

        ItemStack orange = Icons.filler(Material.ORANGE_STAINED_GLASS_PANE);
        for (int slot = PAGE_SIZE; slot < 54; slot++)
        {
            if (slot != ADD_SLOT)
                button(slot, orange);
        }
        button(45, Icons.icon(Material.BIRCH_SIGN, "&b<<"), e ->
        {
            _page--;
            refresh();
        });
        button(53, Icons.icon(Material.BIRCH_SIGN, "&b>>"), e ->
        {
            _page++;
            refresh();
        });
        button(48, Icons.icon(Material.PURPLE_STAINED_GLASS_PANE, "&ePut an item here &b==>",
                "&7Pick up an item in your inventory and", "&7put it in the next slot to make it unique"));
        emptySlot(ADD_SLOT, this::addUnique);
    }

    // An item put in the add slot becomes a unique, or opens the one it already is
    private void addUnique(Click click)
    {
        ItemStack stack = (ItemStack) click.cursor();
        if (stack == null)
            return;
        UniqueItems.Unique existing = _service.getUniques().get(stack);
        UniqueItems.Unique unique = existing != null ? existing : _service.getUniques().add(stack, 0);
        if (existing == null)
            _service.getDatabase().saveUnique(unique.key(), 0);
        openNext(new UniqueEditorMenu(_service, unique));
    }
}
