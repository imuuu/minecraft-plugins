package imu.iAPI.Menus;

import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.function.Function;

/**
 * Asks the viewer for a line of text in place of the menu, which comes back after the answer or a cancel.
 *
 * @param onAnswer gets the typed text; returns null to accept it, or a message to ask again with that error
 */
public record Prompt(Component title, List<Component> lines, String initial, int maxLength, Function<String, String> onAnswer)
{
    public static Prompt of(String legacyTitle, List<String> legacyLines, String initial, int maxLength,
                            Function<String, String> onAnswer)
    {
        return new Prompt(Text.of(legacyTitle), Text.of(legacyLines), initial, maxLength, onAnswer);
    }
}
