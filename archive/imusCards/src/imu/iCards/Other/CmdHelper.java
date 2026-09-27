package imu.iCards.Other;

import java.util.HashMap;

import imu.iAPI.CmdUtil.CmdData;
import imu.iCards.Main.ImusCards;
import net.md_5.bungee.api.ChatColor;


public class CmdHelper 
{
	HashMap<String, CmdData> _cmds = new HashMap<>();

	String _pluginNamePrefix = "";
	public CmdHelper()
	{
		_pluginNamePrefix = ChatColor.translateAlternateColorCodes('&', "&9["+ImusCards._instance.getName()+"]&c");
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
