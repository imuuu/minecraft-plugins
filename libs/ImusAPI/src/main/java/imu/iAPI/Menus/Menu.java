package imu.iAPI.Menus;

import net.kyori.adventure.text.Component;

/**
 * A menu: a title, a size and {@link #render(Canvas)}, which draws the buttons from the menu's current state.
 * The menu is drawn again after every click and on {@link #refresh()}, and only changed slots are sent to the
 * player. A menu never touches the server's inventories, so tests can click through it with
 * {@link imu.iAPI.Menus.Testing.MenuTester}; {@link imu.iAPI.Menus.Paper.PaperMenus} shows it in game.
 */
public abstract class Menu
{
    private final Component _title;
    private final int _rows;
    MenuSession _session;

    /**
     * @param rows 1-6
     */
    protected Menu(Component title, int rows)
    {
        if (rows < 1 || rows > 6)
            throw new IllegalArgumentException("A menu has 1-6 rows, not " + rows);
        _title = title;
        _rows = rows;
    }

    /**
     * @param title &amp; colour codes
     */
    protected Menu(String title, int rows)
    {
        this(Text.of(title), rows);
    }

    /**
     * Draws the buttons. Called on opening, after every click and on {@link #refresh()}.
     */
    protected abstract void render(Canvas canvas);

    /**
     * Each time the menu is shown, also when coming back to it.
     */
    protected void onOpen(Viewer viewer) {}

    /**
     * When the menu is left for good: closed, gone back from, or replaced.
     */
    protected void onClose(Viewer viewer) {}

    /**
     * How often {@link #onTick()} runs and the menu is drawn again while it is shown, in ticks; 0 for never.
     */
    public int refreshTicks()
    {
        return 0;
    }

    /**
     * @return false when nothing changed and the menu needn't be drawn again
     */
    protected boolean onTick()
    {
        return true;
    }

    /**
     * Whether the player's own inventory below is locked while the menu is open: no clicks, drags or number keys in
     * it. On by default; a menu the player puts items in from their inventory (e.g. an enchanting table) turns it
     * off. Moving items into the menu is blocked either way, unless the menu takes them on a click.
     */
    public boolean locksOwnInventory()
    {
        return true;
    }

    /**
     * An item shown in the empty slots of the player's own inventory while the menu is open, or null for none; slots
     * that hold something show their item, kept up to date as items come and go. Shown on the player's screen only:
     * the real slots don't change and are empty again when the menu closes. E.g. a shop fills the gaps of the
     * inventory below with black panes, so the player still sees how much of each item they have.
     */
    public Icon ownInventoryCover()
    {
        return null;
    }

    /**
     * Draws the menu again if it is shown, after its data changed outside a click (another player's trade, data
     * from the database). The drawing waits for the next {@link #refreshDelayTicks()}: many changes in between
     * make one drawing, so a busy shop can't make every viewer draw on every trade. A click draws at once anyway.
     */
    public void refresh()
    {
        if (_session != null && _session.current() == this)
            _session.requestRefresh();
    }

    /**
     * How often changes from {@link #refresh()} are drawn at most, in ticks (20 = a second).
     */
    public int refreshDelayTicks()
    {
        return 5;
    }

    public Component title()
    {
        return _title;
    }

    public int rows()
    {
        return _rows;
    }

    public int size()
    {
        return _rows * 9;
    }

    /**
     * The session showing this menu, or null before it is opened.
     */
    public MenuSession session()
    {
        return _session;
    }
}
