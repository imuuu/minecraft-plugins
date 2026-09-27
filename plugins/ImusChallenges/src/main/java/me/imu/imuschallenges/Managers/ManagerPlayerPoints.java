package me.imu.imuschallenges.Managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;
import me.imu.imuschallenges.Database.Tables.TablePlayerPoints;
import me.imu.imuschallenges.Database.Tables.TablePlayers;
import me.imu.imuschallenges.Database.Tables.TablePointType;
import me.imu.imuschallenges.Enums.POINT_TYPE;
import me.imu.imuschallenges.ImusChallenges;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private void addPoints(TablePlayers player, TablePointType pointType, int points)
    {
        try
        {
            // Query for existing record
            Map<String, Object> fieldValues = new HashMap<>();
            fieldValues.put("player_id", player.getId());
            fieldValues.put("point_type_id", pointType.getId());
            List<TablePlayerPoints> existingPoints = playerPointsDao.queryForFieldValues(fieldValues);

            if (existingPoints.isEmpty())
            {
                // Create new record if it doesn't exist
                TablePlayerPoints playerPoints = new TablePlayerPoints();
                playerPoints.setPlayer(player);
                playerPoints.setPointType(pointType);
                playerPoints.setPoints(points);
                playerPoints.setLifetimePoints(points); // Adjust according to your requirement
                playerPointsDao.create(playerPoints);
            }
            else
            {
                // Update existing record
                TablePlayerPoints playerPoints = existingPoints.get(0);
                playerPoints.setPoints(playerPoints.getPoints() + points);
                playerPoints.setLifetimePoints(playerPoints.getLifetimePoints() + points); // Adjust according to your requirement
                playerPointsDao.update(playerPoints);
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void addPoints(Player player, String pointType, double amount) throws SQLException
    {
        int points = (int) amount;
        TablePointType tablePointType = _managerPointType.findOrCreatePointType(pointType);
        TablePlayers tablePlayer = _managerTablePlayers.findOrCreatePlayer(player);
        addPoints(tablePlayer, tablePointType, points);
    }

    public void addPointsAsync(Player player, POINT_TYPE pointType, double amount)
    {
        addPointsAsync(player, pointType.toString(), amount);
    }

    public void addPointsAsync(Player player, String pointType, double amount)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    addPoints(player, pointType, amount);
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(_main);
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
