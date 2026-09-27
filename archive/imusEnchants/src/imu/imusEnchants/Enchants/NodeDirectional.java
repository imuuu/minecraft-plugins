package imu.imusEnchants.Enchants;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.iAPI.Utilities.ItemUtils;
import imu.imusEnchants.Enums.DIRECTION;

public abstract class NodeDirectional extends Node
{
	private int _steps = 1;
	protected DIRECTION[] _directions = new DIRECTION[] {DIRECTION.UP};
	
	public NodeDirectional()
	{
		SetLock(false);
		RandomizeDirection(InitDirectionAmount());
	}
	
	public NodeDirectional(int steps)
	{
		_steps = steps;
		RandomizeDirection(InitDirectionAmount());
		SetLock(false);
	}
	public abstract int InitDirectionAmount();
	
	public DIRECTION[] GetDirections()
	{
		return _directions;
	}

	public void RandomizeDirection(int amount) 
	{
	    DIRECTION[] directions = DIRECTION.values();
	   
	    DIRECTION[] selectedDirections = new DIRECTION[amount];

	    for (int i = 0; i < amount; i++) 
	    {
	        DIRECTION randomDirection;
	        do 
	        {
	            randomDirection = directions[_random.nextInt(directions.length)];
	        } 
	        while (Arrays.asList(selectedDirections).contains(randomDirection));

	        selectedDirections[i] = randomDirection;
	    }

	    _directions = selectedDirections;
	}
	
	public void RandomizeDirectionMax(int maxAmount) 
	{
        maxAmount = Math.max(1, maxAmount);
        int amount = (_random.nextBoolean() ? 1 : maxAmount);
        RandomizeDirection(amount);
    }
	
	public ItemStack SetDirectionsPD(ItemStack stack)
	{
		_directions = DIRECTION.GetSortedDirections(_directions);
		String serializedDirections = GetSerializedDirections();
		ItemUtils.SetPersistenData(stack, "node_direction_directions", PersistentDataType.STRING, serializedDirections);
		
		return stack;
	}
	
	public DIRECTION[] GetDirectionsPD(ItemStack stack) 
	{
	    String serializedDirections = 
	    		ItemUtils.GetPersistenData(stack, "node_direction_directions", PersistentDataType.STRING);
	    if (serializedDirections != null && !serializedDirections.isEmpty()) 
	    {
	        return GetDeserializedDirections(serializedDirections); 
	    }
	    return new DIRECTION[0]; 
	}

	
	protected String GetSerializedDirections()
	{
		return Arrays.stream(_directions)
                .map(Enum::name)
                .collect(Collectors.joining(","));
	}
	
	protected DIRECTION[] GetDeserializedDirections(String part)
	{
		String[] directionNames = part.split(",");
        return Arrays.stream(directionNames)
                            .map(DIRECTION::valueOf)
                            .toArray(DIRECTION[]::new);
	}

	@Override
	public String Serialize()
	{
		String dir = GetSerializedDirections();
		
		return this.getClass().getSimpleName() + 
				":" + GetX() + 
				":" + GetY() + 
				":" + dir + 
				":" + _steps;
	}

	@Override
	public void Deserialize(String data)
	{
		
		String[] parts = data.split(":");
		_x = Integer.parseInt(parts[1]);
		_y = Integer.parseInt(parts[2]);
		_directions = GetDeserializedDirections(parts[3]);
		_steps = Integer.parseInt(parts[4]);
	}

}
