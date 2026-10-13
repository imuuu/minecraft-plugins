package imu.iAPI.Menus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * What a menu draws into: the button of each slot. A slot set again replaces what was there.
 */
public final class Canvas
{
    private final int _size;
    private final Viewer _viewer;
    private final boolean _canGoBack;
    private final Map<Integer, Button> _buttons = new HashMap<>();

    public Canvas(int size, Viewer viewer, boolean canGoBack)
    {
        _size = size;
        _viewer = viewer;
        _canGoBack = canGoBack;
    }

    public Canvas set(int slot, Button button)
    {
        if (slot < 0 || slot >= _size)
            throw new IllegalArgumentException("Slot " + slot + " is outside the menu of " + _size + " slots");
        if (button == null)
            _buttons.remove(slot);
        else
            _buttons.put(slot, button);
        return this;
    }

    public Canvas set(int slot, Icon icon)
    {
        return set(slot, Button.display(icon));
    }

    public Canvas set(int slot, Icon icon, Consumer<Click> action)
    {
        return set(slot, new Button(icon, action));
    }

    /**
     * Every slot.
     */
    public Canvas fill(Icon icon)
    {
        for (int slot = 0; slot < _size; slot++)
            set(slot, icon);
        return this;
    }

    /**
     * A row, 0 at the top.
     */
    public Canvas fillRow(int row, Icon icon)
    {
        for (int slot = row * 9; slot < (row + 1) * 9 && slot < _size; slot++)
            set(slot, icon);
        return this;
    }

    public void clear(int slot)
    {
        _buttons.remove(slot);
    }

    public Button get(int slot)
    {
        return _buttons.get(slot);
    }

    public int size()
    {
        return _size;
    }

    public int rows()
    {
        return _size / 9;
    }

    /**
     * The first slot of the bottom row.
     */
    public int bottomRow()
    {
        return _size - 9;
    }

    public Viewer viewer()
    {
        return _viewer;
    }

    /**
     * Whether this menu was opened from another one it can go back to.
     */
    public boolean canGoBack()
    {
        return _canGoBack;
    }

    public Map<Integer, Button> buttons()
    {
        return Collections.unmodifiableMap(_buttons);
    }
}
