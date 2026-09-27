package imu.iWaystones.Managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import imu.iAPI.Other.Tuple;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;
import imu.iWaystone.Upgrades.BuildUpgrade;
import imu.iWaystone.Upgrades.BuildUpgradeCommon;
import imu.iWaystone.Upgrades.BuildUpgradeEpic;
import imu.iWaystone.Upgrades.BuildUpgradeLegendary;
import imu.iWaystone.Upgrades.BuildUpgradeRare;
import imu.iWaystone.Waystones.Waystone;
import imu.iWaystones.Enums.VISIBILITY_TYPE;
import imu.iWaystones.Invs.WaystoneListInv;
import imu.iWaystones.Invs.WaystoneMenuInv;
import imu.iWaystones.Main.ImusWaystones;

public class WaystoneManager 
{
	public static WaystoneManager Instance;
	ImusWaystones _main = ImusWaystones._instance;
	private Set<Material> _valid_mats = new HashSet<>();
	
	private Set<Material> _valid_top_mats = new HashSet<>();
	private Set<Material> _valid_mid_mats = new HashSet<>();
	private Set<Material> _valid_low_mats = new HashSet<>();
	private HashMap<Material, BuildUpgrade> _buildUpgrades = new HashMap<>();
	
	private HashMap<UUID, Waystone> _waitingPlayerConfirm = new HashMap<>();
	
	private HashMap<UUID, ArrayList<CustomInvLayout>> _invs = new HashMap<>(); 
	
	private HashMap<UUID, Waystone> _waystones = new HashMap<>();
	private HashSet<UUID> _IsTeleporting = new HashSet<>();
	private HashMap<UUID, HashSet<UUID>> _discoveredWaystones = new HashMap<>();
	//private HashMap<UUID, PlayerUpgradePanel> _playerUpgradePanel = new HashMap<>();
	private HashMap<Location, UUID> _location_of_waystones = new HashMap<>();
	
	public final String pd_waystoneUUID = "iw.waystoneUUID";
	public final String pd_waystoneHolo = "iw.holo";
	public final int _seeDistance = 20;
	public final int _buildDistance = 300;
	
	private WaystoneManagerSQL _waystoneManagersSQL;
	private BukkitTask _runnable;
	
	private final String PD_Upgrade_Item = "iw_upgrade_item";
	public WaystoneManager()
	{
		Instance = this;
		_waystoneManagersSQL = new WaystoneManagerSQL(this);
		Runnable();
	}
	
	public void OnDisable() 
	{
		ClearHolograms();
		if(_runnable != null) _runnable.cancel();
	}
	
	void Runnable()
	{
		if(_runnable != null) _runnable.cancel();
		
		_runnable = new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				for(Waystone ws : _waystones.values())
				{
					if(CheckVisibility(ws)) continue;
					ws.SetHoloNameVisible(false);
				}
			}
			
			boolean CheckVisibility(Waystone ws)
			{
				for(Player p : Bukkit.getOnlinePlayers())
				{
					if(p.getGameMode() == GameMode.SPECTATOR) continue;
					if(ws.GetLoc().getWorld().equals(p.getWorld()) && ws.GetLoc().distance(p.getLocation()) < _seeDistance)
					{
						if(!ws.IsHoloNameVisible())
						{
							ws.SetHoloNameVisible(true);
						}
						return true;
					}
				}
				return false;
			}
		}.runTaskTimerAsynchronously(_main, 0, 20);
	}
	
	public void RegisterInv(Waystone ws, CustomInvLayout inv)
	{
		if(!_invs.containsKey(ws.GetUUID())) _invs.put(ws.GetUUID(), new ArrayList<>());
		_invs.get(ws.GetUUID()).add(inv);
	}
	
	public void UnRegisterInv(Waystone ws, CustomInvLayout inv)
	{
		if(!_invs.containsKey(ws.GetUUID())) return;
		_invs.get(ws.GetUUID()).remove(inv);
	}
	
	public void CloseAllInvs(Waystone ws)
	{
		if(!_invs.containsKey(ws.GetUUID())) return;
		for(CustomInvLayout inv : _invs.get(ws.GetUUID()))
		{
			inv.GetPlayer().closeInventory();
		}
		_invs.remove(ws.GetUUID());
	}
	
	public void Init()
	{
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				SetupValidBlocks();
				
				_waystoneManagersSQL.CreateTables();
				_waystoneManagersSQL.LoadWaystones();
				_waystoneManagersSQL.LoadDiscoveredWaystones();
				
			}
		}.runTaskAsynchronously(_main);
	}
	public void ClearHolograms()
	{
		for(World world : Bukkit.getWorlds())
		{
			for(Entity entity : world.getEntities())
			{
				if(Metods._ins.getPersistenData(entity, pd_waystoneHolo, PersistentDataType.STRING) != null) entity.remove();
			}
		}
	}
	public WaystoneManagerSQL GetWaystoneManagerSQL()
	{
		return _waystoneManagersSQL;
	}
	public Waystone GetWaystone(UUID uuid_ws)
	{
		return _waystones.get(uuid_ws);
	}
	
	public HashMap<UUID, Waystone> GetWaystones()
	{
		return _waystones;
	}
	
	public ArrayList<UUID> GetWaystonesByVisibility(VISIBILITY_TYPE type)
	{
		ArrayList<UUID> array = new ArrayList<>();

		for(Entry<UUID, Waystone> data : GetWaystones().entrySet())
		{
			if(data.getValue().GetVisibilityType() == type) array.add(data.getKey());
		}
		return array;
	}
	
	public boolean IsTeleporting(Player player)
	{
		return _IsTeleporting.contains(player.getUniqueId());
	}
	
	public void SetTeleporting(Player player)
	{
		_IsTeleporting.add(player.getUniqueId());
	}
	
	public void RemoveTeleportin(UUID uuid_player)
	{
		_IsTeleporting.remove(uuid_player);
	}
	
	public ItemStack SetUpgradeItemStackPD(ItemStack stack)
	{
		Metods._ins.setPersistenData(stack, PD_Upgrade_Item, PersistentDataType.INTEGER, 0);
		return stack;
	}
	
	public boolean IsUpgradeItemStack(ItemStack stack)
	{
		return Metods._ins.getPersistenData(stack, PD_Upgrade_Item, PersistentDataType.INTEGER) != null;
	}
	
	void SetupValidBlocks()
	{
		for(Material mat : Material.values())
		{
			if(Tag.WALLS.getValues().contains(mat) )
			{
				AddValidMid(mat);
			}
			if(Tag.SLABS.getValues().contains(mat))
			{
				AddValidTop(mat);
			}

		}
		
		//build upgrades could be own manager;
		
		AddValidLow(Material.COPPER_BLOCK);
		AddValidLow(Material.WAXED_COPPER_BLOCK);
		AddValidLow(Material.EXPOSED_COPPER);
		AddValidLow(Material.OXIDIZED_COPPER);
		AddValidLow(Material.WEATHERED_COPPER);
		AddValidLow(Material.WAXED_WEATHERED_COPPER);
		AddValidLow(Material.WAXED_OXIDIZED_COPPER);
		
		AddValidLow(Material.EMERALD_BLOCK);
		
		AddValidLow(Material.IRON_BLOCK);
		BuildUpgrade bUpgrade = new BuildUpgradeCommon();
		_buildUpgrades.put(bUpgrade.get_mat(),bUpgrade);
		
		AddValidLow(Material.GOLD_BLOCK);
		bUpgrade = new BuildUpgradeRare();
		_buildUpgrades.put(bUpgrade.get_mat(),bUpgrade);
		
		AddValidLow(Material.DIAMOND_BLOCK);
		bUpgrade = new BuildUpgradeEpic();
		_buildUpgrades.put(bUpgrade.get_mat(),bUpgrade);
		
		AddValidLow(Material.NETHERITE_BLOCK);
		bUpgrade = new BuildUpgradeLegendary();
		_buildUpgrades.put(bUpgrade.get_mat(),bUpgrade);
	}
	void AddValidTop(Material mat)
	{
		_valid_mats.add(mat);
		_valid_top_mats.add(mat);
	}
	
	void AddValidMid(Material mat)
	{
		_valid_mats.add(mat);		
		_valid_mid_mats.add(mat);
	}
	
	void AddValidLow(Material mat)
	{
		_valid_mats.add(mat);
		_valid_low_mats.add(mat);
	}
	
	public BuildUpgrade GetBuildUpgrade(Material mat)
	{
		return _buildUpgrades.get(mat);
	}
	
	boolean IsValidMaterial(Block block)
	{
		return _valid_mats.contains(block.getType());
	}
	Waystone CreateWayStone(Block top,Block mid,Block low)
	{
		if(_valid_top_mats.contains(top.getType()) && _valid_mid_mats.contains(mid.getType()) && _valid_low_mats.contains(low.getType())) return new Waystone(new Location(low.getWorld(), low.getX(), low.getY(), low.getZ()));
		return null;
	}
	public Waystone TryToCreateWaystone(Block block)
	{
		if(!IsValidMaterial(block)) return null;
		if(_valid_top_mats.contains(block.getType())) return CreateWayStone(block, block.getWorld().getBlockAt(block.getLocation().add(0, -1, 0)), block.getWorld().getBlockAt(block.getLocation().add(0, -2, 0)));
		if(_valid_mid_mats.contains(block.getType())) return CreateWayStone(block.getWorld().getBlockAt(block.getLocation().add(0, 1, 0)), block, block.getWorld().getBlockAt(block.getLocation().add(0, -1, 0)));
		if(_valid_low_mats.contains(block.getType())) return CreateWayStone( block.getWorld().getBlockAt(block.getLocation().add(0, 2, 0)), block.getWorld().getBlockAt(block.getLocation().add(0, 1, 0)), block);
		
		
		return null;
		
	}
	
	public HashMap<UUID, HashSet<UUID>> GetDiscovered()
	{
		return _discoveredWaystones;
	}
	
	
	public boolean HasDiscovered(Player player, Waystone waystone)
	{
		if(!_discoveredWaystones.containsKey(player.getUniqueId())) return false;
		
		if(_discoveredWaystones.get(player.getUniqueId()).contains(waystone.GetUUID())) return true;
		return false;
	}
	
	public void AddDiscovered(UUID player_uuid, UUID uuid_ws, boolean saveDatabase)
	{
		if(player_uuid == null || uuid_ws == null) return;
		
		if(!_discoveredWaystones.containsKey(player_uuid)) _discoveredWaystones.put(player_uuid, new HashSet<>());
		
		_discoveredWaystones.get(player_uuid).add(uuid_ws);
		
		if(saveDatabase) _waystoneManagersSQL.SaveDiscoveredAsync(player_uuid, uuid_ws);
	}
	
	public void SetPlayerConfirmation(UUID uuid, Waystone waystone)
	{
		if(waystone == null) _waitingPlayerConfirm.remove(uuid);
		_waitingPlayerConfirm.put(uuid, waystone);
	}
	
	public Waystone GetConfirmWaystone(UUID uuid_player)
	{
		 return _waitingPlayerConfirm.get(uuid_player);
	}
	public void RemoveConfirmWaystone(UUID uuid_player)
	{
		_waitingPlayerConfirm.remove(uuid_player);
	}

	public double IsNearByWaystones(Waystone waystone)
	{
		for(Waystone ws : _waystones.values())
		{
			if(!(ws.GetOwnerUUID().equals(waystone.GetOwnerUUID()))) 
			{
				System.out.println("ws: "+ws.GetOwnerUUID() + " waystone: "+waystone.GetOwnerUUID());
				continue;
			}
			
			if(ws.GetLoc().getWorld().equals(waystone.GetLoc().getWorld())) 
			{
				double distance = ws.GetLoc().distance(waystone.GetLoc());
				if(distance < _buildDistance)
				{
					return distance;
				}
			}
		}
		
		return -1;
	}
	
	public void RemoveWaystone(UUID uuid)
	{
		Waystone waystone = _waystones.get(uuid);
		if(waystone == null) return;
		//Metods._ins.printHashMap(_location_of_waystones);
		_location_of_waystones.remove(waystone.GetLowBlock().getLocation());
		_location_of_waystones.remove(waystone.GetMidBlock().getLocation());
		_location_of_waystones.remove(waystone.GetTopBlock().getLocation());
		waystone.RemoveHologramAsync();
		
		_waystones.remove(uuid);		
		_waystoneManagersSQL.RemoveWaystoneAsync(waystone);
		_waystoneManagersSQL.RemoveDiscoveredAsync(waystone.GetUUID());
		CloseAllInvs(waystone);
		for(HashSet<UUID> set : _discoveredWaystones.values()) {set.remove(waystone.GetUUID());}
		
	}
	
	public void RemoveWaystone(Waystone waystone)
	{
		RemoveWaystone(waystone.GetUUID());
	}
	void RegisterWaystoneLocation(Waystone waystone)
	{
		_location_of_waystones.put(waystone.GetLowBlock().getLocation(), waystone.GetUUID());
		_location_of_waystones.put(waystone.GetMidBlock().getLocation(), waystone.GetUUID());
		_location_of_waystones.put(waystone.GetTopBlock().getLocation(), waystone.GetUUID());
	}
	public void SaveWaystoneUpgrades(Waystone waystone)
	{
		_waystoneManagersSQL.SaveUpgradesAsync(waystone);
	}
	
	public void SaveWaystone(Waystone waystone, boolean saveDatabase)
	{
		RegisterWaystoneLocation(waystone);
		_waystones.put(waystone.GetUUID(), waystone);
		AddDiscovered(waystone.GetOwnerUUID(), waystone.GetUUID(), saveDatabase);
		waystone.CreateHologram();
		if(saveDatabase)
		{
			_waystoneManagersSQL.SaveWaystoneAsync(waystone);
			
		}

	}

	public boolean IsWaystone(Block block)
	{
		if(block == null) return false;
		
		return _location_of_waystones.containsKey(block.getLocation());
	}
	
	public boolean IsValid(Waystone waystone)
	{
		if(waystone == null) return false;
		
		if(_valid_top_mats.contains(waystone.GetTopBlock().getType()) && _valid_mid_mats.contains(waystone.GetMidBlock().getType()) && _valid_low_mats.contains(waystone.GetLowBlock().getType())) return true;
		
		return false;
	}
	
	public Waystone GetWaystone(Block block)
	{
		return _waystones.get(_location_of_waystones.get(block.getLocation()));
	}

	
	public void OpenWaystone(Player player, Waystone waystone)
	{
		new WaystoneMenuInv(waystone, player).openThis();
	}
	
	public void OpenWaystoneList(Player player)
	{
		new WaystoneListInv(player).openThis();
	}

	/**
	 * Reloads waystones and discovered waystones from database asynchronously
	 * @param player Whom to send confirmation message
	 */
	public void reload(Player player) {
		WaystoneManagerSQL sql = this._waystoneManagersSQL;
		// Move to async thread
		new BukkitRunnable() {
			@Override
			public void run() {
				Set<Waystone> waystones = sql.getLoadWaystones();
				Set<Tuple<UUID, UUID>> discovered = sql.getLoadDiscoveredWaystones();

				//Move to main thread
				new BukkitRunnable() {
					@Override
					public void run() {
						_waystones.clear();
						for(Waystone waystone : waystones) {
							SaveWaystone(waystone, false);
						}

						_location_of_waystones.clear();
						for (Waystone waystone : waystones) {
							RegisterWaystoneLocation(waystone);
						}

						_discoveredWaystones.clear();
						for(Tuple<UUID, UUID> tuple : discovered) {
							AddDiscovered(tuple.GetKey(), tuple.GetValue(), false);
						}
						player.sendMessage(ChatColor.DARK_GREEN + "Waystones reloaded!");
					}
				}.runTask(_main);
			}
		}.runTaskAsynchronously(_main);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
