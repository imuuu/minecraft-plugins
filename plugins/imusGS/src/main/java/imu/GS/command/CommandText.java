package imu.GS.command;

import imu.iAPI.Other.Metods;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

/**
 * Text helpers shared by the /gs commands.
 */
final class CommandText
{
    private CommandText() {}

    static void send(CommandSender sender, String message)
    {
        sender.sendMessage(Metods.msgC(message));
    }

    /**
     * The words from {@code from} on, joined with spaces, without quotes.
     */
    static String rest(String[] args, int from)
    {
        if (args.length <= from)
            return "";
        return String.join(" ", Arrays.copyOfRange(args, from, args.length)).replace("\"", "").trim();
    }

    static String format(double value)
    {
        if (value == Math.rint(value) && Math.abs(value) < 1e15)
            return String.valueOf((long) value);
        return String.valueOf(Math.round(value * 100) / 100.0);
    }

    static Double number(String text)
    {
        try
        {
            return Double.parseDouble(text.replace(',', '.'));
        } catch (NumberFormatException e)
        {
            return null;
        }
    }
}
