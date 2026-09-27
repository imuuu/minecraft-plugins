package imu.iWaystone.Upgrades;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.iAPI.Other.Metods;
import imu.iWaystones.Enums.UpgradeType;
import imu.iWaystones.Main.ImusWaystones;
import imu.iWaystones.Managers.WaystoneManager;
import imu.iWaystones.Managers.WaystoneManagerSQL;


public class PlayerUpgradePanel 
{
	private UpgradeCastTime _castTime = new UpgradeCastTime();
	private UpgradeCooldown _cooldown = new UpgradeCooldown();
	private UpgradeDimension _dimension = new UpgradeDimension();
	private UpgradeXPusage _xpUsage = new UpgradeXPusage();
	private UpgradeFoundation _foundation = new UpgradeFoundation();
	private UpgradeNameChange _nameChange = new UpgradeNameChange();
	private UUID _uuid_ws;
	
	public PlayerUpgradePanel(UUID uuid_ws)
	{
		_uuid_ws = uuid_ws;

	}
		
	public UpgradeCooldown get_cooldown() {
		return _cooldown;
	}

	public UpgradeDimension get_dimension() {
		return _dimension;
	}

	public UpgradeCastTime get_castTime() {
		return _castTime;
	}

	public UpgradeXPusage get_xpUsage() {
		return _xpUsage;
	}
	
	public UpgradeFoundation get_foundation() {
		return _foundation;
	}

	public UpgradeNameChange get_nameChange() {
		return _nameChange;
	}
	
	public BaseUpgrade[] GetUpgrades()
	{
		return new BaseUpgrade[] {_castTime,_cooldown, _dimension, _xpUsage, _foundation, _nameChange};	
	}
	
	public boolean HasUpgrade()
	{
		for(BaseUpgrade upgrade : GetUpgrades())
		{
			if(upgrade.GetCurrentTier() > 0) return true;
		}
		return false;
	}
	public void ResetUpgrades()
	{
		for(BaseUpgrade upgrade : GetUpgrades())
		{
			upgrade.SetCurrentier(0);
		}
	}
	public void LoadToolTips()
	{
		for(BaseUpgrade upgrade : GetUpgrades()) {upgrade.Tooltip();}
	}
	
	public int GetCooldown()
	{
		return (int)_cooldown.GetCombinedValue(_foundation.GetTier()._value);
	}
	
	public void SetUpgrade(BaseUpgrade upgrade)
	{
		if(upgrade instanceof UpgradeCastTime)
		{
			_castTime = (UpgradeCastTime)upgrade;
			return;
		}
		
		if(upgrade instanceof UpgradeCooldown)
		{
			_cooldown = (UpgradeCooldown)upgrade;
			return;
		}
		
		if(upgrade instanceof UpgradeXPusage)
		{
			_xpUsage = (UpgradeXPusage)upgrade;
			return;
		}
		
		if(upgrade instanceof UpgradeDimension)
		{
			_dimension = (UpgradeDimension)upgrade;
			return;
		}
		
		if(upgrade instanceof UpgradeFoundation)
		{
			_foundation = (UpgradeFoundation)upgrade;
			return;
		}
		
		if(upgrade instanceof UpgradeNameChange)
		{
			_nameChange =(UpgradeNameChange)upgrade;
			return;
		}
	}
	public void SaveUpgradesToDatabase(UUID playerUUID)
	{
		WaystoneManagerSQL.Instance.SaveUpgradeAsync(playerUUID, _uuid_ws, GetUpgrades());
	}
	public ItemStack GetUpgradeItem()
	{
		ItemStack stack = new ItemStack(Material.PAPER);
		Metods.setDisplayName(stack, "&6Waystone Upgrade Data");
		
		ArrayList<String> lores = new ArrayList<>();
		
		if(GetUpgrade(UpgradeType.CAST_TIME).GetCurrentTier() > 0) 	lores.add("&eCast Time: &2"+ 	GetUpgrade(UpgradeType.CAST_TIME).GetCurrentTier());
		if(GetUpgrade(UpgradeType.COOLDOWN).GetCurrentTier() > 0)	lores.add("&eCooldown: &2"+	 	GetUpgrade(UpgradeType.COOLDOWN).GetCurrentTier());
		if(GetUpgrade(UpgradeType.DIMENSION).GetCurrentTier() > 0)	lores.add("&eDimension: &2"+ 	GetUpgrade(UpgradeType.DIMENSION).GetCurrentTier());
		if(GetUpgrade(UpgradeType.XP_USAGE).GetCurrentTier() > 0)	lores.add("&eXp Usage: &2"+ 	GetUpgrade(UpgradeType.XP_USAGE).GetCurrentTier());
		if(GetUpgrade(UpgradeType.FOUNDATION).GetCurrentTier() > 0)	lores.add("&eFoundation: &2"+ 	GetUpgrade(UpgradeType.FOUNDATION).GetCurrentTier());
		
		Metods._ins.setPersistenData(stack, UpgradeType.CAST_TIME.toString(),	PersistentDataType.INTEGER, GetUpgrade(UpgradeType.CAST_TIME).GetCurrentTier());
		Metods._ins.setPersistenData(stack, UpgradeType.COOLDOWN.toString(), 	PersistentDataType.INTEGER, GetUpgrade(UpgradeType.COOLDOWN).GetCurrentTier());
		Metods._ins.setPersistenData(stack, UpgradeType.DIMENSION.toString(),	PersistentDataType.INTEGER, GetUpgrade(UpgradeType.DIMENSION).GetCurrentTier());
		Metods._ins.setPersistenData(stack, UpgradeType.XP_USAGE.toString(), 	PersistentDataType.INTEGER, GetUpgrade(UpgradeType.XP_USAGE).GetCurrentTier());
		Metods._ins.setPersistenData(stack, UpgradeType.FOUNDATION.toString(), 	PersistentDataType.INTEGER, GetUpgrade(UpgradeType.FOUNDATION).GetCurrentTier());
		Metods._ins.SetLores(stack, lores, false);
		Metods._ins.AddGlow(stack);
		WaystoneManager.Instance.SetUpgradeItemStackPD(stack);
		return stack;
	}
	
	public boolean InsertUpgradeItem(ItemStack stack)
	{
		if(!WaystoneManager.Instance.IsUpgradeItemStack(stack)) return false;
		
		Integer castTime = Metods._ins.getPersistenData(stack, UpgradeType.CAST_TIME.toString(), PersistentDataType.INTEGER);
		Integer cooldown = Metods._ins.getPersistenData(stack, UpgradeType.COOLDOWN.toString(), PersistentDataType.INTEGER);
		Integer dimension = Metods._ins.getPersistenData(stack, UpgradeType.DIMENSION.toString(), PersistentDataType.INTEGER);
		Integer xp_usage = Metods._ins.getPersistenData(stack, UpgradeType.XP_USAGE.toString(), PersistentDataType.INTEGER);
		Integer foundation = Metods._ins.getPersistenData(stack, UpgradeType.FOUNDATION.toString(), PersistentDataType.INTEGER);
		
		if(castTime != null 	&& GetUpgrade(UpgradeType.CAST_TIME).GetCurrentTier() 	< castTime) 	GetUpgrade(UpgradeType.CAST_TIME).SetCurrentier(castTime);
		if(cooldown != null 	&& GetUpgrade(UpgradeType.COOLDOWN).GetCurrentTier() 	< cooldown) 	GetUpgrade(UpgradeType.COOLDOWN).SetCurrentier(cooldown);
		if(dimension != null 	&& GetUpgrade(UpgradeType.DIMENSION).GetCurrentTier() 	< dimension) 	GetUpgrade(UpgradeType.DIMENSION).SetCurrentier(dimension);
		if(xp_usage != null 	&& GetUpgrade(UpgradeType.XP_USAGE).GetCurrentTier() 	< xp_usage) 	GetUpgrade(UpgradeType.XP_USAGE).SetCurrentier(xp_usage);
		if(foundation != null 	&& GetUpgrade(UpgradeType.FOUNDATION).GetCurrentTier() 	< foundation) 	GetUpgrade(UpgradeType.FOUNDATION).SetCurrentier(foundation);
		
		
		if(castTime != null || cooldown != null || dimension != null || xp_usage != null || foundation != null ) return true;
		//_wManager.GetWaystoneManagerSQL().SaveUpgradeAsync(_player.getUniqueId(), _waystone.GetUUID(), upgrade);
		
		return false;
	}
	
	public BaseUpgrade GetUpgrade(UpgradeType type)
	{
		switch (type) 
		{
		case CAST_TIME:
			return get_castTime();
		case COOLDOWN:
			return get_cooldown();
		case DIMENSION:
			return get_dimension();
		case XP_USAGE:
			return get_xpUsage();
		case BUILD:
			return ImusWaystones._instance.GetWaystoneManager().GetWaystone(_uuid_ws).GetUpgradeBottomUpgrade();
		case FOUNDATION:
			return get_foundation();
		case RENAME:
			return get_nameChange();


			
		}
		return null;
	}

	
	
}
