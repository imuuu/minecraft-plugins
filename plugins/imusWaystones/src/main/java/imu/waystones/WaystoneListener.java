package imu.waystones;

import imu.iAPI.Menus.Paper.PaperMenus;
import imu.iAPI.Events.FakeBlockBreakEvent;
import imu.waystones.engine.Panel;
import imu.waystones.engine.Visibility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Building, using and breaking waystones.
 */
final class WaystoneListener implements Listener
{
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final ImusWaystones _plugin;
    private final Map<UUID, Long> _lastClick = new HashMap<>();

    WaystoneListener(ImusWaystones plugin)
    {
        _plugin = plugin;
    }

    /**
     * Finishing a waystone asks the player to confirm it.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e)
    {
        Player player = e.getPlayer();
        Block base = _plugin.structure().finishedBy(e.getBlockPlaced());
        if (base == null || _plugin.waystones().at(base) != null || !player.hasPermission("imuswaystones.use"))
            return;
        _plugin.commands().setPending(player, BlockSpot.of(base));
        Component button = LEGACY.deserialize(_plugin.text("confirm-button"))
                .clickEvent(ClickEvent.runCommand("/waystones confirm"))
                .hoverEvent(HoverEvent.showText(LEGACY.deserialize(_plugin.text("confirm-hover"))));
        player.sendMessage(LEGACY.deserialize(_plugin.text("confirm-prompt")).append(button));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onUse(PlayerInteractEvent e)
    {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getHand() != EquipmentSlot.HAND)
            return;
        Waystone ws = _plugin.waystones().at(e.getClickedBlock());
        if (ws == null)
            return;
        e.setCancelled(true);
        Player player = e.getPlayer();
        long now = System.currentTimeMillis();
        Long last = _lastClick.put(player.getUniqueId(), now);
        if (last != null && now - last < 400 || _plugin.teleports().isWaiting(player))
            return;

        if (!_plugin.structure().isWhole(ws))
        {
            _plugin.waystones().remove(ws);
            _plugin.send(player, "broken");
            return;
        }
        if (_plugin.waystones().discover(player.getUniqueId(), ws))
        {
            _plugin.send(player, "discovered", "name", ws.name);
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1f, 1.2f);
            // A waystone found by touch only tells it is found this time
            if (ws.visibility == Visibility.BY_TOUCH)
                return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        Panel fromItem = UpgradeItems.read(hand);
        if (fromItem != null)
        {
            if (!ws.panel(player.getUniqueId()).raiseTo(fromItem))
            {
                _plugin.send(player, "upgrades-not-higher");
                return;
            }
            hand.setAmount(hand.getAmount() - 1);
            _plugin.waystones().savePanel(ws, player.getUniqueId());
            _plugin.send(player, "upgrades-applied");
            return;
        }
        PaperMenus.open(player, new WaystoneMenu(_plugin.menuGame(), ws));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e)
    {
        Waystone ws = _plugin.waystones().at(e.getBlock());
        if (ws == null)
            return;
        // Special TNT and the like ask first; they don't get waystones
        if (e instanceof FakeBlockBreakEvent)
        {
            e.setCancelled(true);
            return;
        }
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE)
        {
            _plugin.send(e.getPlayer(), "creative-break");
            e.setCancelled(true);
        } else if (ws.unbreakable)
        {
            _plugin.send(e.getPlayer(), "unbreakable");
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBroken(BlockBreakEvent e)
    {
        if (e instanceof FakeBlockBreakEvent)
            return;
        Waystone ws = _plugin.waystones().at(e.getBlock());
        if (ws != null)
            destroy(ws, e.getPlayer());
    }

    private void destroy(Waystone ws, Player by)
    {
        _plugin.waystones().remove(ws);
        Player owner = Bukkit.getPlayer(ws.owner);
        if (owner != null && owner != by)
            _plugin.send(owner, "destroyed-owner", "name", ws.name);
        if (by != null)
        {
            _plugin.send(by, "destroyed", "name", ws.name);
            by.playSound(by.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.3f, 1f);
        }
        _plugin.getLogger().info((by == null ? "An explosion" : by.getName()) + " destroyed " + ws.ownerName + "'s waystone "
                + ws.plainName() + " at " + ws.world + " " + ws.x + " " + ws.y + " " + ws.z);
    }

    // Explosions: unbreakable waystones stay, the others are destroyed

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e)
    {
        keepUnbreakable(e.blockList());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e)
    {
        keepUnbreakable(e.blockList());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExploded(EntityExplodeEvent e)
    {
        destroyIn(e.blockList());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExploded(BlockExplodeEvent e)
    {
        destroyIn(e.blockList());
    }

    private void keepUnbreakable(List<Block> blocks)
    {
        blocks.removeIf(block ->
        {
            Waystone ws = _plugin.waystones().at(block);
            return ws != null && ws.unbreakable;
        });
    }

    private void destroyIn(List<Block> blocks)
    {
        Set<Waystone> hit = new LinkedHashSet<>();
        for (Block block : blocks)
        {
            Waystone ws = _plugin.waystones().at(block);
            if (ws != null)
                hit.add(ws);
        }
        hit.forEach(ws -> destroy(ws, null));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBurn(BlockBurnEvent e)
    {
        if (_plugin.waystones().at(e.getBlock()) != null)
            e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e)
    {
        if (e.getBlocks().stream().anyMatch(block -> _plugin.waystones().at(block) != null))
            e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e)
    {
        if (e.getBlocks().stream().anyMatch(block -> _plugin.waystones().at(block) != null))
            e.setCancelled(true);
    }
}
