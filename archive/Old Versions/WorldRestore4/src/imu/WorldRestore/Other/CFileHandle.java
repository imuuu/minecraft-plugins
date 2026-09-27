package imu.WorldRestore.Other;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import imu.WorldRestore.main.Main;

public class CFileHandle 
{
	Main _main = null;
	
	String _dataFileName = "ChunksData.txt";
	
	
	File _data;
	public CFileHandle(Main main)
	{
		_main = main;
		makeDataFile();
		w();


		long start = System.currentTimeMillis();

		r();
		long elapsedTime = System.currentTimeMillis()-start;
		System.out.println("TIME WAS: "+elapsedTime);
			
	}
	
	void makeDataFile()
	{
		_data = new File(_main.getDataFolder()+"/"+_dataFileName);
		try 
		{
			if(_data.createNewFile())
			{
				System.out.println("New DataFile created");
			}else
			{
				System.out.println("File already exist");
			}
		} catch (Exception e) 
		{
			System.out.println("Error occured");
		}		
	}
	
	void w()
	{
		try {
			Path path = Paths.get(_main.getDataFolder()+"/", _dataFileName);
			//Files.write("lol".getBytes(),_data);
			List<String> asd = new ArrayList<>();
			for(int i = 0 ; i < 30000; i++)
			{
				asd.add("lollipoppi");
			}				

			//Files.write(path,"lol\n".getBytes(), StandardOpenOption.APPEND);
			Files.write(path, asd, StandardOpenOption.CREATE);
			
		} catch (Exception e) 
		{
			System.out.println("error occured writing file");
		}
		
		
	}
	void r()
	{
		try 
		{
			//Path path = Paths.get(_main.getDataFolder()+"/", _dataFileName);
			//List<String> list = Files.readAllLines(path);
			//System.out.println("lista: "+ list);
		} catch (Exception e) 
		{
			System.out.println("error occured reading file");
		}
	}
	
	
}
