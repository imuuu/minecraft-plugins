package imu.TokenTp.SubCommands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import imu.TokenTp.Enums.TokenType;
import imu.TokenTp.Interfaces.CommandInterface;
import imu.TokenTp.Managers.TeleTokenManager;
import imu.TokenTp.main.Main;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;
import net.md_5.bungee.api.ChatColor;

public class subTtpTpRequestCmd implements CommandInterface
{
	Main _main = null;
	Metods _itemM = null;
	TeleTokenManager _ttManager;
	public subTtpTpRequestCmd(Main main) 
	{
		_main = main;
		_itemM = ImusAPI._metods;
		_ttManager = main.getTeleTokenManager();
				
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;
    	if(args.length > 1)
    	{
    		ItemStack stack = player.getInventory().getItemInMainHand();
   		 
    		if(stack != null && _ttManager.isToken(stack) && _ttManager.getTokenType(stack) == TokenType.TOKEN_TO_PLAYER)
	   		 {
	   			Player target = Bukkit.getPlayer(args[1]);
	    		if(target != null)
	    		{
	    			if(_ttManager.hasRequestCd(player.getUniqueId()))
	    			{
	    				player.sendMessage(" ");
	    				
//	    				player.sendMessage(ChatColor.GRAY + "=======================================");
//	    				player.sendMessage("Sending               Request            "+ChatColor.AQUA+target.getName());
//	    				player.sendMessage(ChatColor.MAGIC + "=======================================");    				
//	    				player.sendMessage("           Teleport               to ");
//	    				player.sendMessage(ChatColor.GRAY + "=======================================");
	    				
	    				player.sendMessage(ChatColor.YELLOW +""+ ChatColor.MAGIC + "======================================="); 
	    				player.sendMessage("Sending Request to Teleport to "+ChatColor.AQUA+target.getName());
	    				player.sendMessage(ChatColor.YELLOW +""+ ChatColor.MAGIC + "======================================="); 
	    				
	    				player.sendMessage(" ");
	        			
	    				target.sendMessage(ChatColor.DARK_PURPLE  + "======================================="); 
	    				target.sendMessage(ChatColor.DARK_PURPLE +"" + ChatColor.MAGIC + "==="+ ChatColor.GOLD+" Can "+ChatColor.AQUA+player.getName()+ChatColor.GOLD+" tp to you?" + ChatColor.DARK_PURPLE +"" + ChatColor.MAGIC+" ===");
	        			_itemM.sendYesNoConfirm(target, "/tttp anwser "+player.getName()+" yes", "/tttp anwser "+player.getName()+" no");
	        			target.sendMessage(ChatColor.DARK_PURPLE  + "======================================="); 
	        			
	        			_ttManager.makeRequestAnwser(target.getUniqueId(),player.getUniqueId(), null);
	        			_ttManager.setRequestCd(player.getUniqueId());
	        			checkRequest(player.getUniqueId());
	        			
	    			}else
	    			{
	    				player.sendMessage("Request cooldown");
	    			}
	    			
	
	    		}else
	    		{
	    			player.sendMessage("Couldn't find a player named: "+args[1]);
	    		}
	   		 }
    		
    		
    	}
    	
				
        return false;
    }
    
    void checkRequest(UUID uuid)
    {
    	new BukkitRunnable() {
			
    		int i = 0;
			@Override
			public void run() 
			{
				Player p = Bukkit.getPlayer(uuid);
				if(p != null)
				{
					if(!_ttManager.hasRequestAnwsered(uuid) && i > _ttManager.getRequestCDtime())
					{
						_ttManager.resetRequestAnwserEtc(uuid);
						this.cancel();
					}else if(_ttManager.hasRequestAnwsered(uuid) || i > _ttManager.getRequestCDtime())
					{
						this.cancel();
					}
				}else
				{
					_ttManager.resetRequestAnwserEtc(uuid);
					this.cancel();
				}
				
					
				
				
				i++;
				
				
			}
		}.runTaskTimer(_main, 0, 20);
    }
    
   
   
}