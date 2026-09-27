package imu.GS.Interfaces;


public interface IModData 
{
	public String GetValueStr(IModDataValues v,String trueFrontText, String trueBackText, String falseStr);
	public boolean SetAndCheck(IModDataValues v, String str);
	
}
