package imu.def.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.def.Interfaces.CommandInterface;

public class TestSubCmd implements CommandInterface
{
	@Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Player p = (Player) sender;
 
        //if(args.length > 1) return false;
 
        p.sendMessage("example test!");
        return false;
    }
}
