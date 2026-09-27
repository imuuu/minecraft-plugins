package imu.DontLoseItems.CustomEnd.EndCustomEvents;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import imu.DontLoseItems.CustomEnd.EndEvents;
import imu.DontLoseItems.main.DontLoseItems;

import java.util.Random;

public class EndEvent_TntWalk extends EndEvent {

    private final Random random = new Random();

    public EndEvent_TntWalk() 
    {
        super("Walk on TNT", 30); 
        ChestLootAmount = 3; 
    }

    @Override
    public void OnEventStart() 
    {

    }

    @Override
    public void OnEventEnd() 
    {
    	AddChestLootBaseToAll(ChestLootAmount);
    }

    @Override
    public void OnPlayerLeftMiddleOfEvent(Player player) 
    {
        // Handle logic when a player leaves the event in the middle
    }

    @Override
    public void OnPlayerJoinMiddleOfEvent(Player player) {
        // Handle logic when a player joins the event in the middle
    }
    
    private int _tickCounter = 0;
    @Override
    public void OnOneTickLoop() 
    {
    	_tickCounter++;
    	if(_tickCounter % 5 != 0) return;
    	
    	
        for(Player player : GetPlayers())
        {
            Location loc = player.getLocation().subtract(0, 1, 0); // Get block below player
            Block block = loc.getBlock();

            if (EndEvents.Instance.IsPlayerUnstableArea(loc) && block.getType() != Material.AIR) 
            {
            	if (random.nextInt(100) < 8) 
                {
                    TNTPrimed tnt = (TNTPrimed) loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
                    tnt.setFuseTicks(20*2); 
                }
            	
            	if(block.getType() == Material.TNT) return;
            	
            	block.breakNaturally();
            	
                block.setType(Material.TNT); 
   
            }
        }
    }
    
    @EventHandler
    public void OnBlockBreak(BlockBreakEvent e)
    {
    	if (e.isCancelled())
			return;
		
		if(!DontLoseItems.IsEnd(e.getPlayer())) return;
		
		if(e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		
		if(!HasPlayer(e.getPlayer())) return;
		
		if(e.getBlock().getType() == Material.TNT)
		{
			e.setCancelled(true);
		}
		
    }


    @Override
    public String GetEventName() {
        return "Walk on TNT";
    }

    @Override
    public String GetRewardInfo() {
        // Provide information about the rewards of the event
        return "TNT walking challenge";
    }

    @Override
    public String GetDescription() {
        // Provide a description of the event
        return "Blocks turn to TNT under players' feet!";
    }
}
