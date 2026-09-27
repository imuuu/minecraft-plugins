package imu.UCI.Attributes;

public enum AttributeType 
{
	PHYC_RESISTANCE(0),
	FIRE_RESISTANCE(1);
			
	int type;
	
	AttributeType(int i)
	{
		this.type = i;
	}
	public int getType()
	{
		return type;
	}		
}
