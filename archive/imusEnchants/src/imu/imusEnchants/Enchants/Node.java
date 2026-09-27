package imu.imusEnchants.Enchants;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import imu.iAPI.Enums.DEFAULT_FONT_INFO;
import imu.imusEnchants.Enums.TOUCH_TYPE;
import imu.imusEnchants.main.CONSTANTS;

public class Node implements INode
{
	protected int _x;
	protected int _y;
	
	// 0 LEFT, 1 RIGHT, 2 UP, 3 DOWN
	private INode[] _neighbors;
	
	private boolean _isLock = false;
	private boolean _isFrozen = false;
	protected static Random _random = new Random();
	
	public Node() {}
	
	public Node(int x, int y)
	{
		_x = x;
		_y = y;
	}
	
	@Override
	public void SetPosition(int x, int y)
	{
		_x = x;
		_y = y;
	}
	
	public int GetFlatIndex()
	{
		return _x * CONSTANTS.ENCHANT_COLUMNS + _y;
	}
	
	public static int GetFlatIndex(int x, int y)
	{
		return x * CONSTANTS.ENCHANT_COLUMNS + y;
	}
	
	@Override
	public int GetX()
	{
		return _x;
	}

	@Override
	public int GetY()
	{
		return _y;
	}

	@Override
	public INode[] GetNeighbors()
	{
		return _neighbors;
	}

	@Override
	public void SetNeighbors(INode[] nodes)
	{
		_neighbors = nodes;
	}

	@Override
	public boolean IsLocked()
	{
		return _isLock;
	}

	@Override
	public void SetLock(boolean lock)
	{
		_isLock = lock;
	}
	

	@Override
	public boolean IsFrozen()
	{
		return _isFrozen;
	}

	@Override
	public void SetFrozen(boolean frozen)
	{
		_isFrozen = frozen;
	}

	@Override
    public String Serialize() 
	{
        return this.getClass().getSimpleName() + 
        		":" + GetX() + 
        		":" + GetY() +
        		":" + _isFrozen;
        		
    }

    @Override
    public void Deserialize(String data) 
    {
        String[] parts = data.split(":");
        _x = Integer.parseInt(parts[1]);
        _y = Integer.parseInt(parts[2]);
        _isFrozen = Boolean.parseBoolean(parts[3]);
       
    }

	@Override
	public String toString()
	{
		
		return Serialize();
	}

	@Override
	public boolean IsValidGUIitem(TOUCH_TYPE touchType, EnchantedItem enchantedItem, ItemStack stack)
	{
		return false;
	}

	@Override
	public void Activate(EnchantedItem enchantedItem)
	{
		
	}

	@Override
	public ItemStack GetGUIitemLoad(EnchantedItem enchantedItem)
	{
		return new ItemStack(Material.AIR);
	}
	
	@Override
	public ItemStack GetGUIitemUnLoad(EnchantedItem enchantedItem, ItemStack stack)
	{
		return new ItemStack(Material.AIR);
	}


	

	
	
	
	
}
