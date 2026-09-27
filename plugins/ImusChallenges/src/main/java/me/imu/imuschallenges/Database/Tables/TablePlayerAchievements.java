package me.imu.imuschallenges.Database.Tables;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = "player_achievements")
public class TablePlayerAchievements
{
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(canBeNull = false, foreign = true, columnName = "player_id")
    private TablePlayers player;

    @DatabaseField(canBeNull = false)
    private String achievement_name;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING,
            format = "yyyy-MM-dd HH:mm:ss")
    private Date collection_time;

    public TablePlayerAchievements() {    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public TablePlayers getPlayer()
    {
        return player;
    }

    public void setPlayer(TablePlayers player) {
        this.player = player;
    }

    public String getAchievement_name()
    {
        return achievement_name;
    }

    public void setAchievement_name(String achievement_name)
    {
        this.achievement_name = achievement_name;
    }

    public Date getCollection_time()
    {
        return collection_time;
    }

    public void setCollection_time(Date collection_time)
    {
        this.collection_time = collection_time;
    }
}
