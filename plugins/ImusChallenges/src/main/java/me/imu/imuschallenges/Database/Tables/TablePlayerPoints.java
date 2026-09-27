package me.imu.imuschallenges.Database.Tables;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "player_points")
public class TablePlayerPoints
{
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, foreign = true, columnName = "player_id", foreignAutoRefresh = true)
    private TablePlayers player;

    @DatabaseField(canBeNull = false, foreign = true, columnName = "point_type_id", foreignAutoRefresh = true)
    private TablePointType point_type;

    @DatabaseField(canBeNull = false)
    private double points;

    @DatabaseField(canBeNull = false)
    private int lifetime_points;


    public TablePlayerPoints() {    }

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

    public double getPoints()
    {
        return points;
    }

    public void setPoints(double points)
    {
        if(points < 0)
            this.points = 0;

        this.points = points;
    }
    public void addPoints(int pointsToAdd) {
        this.points += pointsToAdd;
        this.lifetime_points += pointsToAdd;

    }

    public void removePoints(int pointsToRemove) {
        this.points -= pointsToRemove;

    }

    public int getLifetimePoints()
    {
        return lifetime_points;
    }

    public void setLifetimePoints(int lifetime_points)
    {
        this.lifetime_points = lifetime_points;
    }

    public void addLifetimePoints(int lifetime_pointsToAdd) {
        this.lifetime_points += lifetime_pointsToAdd;
    }

    public void removeLifetimePoints(int lifetime_pointsToRemove) {
        this.lifetime_points -= lifetime_pointsToRemove;
    }

    public TablePointType getPoint_type()
    {
        return point_type;
    }

    public void setPointType(TablePointType pointType)
    {
        point_type = pointType;
    }


}
