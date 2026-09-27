package me.imu.imuschallenges.Database.Tables;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
@DatabaseTable(tableName = "collected_excluded_materials")
public class TableExludedCollectMaterials
{
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(canBeNull = false, unique = true)
    private String material;

    public TableExludedCollectMaterials() {}

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getMaterial()
    {
        return material;
    }

    public void setMaterial(String material)
    {
        this.material = material;
    }
}
