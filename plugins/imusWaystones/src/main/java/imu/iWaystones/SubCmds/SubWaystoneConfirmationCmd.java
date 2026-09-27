package imu.iWaystones.SubCmds;


import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Other.Metods;
import imu.iAPI.Other.ParticleAnimations;
import imu.iWaystone.Waystones.Waystone;
import imu.iWaystones.Main.ImusWaystones;
import imu.iWaystones.Managers.WaystoneManager;
import imu.iWaystones.Other.CmdData;

public class SubWaystoneConfirmationCmd implements CommandInterface
{
	CmdData _data;
	WaystoneManager _wManager;
	public SubWaystoneConfirmationCmd(CmdData data) 
	{
		_data = data;
		_wManager = ImusWaystones._instance.GetWaystoneManager();
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {
    	if(args.length != 3)
    	{
    		return false;
    	}
    	Player p = (Player)sender;
    	Waystone ws = _wManager.GetConfirmWaystone(p.getUniqueId());
    	_wManager.RemoveConfirmWaystone(p.getUniqueId());
    	if(!_wManager.IsValid(ws))
    	{
    		sender.sendMessage(Metods.msgC("&4Waystone isn't valid!"));
    		return false;
    	}
    	
    	
    	if(!p.hasPermission(ImusWaystones._instance.perm_buildIngnore))
    	{
    		double distance = _wManager.IsNearByWaystones(ws);
    		if(distance > 0)
    		{
    			sender.sendMessage(Metods.msgC("&4You can't build waystones close to each other &e(&9"+ Math.round(_wManager._buildDistance - distance)+"&e m)" ));
        		return false;
    		}
    		
    	}
    	
    	_wManager.SaveWaystone(ws, true);
    	sender.sendMessage(Metods.msgC("&3Waystone has been registered"));
    	p.playSound(ws.GetLoc(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.1f, 0.1f);
    	ParticleAnimations anim = new ParticleAnimations(ws.GetLoc().clone().add(0.5,0.5,0.5));
    	
    	anim.DrawHurricaneAsync(Particle.ASH, 0.8,	0,	5);
    	anim.DrawHurricaneAsync(Particle.ASH, 0.8,	20,	5);
    	anim.DrawHurricaneAsync(Particle.ASH, 0.8,	40,	5);
    	anim.DrawHurricaneAsync(Particle.ASH, 0.8,	60,	5);

		
		
        return false;
    }
    
   
	@Override
	public void FailedMsg(CommandSender arg0, String arg1) {
		
	}
    
   
   
}