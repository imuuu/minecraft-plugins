package me.imu.imusenchants.Enchants;

import me.imu.imusenchants.CONSTANTS;
import me.imu.imusenchants.Enums.DIRECTION;
import me.imu.imusenchants.Enums.TOUCH_TYPE;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.iAPI.Utilities.ItemUtils;

public class NodeBooster extends NodeDirectional
{
	private int _power = 1;
	private int _directionAmount = 1;

	private final static String PD_BOOSTER = "PD_BOOSTER";

	public NodeBooster()
	{
		SetLock(false);
	}

	public NodeBooster(int power, int directionAmount)
	{
		_power = power;
		_directionAmount = directionAmount;
		RandomizeDirection(_directionAmount);
	}
	
	public NodeBooster(ItemStack stack)
	{
		_directions = GetDirectionsPD(stack);
		_directionAmount = _directions.length;
		_power = GetPower();
	}

	@Override
	public int InitDirectionAmount()
	{
		return 4;
	}

	public void SetPower(int power)
	{
		_power = power;
	}

	public int GetPower()
	{
		return _power;
	}


	@Override
	public boolean IsValidGUIitem(TOUCH_TYPE touchType, EnchantedItem enchantedItem, ItemStack stack)
	{
		if (stack.getType() != CONSTANTS.BOOSTER_MATERIAL)
			return false;

		return IsBooster(stack);
	}

	@Override
	public ItemStack GetGUIitemLoad(EnchantedItem enchantedItem)
	{
		return GetItemStack();
	}

	@Override
	public ItemStack GetGUIitemUnLoad(EnchantedItem enchantedItem, ItemStack stack)
	{
		return GetItemStack();
	}

	public ItemStack GetItemStack()
	{
		String str_directions = DIRECTION.GetStrDirection(_directions);
		String color = "&e";
		switch (_directions.length)
		{
		case 1: {color = "&9"; break;}
		case 2: {color = "&a"; break;}
		case 3: {color = "&6"; break;}
		case 4: {color = "&b"; break;}
		}
		ItemStack itemStack = new ItemStack(CONSTANTS.BOOSTER_MATERIAL);
		ItemUtils.SetDisplayName(itemStack, color+"BOOSTER &3(&5"+str_directions+"&3)");
		ItemUtils.AddLore(itemStack, "&6⚡: &2" + _power, true);
		ItemUtils.HideFlag(itemStack, ItemFlag.HIDE_POTION_EFFECTS);
		ItemUtils.AddGlow(itemStack);
		ItemUtils.SetPersistenData(itemStack, PD_BOOSTER, PersistentDataType.INTEGER, _power);
		SetDirectionsPD(itemStack);
		
		
		return itemStack;
	}

	public static int GetPower(ItemStack stack)
	{
		Integer p = ItemUtils.GetPersistenData(stack, PD_BOOSTER, PersistentDataType.INTEGER);
		return p != null ? p : 0;
	}

	public static boolean IsBooster(ItemStack stack)
	{
		return ItemUtils.GetPersistenData(stack, PD_BOOSTER, PersistentDataType.INTEGER) != null;
	}

	public boolean IsBoostingThis(INode node)
	{
		for(DIRECTION dir : _directions)
		{
			int x = GetX();
			int y = GetY();
			
			switch (dir)
			{
			case UP:
				x--;
				break;
			case DOWN:
				x++;
				break;
			case LEFT:
				y--;
				break;
			case RIGHT:
				y++;
				break;
			}
			
			if(x == node.GetX() && y == node.GetY()) return true;
		}
		return false;
	}

	@Override
	public String Serialize()
	{

		String serializedDirections = GetSerializedDirections(); // Get serialized directions
		
		
		return this.getClass().getSimpleName() + 
				":" + GetX() + 
				":" + GetY() + 
				":" + IsFrozen() + 
				":" + _power + ":"
				+ serializedDirections; // Add serialized directions
	}

	@Override
	public void Deserialize(String data)
	{
		String[] parts = data.split(":");
		_x = Integer.parseInt(parts[1]);
		_y = Integer.parseInt(parts[2]);
		SetFrozen(Boolean.parseBoolean(parts[3]));
		_power = Integer.parseInt(parts[4]);
		
		if (parts.length > 5)
		{
			String serializedDirections = parts[5];
			_directions = GetDeserializedDirections(serializedDirections);
			
		}
	}

}
