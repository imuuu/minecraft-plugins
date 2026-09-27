package imu.AC.Xray.Events;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;

import imu.AC.Main.Main;

public class XrayEvents implements Listener
{
	private Main _main;
	ProtocolManager _pManager;
	public XrayEvents(Main main) 
	{
		_main = main;
		_pManager = main.GetProtocolManager();
	}
	
	//
	
	@EventHandler
	public void OnInteract(PlayerInteractEvent e)
	{
		System.out.println("player interact");
		//PacketTest(e.getPlayer());
	}
	
	
	void PacketTest(Player player)
	{
		Location loc = player.getLocation();
		PacketContainer packet = _pManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
		packet.getModifier().writeDefaults();
		packet.getBlockPositionModifier().write(0, new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		packet.getIntegers().write(1, 1);
		
		try {
			_pManager.sendServerPacket(player, packet);
		} catch (InvocationTargetException e) {
			System.out.println("Expect!!!!!!!!!!!!!!!");
			e.printStackTrace();
		}
	}
}
