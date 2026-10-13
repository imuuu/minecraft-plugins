package imu.spawners;

import imu.iAPI.Events.FakeBlockBreakEvent;
import imu.spawners.engine.Luck;
import imu.spawners.engine.Names;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Breaking spawners with silk touch, and placing spawner items.
 */
class SpawnerListener implements Listener
{
    private final ImusSpawners _plugin;
    private final SpawnerItems _items;
    private final PlacedSpawners _placed;
    private final LuckStore _stats;

    SpawnerListener(ImusSpawners plugin, SpawnerItems items, PlacedSpawners placed, LuckStore stats)
    {
        _plugin = plugin;
        _items = items;
        _placed = placed;
        _stats = stats;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e)
    {
        Block block = e.getBlockPlaced();
        EntityType type = _items.typeOf(e.getItemInHand());
        if (type == null || !(block.getState() instanceof CreatureSpawner spawner))
            return;
        spawner.setSpawnedType(type);
        spawner.update();
        _placed.markPlaced(block, e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e)
    {
        Block block = e.getBlock();
        if (e instanceof FakeBlockBreakEvent || block.getType() != Material.SPAWNER)
            return;
        Player player = e.getPlayer();
        boolean placed = _placed.isPlaced(block);
        _placed.broken(block);
        EntityType type = block.getState() instanceof CreatureSpawner spawner ? spawner.getSpawnedType() : null;
        if (type == null || player.getGameMode() == GameMode.CREATIVE
                || !player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH))
            return;

        if (placed)
        {
            // Placed spawners always come back, as the same mob
            e.setExpToDrop(0);
            block.getWorld().dropItemNaturally(block.getLocation(), _items.create(type, player.getUniqueId(), 1));
            return;
        }

        Luck luck = _plugin.luck();
        Luck.Try roll = luck.roll(_stats.get(player.getUniqueId()), ThreadLocalRandom.current().nextInt(100));
        _stats.set(player.getUniqueId(), player.getName(), roll.after());
        if (!roll.success())
        {
            _plugin.send(player, "missed", "roll", (int) roll.roll(), "chance", ImusSpawners.percent(roll.chance()),
                    "next", ImusSpawners.percent(luck.chance(roll.after())));
            return;
        }

        List<EntityType> random = _items.randomTypes();
        boolean randomType = !random.isEmpty() && ThreadLocalRandom.current().nextDouble(100) < _plugin.getConfig().getDouble("random-type-chance", 5);
        if (randomType)
            type = random.get(ThreadLocalRandom.current().nextInt(random.size()));
        block.getWorld().dropItemNaturally(block.getLocation(), _items.create(type, player.getUniqueId(), 1));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.4f);
        _plugin.send(player, randomType ? "got-random" : "got", "roll", (int) roll.roll(), "chance", ImusSpawners.percent(roll.chance()),
                "type", Names.pretty(type.name()));
        _plugin.getLogger().info(player.getName() + " got a " + type + " spawner");
    }
}
