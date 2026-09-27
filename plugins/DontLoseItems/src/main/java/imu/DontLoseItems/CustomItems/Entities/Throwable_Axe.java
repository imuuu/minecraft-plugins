package imu.DontLoseItems.CustomItems.Entities;

import java.util.UUID;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import imu.DontLoseItems.CustomItems.RarityItems.Hell_Double_Axe;

public class Throwable_Axe
{
	public static enum Throwable_State
	{
		TO_TARGET,
		RETURNING,
	}
	
	public ArmorStand ArmorStand;
	public UUID Uuid;
	public String Str_uuid;
	//private Location Loc_start;
	public Hell_Double_Axe HD_axe;
	public Projectile Projectile;
	public Projectile Projectile_AxePos;
	public Vector Direction;
	public Player Player;
	public double DAMAGE = 1;
	

	private Throwable_State _state = Throwable_State.TO_TARGET;
	private Throwable_State _lastState = Throwable_State.TO_TARGET;
	
	public final int MAXIMUM_ENTETIES = 10;
	public final double MAX_DISTANCE_ACTIVE = 60;
	
	public Throwable_Axe(Player player, ArmorStand armorStand, Vector dir,Hell_Double_Axe axe)
	{
		ArmorStand = armorStand;
		HD_axe = axe;
		Uuid = armorStand.getUniqueId();
		Str_uuid = Uuid.toString();
		//Loc_start = armorStand.getLocation().clone();

		Direction = dir;
		Player = player;

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