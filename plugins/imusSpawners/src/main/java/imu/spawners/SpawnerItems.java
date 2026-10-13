package imu.spawners;

import imu.iAPI.Utilities.DataUtils;
import imu.iAPI.Utilities.ItemUtils;
import imu.spawners.engine.Names;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Spawner items that remember their mob. The keys imusapi:spawner_type and imusapi:owner_uuid are the old
 * imusSpawners' ones, so its items still work.
 */
class SpawnerItems
{
    static final String TYPE_KEY = "spawner_type";
    static final String OWNER_KEY = "owner_uuid";

    private final ImusSpawners _plugin;

    SpawnerItems(ImusSpawners plugin)
    {
        _plugin = plugin;
    }

    ItemStack create(EntityType type, UUID owner, int amount)
    {
        ItemStack stack = new ItemStack(Material.SPAWNER, Math.max(1, Math.min(64, amount)));
        // The spawner remembers its mob also without this plugin, and the tooltip shows it
        if (stack.getItemMeta() instanceof BlockStateMeta meta && meta.getBlockState() instanceof CreatureSpawner spawner)
        {
            spawner.setSpawnedType(type);
            meta.setBlockState(spawner);
            stack.setItemMeta(meta);
        }
        ItemUtils.SetDisplayName(stack, _plugin.text("item-name", "type", Names.pretty(type.name())));
        DataUtils.SetPersistentData(stack, TYPE_KEY, PersistentDataType.STRING, type.name());
        if (owner != null)
            DataUtils.SetPersistentData(stack, OWNER_KEY, PersistentDataType.STRING, owner.toString());
        return stack;
    }

    /**
     * The mob of one of these spawner items, or null.
     */
    EntityType typeOf(ItemStack stack)
    {
        if (stack == null || stack.getType() != Material.SPAWNER || !stack.hasItemMeta())
            return null;
        return parse(DataUtils.GetPersistentData(stack, TYPE_KEY, PersistentDataType.STRING));
    }

    static EntityType parse(String name)
    {
        if (name == null)
            return null;
        try
        {
            EntityType type = EntityType.valueOf(name.toUpperCase(Locale.ROOT));
            return type.isAlive() && type.isSpawnable() ? type : null;
        } catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    /**
     * Mobs a spawner can have.
     */
    static List<EntityType> spawnable()
    {
        List<EntityType> types = new ArrayList<>();
        for (EntityType type : EntityType.values())
        {
            if (type.isAlive() && type.isSpawnable())
                types.add(type);
        }
        return types;
    }

    /**
     * Mobs a random spawner can be: not the ones in random-type-excluded.
     */
    List<EntityType> randomTypes()
    {
        Set<String> excluded = _plugin.getConfig().getStringList("random-type-excluded").stream()
                .map(name -> name.toUpperCase(Locale.ROOT)).collect(Collectors.toSet());
        return spawnable().stream().filter(type -> !excluded.contains(type.name())).toList();
    }
}
