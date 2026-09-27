package imu.iAPI.Other;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class MySQLHelper 
{
	public void PrintColunmNames(ResultSet rs) 
	{
		try 
		{
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();

			for (int i = 1; i <= columnCount; i++ ) 
			{
			  String name = rsmd.getColumnName(i);
			  System.out.println("idx: "+i+" columnName: "+name);
			}
		} 
		catch (Exception e) 
		{
			System.out.println("Couldnt print colums names!");
		}

		
	}
}
