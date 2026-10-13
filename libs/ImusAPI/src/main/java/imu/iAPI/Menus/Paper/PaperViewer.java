package imu.iAPI.Menus.Paper;

import imu.iAPI.Menus.Viewer;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * A player, or the console, as a menu {@link Viewer}.
 */
public record PaperViewer(CommandSender sender) implements Viewer
{
    private static final UUID CONSOLE = new UUID(0, 0);

    public static PaperViewer of(CommandSender sender)
    {
        return new PaperViewer(sender);
    }

    /**
     * The player, or null for the console.
     */
    public Player player()
    {
        return sender instanceof Player player ? player : null;
    }

    @Override
    public UUID id()
    {
        return sender instanceof Player player ? player.getUniqueId() : CONSOLE;
    }

    @Override
    public String name()
    {
        return sender.getName();
    }

    @Override
    public boolean hasPermission(String permission)
    {
        return sender.hasPermission(permission);
    }

    @Override
    public void message(Component text)
    {
        sender.sendMessage(text);
    }
}
