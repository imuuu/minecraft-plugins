package imu.iMiniGames.Other;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.iMiniGames.Enums.COMBAT_ATTRIBUTE;

public class CombatDataCard extends PlanerDataCard
{		
	HashMap<UUID, ItemStack[]> _ownGear = new HashMap<>();
	ArenaKit _kit = null;
	
	boolean isRandomKit = true;
	boolean isOwnGearKit = false;
	
	public CombatDataCard(Player owner)
	{
		super(owner);
		resetAttributes();
	}
	
	@Override
	public void resetAttributes()
	{
		setAttribute(COMBAT_ATTRIBUTE.NO_ARROW_SPREAD, 1);
		setAttribute(COMBAT_ATTRIBUTE.SHOW_DMG, 1);
	}
		
	
	public HashMap<UUID, ItemStack[]> get_ownGear() {
		return _ownGear;
	}
	public void setPlayerGear(UUID uuid, ItemStack[] gear)
	{
		_ownGear.put(uuid, gear);
	}
	
	public ItemStack[] getPlayerGear(UUID uuid)
	{
		return _ownGear.get(uuid);
	}
	
	public Integer getAttribute(COMBAT_ATTRIBUTE name)
	{
		if(_attributes.containsKey(name.toString()))
		{
			return _attributes.get(name.toString());
		}
		return 0;
		
	}
	public void setAttribute(COMBAT_ATTRIBUTE att, int value)
	{
		_attributes.put(att.toString(), value);
	}
	public boolean containAttribute(COMBAT_ATTRIBUTE att)
	{
		return _attributes.containsKey(att.toString());
	}
	public boolean isOwnGearKit() {
		return isOwnGearKit;
	}

	public void setOwnGearKit(boolean isOwnGearKit) {
		this.isOwnGearKit = isOwnGearKit;
	}
	
	public boolean isRandomKit() {
		return isRandomKit;
	}

	public void setRandomKit(boolean isRandomKit) 
	{
		this.isRandomKit = isRandomKit;
	}
	
	
	public ArenaKit get_kit() {
		return _kit;
	}

	public void set_kit(ArenaKit _kit) 
	{
		this._kit = _kit;
	}
	
	
}
