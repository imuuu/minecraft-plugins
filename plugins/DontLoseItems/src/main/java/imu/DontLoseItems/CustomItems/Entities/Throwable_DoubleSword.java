package imu.DontLoseItems.CustomItems.Entities;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import imu.DontLoseItems.CustomItems.RarityItems.Hell_Triple_Sword;

public class Throwable_DoubleSword
{
	public static enum Throwable_State
	{
		TO_TARGET,
		DOT_DAMAGE,
		RETURNING,
	}
	
	public ArmorStand ArmorStand;
	public UUID Uuid;
	public String Str_uuid;
	private Location Loc_start;
	private Location Loc_end;
	public Hell_Triple_Sword HB_sword;
	public Vector Direction;
	public Player Player;
	public Location _lastTriggeredLoc;
	public LinkedList<LivingEntity> _entities;
	public HashSet<UUID> _ingnoreUUIDS;
	public Location TargetBlock = null;
	private Throwable_State _state = Throwable_State.TO_TARGET;
	private Throwable_State _lastState = Throwable_State.TO_TARGET;
	
	private double _returnMultiplier = 1.1;
	private double _returnMaxSpeed = 2;
	private double _returnStartSpeed = 0.05;
	
	private int _dotTimeTicks = 20;
	
	public final double MoveSpeed = 1.2f;
	public final int MAXIMUM_ENTETIES = 10;
	public final double MAX_DISTANCE_ACTIVE = 60;
	
	public Throwable_DoubleSword(Player player, ArmorStand armorStand, Location destination,Vector dir,Hell_Triple_Sword sword)
	{
		ArmorStand = armorStand;
		HB_sword = sword;
		Uuid = armorStand.getUniqueId();
		Str_uuid = Uuid.toString();
		Loc_start = armorStand.getLocation().clone();
		Loc_end = destination;
		Direction = dir;
		Player = player;
		_entities = new LinkedList<>();
		_ingnoreUUIDS = new HashSet<>();
		_ingnoreUUIDS.add(player.getUniqueId());
	}
	
	public Throwable_State GetState()
	{
		return _state;
	}
	public Throwable_State GetLastState()
	{
		return _lastState;
	}
	public void SetState(Throwable_State state)
	{
		_lastState = _state;
		this._state = state;
	}
	public Location GetTargetBlock()
	{
		return TargetBlock.clone().subtract(Direction.clone().normalize().multiply(0.5f));
	}
	
	public Location GetStartLoc()
	{
		return Loc_start.clone();
	}
	public void SetDotTimeTicks(int ticks)
	{
		_dotTimeTicks = ticks;
	}
	
	public int GetDotTime()
	{
		 return _dotTimeTicks;
	}
	public boolean IsDotTimeDone()
	{
		if(--_dotTimeTicks <= 0) return true;
		
		return false;
	}
	
	public Location GetEndLoc()
	{
		if(Loc_end == null) return null;
		
		return Loc_end.clone();
	}
	public void SetEndLoc(Location loc)
	{
		Loc_end = loc;
	}
	public double GetReturnSpeed()
	{
		_returnStartSpeed *= _returnMultiplier;
		if(_returnStartSpeed > _returnMaxSpeed) _returnStartSpeed = _returnMaxSpeed;
		return _returnStartSpeed;
	}
	
	public BoundingBox GetBoundingBox() {
		double sizeIncrease = 2;
        // Get the original bounding box of the entity
        BoundingBox originalBBox = ArmorStand.getBoundingBox().clone();

        // Calculate the new dimensions of the bounding box
        double newWidth = originalBBox.getWidthX() + sizeIncrease;
        double newHeight = originalBBox.getHeight() + sizeIncrease;
        double newDepth = originalBBox.getWidthZ() + sizeIncrease;

        // Calculate the new center point of the bounding box
        Vector originalCenter = originalBBox.getCenter();
        Vector newCenter = originalCenter.add(new Vector(0, sizeIncrease * 0.5, 0));

        // Create the new bounding box
        BoundingBox newBBox = new BoundingBox(
            newCenter.getX() - (newWidth 	* 0.5),
            newCenter.getY() - (newHeight 	* 0.5),
            newCenter.getZ() - (newDepth 	* 0.5),
            newCenter.getX() + (newWidth 	* 0.5),
            newCenter.getY() + (newHeight 	* 0.5),
            newCenter.getZ() + (newDepth 	* 0.5)
        );

        // Set the new bounding box for the entity
        return newBBox;
    }
}