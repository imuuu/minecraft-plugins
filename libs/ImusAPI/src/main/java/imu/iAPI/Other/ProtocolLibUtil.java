package imu.iAPI.Other;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import imu.iAPI.InvUtil.WindowInfo;
import imu.iAPI.Main.ImusAPI;

public final class ProtocolLibUtil implements Listener
{
	ProtocolManager _manager;
	
	private HashMap<UUID, WindowInfo> _windows = new HashMap<>();
	
	public ProtocolLibUtil()
	{
		_manager = ImusAPI._instance.GetProtocolManager();
		InitListeners();
		ImusAPI._instance.getServer().getPluginManager().registerEvents(this, ImusAPI._instance);
	}
	
	private final void InitListeners() 
	{
		_manager.addPacketListener(new PacketAdapter(ImusAPI._instance,PacketType.Play.Server.OPEN_WINDOW) 
		{
			@Override
			public void onPacketSending(PacketEvent event)
			{
				//SOME REASON THIS WILL NOT WORK.. gives error that packet has only 1 spot in array.
				PacketContainer packet = event.getPacket();
				int id = packet.getIntegers().read(0);
				//int type = packet.getIntegers().read(1);
				int type = 0;
				//_windows.put(event.getPlayer().getUniqueId(), new WindowInfo(id, type));
				_windows.put(event.getPlayer().getUniqueId(), new WindowInfo(id, type));
			}
		});
	}
	
	
	@EventHandler
	public void OnLeave(PlayerQuitEvent e)
	{
		_windows.remove(e.getPlayer().getUniqueId());
	}
	public int GetInventoryID(Player player)
	{
		return _windows.get(player.getUniqueId())._windowID;
	}
	
	public int GetInventoryType(Player player)
	{
		return _windows.get(player.getUniqueId())._windowType;
	}

	
	
	
}
