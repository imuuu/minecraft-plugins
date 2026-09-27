package imu.HomeTele.Other;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigMaker {

	private Plugin _plugin;
	private File _file;
	private String _fileName;
	private FileConfiguration _config;
	
	public ConfigMaker(Plugin plugin, String fileName)
	{
		_plugin=plugin;
		_file = new File(_plugin.getDataFolder() + "/" + fileName);
		_fileName = fileName;
		_config = YamlConfiguration.loadConfiguration(_file);
	}
	
	public void saveConfig()
	{
		try
		{
			_config.save(_file);
		}catch(IOException e)
		{
			System.out.println("File named "+ _fileName +"didn't found");
			e.printStackTrace();
		}
	}
	
	public FileConfiguration getConfig()
	{
		return _config;
	}
	
	public boolean isExists()
	{
		File b = new File(_plugin.getDataFolder() + "/" + _fileName);
		return b.exists();
	}
}
