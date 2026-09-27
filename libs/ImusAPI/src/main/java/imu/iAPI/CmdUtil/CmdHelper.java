package imu.iAPI.CmdUtil;

import java.util.HashMap;

import net.md_5.bungee.api.ChatColor;

public class CmdHelper 
{
	HashMap<String, CmdData> _cmds = new HashMap<>();

	String _pluginNamePrefix = "";
	public CmdHelper(String pluginName)
	{
		_pluginNamePrefix = ChatColor.translateAlternateColorCodes('&', "&9["+pluginName+"]&c");
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
