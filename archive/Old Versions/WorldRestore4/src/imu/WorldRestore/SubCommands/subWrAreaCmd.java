package imu.WorldRestore.SubCommands;

import java.util.ArrayList;

import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.WorldRestore.Interfaces.CommandInterface;
import imu.WorldRestore.Managers.ChunkManager;
import imu.WorldRestore.Other.ChunkCard;
import imu.WorldRestore.Other.ItemMetods;
import imu.WorldRestore.main.Main;
import net.md_5.bungee.api.ChatColor;

public class subWrAreaCmd implements CommandInterface
{
	Main _main = null;
	ChunkManager _cManager = null;
	ItemMetods _itemM = null;
	public subWrAreaCmd(Main main) 
	{
		_main = main;
		_cManager = _main.getChunkManager();
		_itemM = main.getItemM();
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
        Player player = (Player) sender;
        Chunk chunk = player.getLocation().getChunk();

        if(args.length > 1 && _itemM.isDigit(args[1]))
        {
        	int maxY = 256;
        	int minY = 0;
        	int size = Integer.parseInt(args[1]);
        	boolean checkProtection = true;
        	if(size % 2 > 0)
        	{
            	if(args.length > 2 && _itemM.isDigit(args[2]))
            	{
            		minY = checkValidY(Integer.parseInt(args[2]));            		
            	}
            	if(args.length > 3 && _itemM.isDigit(args[3]))
            	{
            		maxY = checkValidY(Integer.parseInt(args[3]));
            	}
            	if(minY > maxY)
            	{
            		player.sendMessage(ChatColor.RED + "minY is larger than maxY");
            		wrongMessage(player);
            		return false;
            	}
            	
            	if(args.length > 4 && args[4].equalsIgnoreCase("false"))
            	{
            		checkProtection = false;
            	}
            	
            	int s = (size - (size % 2)) / 2;
            	ArrayList<ChunkCard> cards = new ArrayList<ChunkCard>();
            	int startX = chunk.getX()-s;
            	int startZ = chunk.getZ()-s;
            	for(int x = startX; x < startX+size ; ++x)
            	{
            		for(int z = startZ ; z < startZ+size; ++z)
            		{
            			ChunkCard card = new ChunkCard(_cManager, chunk.getWorld().getChunkAt(x, z), _cManager.getDefTargetWorldName(), minY, maxY, 0, false);
            			cards.add(card);
            		}
            	}
            	
            	player.sendMessage(ChatColor.LIGHT_PURPLE + "Start working chunks. Total chunks: "+cards.size() + " between layers: "+minY+" - "+maxY + " Check protection: "+checkProtection);
            	_cManager.fixChunks(cards, false, checkProtection);
            	
            	return false;
        	}
        	
        }
        wrongMessage(player);
       
  
        
		
        return false;
    }
    
   void wrongMessage(Player player)
   {
	   player.sendMessage("/wr area <size> <minY (optional) > <maxY (optional) > <checkProt true:false (optional) >");
   }
   int checkValidY(int y)
   {
	   if(y < 0)
		   y = 0;
	   
	   if(y > 256)
		   y = 256;
	   
	   return y;
   }
   
}