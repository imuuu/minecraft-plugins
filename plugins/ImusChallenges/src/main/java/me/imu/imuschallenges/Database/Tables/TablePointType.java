package me.imu.imuschallenges.Database.Tables;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "point_types")
public class TablePointType
{
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, unique = true)
    private String pointTypeName;

    public TablePointType()
    {

    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getPointTypeName()
    {
        return pointTypeName;
    }

    public void setPointTypeName(String pointTypeName)
    {
        this.pointTypeName = pointTypeName;
    }
}
