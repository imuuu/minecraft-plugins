package Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.bullterrier292.WorldRestore.ChunkINFO;
import me.bullterrier292.WorldRestore.WorldRestore;

public class WRCommands implements CommandExecutor
{

	private WorldRestore _p;
	public String cmd1 ="cord";
	public String cmd2 ="wr";
	
	public WRCommands(WorldRestore plugin)
	{
		_p=plugin;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] arg) 
	{
		if(sender instanceof Player)
		{
			Player player=(Player)sender;
			
			if(cmd.getName().equalsIgnoreCase(cmd1))
			{
				Location loc=((Player) sender).getLocation();
				for(Player playerrr: Bukkit.getServer().getOnlinePlayers())
				{
					
					playerrr.sendMessage("Given Cordinates: "+loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockX());
					return true;
				}
			}
			
			if(cmd.getName().equalsIgnoreCase(cmd2))
			{
				Chunk c=player.getLocation().getChunk();
				if(_p.chunkInfos.containsKey(c))
				{
					ChunkINFO info=_p.chunkInfos.get(c);
					info.fixChunk();
				}else
				{
					player.sendMessage("Chunk not found");
				}
				return true;
			}
			
			
		}else
		{
			sender.sendMessage(ChatColor.RED +"Only player can use this command!");
			return true;
		}
		
		return false;
	}

}
