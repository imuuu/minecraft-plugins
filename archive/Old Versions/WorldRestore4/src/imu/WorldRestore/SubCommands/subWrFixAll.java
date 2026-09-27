package imu.WorldRestore.SubCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.WorldRestore.Interfaces.CommandInterface;
import imu.WorldRestore.Managers.ChunkManager;
import imu.WorldRestore.main.Main;
import net.md_5.bungee.api.ChatColor;

public class subWrFixAll implements CommandInterface
{
	Main _main = null;
	ChunkManager _cManager = null;
	public subWrFixAll(Main main) 
	{
		_main = main;
		_cManager = _main.getChunkManager();
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;
        _main.getChunkFileHandler().setAllChunksToFix();
        player.sendMessage(ChatColor.DARK_PURPLE + "All visited chunks are put to fix");
        return false;
    }
    
   
   
}