package imu.iAPI.Utilities;

import imu.iAPI.Main.ImusAPI;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Asks the player for a line of text in a Paper dialog window.
 *
 * <pre>
 * TextInputDialog.show(player, "Shop name", List.of("&amp; colours work"), currentName, 64,
 *         text -&gt; text.isBlank() ? "The name can't be empty" : apply(text),
 *         () -&gt; menu.open(player));
 * </pre>
 */
public final class TextInputDialog
{
    private static final String INPUT_KEY = "text";
    private static final ClickCallback.Options CALLBACK_OPTIONS = ClickCallback.Options.builder()
            .uses(1)
            .lifetime(Duration.ofMinutes(10))
            .build();

    private TextInputDialog() {}

    /**
     * @param lines   explanation shown above the text field, &amp; colours work
     * @param onSave  runs on the main thread with the typed text. Return null to accept it, or a message
     *                to show the dialog again with that error
     * @param onClose runs on the main thread after the text was accepted or the player cancelled
     */
    public static void show(Player player, String title, List<String> lines, String initial, int maxLength,
                            Function<String, String> onSave, Runnable onClose)
    {
        show(player, title, lines, initial, maxLength, 0, onSave, onClose, null);
    }

    /**
     * A text box of several lines; the text given to onSave has one line per row, separated by \n.
     */
    public static void showMultiline(Player player, String title, List<String> lines, String initial, int maxLength,
                                     int rows, Function<String, String> onSave, Runnable onClose)
    {
        show(player, title, lines, initial, maxLength, rows, onSave, onClose, null);
    }

    private static void show(Player player, String title, List<String> lines, String initial, int maxLength,
                             int rows, Function<String, String> onSave, Runnable onClose, String error)
    {
        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();

        List<DialogBody> body = new ArrayList<>();
        for (String line : lines)
            body.add(DialogBody.plainMessage(legacy.deserialize("&7" + line)));
        if (error != null)
            body.add(DialogBody.plainMessage(Component.text(error, NamedTextColor.RED)));

        TextDialogInput.Builder inputBuilder = DialogInput.text(INPUT_KEY, Component.text(""))
                .initial(initial == null ? "" : initial)
                .maxLength(maxLength)
                .width(300);
        if (rows > 0)
            inputBuilder.multiline(TextDialogInput.MultilineOptions.create(rows, rows * 12));
        DialogInput input = inputBuilder.build();

        ActionButton save = ActionButton.builder(Component.text("Save", NamedTextColor.GREEN))
                .action(DialogAction.customClick(onMainThread((view, audience) ->
                {
                    String text = view.getText(INPUT_KEY);
                    String problem = onSave.apply(text == null ? "" : text);
                    if (problem != null)
                    {
                        show(player, title, lines, text, maxLength, rows, onSave, onClose, problem);
                        return;
                    }
                    if (onClose != null)
                        onClose.run();
                }), CALLBACK_OPTIONS))
                .build();

        ActionButton cancel = ActionButton.builder(Component.text("Cancel", NamedTextColor.GRAY))
                .action(DialogAction.customClick(onMainThread((view, audience) ->
                {
                    if (onClose != null)
                        onClose.run();
                }), CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(factory -> factory.empty()
                .base(DialogBase.builder(legacy.deserialize("&6" + title))
                        .canCloseWithEscape(true)
                        .afterAction(DialogBase.DialogAfterAction.CLOSE)
                        .body(body)
                        .inputs(List.of(input))
                        .build())
                .type(DialogType.confirmation(save, cancel)));

        player.closeInventory();
        player.showDialog(dialog);
    }

    private static DialogActionCallback onMainThread(DialogActionCallback callback)
    {
        return (view, audience) ->
        {
            if (Bukkit.isPrimaryThread())
                callback.accept(view, audience);
            else
                Bukkit.getScheduler().runTask(ImusAPI._instance, () -> callback.accept(view, audience));
        };
    }
}
