package imu.TokenTp.main;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import imu.TokenTp.Commands.TokenTpCmd;
import imu.TokenTp.Commands.TokenTtpTpCmd;
import imu.TokenTp.Events.TokenEvents;
import imu.TokenTp.Handlers.CommandHandler;
import imu.TokenTp.Managers.TeleTokenManager;
import imu.TokenTp.SubCommands.subReloadCmd;
import imu.TokenTp.SubCommands.subSetDescCmd;
import imu.TokenTp.SubCommands.subSetDestCmd;
import imu.TokenTp.SubCommands.subSpawnBaseCmd;
import imu.TokenTp.SubCommands.subSpawnCardCmd;
import imu.TokenTp.SubCommands.subSpawnTokenCmd;
import imu.TokenTp.SubCommands.subTtpTpAnwserCmd;
import imu.TokenTp.SubCommands.subTtpTpListCmd;
import imu.TokenTp.SubCommands.subTtpTpRequestCmd;
import imu.iAPI.Other.ConfigMaker;

public class Main extends JavaPlugin
{

	Main instance = null;
	TeleTokenManager _ttManager = null;

	public void registerCommands() 
    {  	
        CommandHandler handler = new CommandHandler();

        String cmd1="ttp";
        handler.registerCmd(cmd1, new TokenTpCmd());  
        handler.setPermissionOnLastCmd("ttp");
        handler.registerSubCmd(cmd1, "reload", new subReloadCmd(this));
        handler.registerSubCmd(cmd1, "spawn card", new subSpawnCardCmd(this,"spawn card"));
        handler.registerSubCmd(cmd1, "spawn base", new subSpawnBaseCmd(this));
        handler.registerSubCmd(cmd1, "spawn token", new subSpawnTokenCmd(this, "spawn token"));
        handler.registerSubCmd(cmd1, "set desc", new subSetDescCmd(this));
        handler.registerSubCmd(cmd1, "set dest", new subSetDestCmd(this));
        
        String cmd2="tttp";
        handler.registerCmd(cmd2, new TokenTtpTpCmd()); 
        handler.registerSubCmd(cmd2, "list", new subTtpTpListCmd(this));
        handler.registerSubCmd(cmd2, "request", new subTtpTpRequestCmd(this));
        handler.registerSubCmd(cmd2, "anwser", new subTtpTpAnwserCmd(this));
        
        
        getCommand(cmd1).setExecutor(handler);
        getCommand(cmd2).setExecutor(handler);
        
    }
	
	@Override
	public void onEnable() 
	{		
		instance = this;

		_ttManager = new TeleTokenManager(this);

		ConfigsSetup();

		registerCommands();
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN +" TokenTp has been activated!");
		getServer().getPluginManager().registerEvents(new TokenEvents(this), this);

	}
	
	public TeleTokenManager getTeleTokenManager() {
		return _ttManager;
	}

	@Override
	 public void onDisable()
	{
		
	}
	
	public Main getInstance()
	{
		return instance;
	}
	
	public void ConfigsSetup()
	{
		makeSettingsConfig(false);
	}
	
	public void reloadConfigSetting()
	{
		ConfigMaker cm = new ConfigMaker(this, "settings.yml");
		FileConfiguration config = cm.getConfig();
		if(!cm.isExists())
		{
			makeSettingsConfig(false);
		}
		else
		{

			_ttManager.set_teleport_time(config.getInt("TeleportCastTime(Integer)"));
			_ttManager.set_token_time(config.getInt("TokenUsableTime(Integer)"));
			_ttManager.set_request_time( config.getInt("TokenRequestAnwserTime(Integer)"));
			_ttManager.set_accept_time( config.getInt("TokenAcceptCastTime(Integer)"));
			
			makeSettingsConfig(true);
		}
	}
	
	public void makeSettingsConfig(boolean refresh)
	{
		ConfigMaker cm = new ConfigMaker(this, "settings.yml");
		if(refresh)
		{
			cm.clearConfig();
		}
		try 
		{
			_ttManager.set_teleport_time( cm.addDefault("TeleportCastTime", _ttManager.get_teleport_time() ,"TeleportCastTime: How long it takes to cast teleport, seconds"));
			_ttManager.set_token_time( cm.addDefault("TokenUsableTime", _ttManager.get_token_time() ,"TokenUsableTime: How long it takes to use token again, seconds"));
			_ttManager.set_request_time( cm.addDefault("TokenRequestAnwserTime", _ttManager.getRequestCDtime() ,"TokenRequestAnwserTime: How fast you need to anwser yes/no to request from other player, seconds"));
			_ttManager.set_accept_time( cm.addDefault("TokenAcceptCastTime", _ttManager.getAcceptCDtime() ,"TokenAcceptCastTime: After other player has accept your request. How fast you need to press token again to start teleport, seconds"));
			
			
			cm.addComments();
			
		} catch (Exception e) 
		{
			getServer().getConsoleSender().sendMessage(ChatColor.RED +"WARNING: Something got wrong TokenTp fileNamed: "+cm.getFileName());
		}
		
		
	}
//	public ItemMetods getItemM() 
//	{
//		return itemM;
//	}

		
	
	
	
	
	
	
}
