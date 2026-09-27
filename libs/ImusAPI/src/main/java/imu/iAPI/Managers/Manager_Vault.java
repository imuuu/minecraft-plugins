package imu.iAPI.Managers;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Manager_Vault
{
    private static Economy _economy = null;
    private Plugin _plugin;

    public Manager_Vault(Plugin plugin)
    {
        _plugin = plugin;
    }

    public static boolean setupEconomy()
    {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null)
        {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
        {
            return false;
        }
        _economy = rsp.getProvider();
        return true;
    }

    public static Economy getEconomy()
    {
        return _economy;
    }

    public static void giveMoney(Player player, double amount)
    {
        if (_economy != null)
        {
            _economy.depositPlayer(player, amount);
        }
    }

    public static boolean takeMoney(Player player, double amount)
    {
        if (_economy != null && _economy.has(player, amount))
        {
            _economy.withdrawPlayer(player, amount);
            return true;
        }
        return false;
    }

    public static double getBalance(Player player)
    {
        if (_economy != null)
        {
            return _economy.getBalance(player);
        }
        return 0;
    }

    public static boolean transferMoney(Player from, Player to, double amount)
    {
        if (takeMoney(from, amount))
        {
            giveMoney(to, amount);
            return true;
        }
        return false;
    }

    public static boolean hasMoney(Player player, double amount)
    {
        if (_economy != null) {
            return _economy.has(player, amount);
        }
        return false;
    }
}
