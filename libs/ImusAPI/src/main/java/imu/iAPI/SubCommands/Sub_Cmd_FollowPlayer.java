package imu.iAPI.SubCommands;


import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import imu.iAPI.CmdUtil.CmdData;
import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Main.ImusAPI;


public class Sub_Cmd_FollowPlayer implements CommandInterface, Listener
{
	@SuppressWarnings("unused")
	private CmdData _data;
	
	private HashMap<Player, FollowPlayer> _followPlayers;
	
	private BukkitTask _task;
	
	public Sub_Cmd_FollowPlayer(CmdData data) 
	{
		_data = data;
		_followPlayers = new HashMap<>();
	}
	
	public class FollowPlayer
	{
		public Player followPlayer;
		
		public FollowPlayer(Player player)
		{
			followPlayer = player;
		}
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	if(args.length <= 1) return false;
    	
		//String playerName = StringUtils.join(Arrays.copyOfRange(args, 1, args.length)," ");
		String playerName = args[1];
		
		Player follow_player = Bukkit.getPlayer(playerName);
		Player player = (Player)sender;
		
		FollowPlayer fPlayer = new FollowPlayer(follow_player);
		
		if(player == follow_player) return true;
		
		if(_followPlayers.containsKey(player))
		{
			_followPlayers.remove(player);
			return true;
		}
		_followPlayers.put((Player)sender, fPlayer);
		Loop();
		
        return true;
    }
//    @EventHandler
//    private void OnChunk(Playermove e)
//    {
//    	e.
//    }
   
	@Override
	public void FailedMsg(CommandSender arg0, String arg1) {
		
	}
    
	private void Loop()
	{
		if(_task != null) return;
		
		_task = new BukkitRunnable() {
			
			@Override
			public void run()
			{
				System.out.println("follow");
				
				if(_followPlayers.isEmpty())
				{
					_task = null;
					this.cancel();
					return;
				}
				
				for(Entry<Player, FollowPlayer> entry : _followPlayers.entrySet())
				{
					System.out.println("following: "+entry.getKey().getDisplayName()+ " to: "+entry.getValue().followPlayer.getDisplayName());
					Player player = entry.getKey();
					Player playerToFollow = entry.getValue().followPlayer;
					//player.getLocation().getWorld().loadChunk(player.getLocation().getChunk());
					PlayerMoveEvent event = new PlayerMoveEvent(player, player.getLocation(), playerToFollow.getLocation());
					Bukkit.getServer().getPluginManager().callEvent(event);
					//player.teleport(playerToFollow.getLocation());
				}
			}
		}.runTaskTimer(ImusAPI._instance, 10, 1);
	}
   
   
}