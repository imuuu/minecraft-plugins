package imu.GS.command;

import imu.iAPI.Interfaces.CommandInterface;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

/**
 * A /gs sub command as a method of {@link GsCommands}. ImusAPI's CommandHandler only passes players here.
 */
public record SubCommand(BiConsumer<Player, String[]> action) implements CommandInterface
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        action.accept((Player) sender, args);
        return true;
    }

    @Override
    public void FailedMsg(CommandSender sender, String msg)
    {
    }
}
