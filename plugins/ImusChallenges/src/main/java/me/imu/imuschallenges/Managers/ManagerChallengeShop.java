package me.imu.imuschallenges.Managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;
import imu.iAPI.Managers.Manager_CommandSender;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.InvUtil;
import me.imu.imuschallenges.CONSTANTS;
import me.imu.imuschallenges.Database.Tables.TablePlayerShopStats;
import me.imu.imuschallenges.Database.Tables.TablePlayers;
import me.imu.imuschallenges.Enums.POINT_TYPE;
import me.imu.imuschallenges.ImusChallenges;
import me.imu.imuschallenges.Interfaces.ShopStatsCallback;
import me.imu.imuschallenges.Inventories.InventoryChallengeShop;
import me.imu.imuschallenges.Factories.ItemFactory;
import me.imu.imuschallenges.Shop.ShopTier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ManagerChallengeShop
{
    private static ManagerChallengeShop _instance;

    public static ManagerChallengeShop getInstance()
    {
        return _instance;
    }

    private final ShopTier _normal;
    private final ShopTier _special;
    private final HashSet<Player> _hasShopOpen = new HashSet<>();

    private Dao<TablePlayerShopStats, Integer> playerShopStatsDao;
    // Slot reads and writes share one thread, so reopening the shop right after buying a slot sees it
    private final ExecutorService _statsExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "ImusChallenges-shop"));

    private Material[] _randomItemCandidates;

    private BukkitTask _reminderTask;
    private long _reminderIntervalMinutes = -1;

    private final File _stateFile;

    private final Cooldowns _cooldowns = new Cooldowns(); // only for its time formatting

    public ManagerChallengeShop()
    {
        _instance = this;
        ImusChallenges main = ImusChallenges.getInstance();
        try
        {
            InitSQLData();
            playerShopStatsDao = DaoManager.createDao(main.getSource(), TablePlayerShopStats.class);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        ConfigurationSection config = main.getConfig().getConfigurationSection("shop");
        _normal = new ShopTier("normal", false, CONSTANTS.NORMAL_SLOT_COLUMNS * CONSTANTS.NORMAL_SLOT_ROWS, config.getConfigurationSection("normal"));
        _special = new ShopTier("special", true, CONSTANTS.SPECIAL_SLOTS, config.getConfigurationSection("special"));

        loadRandomItemCandidates(config);

        _stateFile = new File(main.getDataFolder(), "shop_state.yml");
        loadState();
        scheduleItemGeneration();
        scheduleReminders(config.getLong("reminder-interval-minutes"));
    }

    /**
     * Applies config.yml again after {@link ImusChallenges#reloadSettings} re-read it. What changes when is
     * described in {@link ShopTier#applyConfig}; the reminder interval and slot prices apply at once.
     */
    public void reload()
    {
        ImusChallenges main = ImusChallenges.getInstance();
        ConfigurationSection config = main.getConfig().getConfigurationSection("shop");
        if (config == null)
        {
            main.getLogger().severe("config.yml has no shop section, the shop keeps its old settings");
            return;
        }

        _normal.applyConfig(config.getConfigurationSection("normal"));
        _special.applyConfig(config.getConfigurationSection("special"));
        loadRandomItemCandidates(config);
        scheduleReminders(config.getLong("reminder-interval-minutes"));
        saveState();
    }

    private void loadRandomItemCandidates(ConfigurationSection config)
    {
        List<Pattern> excluded = new ArrayList<>();
        for (String name : config.getStringList("random-item-excluded"))
        {
            excluded.add(Pattern.compile(Pattern.quote(name.toUpperCase(Locale.ROOT)).replace("*", "\\E.*\\Q")));
        }
        _randomItemCandidates = Arrays.stream(Material.values())
                .filter(m -> m.isItem() && !m.isAir() && !m.isLegacy())
                .filter(m -> excluded.stream().noneMatch(p -> p.matcher(m.name()).matches()))
                .toArray(Material[]::new);
    }

    public void shutdown()
    {
        saveState();
        _statsExecutor.shutdown();
        try
        {
            if (!_statsExecutor.awaitTermination(10, TimeUnit.SECONDS))
                ImusChallenges.getInstance().getLogger().warning("Shop slot writes did not finish in 10 seconds");
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    public void openShop(Player player)
    {
        new InventoryChallengeShop().open(player);
    }

    public void addPlayerToShop(Player player)
    {
        _hasShopOpen.add(player);
    }

    public void removePlayerFromShop(Player player)
    {
        _hasShopOpen.remove(player);
    }

    public ArrayList<Player> closeAllShops()
    {
        // closeInventory() calls back into removePlayerFromShop, so iterate a copy
        ArrayList<Player> players = new ArrayList<>(_hasShopOpen);
        _hasShopOpen.clear();
        for (Player player : players)
        {
            player.closeInventory();
        }
        return players;
    }

    public ShopTier getNormal()
    {
        return _normal;
    }

    public ShopTier getSpecial()
    {
        return _special;
    }

    public String timeLeft(ShopTier tier)
    {
        return _cooldowns.formatTime(tier.getMillisUntilRefresh());
    }

    private ItemStack getRandomItem()
    {
        Material material = Material.DIRT;
        for (int attempt = 0; attempt < 1000; attempt++)
        {
            Material candidate = _randomItemCandidates[ThreadLocalRandom.current().nextInt(_randomItemCandidates.length)];
            if (!ManagerCCollectMaterial.getInstance().isExcludedMaterial(candidate))
            {
                material = candidate;
                break;
            }
        }

        ItemStack stack = new ItemStack(material);
        stack.setAmount(Math.min(ThreadLocalRandom.current().nextInt(1, 65), stack.getMaxStackSize()));
        return stack;
    }

    private void scheduleItemGeneration()
    {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(ImusChallenges.getInstance(), () ->
        {
            boolean changed = false;
            for (ShopTier tier : List.of(_special, _normal))
            {
                if (!tier.isRefreshDue())
                    continue;

                broadcastShopUpdate(tier);
                closeShops(tier);
                tier.generate(this::getRandomItem);
                changed = true;
            }
            if (changed)
                saveState();
        }, 0L, 20);
    }

    // Reminders ======================================================================================================

    private void scheduleReminders(long intervalMinutes)
    {
        if (intervalMinutes == _reminderIntervalMinutes)
            return;

        _reminderIntervalMinutes = intervalMinutes;
        if (_reminderTask != null)
        {
            _reminderTask.cancel();
            _reminderTask = null;
        }
        if (intervalMinutes <= 0)
            return;

        long intervalTicks = intervalMinutes * 60 * 20;
        _reminderTask = Bukkit.getScheduler().runTaskTimer(ImusChallenges.getInstance(), () ->
        {
            for (Player player : Bukkit.getOnlinePlayers())
            {
                if (player.hasPermission(CONSTANTS.PERM_CHALLENGE_SHOP) && !_hasShopOpen.contains(player))
                    remindIfAffordable(player);
            }
        }, intervalTicks, intervalTicks);
    }

    /**
     * Tells the player about their unspent points, but only when one of the items they can see in the shop is
     * within their budget.
     */
    private void remindIfAffordable(Player player)
    {
        // Tier contents are only touched on the main thread, so take the prices along
        List<List<Integer>> tierCosts = new ArrayList<>();
        List<ShopTier> tiers = new ArrayList<>();
        for (ShopTier tier : List.of(_normal, _special))
        {
            if (tier.hasBought(player.getUniqueId()))
                continue;
            tiers.add(tier);
            tierCosts.add(new ArrayList<>(tier.getCosts()));
        }
        if (tiers.isEmpty())
            return;

        _statsExecutor.execute(() ->
        {
            try
            {
                double points = ManagerPlayerPoints.getInstance().getPoints(POINT_TYPE.CHALLENGE_POINT.toString(), player);
                if (points <= 0)
                    return;

                TablePlayerShopStats stats = getShopStatsByPlayerId(player);
                boolean canAfford = false;
                for (int t = 0; t < tiers.size() && !canAfford; t++)
                {
                    List<Integer> costs = tierCosts.get(t);
                    int unlocked = Math.min(tiers.get(t).getBoughtSlots(stats), costs.size());
                    for (int i = 0; i < unlocked; i++)
                    {
                        if (costs.get(i) <= points)
                        {
                            canAfford = true;
                            break;
                        }
                    }
                }
                if (!canAfford || !ImusChallenges.getInstance().isEnabled())
                    return;

                int shownPoints = (int) points;
                Bukkit.getScheduler().runTask(ImusChallenges.getInstance(), () -> sendReminder(player, shownPoints));
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    private void sendReminder(Player player, int points)
    {
        if (!player.isOnline())
            return;

        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();
        Component link = legacy.deserialize("&a&l[Open shop]")
                .clickEvent(ClickEvent.runCommand("/ic shop"))
                .hoverEvent(HoverEvent.showText(legacy.deserialize("&9Click to open the &6Challenge Shop")));
        player.sendMessage(legacy.deserialize("&9You have &2" + points + " &9unspent &6challenge points&9! ").append(link));
    }

    private void broadcastShopUpdate(ShopTier tier)
    {
        for (Player player : Bukkit.getServer().getOnlinePlayers())
        {
            if (!player.hasPermission(CONSTANTS.PERM_BROADCAST_CHALLENGE_SHOP_UPDATE)) continue;

            if (tier.isSpecial())
            {
                player.sendMessage(Metods.msgC("&6Special &9Challenge shop has been updated!"));
            }
            else
            {
                player.sendMessage(Metods.msgC("&2Normal &9Challenge shop has been updated!"));
            }
        }
    }

    private void closeShops(ShopTier tier)
    {
        ArrayList<Player> players = closeAllShops();
        for (Player player : players)
        {
            if (!player.hasPermission(CONSTANTS.PERM_BROADCAST_CHALLENGE_SHOP_UPDATE))
            {
                player.sendMessage(Metods.msgC("&9Challenge shop has been updated!"));
            }
            if (tier.isSpecial())
            {
                player.sendMessage(Metods.msgC("&9You can now buy &2new &6special &9items!"));
            }
            else
            {
                player.sendMessage(Metods.msgC("&9You can now buy &2new &9items!"));
            }
        }
    }

    /**
     * Mystery items are handed out by GadgedMenu commands; without it buying one would only lose the points.
     */
    public boolean canDeliver(ItemStack itemStack)
    {
        if (ItemFactory.isMysteryBox(itemStack) || ItemFactory.isMysteryDust(itemStack))
            return Bukkit.getPluginManager().isPluginEnabled(ShopTier.GADGED_MENU);
        return true;
    }

    public void onGiveItemStack(Player player, ItemStack itemStack)
    {
        if (itemStack == null)
            return;

        if (ItemFactory.isMysteryBox(itemStack))
        {
            int quality = ItemFactory.getMysteryBoxQuality(itemStack);
            final String cmd = "gmysterybox give " + player.getName() + " " + itemStack.getAmount() + " " + quality;
            Manager_CommandSender.getInstance().executeCommandAsConsole(cmd);
            return;
        }

        if (ItemFactory.isMysteryDust(itemStack))
        {
            final String cmd = "mysterydust add " + player.getName() + " " + ItemFactory.getMysteryDustAmount(itemStack);
            Manager_CommandSender.getInstance().executeCommandAsConsole(cmd);
            return;
        }
        InvUtil.AddItemToInventoryOrDrop(player, itemStack);
    }

    // State file =====================================================================================================

    private void loadState()
    {
        if (!_stateFile.exists())
            return;

        YamlConfiguration state = YamlConfiguration.loadConfiguration(_stateFile);
        _normal.load(state.getConfigurationSection(_normal.getName()));
        _special.load(state.getConfigurationSection(_special.getName()));
    }

    public void saveState()
    {
        YamlConfiguration state = new YamlConfiguration();
        _normal.save(state.createSection(_normal.getName()));
        _special.save(state.createSection(_special.getName()));
        try
        {
            state.save(_stateFile);
        } catch (IOException e)
        {
            ImusChallenges.getInstance().getLogger().severe("Could not save " + _stateFile.getName() + ": " + e.getMessage());
        }
    }

    // SQL ============================================================================================================

    private void InitSQLData() throws SQLException
    {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() throws SQLException
    {
        TableUtils.createTableIfNotExists(ImusChallenges.getInstance().getSource(), TablePlayerShopStats.class);
    }

    private TablePlayerShopStats getShopStatsByPlayerId(Player player) throws SQLException
    {
        TablePlayers tablePlayer = ManagerPlayers.getInstance().findOrCreatePlayer(player);

        List<TablePlayerShopStats> stats = playerShopStatsDao.queryForEq("player_id", tablePlayer.getId());
        if (!stats.isEmpty())
        {
            return stats.get(0);
        }
        else
        {
            TablePlayerShopStats newShopStats = new TablePlayerShopStats();
            newShopStats.setPlayer(tablePlayer);
            newShopStats.setBought_normal_slots(_normal.getDefaultSlots());
            newShopStats.setBought_special_slots(_special.getDefaultSlots());
            playerShopStatsDao.create(newShopStats);
            return newShopStats;
        }
    }

    public void addSlotsToPlayerShopAsync(Player player, int addSlotAmount, ShopTier tier)
    {
        _statsExecutor.execute(() ->
        {
            try
            {
                TablePlayerShopStats currentStats = getShopStatsByPlayerId(player);
                tier.addBoughtSlots(currentStats, addSlotAmount);
                playerShopStatsDao.update(currentStats);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    /**
     * Asynchronously gets the shop stats for a player and executes a callback with the retrieved stats.
     *
     * @param player   The player whose stats are to be fetched.
     * @param callback The callback to be executed with the fetched stats main thread.
     */
    public void getShopStatsAsync(Player player, ShopStatsCallback callback)
    {
        _statsExecutor.execute(() ->
        {
            try
            {
                TablePlayerShopStats shopStats = getShopStatsByPlayerId(player);
                if (ImusChallenges.getInstance().isEnabled())
                    Bukkit.getScheduler().runTask(ImusChallenges.getInstance(), () -> callback.onShopStatsRetrieved(shopStats));
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

}
