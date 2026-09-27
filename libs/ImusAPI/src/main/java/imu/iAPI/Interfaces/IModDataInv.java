package imu.iAPI.Interfaces;

public interface IModDataInv 
{
	
	public void openThis();
	public boolean SetModData(IModDataValue modValue, String value);
	public void SetModDataFAILED(IModDataValue modValue, String question,String value);
}
