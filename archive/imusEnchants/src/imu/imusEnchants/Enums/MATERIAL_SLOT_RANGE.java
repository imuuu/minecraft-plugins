package imu.imusEnchants.Enums;

import java.util.Random;

public enum MATERIAL_SLOT_RANGE
{
	ELYTRA		(12,20),
	NETHERITE	(10, 20),
	DIAMOND		(4, 16),
    IRON		(1, 9),
    GOLD		(1, 12),
    
    CROSSBOW	(2,11),
    BOW			(2,11),
    
    SHIELD		(1,10),
    TRIDEN		(1,10),
    
    STONE		(1,10),
    FISH_ROD	(1,10),
    LEATHER		(1,10),
    WOOD		(1, 8);

    private final int _minSlots;
    private final int _maxSlots;

    MATERIAL_SLOT_RANGE(int minSlots, int maxSlots) 
    {
        _minSlots = minSlots;
        _maxSlots = maxSlots;
    }

    public int GetRandomSlots() 
    {
        return new Random().nextInt((_maxSlots - _minSlots) + 1) + _minSlots;
    }
    
    public int GetRandomSlots(int min) 
    {
        return new Random().nextInt((_maxSlots - min) + 1) + min;
    }
    
    public static int GetRandomSlots(int min, int max) 
    {
        return new Random().nextInt((max - min) + 1) + min;
    }
    
    public int GetMaxSlots()
    {
    	return _maxSlots;
    }
    
    public int GetMinSlots()
    {
    	return _minSlots;
    }
}
