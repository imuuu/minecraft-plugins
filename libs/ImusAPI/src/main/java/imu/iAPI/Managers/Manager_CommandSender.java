package imu.iAPI.Managers;

import imu.iAPI.Main.ImusAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class Manager_CommandSender implements Listener
{
    private static Manager_CommandSender _instance;

    public static Manager_CommandSender getInstance()
    {
        return _instance;
    }

    public Manager_CommandSender()
    {
        _instance = this;
        Bukkit.getPluginManager().registerEvents(this, ImusAPI._instance);
    }
    private boolean commandExecuted = false;
    private  Player commandExecutor = null;
    private  String customMessage = "Custom message after command execution";

    public void executeCommandAsPlayer(Player player, String command)
    {
        commandExecutor = player;
        commandExecuted = true;
        Bukkit.dispatchCommand(player, command);
    }

    public void executeCommandAsConsole(String command)
    {
        commandExecuted = true;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        Bukkit.getLogger().info("onPlayerChat");
        if (commandExecuted && event.getPlayer().equals(commandExecutor))
        {
            event.setCancelled(true); // Suppress the original message
            event.getPlayer().sendMessage(customMessage); // Send custom message
            resetCommandFlags();
        }
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event)
    {
        if (commandExecuted && "YourCommand".equals(event.getCommand()))
        {
            // Handle server command output
            resetCommandFlags();
        }
    }

    private void resetCommandFlags()
    {
        commandExecuted = false;
        commandExecutor = null;
    }


}
