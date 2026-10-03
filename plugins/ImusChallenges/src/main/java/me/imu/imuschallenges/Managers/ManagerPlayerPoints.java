package me.imu.imuschallenges.Managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;
import me.imu.imuschallenges.Database.Tables.TablePlayerPoints;
import me.imu.imuschallenges.Database.Tables.TablePlayers;
import me.imu.imuschallenges.Database.Tables.TablePointType;
import me.imu.imuschallenges.Enums.POINT_TYPE;
import me.imu.imuschallenges.ImusChallenges;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ManagerPlayerPoints
{
    private static ManagerPlayerPoints _instance;

    public static ManagerPlayerPoints getInstance()
    {
        return _instance;
    }

    private final ManagerPointType _managerPointType = ManagerPointType.getInstance();
    private final ManagerPlayers _managerTablePlayers = ManagerPlayers.getInstance();

    private final ImusChallenges _main;
    private final Dao<TablePlayerPoints, Integer> playerPointsDao;

    // Every point write runs on this one thread, so a read-modify-write can't overlap another one
    private final ExecutorService _writeExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "ImusChallenges-points"));

    // Who has the most lifetime challenge points; only touched on the write thread
    private boolean _leaderLoaded = false;
    private int _leaderPlayerId = -1;
    private String _leaderName = null;
    private int _leaderLifetime = 0;

    public ManagerPlayerPoints(ImusChallenges main)
    {
        _main = main;
        _instance = this;
        try
        {
            playerPointsDao = DaoManager.createDao(_main.getSource(), TablePlayerPoints.class);
            TableUtils.createTableIfNotExists(_main.getSource(), TablePlayerPoints.class);
        } catch (SQLException e)
        {
            throw new RuntimeException("Could not create DAO or table for TablePlayerPoints", e);
        }
    }

    public void shutdown()
    {
        _writeExecutor.shutdown();
        try
        {
            if (!_writeExecutor.awaitTermination(10, TimeUnit.SECONDS))
                _main.getLogger().warning("Point writes did not finish in 10 seconds");
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    private List<TablePlayerPoints> findPoints(TablePlayers player, TablePointType pointType) throws SQLException
    {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("player_id", player.getId());
        fieldValues.put("point_type_id", pointType.getId());
        return playerPointsDao.queryForFieldValues(fieldValues);
    }

    private void addPoints(TablePlayers player, TablePointType pointType, int points, boolean countLifetime)
    {
        try
        {
            List<TablePlayerPoints> existingPoints = findPoints(player, pointType);
            // Lifetime points count what was earned, so taking points away never lowers them
            int lifetimeGain = countLifetime ? Math.max(0, points) : 0;

            if (existingPoints.isEmpty())
            {
                // Create new record if it doesn't exist
                TablePlayerPoints playerPoints = new TablePlayerPoints();
                playerPoints.setPlayer(player);
                playerPoints.setPointType(pointType);
                playerPoints.setPoints(points);
                playerPoints.setLifetimePoints(lifetimeGain);
                playerPointsDao.create(playerPoints);
                if (lifetimeGain > 0)
                    checkLeader(player, pointType, playerPoints.getLifetimePoints());
            }
            else
            {
                // Update existing record
                TablePlayerPoints playerPoints = existingPoints.get(0);
                playerPoints.setPoints(playerPoints.getPoints() + points);
                playerPoints.setLifetimePoints(playerPoints.getLifetimePoints() + lifetimeGain);
                playerPointsDao.update(playerPoints);
                if (lifetimeGain > 0)
                    checkLeader(player, pointType, playerPoints.getLifetimePoints());
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Runs on the write thread after a player earned challenge points; reports when they passed the leader.
     */
    private void checkLeader(TablePlayers player, TablePointType pointType, int lifetime) throws SQLException
    {
        if (!pointType.getPointTypeName().equalsIgnoreCase(POINT_TYPE.CHALLENGE_POINT.toString()))
            return;

        if (!_leaderLoaded)
        {
            // Read after this gain was saved, so a player who already led stays the leader quietly
            _leaderLoaded = true;
            TablePlayerPoints top = playerPointsDao.queryBuilder()
                    .orderBy("lifetime_points", false)
                    .where().eq("point_type_id", pointType.getId())
                    .queryForFirst();
            if (top != null)
            {
                _leaderPlayerId = top.getPlayer().getId();
                _leaderName = top.getPlayer().getPlayer_name();
                _leaderLifetime = top.getLifetimePoints();
            }
        }

        if (player.getId() == _leaderPlayerId)
        {
            _leaderLifetime = lifetime;
            return;
        }
        if (lifetime <= _leaderLifetime)
            return;

        String previousLeader = _leaderName;
        _leaderPlayerId = player.getId();
        _leaderName = player.getPlayer_name();
        _leaderLifetime = lifetime;

        String newLeader = _leaderName;
        if (_main.isEnabled())
            Bukkit.getScheduler().runTask(_main, () -> ManagerChallengeLeader.getInstance().onNewLeader(newLeader, lifetime, previousLeader));
    }

    private void addPoints(Player player, String pointType, double amount, boolean countLifetime) throws SQLException
    {
        int points = (int) amount;
        TablePointType tablePointType = _managerPointType.findOrCreatePointType(pointType);
        TablePlayers tablePlayer = _managerTablePlayers.findOrCreatePlayer(player);
        addPoints(tablePlayer, tablePointType, points, countLifetime);
    }

    public void addPointsAsync(Player player, POINT_TYPE pointType, double amount)
    {
        addPointsAsync(player, pointType.toString(), amount);
    }

    public void addPointsAsync(Player player, String pointType, double amount)
    {
        _writeExecutor.execute(() ->
        {
            try
            {
                addPoints(player, pointType, amount, true);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    /**
     * Gives back points taken by {@link #trySpendPointsAsync} without counting them as earned again.
     */
    public void refundPointsAsync(Player player, POINT_TYPE pointType, int amount)
    {
        _writeExecutor.execute(() ->
        {
            try
            {
                addPoints(player, pointType.toString(), amount, false);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    /**
     * Takes points only if the player has at least that many, checked against the database.
     *
     * @param callback runs on the main thread with true when the points were taken
     */
    public void trySpendPointsAsync(Player player, POINT_TYPE pointType, int cost, Consumer<Boolean> callback)
    {
        _writeExecutor.execute(() ->
        {
            boolean spent = false;
            try
            {
                spent = trySpendPoints(player, pointType.toString(), cost);
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            final boolean result = spent;
            if (_main.isEnabled())
                Bukkit.getScheduler().runTask(_main, () -> callback.accept(result));
        });
    }

    private boolean trySpendPoints(Player player, String pointType, int cost) throws SQLException
    {
        TablePointType tablePointType = _managerPointType.findOrCreatePointType(pointType);
        if (tablePointType == null)
            return false;

        TablePlayers tablePlayer = _managerTablePlayers.findOrCreatePlayer(player);
        List<TablePlayerPoints> existingPoints = findPoints(tablePlayer, tablePointType);
        if (existingPoints.isEmpty())
            return false;

        TablePlayerPoints playerPoints = existingPoints.get(0);
        if (playerPoints.getPoints() < cost)
            return false;

        playerPoints.setPoints(playerPoints.getPoints() - cost);
        return playerPointsDao.update(playerPoints) == 1;
    }


    private List<TablePlayerPoints> getPoints(TablePlayers player) throws SQLException
    {
        return playerPointsDao.queryForEq("player_id", player.getId());
    }

    public List<TablePlayerPoints> getPoints(Player player) throws SQLException {

        TablePlayers tablePlayer = _managerTablePlayers.findOrCreatePlayer(player);

        return getPoints(tablePlayer);
    }

    public double getPoints(String pointType, Player player) throws SQLException
    {
        List<TablePlayerPoints> pointsList = getPoints(player);

        for (TablePlayerPoints points : pointsList)
        {
            if (points.getPoint_type().getPointTypeName().equalsIgnoreCase(pointType))
            {
                return points.getPoints();
            }
        }
        return 0;
    }

}
