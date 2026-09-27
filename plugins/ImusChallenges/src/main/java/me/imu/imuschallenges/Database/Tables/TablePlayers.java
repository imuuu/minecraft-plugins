package me.imu.imuschallenges.Database.Tables;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "players")
public class TablePlayers
{
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(canBeNull = false)
    private String player_name;
    @DatabaseField(canBeNull = false)
    private String player_uuid;

    public TablePlayers() {}

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getPlayer_name()
    {
        return player_name;
    }

    public void setPlayer_name(String player_name)
    {
        this.player_name = player_name;
    }

    public String getPlayer_uuid()
    {
        return player_uuid;
    }

    public void setPlayer_uuid(String player_uuid)
    {
        this.player_uuid = player_uuid;
    }
}
