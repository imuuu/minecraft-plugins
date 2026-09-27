package imu.iAPI.SqlTables.LootTableSystem;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "loot_item_stacks")
public class TableItemStack
{
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private String display_name;

    @DatabaseField(canBeNull = false, width = 1000)
    private String item;

    public TableItemStack()
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

    public String getDisplay_name()
    {
        return display_name;
    }

    public void setDisplay_name(String display_name)
    {
        this.display_name = display_name;
    }

    public String getItem()
    {
        return item;
    }

    public void setItem(String item)
    {
        this.item = item;
    }
}
