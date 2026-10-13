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
     * Whether clicks in the player's own inventory below are blocked too. Moving items into the menu always is.
     */
    public boolean locksOwnInventory()
    {
        return false;
    }

    /**
     * Draws the menu again if it is shown, e.g. after its data changed outside a click.
     */
    public void refresh()
    {
        if (_session != null && _session.current() == this)
            _session.refresh();
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
