package imu.iAPI.Menus.Paper;

import imu.iAPI.Menus.ClickKind;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.MenuHost;
import imu.iAPI.Menus.MenuSession;
import imu.iAPI.Menus.Prompt;
import imu.iAPI.Menus.Text;
import imu.iAPI.Packets.ClientItems;
import imu.iAPI.Utilities.TextInputDialog;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.DragType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Shows {@link Menu}s to players as chest inventories. ImusAPI enables it; plugins open menus with
 * {@link #open(Player, Menu)}.
 */
public final class PaperMenus implements Listener
{
    private static final long CLICK_COOLDOWN_MS = 100;
    private static PaperMenus _instance;

    private final Plugin _plugin;
    private final Map<UUID, Host> _hosts = new HashMap<>();

    private PaperMenus(Plugin plugin)
    {
        _plugin = plugin;
    }

    public static void enable(Plugin plugin)
    {
        if (_instance != null)
            disable();
        _instance = new PaperMenus(plugin);
        Bukkit.getPluginManager().registerEvents(_instance, plugin);
    }

    /**
     * Closes every menu, giving back the items they hold. ImusAPI calls this when it is disabled.
     */
    public static void disable()
    {
        if (_instance == null)
            return;
        for (Host host : Map.copyOf(_instance._hosts).values())
            _instance.closeSafely(host);
        HandlerList.unregisterAll(_instance);
        _instance = null;
    }

    /**
     * Closes the menus of a plugin while its code still runs. Done on its own when the plugin is disabled; a
     * plugin may call it first in its onDisable.
     */
    public static void closeAll(Plugin plugin)
    {
        if (_instance == null)
            return;
        ClassLoader loader = plugin.getClass().getClassLoader();
        for (Host host : Map.copyOf(_instance._hosts).values())
        {
            boolean ours = host._session.menus().stream().anyMatch(menu -> menu.getClass().getClassLoader() == loader);
            if (ours)
                _instance.closeSafely(host);
        }
    }

    // One player's menus failing to close doesn't stop the others
    private void closeSafely(Host host)
    {
        try
        {
            host._session.close();
        } catch (RuntimeException e)
        {
            _plugin.getLogger().log(Level.SEVERE, "Closing the menus of " + host._player.getName() + " failed", e);
        } finally
        {
            host.end();
            if (host._player.getOpenInventory().getTopInventory().getHolder(false) instanceof Window)
                host._player.closeInventory();
        }
    }

    /**
     * Opens a menu for the player, ending any menus they had open.
     */
    public static MenuSession open(Player player, Menu menu)
    {
        if (_instance == null)
            throw new IllegalStateException("ImusAPI's menus are not enabled");
        Host old = _instance._hosts.get(player.getUniqueId());
        if (old != null)
        {
            old.end();
            old._session.closedByViewer();
        }
        Host host = _instance.new Host(player);
        _instance._hosts.put(player.getUniqueId(), host);
        host._session.open(menu);
        return host._session;
    }

    /**
     * The player's open menus, or null.
     */
    public static MenuSession session(Player player)
    {
        Host host = _instance == null ? null : _instance._hosts.get(player.getUniqueId());
        return host == null ? null : host._session;
    }

    /**
     * Whether the player's window shows the menu now; false when another plugin stopped it from opening.
     */
    public static boolean isShowing(Player player, Menu menu)
    {
        return player.getOpenInventory().getTopInventory().getHolder(false) instanceof Window window
                && window._host._session.current() == menu;
    }

    // Events =========================================================================================================

    @EventHandler(priority = EventPriority.LOW)
    public void onClick(InventoryClickEvent event)
    {
        if (!(event.getView().getTopInventory().getHolder(false) instanceof Window window))
            return;
        Host host = window._host;
        int raw = event.getRawSlot();
        int size = event.getView().getTopInventory().getSize();

        if (raw >= 0 && raw < size)
        {
            event.setCancelled(true);
            if (window != host._window || host._opening || !host.clickAllowed())
                return;
            try
            {
                host.click(raw, kind(event.getClick()), event.getHotbarButton());
            } catch (RuntimeException e)
            {
                // The session closes a menu that throws; this is for anything that slipped past it
                _plugin.getLogger().log(Level.SEVERE, "A menu click of " + host._player.getName() + " failed", e);
                closeSafely(host);
            }
            return;
        }

        // The player's own inventory: items never move into the menu
        Menu menu = host._session.current();
        if (menu == null || menu.locksOwnInventory() || window != host._window
                || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
                || event.getAction() == InventoryAction.COLLECT_TO_CURSOR)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDrag(InventoryDragEvent event)
    {
        if (!(event.getView().getTopInventory().getHolder(false) instanceof Window window))
            return;
        int size = event.getView().getTopInventory().getSize();
        Set<Integer> top = new HashSet<>();
        for (int raw : event.getRawSlots())
        {
            if (raw < size)
                top.add(raw);
        }
        Host host = window._host;
        Menu menu = host._session.current();
        if (top.isEmpty())
        {
            // Only in the player's own inventory: fine unless the menu locks it
            if (menu == null || menu.locksOwnInventory())
                event.setCancelled(true);
            return;
        }
        event.setCancelled(true);

        // A drag over one slot of the menu is a slow click with the cursor's item; it runs once the cancelled
        // drag has put the item back on the cursor
        if (top.size() == 1 && event.getRawSlots().size() == 1 && window == host._window)
        {
            int slot = top.iterator().next();
            ClickKind kind = event.getType() == DragType.SINGLE ? ClickKind.RIGHT : ClickKind.LEFT;
            Bukkit.getScheduler().runTask(_plugin, () ->
            {
                if (window == host._window && !host._opening)
                    host.click(slot, kind, -1);
            });
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event)
    {
        if (!(event.getInventory().getHolder(false) instanceof Window window))
            return;
        Host host = window._host;
        // Closed by us to show another menu or a prompt
        if (window != host._window || host._expectingClose)
            return;
        host.end();
        host._session.closedByViewer();
    }

    // A plugin's menus close while its code is still there to give their items back
    @EventHandler
    public void onPluginDisable(PluginDisableEvent event)
    {
        if (event.getPlugin() != _plugin)
            closeAll(event.getPlugin());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        Host host = _hosts.get(event.getPlayer().getUniqueId());
        if (host != null)
        {
            host.end();
            host._session.closedByViewer();
        }
    }

    private static ClickKind kind(ClickType type)
    {
        return switch (type)
        {
            case LEFT -> ClickKind.LEFT;
            case RIGHT -> ClickKind.RIGHT;
            case SHIFT_LEFT -> ClickKind.SHIFT_LEFT;
            case SHIFT_RIGHT -> ClickKind.SHIFT_RIGHT;
            case MIDDLE -> ClickKind.MIDDLE;
            case DOUBLE_CLICK -> ClickKind.DOUBLE_CLICK;
            case NUMBER_KEY -> ClickKind.NUMBER_KEY;
            case DROP -> ClickKind.DROP;
            case CONTROL_DROP -> ClickKind.CONTROL_DROP;
            case SWAP_OFFHAND -> ClickKind.SWAP_OFFHAND;
            default -> ClickKind.OTHER;
        };
    }

    // One player's window ============================================================================================

    /**
     * The inventory of one shown menu. A new one is made for every menu, so a click is always on the menu it
     * shows.
     */
    private static final class Window implements InventoryHolder
    {
        private final Host _host;
        private final Inventory _inventory;

        private Window(Host host, Menu menu)
        {
            _host = host;
            _inventory = Bukkit.createInventory(this, menu.size(), menu.title());
        }

        @Override
        public Inventory getInventory()
        {
            return _inventory;
        }
    }

    private final class Host implements MenuHost
    {
        private final Player _player;
        private final MenuSession _session;
        private Window _window;
        private BukkitTask _ticker;
        private BukkitTask _flush;
        private BukkitTask _coverTask;
        // The player's own inventory shows fakes now, to be shown for real again when the menu goes
        private boolean _covered;
        private int _lastFlushTick = Integer.MIN_VALUE / 2;
        private long _lastClick;
        private boolean _clicking;
        private boolean _opening;
        private boolean _expectingClose;

        private Host(Player player)
        {
            _player = player;
            _session = new MenuSession(PaperViewer.of(player), this);
        }

        private void click(int slot, ClickKind kind, int hotbarKey)
        {
            _clicking = true;
            try
            {
                _session.click(slot, kind, hotbarKey);
            } finally
            {
                _clicking = false;
            }
            // A trade may have changed the player's items; the real ones go out at the end of the tick
            if (_covered)
                Bukkit.getScheduler().runTask(_plugin, this::cover);
        }

        private boolean clickAllowed()
        {
            long now = System.currentTimeMillis();
            if (now - _lastClick < CLICK_COOLDOWN_MS)
                return false;
            _lastClick = now;
            return true;
        }

        // Opening an inventory inside a click event breaks the client's view, so it waits a tick then
        private void later(Runnable action)
        {
            if (_clicking)
                Bukkit.getScheduler().runTask(_plugin, action);
            else
                action.run();
        }

        @Override
        public void show(Menu menu, Map<Integer, Icon> icons)
        {
            Window window = new Window(this, menu);
            icons.forEach((slot, icon) -> window._inventory.setItem(slot, PaperIcons.stack(icon)));
            _window = window;
            _opening = true;
            later(() ->
            {
                if (_window != window || _session.hasEnded())
                    return;
                _expectingClose = true;
                try
                {
                    _player.openInventory(window._inventory);
                } finally
                {
                    _expectingClose = false;
                    _opening = false;
                }
                cover();
            });

            cancelTicker();
            int ticks = menu.refreshTicks();
            if (ticks > 0)
                _ticker = Bukkit.getScheduler().runTaskTimer(_plugin, _session::tick, ticks, ticks);
            // Anything that changes the player's items sends the real ones; cover them again now and then
            if (menu.ownInventoryCover() != null)
                _coverTask = Bukkit.getScheduler().runTaskTimer(_plugin, this::cover, 20, 20);
        }

        /**
         * Covers the player's own inventory with the menu's cover, on their screen only.
         */
        private void cover()
        {
            Menu menu = _session.current();
            if (menu == null || _opening || _session.prompt() != null || _session.hasEnded())
                return;
            Icon cover = menu.ownInventoryCover();
            if (cover != null && ClientItems.allowed(_player))
            {
                ClientItems.coverOwn(_player, PaperIcons.stack(cover));
                _covered = true;
            }
        }

        // The real items again, once the window that showed the fakes is gone
        private void uncover()
        {
            if (!_covered)
                return;
            _covered = false;
            Bukkit.getScheduler().runTask(_plugin, () ->
            {
                if (_player.isOnline())
                    ClientItems.restore(_player);
            });
        }

        @Override
        public void set(int slot, Icon icon)
        {
            if (_window != null)
                _window._inventory.setItem(slot, PaperIcons.stack(icon));
        }

        @Override
        public void prompt(Prompt prompt)
        {
            cancelTicker();
            uncover();
            later(() ->
            {
                _expectingClose = true;
                try
                {
                    String title = Text.legacy(prompt.title());
                    List<String> lines = prompt.lines().stream().map(Text::legacy).toList();
                    if (prompt.rows() > 0)
                        TextInputDialog.showMultiline(_player, title, lines, prompt.initial(), prompt.maxLength(), prompt.rows(),
                                _session::answer, _session::promptClosed);
                    else
                        TextInputDialog.show(_player, title, lines, prompt.initial(), prompt.maxLength(),
                                _session::answer, _session::promptClosed);
                } finally
                {
                    _expectingClose = false;
                }
            });
        }

        // Changes from outside a click wait for the menu's delay since the last such drawing, and many make one
        @Override
        public void refreshRequested()
        {
            if (_flush != null)
                return;
            Menu menu = _session.current();
            int delay = menu == null ? 1 : Math.max(1, menu.refreshDelayTicks());
            int wait = Math.max(1, _lastFlushTick + delay - Bukkit.getCurrentTick());
            _flush = Bukkit.getScheduler().runTaskLater(_plugin, () ->
            {
                _flush = null;
                _lastFlushTick = Bukkit.getCurrentTick();
                if (!_opening)
                    _session.flush();
                else
                    refreshRequested();
            }, wait);
        }

        @Override
        public Object cursor()
        {
            ItemStack cursor = _player.getOpenInventory().getCursor();
            return cursor == null || cursor.getType().isAir() ? null : cursor;
        }

        @Override
        public void setCursor(Object item)
        {
            _player.getOpenInventory().setCursor(item == null ? null : (ItemStack) item);
        }

        @Override
        public void close()
        {
            end();
            later(() ->
            {
                if (_player.getOpenInventory().getTopInventory().getHolder(false) instanceof Window window && window._host == this)
                {
                    _expectingClose = true;
                    _player.closeInventory();
                }
            });
        }

        @Override
        public void error(String what, RuntimeException error)
        {
            _plugin.getLogger().log(Level.SEVERE, what + " failed for " + _player.getName(), error);
            _player.sendMessage(Text.of("&cSomething went wrong and the menu was closed. See the console."));
        }

        private void end()
        {
            cancelTicker();
            uncover();
            _hosts.remove(_player.getUniqueId(), this);
        }

        private void cancelTicker()
        {
            if (_coverTask != null)
                _coverTask.cancel();
            _coverTask = null;
            if (_flush != null)
                _flush.cancel();
            _flush = null;
            if (_ticker != null)
                _ticker.cancel();
            _ticker = null;
        }
    }
}
