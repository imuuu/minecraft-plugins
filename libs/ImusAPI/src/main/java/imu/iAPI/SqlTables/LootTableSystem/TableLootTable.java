package imu.iAPI.SqlTables.LootTableSystem;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "loot_tables")
public class TableLootTable
{
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, unique = true)
    private String name;

    public TableLootTable()
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

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
