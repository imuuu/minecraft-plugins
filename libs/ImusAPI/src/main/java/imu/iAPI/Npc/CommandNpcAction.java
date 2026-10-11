package imu.iAPI.Npc;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * The built-in action: runs commands when the NPC is clicked. One command per line, without the slash.
 * Commands run as the server; a line starting with "player:" runs as the player who clicked.
 * %player% and %uuid% are the player who clicked.
 */
public class CommandNpcAction implements NpcAction
{
    public static final String ID = "imusapi:commands";
    public static final String PERMISSION = "ia.npc.commands";
    private static final String AS_PLAYER = "player:";

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public String getName()
    {
        return "Runs commands";
    }

    @Override
    public void use(Player player, LivingEntity npc, String data)
    {
        for (String line : lines(data))
        {
            String command = line.replace("%player%", player.getName()).replace("%uuid%", player.getUniqueId().toString());
            if (command.regionMatches(true, 0, AS_PLAYER, 0, AS_PLAYER.length()))
            {
                String asPlayer = command.substring(AS_PLAYER.length()).trim();
                player.performCommand(asPlayer.startsWith("/") ? asPlayer.substring(1) : asPlayer);
            }
            else
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    @Override
    public String describe(String data)
    {
        List<String> lines = lines(data);
        if (lines.isEmpty())
            return "&cno commands yet";
        String first = "&f/" + lines.get(0);
        return lines.size() == 1 ? first : first + " &7+" + (lines.size() - 1) + " more";
    }

    @Override
    public String getDataPrompt()
    {
        return "Commands";
    }

    @Override
    public List<String> getDataHelp()
    {
        return List.of(
                "One command per line, without the /",
                "&e%player% &7= the player who clicked",
                "Runs as the server. Start a line with &eplayer: &7to run it as the player");
    }

    @Override
    public int getDataRows()
    {
        return 6;
    }

    @Override
    public String parseData(String text)
    {
        List<String> lines = lines(text);
        if (lines.isEmpty())
            throw new IllegalArgumentException("Write at least one command");
        return String.join("\n", lines);
    }

    @Override
    public String getPermission()
    {
        return PERMISSION;
    }

    static List<String> lines(String data)
    {
        List<String> lines = new ArrayList<>();
        if (data == null)
            return lines;
        for (String line : data.split("\n"))
        {
            String trimmed = line.trim();
            if (trimmed.startsWith("/"))
                trimmed = trimmed.substring(1);
            if (!trimmed.isEmpty())
                lines.add(trimmed);
        }
        return lines;
    }
}
