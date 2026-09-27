package imu.DontLoseItems.Managers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import imu.DontLoseItems.Enums.DIFFICULT;
import imu.DontLoseItems.Inventories.Inv_SelectDifficulty;
import imu.DontLoseItems.main.DontLoseItems;
import imu.DontLoseItems.other.PlayerDifficultSetting;
import imu.DontLoseItems.other.StoredBlock;
import imu.iAPI.Other.ConfigMaker;

public class Manager_Difficult implements Listener
{
	public static Manager_Difficult Instance;
	private DontLoseItems _main = DontLoseItems.Instance;
	private HashMap<UUID, PlayerDifficultSetting> _playerSettings = new HashMap<>();
	private HashMap<UUID, List<StoredBlock>> _playerPortalAres = new HashMap<>();
	
	private double _netherCooldown = 345600000; //4 days
	//private double _netherCooldown = 1000 * 30; //4 days

	public Manager_Difficult()
	{
		Instance = this;
		loadAllPlayerSettings();
	}

	 public void setPlayerDifficulty(UUID playerID, DIFFICULT difficulty, Date setDate) 
	 {
	        PlayerDifficultSetting setting = _playerSettings.getOrDefault(playerID, new PlayerDifficultSetting(true));
	        setting.NetherDifficulty = difficulty;
	        setting.setDate = setDate; // Set the date in PlayerDifficultSetting
	        _playerSettings.put(playerID, setting);
	        savePlayerSetting(playerID);
	}

    public PlayerDifficultSetting getPlayerSettings(UUID playerID) 
    {
        return _playerSettings.getOrDefault(playerID, new PlayerDifficultSetting(false));
    }
	
	
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) 
    {
        UUID playerId = event.getPlayer().getUniqueId();
        
        // Remove the player's portal frame data
        if (_playerPortalAres.containsKey(playerId)) {
            _playerPortalAres.remove(playerId);
        }

        restorePortal(playerId);
    }
	@EventHandler
	public void CancelPortals(PlayerPortalEvent e)
	{
		Player player = e.getPlayer();
		
		Location to = e.getTo();
		if (to == null)
			return;

		World world = to.getWorld();
		if (world == null)
			return;

		if (world.getName().equals("world_nether"))
		{
			handleNetherPortal(player, e);
		}
		// Rest of the existing code...
	}
	
	
	private void handleNetherPortal(Player player, PlayerPortalEvent event)
	{

		PlayerDifficultSetting dificultSetting = getPlayerSettings(player.getUniqueId());
		UUID playerId = player.getUniqueId();
		Date lastSetDate = dificultSetting.setDate;
		double sum = new Date().getTime() - lastSetDate.getTime();
		if (lastSetDate == null || sum < 10 || sum  > _netherCooldown)
		{ 
//			if(!cooldowns.isCooldownReady("uuid") && player.isOp())
//			{
//				event.setCancelled(true);
//				return;
//			}
//			if(player.isOp())cooldowns.addCooldownInSeconds("uuid", 10);

			Location portalLocation = findFirstPortalFrame(player);
			
			HashMap<Location, Material> portalFrames = findAndStorePortalFrame(portalLocation, new HashMap<>());
			
			_playerPortalAres.put(playerId, new ArrayList<>());
			for(Location portalPartLocation : portalFrames.keySet())
			{
				Block b = portalLocation.getBlock();
				StoredBlock sBlock = new StoredBlock(portalPartLocation, b.getType(), b.getBlockData());
				_playerPortalAres.get(playerId).add(sBlock);
			}
			event.setCancelled(true);
			removePortalFrame(portalFrames);
			new BukkitRunnable() 
			{
				
				@Override
				public void run()
				{
					new Inv_SelectDifficulty().open(player);
				}
			}.runTaskLater(_main, 15);
			
			
		} 
	}
	
	//private HashMap<Location, Material> portalFrames = new HashMap<>();
	
	private void removePortalFrame(HashMap<Location, Material> portalFrames) 
	{
	    for (Location loc : portalFrames.keySet()) 
	    {
	        loc.getBlock().setType(Material.AIR); 
	    }
	}
	
	
	public void restorePortal(UUID playerId) 
	{
	    List<StoredBlock> portalAreas = _playerPortalAres.get(playerId);
	    if (portalAreas != null) 
	    {
	       
	        for (StoredBlock storedBlock : portalAreas) 
	        {
	        	 storedBlock.restore(); 
	        }
	        _playerPortalAres.remove(playerId); // Clear the stored data for this player
	    }
	}


	
	private Location findFirstPortalFrame(Player player) 
	{
	    Location playerLocation = player.getLocation();
	    int radius = 5; // Define a search radius. Adjust as necessary.

	    for (int x = -radius; x <= radius; x++) {
	        for (int y = -radius; y <= radius; y++) {
	            for (int z = -radius; z <= radius; z++) {
	                Location checkLocation = playerLocation.clone().add(x, y, z);
	                if (checkLocation.getBlock().getType() == Material.NETHER_PORTAL) { // Check for portal frame material
	                    return checkLocation; // Return the first found portal frame location
	                }
	            }
	        }
	    }

	    return player.getLocation(); // Return null if no portal frame is found
	}


	private HashMap<Location, Material> findAndStorePortalFrame(Location location, HashMap<Location, Material> portalFrames) 
	{
	    // Check if the block is a Nether Portal block
	    if (location.getBlock().getType() != Material.NETHER_PORTAL) {
	        return portalFrames;
	    }


	    if (portalFrames.containsKey(location)) {
	        return portalFrames;
	    }

	    portalFrames.put(location, Material.NETHER_PORTAL);

	    // Recursively check adjacent blocks
	    findAndStorePortalFrame(location.clone().add(1, 0, 0), portalFrames); // East
	    findAndStorePortalFrame(location.clone().add(-1, 0, 0), portalFrames); // West
	    findAndStorePortalFrame(location.clone().add(0, 1, 0), portalFrames); // Up
	    findAndStorePortalFrame(location.clone().add(0, -1, 0), portalFrames); // Down
	    findAndStorePortalFrame(location.clone().add(0, 0, 1), portalFrames); // South
	    findAndStorePortalFrame(location.clone().add(0, 0, -1), portalFrames); // North
	    
	    return portalFrames;
	}


	
	final String syntax = "player_difficulty";
	final String syntax_dot = syntax + ".";
	
	private void savePlayerSetting(UUID playerID) 
	{
		System.out.println("saving player option");
		
        ConfigMaker cm = new ConfigMaker(_main, syntax+".yml");
        FileConfiguration config = cm.getConfig();
        PlayerDifficultSetting setting = _playerSettings.get(playerID);
        config.set(syntax_dot+ playerID.toString() + ".difficulty", setting.NetherDifficulty.toString());
        config.set(syntax_dot+ playerID.toString() + ".setDate", setting.setDate.getTime());
        cm.saveConfig();
    }
	
	private void loadAllPlayerSettings() {
	    System.out.println("=========================== >loading savefile");
	    ConfigMaker cm = new ConfigMaker(_main, syntax + ".yml");
	    FileConfiguration config = cm.getConfig();

	    // Correctly access the "player_difficulty" section
	    ConfigurationSection difficultySection = config.getConfigurationSection(syntax);
	    if (difficultySection != null) {
	        for (String key : difficultySection.getKeys(false)) {
	            String fullPath = syntax_dot + key;
	            try {
	                UUID playerID = UUID.fromString(key); // Key is the player's UUID
	                System.out.println("LOADING FOUND: " + playerID);
	                DIFFICULT difficulty = DIFFICULT.valueOf(config.getString(fullPath + ".difficulty"));
	                Date setDate = new Date(config.getLong(fullPath + ".setDate"));
	                PlayerDifficultSetting setting = new PlayerDifficultSetting(false);
	                setting.NetherDifficulty = difficulty;
	                setting.setDate = setDate;
	                _playerSettings.put(playerID, setting);
	            } catch (IllegalArgumentException e) {
	                System.out.println("Invalid UUID format for key: " + key);
	            }
	        }
	    }
	}


//    private void loadAllPlayerSettings() 
//    {
//    	System.out.println("=========================== >loading savefile");
//        ConfigMaker cm = new ConfigMaker(_main, syntax+".yml");
//        FileConfiguration config = cm.getConfig();
//
//        if (config.getConfigurationSection("") != null) 
//        {
//            for (String key : config.getConfigurationSection("").getKeys(false)) 
//            {
//            	key = syntax_dot + key;
//                UUID playerID = UUID.fromString(key);
//                System.out.println("LOADING FOUND: "+playerID);
//                DIFFICULT difficulty = DIFFICULT.valueOf(config.getString(key + ".difficulty"));
//                Date setDate = new Date(config.getLong(key + ".setDate"));
//                PlayerDifficultSetting setting = new PlayerDifficultSetting(false);
//                setting.NetherDifficulty = difficulty;
//                setting.setDate = setDate;
//                _playerSettings.put(playerID, setting);
//            }
//        }
//    }

	// ... Rest of your class code
}
