package imu.iWaystones.Other;

import java.util.HashMap;

import org.bukkit.ChatColor;

import imu.iWaystones.Main.ImusWaystones;

public class CmdHelper 
{
	HashMap<String, CmdData> _cmds = new HashMap<>();

	String _pluginNamePrefix = "";
	public CmdHelper()
	{
		_pluginNamePrefix = ChatColor.translateAlternateColorCodes('&', "&9["+ImusWaystones._instance.getName()+"]&c");
	}
	
	public void setCmd(String cmdName, String description, String syntaxText)
	{
		_cmds.put(cmdName, new CmdData(cmdName, description, _pluginNamePrefix+" /" + syntaxText));
	}
	
	public CmdData getCmdData(String cmdName)
	{
		return _cmds.get(cmdName);
	}
}
