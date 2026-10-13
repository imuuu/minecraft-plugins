package imu.iAPI.Menus;

import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.function.Function;

/**
 * Asks the viewer for text in place of the menu, which comes back after the answer or a cancel.
 *
 * @param rows     0 for one line, more for a text box of that many rows; the answer then has a \n between rows
 * @param onAnswer gets the typed text; returns null to accept it, or a message to ask again with that error
 */
public record Prompt(Component title, List<Component> lines, String initial, int maxLength, int rows,
                     Function<String, String> onAnswer)
{
    public static Prompt of(String legacyTitle, List<String> legacyLines, String initial, int maxLength,
                            Function<String, String> onAnswer)
    {
        return new Prompt(Text.of(legacyTitle), Text.of(legacyLines), initial, maxLength, 0, onAnswer);
    }

    /**
     * The same prompt as a text box of several rows.
     */
    public Prompt multiline(int rows)
    {
        return new Prompt(title, lines, initial, maxLength, rows, onAnswer);
    }
}
