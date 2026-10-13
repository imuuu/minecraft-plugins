package imu.waystones;

import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Paper.PaperIcons;
import imu.iAPI.Menus.Viewer;
import imu.iAPI.Utilities.InvUtil;
import imu.waystones.engine.Cost;
import imu.waystones.engine.Destinations;
import imu.waystones.engine.Panel;
import imu.waystones.engine.Rules;
import imu.waystones.engine.Stone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * The waystone menus' {@link WaystoneGame} on the server.
 */
final class PaperWaystoneGame implements WaystoneGame
{
    private final ImusWaystones _plugin;

    PaperWaystoneGame(ImusWaystones plugin)
    {
        _plugin = plugin;
    }

    private static Player player(Viewer viewer)
    {
        return Objects.requireNonNull(Bukkit.getPlayer(viewer.id()), "The player is offline");
    }

    @Override
    public String text(String key, Object... placeholders)
    {
        return _plugin.text(key, placeholders);
    }

    @Override
    public List<String> textList(String key)
    {
        return _plugin.getConfig().getStringList("messages." + key);
    }

    @Override
    public Rules rules()
    {
        return _plugin.rules();
    }

    @Override
    public Collection<Waystone> all()
    {
        return _plugin.waystones().all();
    }

    @Override
    public Waystone get(UUID id)
    {
        return _plugin.waystones().get(id);
    }

    @Override
    public List<Waystone> destinations(Waystone from, UUID player)
    {
        Waystones waystones = _plugin.waystones();
        List<Stone> stones = Destinations.from(from.stone(), waystones.stones(), waystones.discovered(player),
                from.panel(player), _plugin.rules());
        List<Waystone> list = new ArrayList<>();
        for (Stone stone : stones)
        {
            Waystone ws = waystones.get(stone.id());
            if (ws != null)
                list.add(ws);
        }
        return list;
    }

    @Override
    public long cooldownLeft(Viewer viewer, Waystone from)
    {
        return _plugin.teleports().cooldownLeft(player(viewer), from);
    }

    @Override
    public Icon icon(Waystone ws)
    {
        return PaperIcons.of(ws.icon);
    }

    @Override
    public void save(Waystone ws)
    {
        _plugin.waystones().save(ws);
    }

    @Override
    public void savePanel(Waystone ws, UUID player)
    {
        _plugin.waystones().savePanel(ws, player);
    }

    @Override
    public void remove(Waystone ws)
    {
        _plugin.waystones().remove(ws);
    }

    @Override
    public void travel(Viewer viewer, Waystone from, Waystone to)
    {
        Player player = player(viewer);
        if (!_plugin.structure().isWhole(to))
        {
            _plugin.send(player, "destination-gone");
            return;
        }
        int levels = Teleports.isFree(player, from) ? 0 : _plugin.rules().xpLevels(from.panel(player.getUniqueId()));
        if (player.getLevel() < levels)
        {
            _plugin.send(player, "not-enough-xp", "levels", levels);
            return;
        }
        _plugin.teleports().begin(player, from, to);
    }

    @Override
    public boolean teleport(Viewer viewer, Waystone to)
    {
        Location landing = Teleports.landing(to);
        if (landing == null)
            return false;
        player(viewer).teleport(landing);
        return true;
    }

    @Override
    public boolean take(Viewer viewer, List<Cost> cost)
    {
        PlayerInventory inventory = player(viewer).getInventory();
        for (Cost part : cost)
        {
            Material material = Material.matchMaterial(part.material());
            if (material == null || !inventory.containsAtLeast(new ItemStack(material), part.amount()))
                return false;
        }
        for (Cost part : cost)
            inventory.removeItem(new ItemStack(Material.matchMaterial(part.material()), part.amount()));
        return true;
    }

    @Override
    public void giveUpgrades(Viewer viewer, Panel panel)
    {
        InvUtil.AddItemToInventoryOrDrop(player(viewer), UpgradeItems.create(_plugin, panel));
    }

    @Override
    public void sound(Viewer viewer, String sound, float volume, float pitch)
    {
        Player player = player(viewer);
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
