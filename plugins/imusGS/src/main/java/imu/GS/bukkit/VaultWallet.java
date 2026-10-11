package imu.GS.bukkit;

import imu.GS.engine.customer.Wallet;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

/**
 * Money through Vault. The economy plugin is looked up the first time it is needed, so it may load after imusGS.
 */
public class VaultWallet implements Wallet
{
    private Economy _economy;

    public boolean isAvailable()
    {
        return economy() != null;
    }

    private Economy economy()
    {
        if (_economy == null && Bukkit.getPluginManager().getPlugin("Vault") != null)
        {
            RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (provider != null)
                _economy = provider.getProvider();
        }
        return _economy;
    }

    private OfflinePlayer player(UUID id)
    {
        return Bukkit.getOfflinePlayer(id);
    }

    @Override
    public double balance(UUID player)
    {
        Economy economy = economy();
        return economy == null ? 0 : economy.getBalance(player(player));
    }

    @Override
    public boolean withdraw(UUID player, double amount)
    {
        Economy economy = economy();
        if (economy == null)
            return false;
        if (amount <= 0)
            return true;
        return economy.withdrawPlayer(player(player), amount).transactionSuccess();
    }

    @Override
    public boolean deposit(UUID player, double amount)
    {
        Economy economy = economy();
        if (economy == null)
            return false;
        if (amount <= 0)
            return true;
        return economy.depositPlayer(player(player), amount).transactionSuccess();
    }
}
