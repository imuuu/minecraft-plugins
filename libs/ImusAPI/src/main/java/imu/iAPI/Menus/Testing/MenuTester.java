package imu.iAPI.Menus.Testing;

import imu.iAPI.Menus.ClickKind;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.MenuHost;
import imu.iAPI.Menus.MenuSession;
import imu.iAPI.Menus.Prompt;
import imu.iAPI.Menus.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clicks through menus in a unit test, without a server. It holds the slots as a player would see them, and
 * throws when a button fails.
 *
 * <pre>
 * MenuTester menu = MenuTester.open(new TntMenu(...));
 * assertEquals("Chunk TNT", menu.name(0));
 * menu.click(0, ClickKind.RIGHT);
 * menu.assertShowing(ConfirmMenu.class);
 * System.out.println(menu.dump());
 * </pre>
 */
public final class MenuTester implements MenuHost
{
    private final TestViewer _viewer;
    private final MenuSession _session;
    private final Map<Integer, Icon> _slots = new HashMap<>();
    private final List<Menu> _shown = new ArrayList<>();
    private Menu _menu;
    private Prompt _prompt;
    private boolean _closed;
    private Object _cursor;

    private MenuTester(TestViewer viewer)
    {
        _viewer = viewer;
        _session = new MenuSession(viewer, this);
    }

    /**
     * Opens the menu for an admin with every permission.
     */
    public static MenuTester open(Menu menu)
    {
        return open(menu, TestViewer.admin("Tester"));
    }

    public static MenuTester open(Menu menu, TestViewer viewer)
    {
        MenuTester tester = new MenuTester(viewer);
        tester._session.open(menu);
        return tester;
    }

    // What is shown ==================================================================================================

    public TestViewer viewer()
    {
        return _viewer;
    }

    public MenuSession session()
    {
        return _session;
    }

    /**
     * The menu shown, null after closing.
     */
    public Menu menu()
    {
        return _closed ? null : _menu;
    }

    /**
     * Every menu shown so far, in order, also ones shown again.
     */
    public List<Menu> shown()
    {
        return List.copyOf(_shown);
    }

    /**
     * Checks that a menu of the type is shown and returns it.
     */
    public <T extends Menu> T assertShowing(Class<T> type)
    {
        if (_closed)
            throw new AssertionError("Expected " + type.getSimpleName() + " but the menu was closed");
        if (_prompt != null)
            throw new AssertionError("Expected " + type.getSimpleName() + " but a prompt is open: " + Text.plain(_prompt.title()));
        if (!type.isInstance(_menu))
            throw new AssertionError("Expected " + type.getSimpleName() + " but " + _menu.getClass().getSimpleName() + " is shown");
        return type.cast(_menu);
    }

    public boolean isClosed()
    {
        return _closed;
    }

    public String title()
    {
        return Text.plain(_menu.title());
    }

    /**
     * The icon in a slot, or null when it is empty.
     */
    public Icon icon(int slot)
    {
        return _slots.get(slot);
    }

    /**
     * The name in a slot without colours, or null.
     */
    public String name(int slot)
    {
        Icon icon = icon(slot);
        return icon == null ? null : icon.plainName();
    }

    public List<String> lore(int slot)
    {
        Icon icon = icon(slot);
        return icon == null ? List.of() : icon.plainLore();
    }

    /**
     * The first slot whose name contains the text, or -1.
     */
    public int find(String nameContains)
    {
        for (int slot = 0; slot < _menu.size(); slot++)
        {
            String name = name(slot);
            if (name != null && name.contains(nameContains))
                return slot;
        }
        return -1;
    }

    /**
     * Like {@link #find(String)}, but fails when there is no such slot.
     */
    public int slotOf(String nameContains)
    {
        int slot = find(nameContains);
        if (slot < 0)
            throw new AssertionError("No slot named like '" + nameContains + "' in\n" + dump());
        return slot;
    }

    // Doing things ===================================================================================================

    public MenuTester click(int slot)
    {
        return click(slot, ClickKind.LEFT);
    }

    public MenuTester click(int slot, ClickKind kind)
    {
        if (_closed)
            throw new AssertionError("Clicked slot " + slot + " of a closed menu");
        if (_prompt != null)
            throw new AssertionError("Clicked slot " + slot + " while a prompt is open: " + Text.plain(_prompt.title()));
        _session.click(slot, kind, -1);
        return this;
    }

    /**
     * Left clicks the slot named like the text.
     */
    public MenuTester click(String nameContains)
    {
        return click(slotOf(nameContains));
    }

    public MenuTester click(String nameContains, ClickKind kind)
    {
        return click(slotOf(nameContains), kind);
    }

    /**
     * Runs the menu's timer once, as the server does every {@link Menu#refreshTicks()}.
     */
    public MenuTester tick()
    {
        _session.tick();
        return this;
    }

    /**
     * The open prompt, or null.
     */
    public Prompt prompt()
    {
        return _prompt;
    }

    /**
     * Types an answer to the open prompt and saves it.
     *
     * @return null when it was accepted and the menu is back, otherwise the problem shown
     */
    public String answer(String text)
    {
        if (_prompt == null)
            throw new AssertionError("No prompt is open");
        String problem = _session.answer(text);
        if (problem == null)
            _session.promptClosed();
        return problem;
    }

    public MenuTester cancelPrompt()
    {
        if (_prompt == null)
            throw new AssertionError("No prompt is open");
        _session.promptClosed();
        return this;
    }

    /**
     * The player closes the window.
     */
    public MenuTester closeWindow()
    {
        _session.closedByViewer();
        _closed = true;
        return this;
    }

    /**
     * The menu as text, one row a line, each slot by its name or material.
     */
    public String dump()
    {
        if (_closed)
            return "(closed)";
        StringBuilder text = new StringBuilder(title()).append('\n');
        for (int row = 0; row < _menu.rows(); row++)
        {
            for (int column = 0; column < 9; column++)
            {
                Icon icon = _slots.get(row * 9 + column);
                String cell = icon == null ? "." : icon.hideTooltip() ? "#" : icon.name() != null ? icon.plainName() : icon.material().replace("minecraft:", "");
                if (cell.length() > 14)
                    cell = cell.substring(0, 13) + "~";
                text.append(String.format("%-15s", cell));
            }
            text.append('\n');
        }
        return text.toString();
    }

    /**
     * What the viewer holds on the cursor; any object stands for an item.
     */
    @Override
    public Object cursor()
    {
        return _cursor;
    }

    public MenuTester holding(Object item)
    {
        _cursor = item;
        return this;
    }

    // MenuHost =======================================================================================================

    @Override
    public void show(Menu menu, Map<Integer, Icon> icons)
    {
        _menu = menu;
        _prompt = null;
        _slots.clear();
        _slots.putAll(icons);
        _shown.add(menu);
    }

    @Override
    public void set(int slot, Icon icon)
    {
        if (icon == null)
            _slots.remove(slot);
        else
            _slots.put(slot, icon);
    }

    @Override
    public void prompt(Prompt prompt)
    {
        _prompt = prompt;
    }

    @Override
    public void setCursor(Object item)
    {
        _cursor = item;
    }

    @Override
    public void close()
    {
        _closed = true;
    }

    @Override
    public void error(String what, RuntimeException error)
    {
        throw new AssertionError(what + " failed", error);
    }
}
