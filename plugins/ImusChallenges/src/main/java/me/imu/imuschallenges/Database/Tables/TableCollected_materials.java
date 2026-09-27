package me.imu.imuschallenges.Database.Tables;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Date;

@DatabaseTable(tableName = "collected_materials")
public class TableCollected_materials
{
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private String material;

    @DatabaseField(canBeNull = false)
    private String player_name;

    @DatabaseField(canBeNull = false)
    private String player_uuid;
    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING,
            format = "yyyy-MM-dd HH:mm:ss")
    private Date collection_time;

    public TableCollected_materials() {    }

    public TableCollected_materials(Player player, Material material, Date collectionTime) {
        this.player_name = player.getName();
        this.player_uuid = player.getUniqueId().toString();
        this.material = material.name();
        this.collection_time = collectionTime;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    private Material _material;
    public Material getMaterial()
    {
        if(_material == null)
        {
            _material = Material.getMaterial(material);
        }
        return _material;
    }

    public void setMaterial(String material)
    {
        this.material = material;
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

    public Date getCollection_time()
    {
        return collection_time;
    }

    public void setCollection_time(Date collection_time)
    {
        this.collection_time = collection_time;
    }

    // Assuming you want to store the timestamp


}

