package me.imu.imusminiquests.Managers;

import imu.iAPI.Managers.Manager_Vault;
import me.imu.imusminiquests.ImusMiniQuests;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Money rewards that grow with the server's economy. Every so often it adds up the balances of
 * every player who has ever joined, online or not, skipping ops (and, if set, players who haven't
 * played for a while). A percent money reward is then a share of the total, the average or the
 * median of those balances, picked with money.basis.
 * <p>
 * Offline balances can be slow to read (Essentials loads each player's file), so the survey reads
 * a few players per tick and swaps in the new numbers when it is done.
 */
public class ManagerEconomy
{
    public enum BASIS {TOTAL, AVERAGE, MEDIAN}

    private static final int PLAYERS_PER_TICK = 25;

    private final ImusMiniQuests _plugin;
    private BukkitTask _refreshTimer;
    private BukkitTask _survey;

    private double _total;
    private double _average;
    private double _median;
    private int _counted;
    private long _surveyedAt;

    public ManagerEconomy(ImusMiniQuests plugin)
    {
        _plugin = plugin;
    }

    public static boolean isAvailable()
    {
        return Manager_Vault.getEconomy() != null;
    }

    /**
     * Starts the survey timer. Waits a few seconds first so the economy plugin has finished
     * loading.
     */
    public void start()
    {
        stop();
        long minutes = Math.max(1, _plugin.getConfig().getLong("money.refresh-minutes", 30));
        _refreshTimer = Bukkit.getScheduler().runTaskTimer(_plugin, this::survey, 20L * 5, 20L * 60 * minutes);
    }

    public void stop()
    {
        if (_refreshTimer != null) _refreshTimer.cancel();
        if (_survey != null) _survey.cancel();
        _refreshTimer = null;
        _survey = null;
    }

    /**
     * Reads every counted player's balance, a few per tick. Does nothing when a survey is
     * already running.
     */
    public void survey()
    {
        Economy economy = Manager_Vault.getEconomy();
        if (economy == null || (_survey != null && !_survey.isCancelled())) return;

        FileConfiguration config = _plugin.getConfig();
        int inactiveDays = config.getInt("money.exclude-inactive-days", 0);
        long cutoff = inactiveDays > 0 ? System.currentTimeMillis() - inactiveDays * 86_400_000L : 0;
        boolean skipOps = config.getBoolean("money.exclude-ops", true);

        OfflinePlayer[] players = Bukkit.getOfflinePlayers();
        List<Double> balances = new ArrayList<>();
        int[] next = {0};

        _survey = Bukkit.getScheduler().runTaskTimer(_plugin, () ->
        {
            int end = Math.min(players.length, next[0] + PLAYERS_PER_TICK);
            for (; next[0] < end; next[0]++)
            {
                OfflinePlayer player = players[next[0]];
                if (skipOps && player.isOp()) continue;
                if (cutoff > 0 && !player.isOnline() && player.getLastSeen() < cutoff) continue;
                if (!economy.hasAccount(player)) continue;

                double balance = economy.getBalance(player);
                if (balance > 0) balances.add(balance);
            }

            if (next[0] >= players.length)
            {
                finish(balances);
                _survey.cancel();
            }
        }, 0L, 1L);
    }

    private void finish(List<Double> balances)
    {
        double[] sorted = balances.stream().mapToDouble(Double::doubleValue).sorted().toArray();
        double total = Arrays.stream(sorted).sum();
        int n = sorted.length;

        _total = total;
        _counted = n;
        _average = n == 0 ? 0 : total / n;
        _median = n == 0 ? 0 : (n % 2 == 1 ? sorted[n / 2] : (sorted[n / 2 - 1] + sorted[n / 2]) / 2);
        _surveyedAt = System.currentTimeMillis();
        _plugin.getLogger().info("Money rewards: counted " + n + " players, total " + format(_total)
                + ", average " + format(_average) + ", median " + format(_median));
    }

    public BASIS getBasis()
    {
        try
        {
            return BASIS.valueOf(_plugin.getConfig().getString("money.basis", "MEDIAN").toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException e)
        {
            return BASIS.MEDIAN;
        }
    }

    /** The amount percent rewards are a share of, from the last survey. 0 before the first one. */
    public double getBasisAmount()
    {
        return switch (getBasis())
        {
            case TOTAL -> _total;
            case AVERAGE -> _average;
            case MEDIAN -> _median;
        };
    }

    /**
     * Keeps a money reward between money.min and money.max (0 = no maximum).
     */
    public double clamp(double amount)
    {
        FileConfiguration config = _plugin.getConfig();
        double min = Math.max(0, config.getDouble("money.min", 50));
        double max = config.getDouble("money.max", 5000);
        amount = Math.max(min, amount);
        if (max > 0) amount = Math.min(max, amount);
        return Math.round(amount * 100) / 100.0;
    }

    public static String format(double amount)
    {
        Economy economy = Manager_Vault.getEconomy();
        return economy != null ? economy.format(amount) : String.format("%.2f", amount);
    }

    public static void deposit(Player player, double amount)
    {
        Manager_Vault.giveMoney(player, amount);
    }

    public double getTotal() {return _total;}

    public double getAverage() {return _average;}

    public double getMedian() {return _median;}

    public int getCounted() {return _counted;}

    /** When the last survey finished, 0 when none has. */
    public long getSurveyedAt() {return _surveyedAt;}
}
