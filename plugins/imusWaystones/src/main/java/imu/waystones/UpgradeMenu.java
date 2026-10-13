package imu.waystones;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Utilities.InvUtil;
import imu.iAPI.Utilities.ItemUtils;
import imu.iAPI.Utilities.TextInputDialog;
import imu.iAPI.Utilities.TextUtils;
import imu.waystones.engine.Cost;
import imu.waystones.engine.Panel;
import imu.waystones.engine.Rules;
import imu.waystones.engine.UpgradeKind;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The player's own upgrades on a waystone: each costs items, and the next tier more.
 */
final class UpgradeMenu extends CustomInventory
{
    private static final Map<UpgradeKind, Integer> SLOTS = Map.of(UpgradeKind.CAST_TIME, 10, UpgradeKind.COOLDOWN, 12,
            UpgradeKind.DIMENSION, 14, UpgradeKind.XP_USAGE, 16, UpgradeKind.FOUNDATION, 22);
    private static final Map<UpgradeKind, Material> ICONS = Map.of(UpgradeKind.CAST_TIME, Material.CLOCK, UpgradeKind.COOLDOWN,
            Material.COMPASS, UpgradeKind.DIMENSION, Material.ENDER_PEARL, UpgradeKind.XP_USAGE, Material.EXPERIENCE_BOTTLE,
            UpgradeKind.FOUNDATION, Material.DIAMOND_BLOCK);
    private static final int BACK = 27, RENAME = 30, EXTRACT = 32;

    private final ImusWaystones _plugin;
    private final Waystone _ws;

    UpgradeMenu(ImusWaystones plugin, Waystone ws)
    {
        super(plugin, plugin.text("upgrades-title", "name", ws.name), 36);
        _plugin = plugin;
        _ws = ws;
    }

    @Override
    public INVENTORY_AREA setInventoryLock()
    {
        return INVENTORY_AREA.UPPER_INV;
    }

    @Override
    public void onAwake()
    {
    }

    @Override
    public void onOpen()
    {
        super.onOpen();
        render();
    }

    private void render()
    {
        Player player = getPlayer();
        Panel panel = _ws.panel(player.getUniqueId());
        clearButtons();
        ItemStack background = ItemUtils.SetDisplayNameEmpty(new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        for (int i = 0; i < getSize(); i++)
            addButton(new Button(i, background.clone()));

        if (_ws.maxedOut)
            addButton(new Button(13, WaystoneMenu.named(Material.NETHER_STAR, _plugin.text("maxed-out"), List.of(_plugin.text("maxed-out-lore")))));
        else
        {
            for (UpgradeKind kind : UpgradeKind.BUYABLE)
                addButton(new Button(SLOTS.get(kind), icon(kind, panel), e -> buy(kind, e.getClick())));
            addButton(new Button(EXTRACT, WaystoneMenu.named(Material.GRINDSTONE, _plugin.text("extract"), List.of(_plugin.text("extract-lore"))),
                    e -> extract(e.getClick())));
        }

        addButton(new Button(BACK, WaystoneMenu.named(Material.RED_WOOL, _plugin.text("menu-back"), List.of()),
                e -> Bukkit.getScheduler().runTask(_plugin, () -> new WaystoneMenu(_plugin, _ws).open(player))));
        if (player.getUniqueId().equals(_ws.owner) || player.hasPermission(ImusWaystones.ADMIN))
            addButton(new Button(RENAME, WaystoneMenu.named(Material.NAME_TAG, _plugin.text("rename"), List.of()), e -> rename()));
        updateButtons(true);
    }

    private ItemStack icon(UpgradeKind kind, Panel panel)
    {
        Rules rules = _plugin.rules();
        int max = rules.maxTier(kind);
        if (max == 0)
            return WaystoneMenu.named(Material.BARRIER, _plugin.text("upgrade-off", "upgrade", _plugin.text("upgrade." + kind.key() + ".name")), List.of());
        int tier = Math.min(panel.tier(kind), max);
        List<String> lore = new ArrayList<>(_plugin.getConfig().getStringList("messages.upgrade." + kind.key() + ".lore"));
        lore.add(" ");
        lore.add(_plugin.text("upgrade-now", "value", value(kind, panel)));
        lore.add(_plugin.text("upgrade-tier", "tier", tier, "max", max));
        List<Cost> next = rules.nextCost(kind, tier);
        if (next == null)
            lore.add(_plugin.text("upgrade-maxed"));
        else
        {
            lore.add(" ");
            lore.add(_plugin.text("upgrade-cost"));
            for (Cost cost : next)
                lore.add(_plugin.text("upgrade-cost-line", "amount", cost.amount(), "item", pretty(cost.material())));
            lore.add(" ");
            lore.add(_plugin.text("upgrade-click"));
            if (getPlayer().hasPermission(ImusWaystones.ADMIN))
                lore.add(_plugin.text("upgrade-admin-click"));
        }
        return WaystoneMenu.named(ICONS.get(kind), _plugin.text("upgrade." + kind.key() + ".name"), lore);
    }

    /**
     * What the upgrade gives now, e.g. "12s" or "Nether".
     */
    private String value(UpgradeKind kind, Panel panel)
    {
        Rules rules = _plugin.rules();
        return switch (kind)
        {
            case CAST_TIME -> trim(rules.castSeconds(panel)) + "s";
            case COOLDOWN, FOUNDATION -> TextUtils.FormatTime((long) (rules.cooldownSeconds(panel) * 1000));
            case XP_USAGE -> rules.xpLevels(panel) + " lvl";
            case DIMENSION -> _plugin.text("dimension." + Math.min(2, panel.tier(UpgradeKind.DIMENSION)));
            default -> "";
        };
    }

    private void buy(UpgradeKind kind, ClickType click)
    {
        Player player = getPlayer();
        Rules rules = _plugin.rules();
        Panel panel = _ws.panel(player.getUniqueId());
        int tier = panel.tier(kind);
        List<Cost> cost = rules.nextCost(kind, tier);
        if (cost == null)
            return;
        boolean free = click == ClickType.SHIFT_RIGHT && player.hasPermission(ImusWaystones.ADMIN);
        if (!free && !take(player.getInventory(), cost))
        {
            _plugin.send(player, "cant-afford");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1f);
            return;
        }
        panel.set(kind, tier + 1);
        _plugin.waystones().savePanel(_ws, player.getUniqueId());
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1.4f);
        render();
    }

    /**
     * Takes the items when the player has all of them.
     */
    private static boolean take(PlayerInventory inventory, List<Cost> cost)
    {
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

    private void extract(ClickType click)
    {
        if (click != ClickType.SHIFT_RIGHT)
            return;
        Player player = getPlayer();
        Panel panel = _ws.panel(player.getUniqueId());
        if (panel.isEmpty())
        {
            _plugin.send(player, "nothing-to-extract");
            return;
        }
        InvUtil.AddItemToInventoryOrDrop(player, UpgradeItems.create(_plugin, panel));
        panel.clear();
        _plugin.waystones().savePanel(_ws, player.getUniqueId());
        _plugin.send(player, "upgrades-extracted");
        render();
    }

    private void rename()
    {
        Player player = getPlayer();
        Bukkit.getScheduler().runTask(_plugin, () ->
        {
            player.closeInventory();
            TextInputDialog.show(player, _plugin.text("rename-title"), List.of(_plugin.text("rename-help")), _ws.name, 40, text ->
            {
                if (text.isBlank())
                    return _plugin.text("rename-empty");
                _ws.name = text.trim();
                _plugin.waystones().save(_ws);
                _plugin.send(player, "renamed", "name", _ws.name);
                return null;
            }, () ->
            {
                if (_plugin.waystones().get(_ws.id) != null)
                    new UpgradeMenu(_plugin, _ws).open(player);
            });
        });
    }

    static String pretty(String material)
    {
        StringBuilder name = new StringBuilder();
        for (String word : material.toLowerCase().split("_"))
        {
            if (!name.isEmpty())
                name.append(' ');
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.toString();
    }

    private static String trim(double value)
    {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(Math.round(value * 10) / 10.0);
    }
}
