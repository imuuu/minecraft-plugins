package imu.iAPI.Menus.Paper;

import imu.iAPI.Menus.ClickKind;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.MenuHost;
import imu.iAPI.Menus.MenuSession;
import imu.iAPI.Menus.Prompt;
import imu.iAPI.Menus.Text;
import imu.iAPI.Utilities.TextInputDialog;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
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
     * Closes every menu.
     */
    public static void disable()
    {
        if (_instance == null)
            return;
        for (Host host : Map.copyOf(_instance._hosts).values())
            host._session.close();
        HandlerList.unregisterAll(_instance);
        _instance = null;
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
            host._clicking = true;
            try
            {
                host._session.click(raw, kind(event.getClick()), event.getHotbarButton());
            } finally
            {
                host._clicking = false;
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
        if (!(event.getView().getTopInventory().getHolder(false) instanceof Window))
            return;
        int size = event.getView().getTopInventory().getSize();
        for (int raw : event.getRawSlots())
        {
            if (raw < size)
            {
                event.setCancelled(true);
                return;
            }
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
        private long _lastClick;
        private boolean _clicking;
        private boolean _opening;
        private boolean _expectingClose;

        private Host(Player player)
        {
            _player = player;
            _session = new MenuSession(PaperViewer.of(player), this);
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
            });

            cancelTicker();
            int ticks = menu.refreshTicks();
            if (ticks > 0)
                _ticker = Bukkit.getScheduler().runTaskTimer(_plugin, _session::tick, ticks, ticks);
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
            later(() ->
            {
                _expectingClose = true;
                try
                {
                    TextInputDialog.show(_player, Text.legacy(prompt.title()),
                            prompt.lines().stream().map(Text::legacy).toList(), prompt.initial(), prompt.maxLength(),
                            _session::answer, _session::promptClosed);
                } finally
                {
                    _expectingClose = false;
                }
            });
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
            _player.sendMessage(Text.of("&cSomething went wrong, see the console."));
        }

        private void end()
        {
            cancelTicker();
            _hosts.remove(_player.getUniqueId(), this);
        }

        private void cancelTicker()
        {
            if (_ticker != null)
                _ticker.cancel();
            _ticker = null;
        }
    }
}
