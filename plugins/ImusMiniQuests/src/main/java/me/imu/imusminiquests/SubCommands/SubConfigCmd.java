package me.imu.imusminiquests.SubCommands;

import imu.iAPI.Config.ConfigMenu;
import imu.iAPI.Interfaces.CommandInterface;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SubConfigCmd implements CommandInterface
{
    private final ConfigMenu _menu;

    public SubConfigCmd(ConfigMenu menu)
    {
        _menu = menu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        if (!(sender instanceof Player player))
        {
            sender.sendMessage("Only players can open the settings menu, use /imq reload after editing config.yml");
            return true;
        }
        _menu.open(player);
        return true;
    }

    @Override
    public void FailedMsg(CommandSender arg0, String arg1)
    {

    }
}
