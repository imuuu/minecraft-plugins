package imu.iAPI.Permissions;

import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Paper.PaperMenus;
import imu.iAPI.Menus.Paper.PaperViewer;
import imu.iAPI.Menus.Viewer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.DoubleSupplier;
import java.util.logging.Level;

/**
 * The permissions menu on the server. Reads the permissions of the plugins built on imusAPI from their plugin.yml,
 * keeps what admins give and take in permissions.yml, and hands them to the players as permission attachments
 * when they join and at once when changed.
 * <p>
 * No permissions plugin is needed, and one (e.g. LuckPerms) can be used next to it: Bukkit adds both up, and what is
 * taken away here wins over the plugin defaults.
 */
public final class PaperPermissions implements PermissionGame, Listener
{
    public static final String USE_PERMISSION = "ia.permissions";
    private static PaperPermissions _instance;

    private final JavaPlugin _plugin;
    private final File _file;
    private final DoubleSupplier _range;
    private final PermissionGrants _grants = new PermissionGrants();
    // Given first, then taken: a later attachment wins, so a node taken away stays off even when a parent gives it
    private final Map<UUID, List<PermissionAttachment>> _attachments = new HashMap<>();
    private final Set<String> _discovered = new HashSet<>();

    /**
     * @param range how close players are listed in the menu, in blocks
     */
    public PaperPermissions(JavaPlugin plugin, DoubleSupplier range)
    {
        _plugin = plugin;
        _file = new File(plugin.getDataFolder(), "permissions.yml");
        _range = range;
    }

    public void enable()
    {
        _instance = this;
        _grants.load(YamlConfiguration.loadConfiguration(_file));
        Bukkit.getPluginManager().registerEvents(this, _plugin);
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
        {
            if (plugin.isEnabled())
                discover(plugin);
        }
        for (Player player : Bukkit.getOnlinePlayers())
            apply(player);
    }

    /**
     * Reads permissions.yml again after it was edited by hand, and applies it to everyone online.
     *
     * @return how many players have something given or taken
     */
    public int reload()
    {
        _grants.load(YamlConfiguration.loadConfiguration(_file));
        for (Player player : Bukkit.getOnlinePlayers())
            apply(player);
        return _grants.size();
    }

    public void disable()
    {
        HandlerList.unregisterAll(this);
        for (UUID player : new ArrayList<>(_attachments.keySet()))
            detach(player);
        for (String plugin : _discovered)
            Permissions.unregister(plugin);
        _discovered.clear();
        if (_instance == this)
            _instance = null;
    }

    /**
     * Opens the permissions menu of a plugin: the players close by, to pick whose permissions to change. For a
     * plugin's own command, e.g. /imustnt permissions.
     *
     * @return false when the player may not use it or the plugin has no permissions; they were told why
     */
    public static boolean open(Player player, String plugin)
    {
        PaperPermissions permissions = _instance;
        if (permissions == null || !player.hasPermission(USE_PERMISSION))
        {
            PaperViewer.of(player).message("&cYou don't have permission to change permissions!");
            return false;
        }
        PluginPermissions list = permissions.permissionsOf(plugin);
        if (list == null || list.isEmpty())
        {
            PaperViewer.of(player).message("&c" + plugin + " has no permissions to change");
            return false;
        }
        PaperMenus.open(player, new PermissionPlayersMenu(permissions, list));
        return true;
    }

    /**
     * The registered permissions of a plugin, or read from its plugin.yml when it isn't registered; null when there
     * is no such plugin.
     */
    public PluginPermissions permissionsOf(String name)
    {
        PluginPermissions registered = Permissions.get(name);
        if (registered != null)
            return registered;
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
        {
            if (plugin.getName().equalsIgnoreCase(name))
                return read(plugin);
        }
        return null;
    }

    // Plugins ========================================================================================================

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event)
    {
        discover(event.getPlugin());
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event)
    {
        String name = event.getPlugin().getName();
        if (_discovered.remove(name))
            Permissions.unregister(name);
    }

    /**
     * Lists a plugin built on imusAPI, or one whose plugin.yml gives levels, unless it registered itself.
     */
    private void discover(Plugin plugin)
    {
        if (Permissions.get(plugin.getName()) != null)
            return;
        PluginPermissions permissions = read(plugin);
        if (permissions == null || permissions.isEmpty())
            return;
        boolean levels = permissions.permissions().stream().anyMatch(permission -> permission.level() != null);
        if (!builtOnImusAPI(plugin) && !levels)
            return;
        Permissions.register(permissions);
        _discovered.add(plugin.getName());
    }

    private boolean builtOnImusAPI(Plugin plugin)
    {
        if (plugin == _plugin)
            return true;
        List<String> needs = new ArrayList<>(plugin.getPluginMeta().getPluginDependencies());
        needs.addAll(plugin.getPluginMeta().getPluginSoftDependencies());
        return needs.stream().anyMatch(name -> name.equalsIgnoreCase(_plugin.getName()));
    }

    private PluginPermissions read(Plugin plugin)
    {
        try (InputStream in = plugin.getResource("plugin.yml"))
        {
            if (in == null)
                return null;
            String yml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return PluginPermissions.fromYaml(plugin.getName(), yml, problem -> _plugin.getLogger().warning(problem));
        } catch (IOException | RuntimeException e)
        {
            _plugin.getLogger().log(Level.WARNING, "Couldn't read the permissions of " + plugin.getName(), e);
            return null;
        }
    }

    // Players ========================================================================================================

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event)
    {
        apply(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        detach(event.getPlayer().getUniqueId());
    }

    private void apply(Player player)
    {
        detach(player.getUniqueId());
        Map<String, Boolean> grants = _grants.of(player.getUniqueId());
        if (grants.isEmpty())
        {
            player.updateCommands();
            return;
        }
        PermissionAttachment given = player.addAttachment(_plugin);
        PermissionAttachment taken = player.addAttachment(_plugin);
        grants.forEach((node, value) -> (value ? given : taken).setPermission(node, value));
        _attachments.put(player.getUniqueId(), List.of(given, taken));
        // Commands the player can now use (or not) show up in tab completion
        player.updateCommands();
    }

    private void detach(UUID id)
    {
        List<PermissionAttachment> attachments = _attachments.remove(id);
        Player player = Bukkit.getPlayer(id);
        if (attachments == null || player == null)
            return;
        for (PermissionAttachment attachment : attachments)
            player.removeAttachment(attachment);
    }

    @Override
    public List<PermissionTarget> nearby(Viewer viewer)
    {
        Player looker = Bukkit.getPlayer(viewer.id());
        double range = _range.getAsDouble();
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (looker == null || near(looker.getLocation(), player.getLocation(), range))
                players.add(player);
        }
        if (looker != null)
        {
            Location at = looker.getLocation();
            players.sort(Comparator.comparingDouble(player -> player.getLocation().distanceSquared(at)));
        }

        List<PermissionTarget> targets = new ArrayList<>();
        for (Player player : players)
        {
            String where = looker == null ? "&7Online"
                    : player == looker ? "&7You" : "&7" + Math.round(player.getLocation().distance(looker.getLocation())) + " blocks away";
            targets.add(target(player, where));
        }
        return targets;
    }

    private static boolean near(Location a, Location b, double range)
    {
        return a.getWorld() == b.getWorld() && a.distanceSquared(b) <= range * range;
    }

    @Override
    public PermissionTarget find(String name)
    {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null)
            return target(online, "&7Online");
        OfflinePlayer offline = Bukkit.getOfflinePlayerIfCached(name);
        if (offline == null || !offline.hasPlayedBefore())
            return null;
        return target(offline, "&7Offline");
    }

    private PermissionTarget target(OfflinePlayer player, String where)
    {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (head.getItemMeta() instanceof SkullMeta meta)
        {
            meta.setOwningPlayer(player);
            head.setItemMeta(meta);
        }
        String name = player.getName() != null ? player.getName() : _grants.nameOf(player.getUniqueId());
        return new PermissionTarget(player.getUniqueId(), name == null ? player.getUniqueId().toString() : name,
                player.isOp(), player.isOnline(), Icon.of(Material.PLAYER_HEAD).base(head).lore(where));
    }

    @Override
    public boolean has(PermissionTarget target, PermissionInfo permission)
    {
        Player online = Bukkit.getPlayer(target.id());
        if (online != null)
            return online.hasPermission(permission.node());

        // Offline: worked out the way Bukkit would when they join
        Boolean grant = _grants.get(target.id(), permission.node());
        if (grant != null)
            return grant;
        for (Map.Entry<String, Boolean> given : _grants.of(target.id()).entrySet())
        {
            if (given.getValue() && givesChild(given.getKey(), permission.node(), 8))
                return true;
        }
        return defaultOf(permission).getValue(target.op());
    }

    private static boolean givesChild(String parent, String node, int depth)
    {
        Permission permission = Bukkit.getPluginManager().getPermission(parent);
        if (permission == null || depth == 0)
            return false;
        for (Map.Entry<String, Boolean> child : permission.getChildren().entrySet())
        {
            if (child.getValue() && (child.getKey().equalsIgnoreCase(node) || givesChild(child.getKey(), node, depth - 1)))
                return true;
        }
        return false;
    }

    @Override
    public PermissionDefault defaultOf(PermissionInfo permission)
    {
        Permission bukkit = Bukkit.getPluginManager().getPermission(permission.node());
        return bukkit != null ? bukkit.getDefault() : permission.byDefault();
    }

    @Override
    public boolean isOp(Viewer viewer)
    {
        Player player = Bukkit.getPlayer(viewer.id());
        return player != null && player.isOp();
    }

    @Override
    public Boolean grant(PermissionTarget target, String node)
    {
        return _grants.get(target.id(), node);
    }

    @Override
    public void setGrant(PermissionTarget target, String node, Boolean value, Viewer by)
    {
        if (!_grants.set(target.id(), target.name(), node, value))
            return;
        save();
        _plugin.getLogger().info(by.name() + (value == null ? " reset " : value ? " gave " : " took away ") + node
                + (value == null ? " of " : value ? " to " : " from ") + target.name());
        Player online = Bukkit.getPlayer(target.id());
        if (online != null)
            apply(online);
    }

    private void save()
    {
        YamlConfiguration file = new YamlConfiguration();
        file.options().setHeader(List.of("Permissions given (and taken away, with a -) in /ia perms.",
                "Edit in game, or here and then /ia perms reload."));
        _grants.save(file);
        try
        {
            file.save(_file);
        } catch (IOException e)
        {
            _plugin.getLogger().log(Level.SEVERE, "Couldn't save " + _file, e);
        }
    }
}
