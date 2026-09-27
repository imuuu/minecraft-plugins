package imu.iAPI.Enums;

public enum ENCHANTMENT_TIER
{
	TIER_1,
	TIER_2,
	TIER_3;
	
	public static ENCHANTMENT_TIER GetLowerTier(ENCHANTMENT_TIER tier) 
	{
        int ordinal = tier.ordinal();
        if (ordinal > 0) 
        {
            return values()[ordinal - 1];
        }
        return tier;
    }

    public static ENCHANTMENT_TIER GetUpperTier(ENCHANTMENT_TIER tier) 
    {
        int ordinal = tier.ordinal();
        if (ordinal < values().length - 1) 
        {
            return values()[ordinal + 1];
        }
        return tier; 
    }
}
