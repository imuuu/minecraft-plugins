package imu.iAPI.Menus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * One viewer's way through menus: the menu shown, the ones to go back to, and an open prompt. It draws the menus
 * and tells its {@link MenuHost} what changed; it doesn't know about the server.
 * <p>
 * A menu that throws (drawing, a click, a prompt answer, its timer) is closed at once with every menu under it:
 * a menu in a broken state could hand out items it shouldn't, and closing gives back the items the menus hold.
 * One menu's {@code onClose} throwing doesn't stop the others from closing.
 */
public final class MenuSession
{
    private final Viewer _viewer;
    private final MenuHost _host;
    private final Deque<Menu> _previous = new ArrayDeque<>();
    private Menu _current;
    private Map<Integer, Button> _buttons = Map.of();
    private Prompt _prompt;
    private boolean _ended;
    // While a menu is being shown; a refresh from its onOpen waits for the first drawing
    private boolean _showing;
    // A change from outside a click waits to be drawn
    private boolean _dirty;

    public MenuSession(Viewer viewer, MenuHost host)
    {
        _viewer = viewer;
        _host = host;
    }

    public Viewer viewer()
    {
        return _viewer;
    }

    MenuHost host()
    {
        return _host;
    }

    /**
     * The menu shown, or null when the session has ended.
     */
    public Menu current()
    {
        return _ended ? null : _current;
    }

    public boolean hasEnded()
    {
        return _ended;
    }

    public boolean canGoBack()
    {
        return !_previous.isEmpty();
    }

    /**
     * The menu shown and the ones to go back to, the shown one first; empty when the session has ended.
     */
    public List<Menu> menus()
    {
        List<Menu> menus = new ArrayList<>();
        if (_ended)
            return menus;
        if (_current != null)
            menus.add(_current);
        menus.addAll(_previous);
        return menus;
    }

    /**
     * The prompt waiting for an answer, or null.
     */
    public Prompt prompt()
    {
        return _prompt;
    }

    // Moving between menus ===========================================================================================

    /**
     * Shows a menu; {@link #back()} returns to the one shown now.
     */
    public void open(Menu menu)
    {
        checkActive();
        if (_current != null)
            _previous.push(_current);
        show(menu);
    }

    /**
     * Shows a menu in place of the one shown now, which is closed.
     */
    public void replace(Menu menu)
    {
        checkActive();
        if (_current != null)
            closeMenu(_current);
        show(menu);
    }

    /**
     * Returns to the previous menu, or closes when there is none.
     */
    public void back()
    {
        if (_ended)
            return;
        if (_previous.isEmpty())
        {
            close();
            return;
        }
        closeMenu(_current);
        show(_previous.pop());
    }

    /**
     * Closes the window and ends the session.
     */
    public void close()
    {
        if (_ended)
            return;
        end();
        _host.close();
    }

    /**
     * The viewer closed the window themselves.
     */
    public void closedByViewer()
    {
        if (!_ended)
            end();
    }

    private void end()
    {
        _ended = true;
        _prompt = null;
        if (_current != null)
            closeMenu(_current);
        while (!_previous.isEmpty())
            closeMenu(_previous.pop());
        _buttons = Map.of();
    }

    /**
     * Something a menu did threw: tell the host and close every menu.
     */
    private void fail(String what, RuntimeException error)
    {
        try
        {
            _host.error(what, error);
        } finally
        {
            close();
        }
    }

    private static String nameOf(Menu menu)
    {
        return menu == null ? "a menu" : menu.getClass().getSimpleName();
    }

    private void show(Menu menu)
    {
        Objects.requireNonNull(menu, "menu");
        _current = menu;
        _prompt = null;
        // Drawn whole now, so nothing waits any more
        _dirty = false;
        menu._session = this;
        _showing = true;
        try
        {
            menu.onOpen(_viewer);
            _buttons = render(menu);
        } catch (RuntimeException e)
        {
            _showing = false;
            fail("Opening " + nameOf(menu), e);
            return;
        } finally
        {
            _showing = false;
        }
        try
        {
            _host.show(menu, icons(_buttons));
        } catch (RuntimeException e)
        {
            fail("Showing " + nameOf(menu), e);
        }
    }

    // Each menu closes on its own, so one that throws doesn't keep the others (and their items) open
    private void closeMenu(Menu menu)
    {
        try
        {
            menu.onClose(_viewer);
        } catch (RuntimeException e)
        {
            _host.error("Closing " + nameOf(menu), e);
        } finally
        {
            if (menu._session == this && !_previous.contains(menu))
                menu._session = null;
        }
    }

    private void checkActive()
    {
        if (_ended)
            throw new IllegalStateException("The menu session of " + _viewer.name() + " has ended");
    }

    // Drawing ========================================================================================================

    private Map<Integer, Button> render(Menu menu)
    {
        Canvas canvas = new Canvas(menu.size(), _viewer, !_previous.isEmpty());
        menu.render(canvas);
        return new HashMap<>(canvas.buttons());
    }

    private static Map<Integer, Icon> icons(Map<Integer, Button> buttons)
    {
        Map<Integer, Icon> icons = new HashMap<>();
        buttons.forEach((slot, button) ->
        {
            if (button.icon() != null)
                icons.put(slot, button.icon());
        });
        return icons;
    }

    /**
     * Marks the menu shown as changed; the host draws it with {@link #flush()} a little later.
     */
    public void requestRefresh()
    {
        if (_ended || _current == null || _showing || _dirty)
            return;
        _dirty = true;
        _host.refreshRequested();
    }

    /**
     * Draws the menu now if it waits to be drawn.
     */
    public void flush()
    {
        if (_dirty)
            refresh();
    }

    /**
     * Draws the menu shown again at once and sends the slots that changed.
     */
    public void refresh()
    {
        _dirty = false;
        if (_ended || _current == null || _prompt != null || _showing)
            return;
        try
        {
            Map<Integer, Button> next = render(_current);
            for (int slot = 0; slot < _current.size(); slot++)
            {
                Button before = _buttons.get(slot);
                Button after = next.get(slot);
                Icon was = before == null ? null : before.icon();
                Icon now = after == null ? null : after.icon();
                if (!Objects.equals(was, now))
                    _host.set(slot, now);
            }
            _buttons = next;
        } catch (RuntimeException e)
        {
            fail("Drawing " + nameOf(_current), e);
        }
    }

    /**
     * Runs the menu's timer and draws it again; the host calls this every {@link Menu#refreshTicks()}.
     */
    public void tick()
    {
        if (_ended || _current == null)
            return;
        boolean changed;
        try
        {
            changed = _current.onTick();
        } catch (RuntimeException e)
        {
            fail("The timer of " + nameOf(_current), e);
            return;
        }
        if (changed)
            refresh();
    }

    /**
     * The button in a slot of the menu as it was last drawn, or null.
     */
    public Button button(int slot)
    {
        return _buttons.get(slot);
    }

    // Clicks and prompts =============================================================================================

    /**
     * A click on a slot of the menu shown.
     *
     * @param hotbarKey 0-8 for a number key, otherwise -1
     */
    public void click(int slot, ClickKind kind, int hotbarKey)
    {
        if (_ended || _current == null || _prompt != null)
            return;
        Button button = _buttons.get(slot);
        if (button == null || button.action() == null)
            return;

        Menu menu = _current;
        try
        {
            button.action().accept(new Click(this, menu, slot, kind, hotbarKey));
        } catch (RuntimeException e)
        {
            fail("Clicking slot " + slot + " of " + nameOf(menu), e);
            return;
        }
        refresh();
    }

    public void prompt(Prompt prompt)
    {
        checkActive();
        _prompt = Objects.requireNonNull(prompt, "prompt");
        _host.prompt(prompt);
    }

    /**
     * The viewer's answer to the prompt.
     *
     * @return null when it was accepted, otherwise the problem to show with the prompt again
     */
    public String answer(String text)
    {
        if (_prompt == null)
            return null;
        try
        {
            return _prompt.onAnswer().apply(text);
        } catch (RuntimeException e)
        {
            // Accepted, so the dialog closes; the session has ended and the menu doesn't come back
            fail("Answering the prompt of " + nameOf(_current), e);
            return null;
        }
    }

    /**
     * The prompt was answered or cancelled; the menu comes back.
     */
    public void promptClosed()
    {
        if (_prompt == null || _ended)
            return;
        _prompt = null;
        show(_current);
    }
}
