package imu.iWaystones.SubCmds;

import imu.iAPI.Interfaces.CommandInterface;
import imu.iWaystones.Main.ImusWaystones;
import imu.iWaystones.Managers.WaystoneManager;
import imu.iWaystones.Other.CmdData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SubWaystoneReloadCommand implements CommandInterface {
    CmdData _data;
    WaystoneManager _wManager;
    public SubWaystoneReloadCommand(CmdData data) {
        _data = data;
        _wManager = ImusWaystones._instance.GetWaystoneManager();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) 
    {
    	
        if(commandSender instanceof Player) 
        {
        	Player player = (Player) commandSender;
            if(player.hasPermission(ImusWaystones._instance.perm_reload)) {
                _wManager.reload(player);
                return true;
            } else {
                player.sendMessage(ChatColor.DARK_GREEN + "You don't have permission to do that!");
                return false;
            }
        }
        return false;
    }

    @Override
    public void FailedMsg(CommandSender commandSender, String s) {

    }
}
