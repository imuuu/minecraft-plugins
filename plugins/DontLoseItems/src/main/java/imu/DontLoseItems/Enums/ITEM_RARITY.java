package imu.DontLoseItems.Enums;

public enum ITEM_RARITY
{
	NONE,
    Common,
    Uncommon,
    Rare,
    Epic,
    Mythic,
    Legendary,
	Void;
    
	public int GetIndex()
    {
    	switch (this)
		{
    	case NONE: 		return -1;
		case Common: 	return 0;
		case Uncommon:  return 1;
		case Rare:      return 2;
		case Epic:		return 3;
		case Mythic:	return 4;
		case Legendary:	return 5;
		case Void:		return 6;
		default:
			return -1;
		}
    }
	
	public static ITEM_RARITY GetRarity(Integer index)
	{
		if(index == null) return NONE;
		if(index > 6) return Void;
		
		switch (index)
		{
		case 0: return Common;
		case 1: return Uncommon;
		case 2: return Rare;
		case 3: return Epic;
		case 4: return Mythic;
		case 5: return Legendary;
		case 6: return Void;
		
		}
		return Common;
	}
	
	public ITEM_RARITY GetIncreaseRarity(int range)
	{
		return GetRarity(GetIndex()+range);
	}
}
