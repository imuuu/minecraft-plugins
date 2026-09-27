package imu.TokenTp.SubCommands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import imu.TokenTp.CustomItems.ItemTeleTokenToken;
import imu.TokenTp.Enums.TeleState;
import imu.TokenTp.Interfaces.CommandInterface;
import imu.TokenTp.Managers.TeleTokenManager;
import imu.TokenTp.main.Main;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;
import net.md_5.bungee.api.ChatColor;

public class subTtpTpAnwserCmd implements CommandInterface
{
	Main _main = null;
	Metods _itemM = null;
	TeleTokenManager _ttManager;
	
	public subTtpTpAnwserCmd(Main main) 
	{
		_main = main;
		_itemM =ImusAPI._metods;
		_ttManager = main.getTeleTokenManager();
				
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	Player player = (Player) sender;
    	if(args.length == 3)
    	{
    		Player teleporter = Bukkit.getPlayer(args[1]);
    		if(teleporter != null )
    		{
    			
    				if(!_ttManager.hasRequestAnwsered(teleporter.getUniqueId()) )
        			{
    					if(_ttManager.hasRequestTargetThis(teleporter.getUniqueId(), player.getUniqueId()))
    	    			{
    						player.sendMessage(" ");
    						if(_itemM.doesStrArrayCointainStr(args, "yes"))
                    		{  							
                				player.sendMessage(ChatColor.GREEN+"You accepted teleport request");
                				teleporter.sendMessage(player.getName()+ " has"+ChatColor.GREEN+" accepted "+ChatColor.WHITE+"your teleport request!");
                				ItemTeleTokenToken temp = new ItemTeleTokenToken(_main);
                				temp.setTokenDesc();
                				String tokenSTR = temp.getDisplayName();
                				player.sendMessage(" ");
                				teleporter.sendMessage(ChatColor.GREEN + "Press "+ tokenSTR + ChatColor.GREEN+" to start teleporting! Remember do not move!");
                				
                				_ttManager.makeRequestAnwser(player.getUniqueId(), teleporter.getUniqueId(), true);
                				
                				_ttManager.setAcceptCd(teleporter.getUniqueId());
                				_ttManager.setRequestCd(teleporter.getUniqueId(), 0);
                				checkAccept(teleporter.getUniqueId());
                    		}
                    		else
                    		{
                    			player.sendMessage(ChatColor.RED + "You denied teleport request");
                    			teleporter.sendMessage(ChatColor.AQUA + player.getName()+ ChatColor.WHITE +" has "+ChatColor.RED+ "denied "+ChatColor.WHITE+ "your teleport request!");
                    			_ttManager.makeRequestAnwser(player.getUniqueId(), teleporter.getUniqueId(), false);
                    			_ttManager.resetRequestAnwserEtc(teleporter.getUniqueId());
                    		}
    	    			}
    	    			else
    	    			{
    	    				player.sendMessage(ChatColor.RED + "Request has been expired!");
    	    			}
        				
        			}
    				else
        			{
        				player.sendMessage("You already have sent your anwser!");
        			}
    			
   			
    		}else
    		{
    			System.out.println("Couldnt find player");
    		}
    		
    		
    	}
    	
    	
        return false;
    }
    
    void checkAccept(UUID uuid)
	{
		new BukkitRunnable() {
			
			int i = 0;
			@Override
			public void run() 
			{
				
				Player p = Bukkit.getPlayer(uuid);
				if(p != null)
				{
					if( (_ttManager.resetIfNoCdsLeft(uuid)) || i-10 > _ttManager.getAcceptCDtime())
					{
						
						if(_ttManager.getTeleState(uuid) == TeleState.ACTIVATED)
						{
							p.sendMessage(ChatColor.RED + "Teleport connection has been lost");
						}
						
						this.cancel();
					}
				}else
				{
					System.out.println("Couldnt find right player in server to teleport");
					_ttManager.resetRequestAnwserEtc(uuid);
					this.cancel();
				}
				
				
				i++;
			}
		}.runTaskTimer(_main, 0, 20);
	}
	
	
	
	 
	
   
   
}