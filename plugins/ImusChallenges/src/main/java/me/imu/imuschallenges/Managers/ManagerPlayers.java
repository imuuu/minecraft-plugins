package me.imu.imuschallenges.Managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;
import me.imu.imuschallenges.Database.Tables.TablePlayers;
import me.imu.imuschallenges.ImusChallenges;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;

public class ManagerPlayers
{
    private ImusChallenges _main = ImusChallenges.getInstance();
    private static ManagerPlayers _instance;

    public static ManagerPlayers getInstance() {return _instance;}
    private final Dao<TablePlayers, Integer> _tablePlayersDao;

    public ManagerPlayers()
    {
        _instance = this;
        createTables();
        try
        {
            _tablePlayersDao = DaoManager.createDao(_main.getSource(), TablePlayers.class);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Could not create tablePlayersDao");
        }

    }

    private void createTables()
    {
        try
        {
            TableUtils.createTableIfNotExists(_main.getSource(), TablePlayers.class);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            Bukkit.getLogger().info("[ImusChallenges] Tables created!");
        }
    }

    public TablePlayers getPlayer(Player player) throws SQLException
    {
        List<TablePlayers> players = _tablePlayersDao.queryForEq("player_uuid", player.getUniqueId().toString());
        if (!players.isEmpty())
        {
            return players.get(0);
        }
        return null;
    }
    public TablePlayers findOrCreatePlayer(Player player) throws SQLException
    {
        List<TablePlayers> players = _tablePlayersDao.queryForEq("player_uuid", player.getUniqueId().toString());
        if (!players.isEmpty())
        {
            return players.get(0);
        }

        // If not found, create a new record
        TablePlayers newPlayer = new TablePlayers();
        newPlayer.setPlayer_name(player.getName());
        newPlayer.setPlayer_uuid(player.getUniqueId().toString());
        _tablePlayersDao.create(newPlayer);
        return newPlayer;
    }
}
