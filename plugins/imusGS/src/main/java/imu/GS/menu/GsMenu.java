package imu.GS.menu;

import imu.GS.GsService;
import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Click;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.MenuSession;
import imu.iAPI.Menus.Paper.PaperIcons;
import imu.iAPI.Menus.Paper.PaperMenus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Base of imusGS's menus: they draw item stacks in {@link #draw()}, which runs whenever the menu is drawn.
 */
public abstract class GsMenu extends Menu
{
    protected final GsService _service;
    private Canvas _canvas;

    /**
     * @param size slots, a multiple of 9
     */
    protected GsMenu(GsService service, String title, int size)
    {
        super(title, size / 9);
        _service = service;
    }

    /**
     * Puts the buttons in place with {@link #button}. Runs on opening, after every click and on refresh.
     */
    protected abstract void draw();

    @Override
    protected final void render(Canvas canvas)
    {
        _canvas = canvas;
        try
        {
            draw();
        } finally
        {
            _canvas = null;
        }
    }

    /**
     * A button a helper makes before it is put in place with {@link #addButton(Button)}.
     */
    public record Button(int slot, ItemStack stack, Consumer<Click> action)
    {
        public Button(int slot, ItemStack stack)
        {
            this(slot, stack, null);
        }
    }

    protected void addButton(Button button)
    {
        if (button.action() == null)
            button(button.slot(), button.stack());
        else
            button(button.slot(), button.stack(), button.action());
    }

    protected void button(int slot, ItemStack stack)
    {
        _canvas.set(slot, PaperIcons.of(stack));
    }

    protected void button(int slot, ItemStack stack, Consumer<Click> action)
    {
        _canvas.set(slot, PaperIcons.of(stack), action);
    }

    /**
     * An empty slot that hears clicks, e.g. to take the item on the cursor.
     */
    protected void emptySlot(int slot, Consumer<Click> action)
    {
        _canvas.set(slot, imu.iAPI.Menus.Button.empty(action));
    }

    public int getSize()
    {
        return size();
    }

    /**
     * The player looking at the menu, or null before it opens.
     */
    public Player getPlayer()
    {
        MenuSession session = session();
        return session == null ? null : Bukkit.getPlayer(session.viewer().id());
    }

    public void open(Player player)
    {
        PaperMenus.open(player, this);
    }

    /**
     * Shows another menu in place of this one.
     */
    protected void openNext(Menu menu)
    {
        MenuSession session = session();
        if (session != null && !session.hasEnded())
            session.replace(menu);
    }

    public void closeNext()
    {
        MenuSession session = session();
        if (session != null)
            session.close();
    }
}
