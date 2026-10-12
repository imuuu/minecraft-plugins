package imu.GS.live;

import imu.GS.GsService;
import imu.GS.bukkit.FlashItems;
import imu.GS.engine.live.ServerProgress;
import imu.iAPI.CustomEnchants.CustomEnchants;
import imu.iAPI.CustomItems.CustomItems;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

/**
 * Watches what players have: every minute (and when they leave) the inventory, armor, offhand, ender chest and
 * the shulker boxes in them of every player that isn't an op and plays in survival or adventure. Items bought
 * from a flash deal don't count; the shop gave them.
 */
public class ProgressTracker implements Listener
{
    private final GsService _service;
    private final ServerProgress _progress = new ServerProgress();
    private BukkitTask _task;

    public ProgressTracker(GsService service)
    {
        _service = service;
    }

    public ServerProgress getProgress()
    {
        return _progress;
    }

    public void start()
    {
        stop();
        long ticks = Math.max(5, _service.getPlugin().getConfig().getInt("live.progress.scan-seconds", 60)) * 20L;
        _task = Bukkit.getScheduler().runTaskTimer(_service.getPlugin(), this::scanAll, ticks, ticks);
    }

    public void stop()
    {
        if (_task != null)
            _task.cancel();
        _task = null;
    }

    public static boolean isTracked(Player player)
    {
        return !player.isOp() && (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE);
    }

    /**
     * Looks at every tracked player now.
     */
    public void scanAll()
    {
        for (Player player : Bukkit.getOnlinePlayers())
            scan(player);
    }

    public void scan(Player player)
    {
        if (!isTracked(player))
            return;
        for (ItemStack item : player.getInventory().getContents())
            see(item, true);
        for (ItemStack item : player.getEnderChest().getContents())
            see(item, true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e)
    {
        scan(e.getPlayer());
    }

    private void see(ItemStack item, boolean openShulkers)
    {
        if (item == null || item.getType().isAir() || FlashItems.isMarked(item))
            return;
        _progress.seeItem(item.getType().name());
        String custom = CustomItems.idOf(item);
        if (custom != null)
            _progress.seeItem(custom);
        CustomEnchants.read(item).forEach(_progress::seeEnchant);
        for (Map.Entry<Enchantment, Integer> enchant : item.getEnchantments().entrySet())
            _progress.seeEnchant(enchant.getKey().getKey().toString(), enchant.getValue());
        if (!item.hasItemMeta())
            return;
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta book)
        {
            for (Map.Entry<Enchantment, Integer> enchant : book.getStoredEnchants().entrySet())
                _progress.seeEnchant(enchant.getKey().getKey().toString(), enchant.getValue());
        }
        if (openShulkers && meta instanceof BlockStateMeta state && state.getBlockState() instanceof ShulkerBox box)
        {
            for (ItemStack inside : box.getInventory().getContents())
                see(inside, false);
        }
    }

    /**
     * Whether the material may be in a flash deal (or rotating stock): someone has had it, or the check is off.
     */
    public boolean allows(String part, org.bukkit.Material material)
    {
        return !_service.getPlugin().getConfig().getBoolean("live.progress." + part, false) || _progress.hasSeen(material.name());
    }

    public boolean appliesTo(String part)
    {
        return _service.getPlugin().getConfig().getBoolean("live.progress." + part, false);
    }
}
