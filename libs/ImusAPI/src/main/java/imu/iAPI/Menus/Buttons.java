package imu.iAPI.Menus;

import org.bukkit.Material;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

/**
 * Common buttons. They draw from the value given and hand changes to a setter; the menu is drawn again after the
 * click, so it shows the new value.
 */
public final class Buttons
{
    private Buttons() {}

    public static Button back()
    {
        return Button.of(Icon.of(Material.RED_STAINED_GLASS_PANE, "&bGo Back"), Click::back);
    }

    /**
     * Puts a back button in the slot when there is a menu to go back to.
     */
    public static void back(Canvas canvas, int slot)
    {
        if (canvas.canGoBack())
            canvas.set(slot, back());
    }

    public static Button close()
    {
        return Button.of(Icon.of(Material.BARRIER, "&cClose"), Click::close);
    }

    /**
     * Shows on or off in the lore and turns it the other way on a click.
     */
    public static Button toggle(Icon icon, boolean on, Consumer<Boolean> set)
    {
        Icon shown = icon.addLore(on ? "&aOn" : "&cOff", "&bClick &7to turn " + (on ? "off" : "on")).glow(on);
        return Button.of(shown, click -> set.accept(!on));
    }

    /**
     * Steps through options: left click the next one, right click the previous one.
     */
    public static <T> Button cycle(Icon icon, List<T> options, T current, Function<T, String> label, Consumer<T> set)
    {
        int index = Math.max(0, options.indexOf(current));
        String[] lines = new String[options.size() + 1];
        for (int i = 0; i < options.size(); i++)
            lines[i] = (i == index ? "&a> " : "&7  ") + label.apply(options.get(i));
        lines[options.size()] = "&bLeft/right click &7to change";
        return Button.of(icon.addLore(lines), click ->
        {
            int step = click.isRight() ? -1 : 1;
            set.accept(options.get(Math.floorMod(index + step, options.size())));
        });
    }

    /**
     * A number between min and max: left click adds one, right click takes one away, with shift ten.
     */
    public static Button counter(Icon icon, int value, int min, int max, IntConsumer set)
    {
        Icon shown = icon.amount(Math.max(1, Math.min(99, value)))
                .addLore("&9" + value, "&bLeft &7+1, &bright &7-1, &bshift &7x10");
        return Button.of(shown, click ->
        {
            int step = (click.isRight() ? -1 : 1) * (click.isShift() ? 10 : 1);
            int next = Math.max(min, Math.min(max, value + step));
            if (next != value)
                set.accept(next);
        });
    }

    /**
     * Opens a {@link ConfirmMenu} before doing something that can't be undone.
     */
    public static Button confirm(Icon icon, String question, Consumer<Click> onYes)
    {
        return Button.of(icon, click -> click.open(new ConfirmMenu(question, icon, onYes)));
    }
}
