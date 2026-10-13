package imu.iAPI.Menus;

/**
 * A click on a menu button. Moves to other menus through the session; after the action the menu is drawn again,
 * so a button only has to change the state the menu draws from.
 */
public final class Click
{
    private final MenuSession _session;
    private final Menu _menu;
    private final int _slot;
    private final ClickKind _kind;
    private final int _hotbarKey;

    Click(MenuSession session, Menu menu, int slot, ClickKind kind, int hotbarKey)
    {
        _session = session;
        _menu = menu;
        _slot = slot;
        _kind = kind;
        _hotbarKey = hotbarKey;
    }

    public Viewer viewer()
    {
        return _session.viewer();
    }

    public MenuSession session()
    {
        return _session;
    }

    /**
     * The menu that was clicked.
     */
    public Menu menu()
    {
        return _menu;
    }

    public int slot()
    {
        return _slot;
    }

    public ClickKind kind()
    {
        return _kind;
    }

    /**
     * 0-8 for a number key click, otherwise -1.
     */
    public int hotbarKey()
    {
        return _hotbarKey;
    }

    public boolean isLeft()
    {
        return _kind.isLeft();
    }

    public boolean isRight()
    {
        return _kind.isRight();
    }

    public boolean isShift()
    {
        return _kind.isShift();
    }

    /**
     * Opens another menu; {@link #back()} there returns here.
     */
    public void open(Menu menu)
    {
        _session.open(menu);
    }

    /**
     * Opens another menu in place of this one.
     */
    public void replace(Menu menu)
    {
        _session.replace(menu);
    }

    public void back()
    {
        _session.back();
    }

    public void close()
    {
        _session.close();
    }

    public void prompt(Prompt prompt)
    {
        _session.prompt(prompt);
    }

    /**
     * The item on the viewer's cursor, or null. It is the platform's own item (an ItemStack in game).
     */
    public Object cursor()
    {
        return _session.host().cursor();
    }

    /**
     * Puts an item on the viewer's cursor, null to empty it; for menus that hold items in their slots.
     */
    public void setCursor(Object item)
    {
        _session.host().setCursor(item);
    }

    public void message(String legacy)
    {
        viewer().message(legacy);
    }
}
