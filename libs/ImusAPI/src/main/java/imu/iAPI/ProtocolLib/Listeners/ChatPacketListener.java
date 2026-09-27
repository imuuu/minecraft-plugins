package imu.iAPI.ProtocolLib.Listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import jdk.internal.org.jline.utils.Log;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class ChatPacketListener extends PacketAdapter
{
    public ChatPacketListener(Plugin plugin)
    {
        super(plugin, PacketType.Play.Server.CHAT, PacketType.Play.Client.CHAT);
    }

    @Override
    public void onPacketReceiving(PacketEvent event)
    {
        Bukkit.getLogger().info("onPacketReceiving");

        if (event.getPacketType() == PacketType.Play.Client.CHAT)
        {
            String message = event.getPacket().getStrings().read(0);
            Bukkit.getLogger().info("Received chat message: " + message);
            // Additional logic here
        }
    }

    @Override
    public void onPacketSending(PacketEvent event)
    {
        Bukkit.getLogger().info("onPacketSending");
    }
}
