package imu.GS.Other;

import java.util.HashMap;

import org.bukkit.ChatColor;

import imu.GS.Main.Main;

public class CmdHelper 
{
	HashMap<String, CmdData> _cmds = new HashMap<>();
	Main _main;
	
	String _pluginNamePrefix = "";
	public CmdHelper(Main main)
	{
		_main = main;
		_pluginNamePrefix = ChatColor.translateAlternateColorCodes('&', "&9["+_main.getName()+"]&c");
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
