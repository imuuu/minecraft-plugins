package imu.imustnt;

import imu.iAPI.Events.FakeBlockBreakEvent;
import imu.imustnt.engine.LayerRule;
import imu.imustnt.engine.TntType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Placing, lighting and breaking special TNT, and its blast.
 */
class TntListener implements Listener
{
    private final ImusTNT _plugin;
    private final PlacedTnt _placed;
    private final NamespacedKey _typeKey;
    private final NamespacedKey _placerKey;
    /** True while the blast asks protection plugins about its blocks, so that event isn't taken as a new blast */
    private boolean _asking;

    TntListener(ImusTNT plugin, PlacedTnt placed)
    {
        _plugin = plugin;
        _placed = placed;
        _typeKey = new NamespacedKey(plugin, "type");
        _placerKey = new NamespacedKey(plugin, "placer");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e)
    {
        Block block = e.getBlockPlaced();
        if (block.getType() != Material.TNT)
            return;
        TntType type = _plugin.typeOf(e.getItemInHand());
        if (type == null)
        {
            // Vanilla TNT clears whatever was stored for this spot before
            _placed.remove(block);
            return;
        }
        _placed.set(block, type);
        block.getWorld().spawnParticle(Particle.FLAME, block.getLocation().add(0.5, 0.5, 0.5), 40, 0.4, 0.4, 0.4, 0.05);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e)
    {
        if (e instanceof FakeBlockBreakEvent)
            return;
        TntType type = _placed.get(e.getBlock());
        if (type == null)
            return;
        _placed.remove(e.getBlock());
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        e.setDropItems(false);
        e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), _plugin.item(type, 1));
    }

    /**
     * Special TNT only goes off with flint and steel.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onLight(PlayerInteractEvent e)
    {
        Block block = e.getClickedBlock();
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || block == null || e.getHand() != EquipmentSlot.HAND)
            return;
        // e.g. a claim plugin denied using the block
        if (e.useInteractedBlock() == Event.Result.DENY)
            return;
        Player player = e.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.FLINT_AND_STEEL)
            return;
        TntType type = _placed.get(block);
        if (type == null)
            return;
        e.setCancelled(true);
        _placed.remove(block);
        block.setType(Material.AIR);
        if (player.getGameMode() != GameMode.CREATIVE)
            player.damageItemStack(EquipmentSlot.HAND, 1);

        block.getWorld().spawn(block.getLocation().add(0.5, 0, 0.5), TNTPrimed.class, tnt ->
        {
            tnt.setFuseTicks(Math.max(1, _plugin.getConfig().getInt("fuse-ticks", 80)));
            tnt.setSource(player);
            tnt.getPersistentDataContainer().set(_typeKey, PersistentDataType.STRING, type.name());
            tnt.getPersistentDataContainer().set(_placerKey, PersistentDataType.STRING, player.getUniqueId().toString());
        });
        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_TNT_PRIMED, 1, 1);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlast(EntityExplodeEvent e)
    {
        if (_asking || !(e.getEntity() instanceof TNTPrimed tnt))
            return;
        PersistentDataContainer data = tnt.getPersistentDataContainer();
        TntType type = TntType.parse(data.get(_typeKey, PersistentDataType.STRING));
        if (type == null)
            return;
        // The blast itself breaks nothing; the layer is what changes
        e.blockList().clear();

        // Protection plugins decide by the player, so a TNT whose player has left doesn't do anything
        Player player = placer(data);
        if (player == null)
            return;

        List<Block> blocks = layer(type, e.getLocation());
        EntityExplodeEvent ask = new EntityExplodeEvent(tnt, e.getLocation(), blocks, 0f, e.getExplosionResult());
        _asking = true;
        try
        {
            Bukkit.getPluginManager().callEvent(ask);
        } finally
        {
            _asking = false;
        }
        if (ask.isCancelled())
            return;

        Material replacement = Material.valueOf(type.replacement());
        for (Block block : ask.blockList())
        {
            FakeBlockBreakEvent mayBreak = new FakeBlockBreakEvent(block, player);
            Bukkit.getPluginManager().callEvent(mayBreak);
            if (!mayBreak.isCancelled())
                block.setType(replacement);
        }
        Location center = e.getLocation().getChunk().getBlock(8, e.getLocation().getBlockY(), 8).getLocation();
        center.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, center, 2);
    }

    private Player placer(PersistentDataContainer data)
    {
        String uuid = data.get(_placerKey, PersistentDataType.STRING);
        if (uuid == null)
            return null;
        try
        {
            return Bukkit.getPlayer(UUID.fromString(uuid));
        } catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    /**
     * The blocks of the chunk layer(s) the TNT changes, from its own layer down.
     */
    private List<Block> layer(TntType type, Location at)
    {
        World world = at.getWorld();
        LayerRule rule = _plugin.rule();
        List<Block> blocks = new ArrayList<>();
        int top = at.getBlockY();
        int bottom = Math.max(world.getMinHeight(), top - _plugin.layers() + 1);
        for (int y = top; y >= bottom; y--)
        {
            for (int x = 0; x < 16; x++)
            {
                for (int z = 0; z < 16; z++)
                {
                    Block block = at.getChunk().getBlock(x, y, z);
                    Material material = block.getType();
                    LayerRule.Cell cell = new LayerRule.Cell(material.name(), material.getBlastResistance(), material.isAir(), material.isInteractable());
                    // Other special TNTs stay too, like chests do
                    if (rule.changes(type, cell) && _placed.get(block) == null)
                        blocks.add(block);
                }
            }
        }
        return blocks;
    }

    // Redstone, fire or other explosions would light special TNT as vanilla TNT and waste it

    @EventHandler(ignoreCancelled = true)
    public void onPrime(TNTPrimeEvent e)
    {
        if (_placed.get(e.getBlock()) != null)
            e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBurn(BlockBurnEvent e)
    {
        if (_placed.get(e.getBlock()) != null)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplodeBlocks(EntityExplodeEvent e)
    {
        e.blockList().removeIf(block -> _placed.get(block) != null);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e)
    {
        e.blockList().removeIf(block -> _placed.get(block) != null);
    }

    // The type is stored at the block's spot, so a moved special TNT would turn vanilla

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e)
    {
        if (e.getBlocks().stream().anyMatch(block -> _placed.get(block) != null))
            e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e)
    {
        if (e.getBlocks().stream().anyMatch(block -> _placed.get(block) != null))
            e.setCancelled(true);
    }
}
