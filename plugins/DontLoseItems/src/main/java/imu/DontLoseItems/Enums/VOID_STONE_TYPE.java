package imu.DontLoseItems.Enums;

public enum VOID_STONE_TYPE
{
	NONE,
	UNSTABLE, // adds +1/+2 or -1/-2
	REFORCE, // destroy, lower tier, upper tier
	UNANOINTMENT; // removes one and adds new one not exist
	
	public int GetIndex()
    {
    	switch (this)
		{
		case NONE: 			return -1;
		case UNSTABLE:		return 0;
		case REFORCE:		return 1;
		case UNANOINTMENT:	return 2;

		}
		return -1;
    }
	
	public static VOID_STONE_TYPE GetFromIndex(Integer i)
    {
		if(i == null) return NONE;
		
    	switch (i)
		{
		case -1: 	return NONE;
		case 0:		return UNSTABLE;
		case 1:		return REFORCE;
		case 2:		return UNANOINTMENT;

		}
		return NONE;
    }
}
