package imu.WorldRestore.SubCommands;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.WorldRestore.Interfaces.CommandInterface;
import imu.WorldRestore.Managers.ChunkManager;
import imu.WorldRestore.main.Main;
import net.md_5.bungee.api.ChatColor;

public class subTeleportCmd implements CommandInterface
{
	Main _main = null;
	ChunkManager _cManager = null;
	public subTeleportCmd(Main main) 
	{
		_main = main;
		_cManager = _main.getChunkManager();
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;
        tpPlayer(player);
        return false;
    }
    
    void tpPlayer(Player player)
	{
		World w = player.getWorld();
		World tw = _main.getServer().getWorld(_cManager.getDefTargetWorldName());
		if(w.getName().equalsIgnoreCase(_cManager.getWorldName()))
		{
			player.sendMessage(ChatColor.GOLD +"You have now teleported "+ChatColor.DARK_PURPLE+"copy"+ChatColor.GOLD+" world");
			player.teleport(new Location(tw, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(),player.getLocation().getPitch()));
		}else
		{
			player.sendMessage(ChatColor.GOLD +"You have now teleported "+ChatColor.WHITE+"normal"+ChatColor.GOLD+" world");
			player.teleport(new Location(_main.getServer().getWorld(_cManager.getWorldName()), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch()));
		}
	}
    
   
   
}