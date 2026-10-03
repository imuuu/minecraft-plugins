package imu.iAPI.Config;

import imu.iAPI.Main.ImusAPI;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
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

/**
 * Asks for a new value of a {@link ConfigEntry} in a Paper dialog window.
 */
public final class ConfigValueDialog
{
    private static final String INPUT_KEY = "value";
    private static final ClickCallback.Options CALLBACK_OPTIONS = ClickCallback.Options.builder()
            .uses(1)
            .lifetime(Duration.ofMinutes(10))
            .build();

    private ConfigValueDialog() {}

    /**
     * @param onDone runs on the main thread when the player saves or cancels
     */
    public static void show(Player player, ConfigMenu menu, ConfigEntry entry, Runnable onDone)
    {
        show(player, menu, entry, onDone, null, null);
    }

    private static void show(Player player, ConfigMenu menu, ConfigEntry entry, Runnable onDone, String error, String typed)
    {
        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();
        String current = entry.format(entry.get(menu.getPlugin().getConfig()));

        List<DialogBody> body = new ArrayList<>();
        for (String line : entry.getDescription())
            body.add(DialogBody.plainMessage(legacy.deserialize("&7" + line)));
        body.add(DialogBody.plainMessage(legacy.deserialize("&9Current value: &a" + current)));
        if (entry.hasRange())
            body.add(DialogBody.plainMessage(legacy.deserialize("&9Allowed: &7" + entry.rangeText())));
        if (entry.getNote() != null)
            body.add(DialogBody.plainMessage(legacy.deserialize("&9Takes effect: &e" + entry.getNote())));
        if (error != null)
            body.add(DialogBody.plainMessage(Component.text(error, NamedTextColor.RED)));

        DialogInput input = DialogInput.text(INPUT_KEY, Component.text("New value"))
                .initial(typed != null ? typed : current)
                .maxLength(256)
                .width(300)
                .build();

        ActionButton save = ActionButton.builder(Component.text("Save", NamedTextColor.GREEN))
                .action(DialogAction.customClick(onMainThread((view, audience) ->
                {
                    String text = view.getText(INPUT_KEY);
                    Object value;
                    try
                    {
                        value = entry.parse(text);
                    } catch (IllegalArgumentException e)
                    {
                        show(player, menu, entry, onDone, e.getMessage(), text);
                        return;
                    }
                    menu.setValue(player, entry, value);
                    onDone.run();
                }), CALLBACK_OPTIONS))
                .build();

        ActionButton cancel = ActionButton.builder(Component.text("Cancel", NamedTextColor.GRAY))
                .action(DialogAction.customClick(onMainThread((view, audience) -> onDone.run()), CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(factory -> factory.empty()
                .base(DialogBase.builder(legacy.deserialize("&6" + entry.getName()))
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
