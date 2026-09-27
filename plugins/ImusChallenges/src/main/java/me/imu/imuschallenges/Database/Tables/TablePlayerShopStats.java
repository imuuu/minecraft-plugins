package me.imu.imuschallenges.Database.Tables;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import me.imu.imuschallenges.CONSTANTS;

@DatabaseTable(tableName = "player_shop_stats")
public class TablePlayerShopStats
{
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, foreign = true, columnName = "player_id", foreignAutoRefresh = true)
    private TablePlayers player;

    @DatabaseField(canBeNull = false, defaultValue = "2")
    private int bought_normal_slots;

    @DatabaseField(canBeNull = false, defaultValue = "1")
    private int bought_special_slots;
    public TablePlayerShopStats() {}

    public int getBought_special_slots()
    {
        return bought_special_slots;
    }

    public void setBought_special_slots(int bought_special_slots)
    {
        this.bought_special_slots = bought_special_slots;
    }

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

    public void setPlayer(TablePlayers player)
    {
        this.player = player;
    }

    public int getBought_normal_slots()
    {
        return bought_normal_slots;
    }

    public void setBought_normal_slots(int bought_normal_slots)
    {
        this.bought_normal_slots = bought_normal_slots;
    }
}
