package imu.iAPI.SqlTables.LootTableSystem;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import imu.iAPI.LootTables.LOOT_TYPE;

@DatabaseTable(tableName = "loot_items")
public class TableLootItems
{
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private int loot_item_id;

    @DatabaseField(canBeNull = false, foreign = true, columnName = "loot_table_id", foreignAutoRefresh = true)
    private TableLootTable lootTable;

    @DatabaseField(canBeNull = false)
    private LOOT_TYPE type;

    @DatabaseField
    private int weight;

    @DatabaseField
    private int minAmount = -1;

    @DatabaseField
    private int maxAmount = -1;

    public TableLootItems()
    {
    }

    public int getLoot_item_id()
    {
        return loot_item_id;
    }

    public void setLoot_item_id(int loot_item_id)
    {
        this.loot_item_id = loot_item_id;
    }

    public int getMinAmount()
    {
        return minAmount;
    }

    public void setMinAmount(int minAmount)
    {
        this.minAmount = minAmount;
    }

    public int getMaxAmount()
    {
        return maxAmount;
    }

    public void setMaxAmount(int maxAmount)
    {
        this.maxAmount = maxAmount;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public TableLootTable getLootTable()
    {
        return lootTable;
    }

    public void setLootTable(TableLootTable lootTable)
    {
        this.lootTable = lootTable;
    }

    public LOOT_TYPE getType()
    {
        return type;
    }

    public void setType(LOOT_TYPE type)
    {
        this.type = type;
    }

    public int getWeight()
    {
        return weight;
    }

    public void setWeight(int weight)
    {
        this.weight = weight;
    }
}
