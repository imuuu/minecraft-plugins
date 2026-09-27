package imu.iWaystone.Waystones;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Metods;
import imu.iAPI.Other.XpUtil;
import imu.iWaystone.Upgrades.BaseUpgrade;
import imu.iWaystone.Upgrades.BuildUpgrade;
import imu.iWaystone.Upgrades.PlayerUpgradePanel;
import imu.iWaystone.Upgrades.UpgradeBottomBuild;
import imu.iWaystone.Upgrades.UpgradeCastTime;
import imu.iWaystone.Upgrades.UpgradeXPusage;
import imu.iWaystones.Enums.VISIBILITY_TYPE;
import imu.iWaystones.Main.ImusWaystones;

public class Waystone 
{
	private boolean _enable = true;
	private boolean _unbreakable = false;
	private boolean _maxOut = false;
	private String _name="&6New Waystone";
	//private Block _top,_mid,_low;
	private UUID _owner_uuid;
	private String _owner_name;
	private UUID _uuid;
	private Location _loc;
	private VISIBILITY_TYPE _visibilityType = VISIBILITY_TYPE.BY_TOUCH;
	private ItemStack _displayItem = new ItemStack(Material.BLACKSTONE_WALL);
	private ArmorStand _hologram;
	private HashMap<UUID, PlayerUpgradePanel> _playerUpgradePanel = new HashMap<>();
	
	//private double base_cooldown = 600;
	private double base_casttime = 30;
	private double base_xpUsage = 10;
	private ImusWaystones _main = ImusWaystones._instance;
	
	private final double _move_telecancel_dis = 0.2;
	private Cooldowns _cds = new Cooldowns();
	private BuildUpgrade _buildUpgrade;
	
	private UpgradeBottomBuild _upgradeBottomBuild = new UpgradeBottomBuild();
	
	public Waystone(Location loc) 
	{
		_uuid = UUID.randomUUID();		
		_loc = loc;
		ReadBuildUpgrade();
	}
	
	public void SetBuildUpgrade(BuildUpgrade buildUpgrade)
	{
		//System.out.println("setting up buildUpgrade => "+buildUpgrade);
		_buildUpgrade = buildUpgrade;
	}
	
	public BuildUpgrade GetBuildUpgrade()
	{
		return _buildUpgrade;
	}
	
	public UpgradeBottomBuild GetUpgradeBottomUpgrade()
	{
		ReadBuildUpgrade();
		_upgradeBottomBuild.ReadTier(GetBuildUpgrade());
		_upgradeBottomBuild.Tooltip();
		return _upgradeBottomBuild;
	}
	
	public void CreateHologram()
	{

		new BukkitRunnable() {
			@Override
			public void run() 
			{				
				for(Entity ent : _loc.getWorld().getNearbyEntities(_loc, 10, 10, 10))
				{
					if(IsThisHolo(ent)) 
					{
						ent.remove();
					}
				}
				
				if(_hologram != null) _hologram.remove();
				
				_hologram =Metods._ins.CreateHologram(_name, _loc.clone().add(0.5, 0.5, 0.5));
				_hologram.setCustomName(Metods.msgC(_name));
				Metods._ins.setPersistenData(_hologram, ImusWaystones._instance.GetWaystoneManager().pd_waystoneHolo, PersistentDataType.STRING, _uuid.toString());

				_hologram.setCustomNameVisible(true);

			}
		}.runTask(_main);
		
		
	}
	
	
	public PlayerUpgradePanel GetPlayerUpgradePanel(UUID uuid_player)
	{
		if(!_playerUpgradePanel.containsKey(uuid_player)) _playerUpgradePanel.put(uuid_player, new PlayerUpgradePanel(GetUUID()));
		return _playerUpgradePanel.get(uuid_player);
	}
	
	public void SetPlayerUpgrade(UUID uuid_player, BaseUpgrade upgrade)
	{
		//System.out.println("Setting upgrade to player: "+uuid_player + " upgrade: "+upgrade + " tier: "+upgrade.GetCurrentTier());
		if(!_playerUpgradePanel.containsKey(uuid_player)) _playerUpgradePanel.put(uuid_player, new PlayerUpgradePanel(GetUUID()));
		
		_playerUpgradePanel.get(uuid_player).SetUpgrade(upgrade);
	}
	
	public HashMap<UUID, PlayerUpgradePanel> GetPlayerUpgrades()
	{
		return _playerUpgradePanel;
	}
	
	public ArmorStand GetHologram()
	{
		return _hologram;
	}
	boolean IsThisHolo(Entity entity)
	{
		String pd = Metods._ins.getPersistenData(entity, ImusWaystones._instance.GetWaystoneManager().pd_waystoneHolo, PersistentDataType.STRING);
		
		if(pd == null || pd == "") return false;
		
		UUID uuid = UUID.fromString(pd);
		
		if(uuid.equals(GetUUID())) return true;
		
		return false;
	}
	public BukkitTask RemoveHologramAsync()
	{
		List<Entity> ents = _loc.getWorld().getEntities();
		return new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				for(Entity ent : ents)
				{
					if(IsThisHolo(ent))
					{
						new BukkitRunnable() {
							
							@Override
							public void run() 
							{
								ent.remove();
							}
						}.runTask(_main);						
					}
				}
			}
		}.runTaskAsynchronously(_main);
		
	}
	
	public boolean IsHoloNameVisible()
	{
		if(_hologram == null) return false;
		
		return _hologram.isCustomNameVisible();
	}
	
	public void SetHoloNameVisible(boolean b)
	{
		if(_hologram == null) return;
		
		_hologram.setCustomNameVisible(b);
	}
	
	public Location GetLoc()
	{
		return _loc.clone();
	}
	
	public String GetName()
	{
		return _name;
	}
	
	public void SetName(String name)
	{
		_name = name;
	}
	
	public void SetUUID(UUID uuid)
	{
		_uuid = uuid;
	}
	
	public ItemStack GetDisplayItem()
	{
		return _displayItem;
	}
	
	public void SetDisplayitem(ItemStack stack)
	{
		_displayItem = stack;
	}
	
	public UUID GetUUID()
	{
		return _uuid;
	}
	
	public void SetOwner(Player player)
	{
		_owner_uuid = player.getUniqueId();
		_owner_name = player.getName();
	}
	
	public void SetOwner(String name, UUID uuid)
	{
		_owner_name = name;
		_owner_uuid = uuid;
	}
	
	public UUID GetOwnerUUID()
	{
		return _owner_uuid;
	}
	
	public String GetOwnerName()
	{
		return _owner_name;
	}
	
	public VISIBILITY_TYPE GetVisibilityType()
	{
		return _visibilityType;
	}
	
	public void SetVisibilityType(VISIBILITY_TYPE type)
	{
		_visibilityType = type;
	}
	public void SendMessageToOwner(String str)
	{
		new BukkitRunnable() {
			
			@Override
			public void run() {
				try 
				{
					//System.out.println("Owner uuid: "+_owner_uuid);
					Player p = Bukkit.getServer().getPlayer(_owner_uuid);
					if(p == null) return;
					
					Bukkit.getServer().getPlayer(_owner_uuid).sendMessage(Metods.msgC(str));
				} 
				catch (Exception e) 
				{
					//Bukkit.getLogger().info("Couldnt inform owner!");
				}
			}
		}.runTask(ImusWaystones._instance);
	}
	
	public Block GetTopBlock()
	{
		
		return _loc.getWorld().getBlockAt(_loc.getBlockX(), _loc.getBlockY()+2, _loc.getBlockZ());
	}
	
	public Block GetMidBlock()
	{
		return _loc.getWorld().getBlockAt(_loc.getBlockX(), _loc.getBlockY()+1, _loc.getBlockZ());
	}
	
	public Block GetLowBlock()
	{
		return _loc.getWorld().getBlockAt(_loc.getBlockX(), _loc.getBlockY(), _loc.getBlockZ());
	}


	public double getBase_cooldown() {
		return GetBuildUpgrade(). get_value();
	}

	public void ReadBuildUpgrade()
	{
		SetBuildUpgrade(_main.GetWaystoneManager().GetBuildUpgrade(GetLowBlock().getType()));
	}
//	public void setBase_cooldown(int base_cooldown) {
//		this.base_cooldown = base_cooldown;
//	}


	public double getBase_casttime() {
		return base_casttime;
	}


	public void setBase_casttime(int base_casttime) {
		this.base_casttime = base_casttime;
	}


	public double getBase_xpUsage() {
		return base_xpUsage;
	}


	public void setBase_xpUsage(double base_xpUsage) {
		this.base_xpUsage = base_xpUsage;
	}
	
	public void SetCooldownPlayer(Player player)
	{
//		UpgradeCooldown uCd = GetPlayerUpgrades().get(player.getUniqueId()).get_cooldown();
//		UpgradeFoundation found =  GetPlayerUpgrades()get_foundation();
		_cds.setCooldownInSeconds(player.getUniqueId().toString(), GetPlayerUpgrades().get(player.getUniqueId()).GetCooldown());
	}
	
	public boolean IsCooldown(Player player)
	{
		return !_cds.isCooldownReady(player.getUniqueId().toString());
	}
	
	public String GetCooldown(Player player)
	{
		return _cds.GetCdInReadableTime(player.getUniqueId().toString());
	}
	
	public void RollVisibilityType() 
	{
        VISIBILITY_TYPE[] values = VISIBILITY_TYPE.values();
        int currentIndex = _visibilityType.ordinal();
        int nextIndex = (currentIndex + 1) % values.length;
        _visibilityType = values[nextIndex];
    }
	
	
	public Double GetValue(BaseUpgrade upgrade)
	{
		if(upgrade instanceof UpgradeCastTime)
		{		
			return upgrade.GetCombinedValue(base_casttime);
		}

		if(upgrade instanceof UpgradeXPusage)
		{		
			return upgrade.GetCombinedValue(base_xpUsage);
		}
		
//		if(upgrade instanceof UpgradeCooldown)
//		{		
//			return upgrade.GetCombinedValue(GetBuildUpgrade().get_value());
//		}
		return null;
	}
	
	
	public boolean HasEnoughExpToTeleport(Player player)
	{
		PlayerUpgradePanel panel = _playerUpgradePanel.get(player.getUniqueId());
		
		if(!panel.get_xpUsage().IsEnabled()) return true;
		
		if(IsMaxOut()) return true;
		
		double neededXP = panel.get_xpUsage().GetCombinedValue(base_xpUsage);

		if((XpUtil.GetPlayerLevel(player) - neededXP) >= 0)
		{
			return true;
		}
		return false;
	}
	
	public void ReduceExp(Player player)
	{
		double neededXP = _playerUpgradePanel.get(player.getUniqueId()).get_xpUsage().GetCombinedValue(base_xpUsage);
		XpUtil.SetPlayerLevel(player, XpUtil.GetPlayerLevel(player) - neededXP);
	}
	
	public void StartTeleporting(Player player, Waystone target_waystone)
	{
		if(_main.GetWaystoneManager().IsTeleporting(player)) return;
		
		
		
		PlayerUpgradePanel panel = _playerUpgradePanel.get(player.getUniqueId());
		Waystone thiss = this;
		UUID uuid_player = player.getUniqueId();
		Location startLoc = player.getLocation().clone();
		_main.GetWaystoneManager().SetTeleporting(player);
		
		new BukkitRunnable() 
		{
			int seconds =  player.isOp() || IsMaxOut() ? 1 : (int)panel.get_castTime().GetCombinedValue(base_casttime);
					
			void TeleCancel()
			{
				ImusWaystones._instance.GetWaystoneManager().RemoveTeleportin(uuid_player);
				if(player != null)
				{
					player.sendTitle(Metods.msgC("&4Teleporting Canceled!"), "", 1, 20, 1);
				}
			}
			
			void TeleConfirm()
			{
				ImusWaystones._instance.GetWaystoneManager().RemoveTeleportin(uuid_player);
				
				if(!_main.GetWaystoneManager().IsValid(target_waystone) || !_main.GetWaystoneManager().IsValid(thiss))
				{
					player.sendMessage(Metods.msgC("&cDestination waystone might be broken!"));
					TeleCancel();
					return;
				}
				if(!HasEnoughExpToTeleport(player)) 
				{
					player.sendMessage(Metods.msgC("&cYou don't have enough xp to teleport!"));
					TeleCancel();
					return;
				}
				
				
				new BukkitRunnable() {
					
					@Override
					public void run() 
					{
						int x = ThreadLocalRandom.current().nextInt(-2,2);
						int z = ThreadLocalRandom.current().nextInt(-2,2);
						
						x = x == 0 ? 1 : x;
						z = z == 0 ? 1 : z;
						
						player.teleport(target_waystone.GetLoc().clone().add(x, 1, z));
						
						if(!IsMaxOut())
						{
							if(GetPlayerUpgradePanel(player.getUniqueId()).get_xpUsage().IsEnabled()) ReduceExp(player);
							
							SetCooldownPlayer(player);
						}
						
						target_waystone.CreateHologram();
					}
				}.runTask(_main);
				
			}
			
			@Override
			public void run() 
			{

				if(startLoc.getWorld() != player.getWorld() || startLoc.distance(player.getLocation()) > _move_telecancel_dis || player == null)
				{
					TeleCancel();					
					this.cancel();
					return;
				}
				
				if(seconds <= 0)
				{
					TeleConfirm();
					this.cancel();
					return;
				}
				
				player.sendTitle(Metods.msgC("&2Teleporting in &5"+seconds+" &2s"), Metods.msgC("&4Don't move"), 0, 22, 0);
				player.playSound(_loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f, 0.1f);
				seconds--;
			}
			
		}.runTaskTimerAsynchronously(ImusWaystones._instance, 0, 20);
	}

	public boolean IsEnable()
	{
		return _enable;
	}

	public void SetEnable(boolean _enable)
	{
		this._enable = _enable;
	}

	public boolean IsUnbreakable()
	{
		return _unbreakable;
	}

	public void SetUnbreakable(boolean _unbreakable)
	{
		this._unbreakable = _unbreakable;
	}

	public boolean IsMaxOut()
	{
		return _maxOut;
	}

	public void SetMaxOut(boolean _maxOut)
	{
		this._maxOut = _maxOut;
	}
	
	
}
