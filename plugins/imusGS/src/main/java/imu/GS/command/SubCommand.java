package imu.GS.command;

import imu.iAPI.Interfaces.CommandInterface;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

/**
 * A /gs sub command as a method of {@link GsCommands}. ImusAPI's CommandHandler passes only players here, unless
 * the sub command was allowed for the console too.
 */
public record SubCommand(BiConsumer<CommandSender, String[]> action) implements CommandInterface
{
    /**
     * A sub command only players run.
     */
    public static SubCommand players(BiConsumer<Player, String[]> action)
    {
        return new SubCommand((sender, args) -> action.accept((Player) sender, args));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        action.accept(sender, args);
        return true;
    }

    @Override
    public void FailedMsg(CommandSender sender, String msg)
    {
    }
}
