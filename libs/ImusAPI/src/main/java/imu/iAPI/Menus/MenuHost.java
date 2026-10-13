package imu.iAPI.Menus;

import java.util.Map;

/**
 * Shows a session's menus to its viewer: an inventory window in game, a record of slots in tests.
 */
public interface MenuHost
{
    /**
     * Shows a menu in place of whatever was shown.
     */
    void show(Menu menu, Map<Integer, Icon> icons);

    /**
     * Changes one slot of the menu shown; a null icon empties it.
     */
    void set(int slot, Icon icon);

    /**
     * The menu shown changed outside a click. The host calls {@link MenuSession#flush()} later, at most every
     * {@link Menu#refreshDelayTicks()}, however many times this was called in between.
     */
    void refreshRequested();

    /**
     * Shows the prompt in place of the menu. Answers go to {@link MenuSession#answer(String)}, and the end, saved
     * or cancelled, to {@link MenuSession#promptClosed()}.
     */
    void prompt(Prompt prompt);

    /**
     * The item on the viewer's cursor, or null.
     */
    Object cursor();

    void setCursor(Object item);

    /**
     * Closes the window; the session has ended.
     */
    void close();

    /**
     * A menu threw. The session closes every menu right after, unless only a menu's onClose threw.
     */
    void error(String what, RuntimeException error);
}
