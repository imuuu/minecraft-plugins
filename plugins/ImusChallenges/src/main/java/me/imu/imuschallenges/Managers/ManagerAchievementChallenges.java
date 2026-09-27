package me.imu.imuschallenges.Managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;
import imu.iAPI.Other.Metods;
import me.imu.imuschallenges.CONSTANTS;
import me.imu.imuschallenges.Database.Tables.TablePlayerAchievements;
import me.imu.imuschallenges.Database.Tables.TablePlayers;
import me.imu.imuschallenges.Enums.POINT_TYPE;
import me.imu.imuschallenges.ImusChallenges;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ManagerAchievementChallenges implements Listener
{
    private ImusChallenges _main = ImusChallenges.getInstance();

    private static ManagerPlayers _managerTablePlayers = ManagerPlayers.getInstance();
    private Map<UUID, Set<String>> _playerCompletedAdvancements;
    private Set<String> _globalCompletedAdvancements;

    private final ConcurrentLinkedQueue<PlayerAdvancementPair> buffer = new ConcurrentLinkedQueue<>();
    private final BukkitScheduler scheduler = Bukkit.getScheduler();

    private final Dao<TablePlayerAchievements, Integer> _tablePlayerAchievementsDao;

    public ManagerAchievementChallenges()
    {
        _playerCompletedAdvancements = new HashMap<>();
        _globalCompletedAdvancements = new HashSet<>();
        createTables();
        startBufferProcessor();

        try
        {
            _tablePlayerAchievementsDao = DaoManager.createDao(_main.getSource(), TablePlayerAchievements.class);
        } catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Could not create tablePlayersDao");
        }

    }

    private static class PlayerAdvancementPair
    {
        final Player player;
        final String advancementKey;

        PlayerAdvancementPair(Player player, String advancementKey)
        {
            this.player = player;
            this.advancementKey = advancementKey;
        }
    }

    private void createTables()
    {
        try
        {
            TableUtils.createTableIfNotExists(_main.getSource(), TablePlayerAchievements.class);

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            Bukkit.getLogger().info("[ImusChallenges] Tables created!");
            loadGlobalCompletedAdvancements();
        }
    }

    private void loadGlobalCompletedAdvancements()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // Get the DAO for TablePlayerAchievements
                    Dao<TablePlayerAchievements, Integer> dao = _tablePlayerAchievementsDao;

                    // Create a query builder
                    QueryBuilder<TablePlayerAchievements, Integer> queryBuilder = dao.queryBuilder();

                    // Select the distinct achievement names
                    queryBuilder.selectColumns("achievement_name").distinct();

                    // Execute the query and process the results
                    List<TablePlayerAchievements> results = queryBuilder.query();
                    for (TablePlayerAchievements achievement : results)
                    {
                        _globalCompletedAdvancements.add(achievement.getAchievement_name());
                    }
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(_main);
    }

    // In ManagerAchievementChallenges class

    private void loadPlayerAchievements(Player player)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Set<String> playerAchievements = new HashSet<>();
                try
                {
                    // Get the DAO for TablePlayerAchievements
                    Dao<TablePlayerAchievements, Integer> dao = _tablePlayerAchievementsDao;

                    // Create a query builder
                    QueryBuilder<TablePlayerAchievements, Integer> queryBuilder = dao.queryBuilder();

                    TablePlayers tablePlayer = _managerTablePlayers.findOrCreatePlayer(player);
                    // Build the query
                    queryBuilder.where().eq("player_id", tablePlayer.getId());
                    queryBuilder.selectColumns("achievement_name");

                    // Execute the query
                    List<TablePlayerAchievements> results = queryBuilder.query();
                    for (TablePlayerAchievements achievement : results)
                    {
                        playerAchievements.add(achievement.getAchievement_name());
                    }
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    Bukkit.getScheduler().runTask(_main, () -> _playerCompletedAdvancements.put(player.getUniqueId(), playerAchievements));
                }
            }
        }.runTaskAsynchronously(_main);
    }

    private void startBufferProcessor()
    {
        scheduler.runTaskTimerAsynchronously(_main, () ->
        {
            while (!buffer.isEmpty())
            {
                PlayerAdvancementPair pair = buffer.poll();
                if (pair != null && !_globalCompletedAdvancements.contains(pair.advancementKey))
                {
                    _globalCompletedAdvancements.add(pair.advancementKey);
                    savePlayerAdvancement(pair.player, pair.advancementKey);
                    informPlayer(pair.player, pair.advancementKey);  // Notify player
                }
            }
        }, 20L, 100L); // Runs every 5 seconds, adjustable
    }

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event)
    {
        Player player = event.getPlayer();
        Advancement advancement = event.getAdvancement();

        if (advancement.getDisplay() == null)
        {
            return;
        }

        String advancementKey = advancement.getDisplay().getTitle();
        int points = ManagerAdvancement.getInstance().getPoints(player, advancement).getPoints();
        if (!_globalCompletedAdvancements.contains(advancementKey))
        {
            if(!player.hasPermission(CONSTANTS.PERM_SERVER_WIDE_ACHIEVEMENT_CHALLENGE))
            {
                return;
            }

            points += CONSTANTS.FIRST_ADVANCEMENT_COMPLITION;
            ManagerPlayerPoints.getInstance().addPointsAsync(player, POINT_TYPE.CHALLENGE_POINT, points);
            buffer.offer(new PlayerAdvancementPair(player, advancementKey));
        }
        else
        {
            ManagerPlayerPoints.getInstance().addPointsAsync(player, POINT_TYPE.CHALLENGE_POINT, points);
        }
    }


    private void markAdvancementAsCompleted(Player player, String advancementKey)
    {
        _playerCompletedAdvancements.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(advancementKey);
    }

    private boolean hasPlayerCompletedAdvancement(Player player, String advancementKey)
    {
        Set<String> completedAdvancements = _playerCompletedAdvancements.get(player.getUniqueId());
        return completedAdvancements != null && completedAdvancements.contains(advancementKey);
    }

    private void savePlayerAdvancement(Player player, String achievementName)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    TablePlayerAchievements tablePlayerAchievements = new TablePlayerAchievements();
                    TablePlayers tablePlayer = _managerTablePlayers.findOrCreatePlayer(player);
                    tablePlayerAchievements.setPlayer(tablePlayer);
                    tablePlayerAchievements.setAchievement_name(achievementName);
                    tablePlayerAchievements.setCollection_time(new Date());
                    _tablePlayerAchievementsDao.create(tablePlayerAchievements);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(_main);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        loadPlayerAchievements(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        UUID playerUuid = event.getPlayer().getUniqueId();
        _playerCompletedAdvancements.remove(playerUuid);
    }

    private void informPlayer(Player player, String achievement)
    {
        if (player.hasPermission(CONSTANTS.PERM_SERVER_WIDE_ACHIEVEMENT_CHALLENGE_BROADCAST))
        {
            Bukkit.getServer().broadcast(Metods.msgC("&9" + player.getName() + " &9is &5FIRST &9to complete the &a["+achievement+"]"), CONSTANTS.PERM_SERVER_WIDE_ACHIEVEMENT_CHALLENGE_BROADCAST);
            return;
        }

       /* String message;
        message = Metods.msgC("&9New &6Achievement &9found! The &3" + achievement);

        player.sendMessage(message);*/
    }
}

