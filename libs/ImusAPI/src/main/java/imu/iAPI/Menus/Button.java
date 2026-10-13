package imu.iAPI.Menus;

import java.util.function.Consumer;

/**
 * An icon in a slot and what clicking it does. A button without an action only shows its icon.
 */
public record Button(Icon icon, Consumer<Click> action)
{
    public static Button display(Icon icon)
    {
        return new Button(icon, null);
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
