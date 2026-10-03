package me.imu.imuschallenges.Managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

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
                    onLifetimePointsChanged(player, pointType, playerPoints.getLifetimePoints());
            }
            else
            {
                // Update existing record
                TablePlayerPoints playerPoints = existingPoints.get(0);
                playerPoints.setPoints(playerPoints.getPoints() + points);
                playerPoints.setLifetimePoints(playerPoints.getLifetimePoints() + lifetimeGain);
                playerPointsDao.update(playerPoints);
                if (lifetimeGain > 0)
                    onLifetimePointsChanged(player, pointType, playerPoints.getLifetimePoints());
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Runs on the write thread after a player earned challenge points: updates the scoreboard and reports when
     * they passed the leader.
     */
    private void onLifetimePointsChanged(TablePlayers player, TablePointType pointType, int lifetime) throws SQLException
    {
        if (!pointType.getPointTypeName().equalsIgnoreCase(POINT_TYPE.CHALLENGE_POINT.toString()))
            return;

        String playerName = player.getPlayer_name();
        if (_main.isEnabled())
            Bukkit.getScheduler().runTask(_main, () -> ManagerPointsScoreboard.getInstance().setPoints(playerName, lifetime));

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
     * @param callback runs on the main thread with the player's lifetime challenge points
     */
    public void getLifetimePointsAsync(Player player, IntConsumer callback)
    {
        _writeExecutor.execute(() ->
        {
            try
            {
                TablePointType pointType = _managerPointType.getPointTypeByName(POINT_TYPE.CHALLENGE_POINT.toString());
                int lifetime = 0;
                if (pointType != null)
                {
                    List<TablePlayerPoints> points = findPoints(_managerTablePlayers.findOrCreatePlayer(player), pointType);
                    if (!points.isEmpty())
                        lifetime = points.get(0).getLifetimePoints();
                }
                final int result = lifetime;
                if (_main.isEnabled())
                    Bukkit.getScheduler().runTask(_main, () -> callback.accept(result));
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    /**
     * Sets a player's challenge points, or their lifetime points, to an exact amount. Setting the spendable points
     * above the lifetime points raises those too, since nobody can have more than they ever earned.
     *
     * @param lifetime true sets the lifetime points instead of the spendable ones
     * @param callback runs on the main thread with the player's name as stored, or null when no such player exists
     */
    public void setPointsAsync(String playerName, int amount, boolean lifetime, Consumer<String> callback)
    {
        _writeExecutor.execute(() ->
        {
            String resolvedName = null;
            int newLifetime = 0;
            try
            {
                TablePlayers tablePlayer = _managerTablePlayers.findByName(playerName);
                TablePointType pointType = _managerPointType.getPointTypeByName(POINT_TYPE.CHALLENGE_POINT.toString());
                if (tablePlayer != null && pointType != null)
                {
                    resolvedName = tablePlayer.getPlayer_name();
                    List<TablePlayerPoints> existing = findPoints(tablePlayer, pointType);
                    TablePlayerPoints playerPoints = existing.isEmpty() ? new TablePlayerPoints() : existing.get(0);
                    if (existing.isEmpty())
                    {
                        playerPoints.setPlayer(tablePlayer);
                        playerPoints.setPointType(pointType);
                    }

                    if (lifetime)
                    {
                        playerPoints.setLifetimePoints(amount);
                    }
                    else
                    {
                        playerPoints.setPoints(amount);
                        playerPoints.setLifetimePoints(Math.max(playerPoints.getLifetimePoints(), amount));
                    }
                    newLifetime = playerPoints.getLifetimePoints();

                    if (existing.isEmpty())
                        playerPointsDao.create(playerPoints);
                    else
                        playerPointsDao.update(playerPoints);

                    // The leader is looked up again on the next points earned
                    _leaderLoaded = false;
                    _leaderPlayerId = -1;
                    _leaderName = null;
                    _leaderLifetime = 0;
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            final String name = resolvedName;
            final int scoreboardPoints = newLifetime;
            if (_main.isEnabled())
                Bukkit.getScheduler().runTask(_main, () ->
                {
                    if (name != null)
                        ManagerPointsScoreboard.getInstance().setPoints(name, scoreboardPoints);
                    callback.accept(name);
                });
        });
    }

    /**
     * Sets challenge points and lifetime points back to zero, for one player or for everyone.
     *
     * @param playerName null resets everyone
     * @param callback   runs on the main thread with the number of rows reset and the player's name as stored,
     *                   which is null when no such player exists
     */
    public void resetPointsAsync(String playerName, BiConsumer<Integer, String> callback)
    {
        _writeExecutor.execute(() ->
        {
            int rows = 0;
            String resolvedName = null;
            try
            {
                TablePointType pointType = _managerPointType.getPointTypeByName(POINT_TYPE.CHALLENGE_POINT.toString());
                TablePlayers tablePlayer = playerName == null ? null : _managerTablePlayers.findByName(playerName);
                if (tablePlayer != null)
                    resolvedName = tablePlayer.getPlayer_name();

                if (pointType != null && (playerName == null || tablePlayer != null))
                {
                    UpdateBuilder<TablePlayerPoints, Integer> update = playerPointsDao.updateBuilder();
                    update.updateColumnValue("points", 0).updateColumnValue("lifetime_points", 0);
                    Where<TablePlayerPoints, Integer> where = update.where().eq("point_type_id", pointType.getId());
                    if (tablePlayer != null)
                        where.and().eq("player_id", tablePlayer.getId());
                    rows = update.update();
                }

                // The leader is looked up again on the next points earned
                _leaderLoaded = false;
                _leaderPlayerId = -1;
                _leaderName = null;
                _leaderLifetime = 0;
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            final int resetRows = rows;
            final String name = resolvedName;
            if (_main.isEnabled())
                Bukkit.getScheduler().runTask(_main, () ->
                {
                    if (playerName == null)
                        ManagerPointsScoreboard.getInstance().resetAll();
                    else if (name != null)
                        ManagerPointsScoreboard.getInstance().setPoints(name, 0);
                    callback.accept(resetRows, name);
                });
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
