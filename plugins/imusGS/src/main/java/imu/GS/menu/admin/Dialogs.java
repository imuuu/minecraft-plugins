package imu.GS.menu.admin;

import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Utilities.TextInputDialog;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Asking admins for text and numbers in a dialog window. The menu that asked closes while the dialog is open and
 * opens again when it is done.
 */
final class Dialogs
{
    private Dialogs() {}

    /**
     * @param apply runs with the typed text; return null to accept it or a message to ask again
     */
    static void text(CustomInventory menu, String title, List<String> lines, String initial, Function<String, String> apply)
    {
        Player player = menu.getPlayer();
        TextInputDialog.show(player, title, lines, initial, 256, apply, () -> menu.open(player));
    }

    static void number(CustomInventory menu, String title, List<String> lines, double initial, double min, double max, Consumer<Double> apply)
    {
        text(menu, title, lines, format(initial), text ->
        {
            double value;
            try
            {
                value = Double.parseDouble(text.trim().replace(',', '.'));
            } catch (NumberFormatException e)
            {
                return "'" + text + "' is not a number";
            }
            if (Double.isNaN(value) || value < min || value > max)
                return "Must be between " + format(min) + " and " + format(max);
            apply.accept(value);
            return null;
        });
    }

    static void wholeNumber(CustomInventory menu, String title, List<String> lines, int initial, int min, int max, Consumer<Integer> apply)
    {
        text(menu, title, lines, String.valueOf(initial), text ->
        {
            int value;
            try
            {
                value = Integer.parseInt(text.trim());
            } catch (NumberFormatException e)
            {
                return "'" + text + "' is not a whole number";
            }
            if (value < min || value > max)
                return "Must be between " + min + " and " + max;
            apply.accept(value);
            return null;
        });
    }

    static String format(double value)
    {
        if (value == Math.rint(value) && Math.abs(value) < 1e15)
            return String.valueOf((long) value);
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
