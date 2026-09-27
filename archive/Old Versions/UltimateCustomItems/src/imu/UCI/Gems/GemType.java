package imu.UCI.Gems;

public enum GemType 
{
	BLUE(0),
	RED(1),
	YELLOW(2),
	DIAMOND(3);
			
	int type;
	
	GemType(int i)
	{
		this.type = i;
	}
	public int getType()
	{
		return type;
	}		
}
