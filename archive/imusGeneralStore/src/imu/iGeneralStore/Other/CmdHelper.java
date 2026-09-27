package imu.iGeneralStore.Other;

import java.util.HashMap;

import imu.iGeneralStore.Main.Main;
import net.md_5.bungee.api.ChatColor;

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
