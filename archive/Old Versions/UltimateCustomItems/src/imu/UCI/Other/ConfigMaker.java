package imu.UCI.Other;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ConfigMaker {

	private Plugin _plugin;
	private File _file;
	private String _fileName;
	private FileConfiguration _config;
	
	public ArrayList<String> lines = new ArrayList<>();
	
	public ConfigMaker(Plugin plugin, String fileName)
	{
		_plugin=plugin;
		_file = new File(_plugin.getDataFolder() + "/" + fileName);
		_fileName = fileName;
		_config = YamlConfiguration.loadConfiguration(_file);
	}
	
	public String getFileName()
	{
		return _fileName;
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
	
	public void removeConfig()
	{
		File f= new File(_plugin.getDataFolder() + "/" + _fileName);
		f.delete();
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
	
	public File getFile()
	{
		return new File(_plugin.getDataFolder() + "/" + _fileName);
	}
	
	public void clearConfig()
	{
		for (String key : _config.getConfigurationSection("").getKeys(false)) 
		{
			_config.set(key, null);
		}
		saveConfig();
	}
	
	public String getFilePath()
	{
		return _plugin.getDataFolder() + "/" + _fileName;
	}
	void saveInvTOconfig(Player player)
	{
		ConfigMaker cm = new ConfigMaker(_plugin, _fileName);
		FileConfiguration config = cm.getConfig();
		
		config.set(player.getUniqueId().toString(), player.getInventory().getContents());
		cm.saveConfig();
	}
	
	ItemStack[] getSavedInvFromConfig(Player player)
	{
		ConfigMaker cm = new ConfigMaker(_plugin, _fileName);
		FileConfiguration config = cm.getConfig();
		if(config.contains(player.getUniqueId().toString()))
		{
			System.out.println("Player exist in invs");
			@SuppressWarnings("unchecked")
			List<ItemStack> stacks = (List<ItemStack>) config.getList(player.getUniqueId().toString());
			ItemStack[] content = new ItemStack[stacks.size()];
			stacks.toArray(content);
			return content;
			
		}
		return null;
	}
	
	
	public void addComments() throws Exception
	{
		PrintWriter pw = new PrintWriter(getFilePath());
		pw.close();
		Files.write(Paths.get(getFilePath()),lines);
	}
	
	
	
	@SuppressWarnings("unchecked")
	public <T> T addDefault(String name, T value, String comment) 
	{
		name = name+"("+value.getClass().getSimpleName()+")";
		if(!isExists() || !_config.contains(name))
		{		
			
			_config.set(name,value);
		}
		saveConfig();
		
		if(comment != null && comment != "" )
		{
			lines.add("# "+comment);
		}		
		String line = name+": "+ _config.get(name).toString() ;
		lines.add(line);
		
		return (T)_config.get(name);
		
	}
	
	public <K,V> void saveHashMap(HashMap<K, V> map, String path) 
	{
		for(Entry<K,V> entry : map.entrySet())
		{
			
			String key = entry.getKey().toString()+"key";
			System.out.println("GET KEY: "+key);
			_config.set(path+"."+key, entry.getKey());			
			_config.set(path+"."+key+"."+"value", entry.getValue());			
		}
		
		saveConfig();
	}
	
	public <K,V> HashMap<K, V> getHashMap(String path)
	{			
		HashMap<K, V> map= new HashMap<>();
		for (String key : _config.getConfigurationSection(path).getKeys(false)) 
		{
			System.out.println("key===: "+ _config.get(key));
			//map.put((K)_config.get(path+"."+key), (V)_config.get(path+"."+key+"."+"value"));
		}
		
		return map;
	}
}
