package imu.iAPI.Menus;

import java.util.function.Consumer;

/**
 * An icon in a slot and what clicking it does. A button without an action only shows its icon; one without an
 * icon is an empty slot that still hears clicks, e.g. to take the item on the player's cursor.
 */
public record Button(Icon icon, Consumer<Click> action)
{
    public static Button display(Icon icon)
    {
        return new Button(icon, null);
    }

    /**
     * An empty slot that hears clicks.
     */
    public static Button empty(Consumer<Click> action)
    {
        return new Button(null, action);
    }

    public static Button of(Icon icon, Consumer<Click> action)
    {
        return new Button(icon, action);
    }

    /**
     * Runs the action on a left or right click without shift.
     */
    public static Button onClick(Icon icon, Runnable action)
    {
        return new Button(icon, click -> action.run());
    }
}
