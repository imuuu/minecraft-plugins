package imu.waystones;

import imu.iAPI.Utilities.DataUtils;
import imu.iAPI.Utilities.ItemUtils;
import imu.waystones.engine.Panel;
import imu.waystones.engine.UpgradeKind;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Upgrades taken off a waystone onto a paper, to use on another one. The keys are the old plugin's:
 * imusapi:iw_upgrade_item marks the paper and imusapi:cast_time etc. hold the tiers.
 */
final class UpgradeItems
{
    private static final String MARK = "iw_upgrade_item";

    private UpgradeItems()
    {
    }

    private static String key(UpgradeKind kind)
    {
        return kind.name().toLowerCase(Locale.ROOT);
    }

    static ItemStack create(ImusWaystones plugin, Panel panel)
    {
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemUtils.SetDisplayName(paper, plugin.text("upgrade-item-name"));
        List<String> lore = new ArrayList<>();
        for (UpgradeKind kind : UpgradeKind.BUYABLE)
        {
            int tier = panel.tier(kind);
            if (tier > 0)
                lore.add(plugin.text("upgrade-item-line", "upgrade", plugin.text("upgrade." + kind.key() + ".name"), "tier", tier));
            DataUtils.SetPersistentData(paper, key(kind), PersistentDataType.INTEGER, tier);
        }
        lore.add(" ");
        lore.add(plugin.text("upgrade-item-hint"));
        ItemUtils.SetLores(paper, lore, false);
        ItemUtils.AddGlow(paper);
        DataUtils.SetPersistentData(paper, MARK, PersistentDataType.INTEGER, 0);
        return paper;
    }

    /**
     * The upgrades on the item, or null when it isn't an upgrade item.
     */
    static Panel read(ItemStack item)
    {
        if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()
                || DataUtils.GetPersistentData(item, MARK, PersistentDataType.INTEGER) == null)
            return null;
        Panel panel = new Panel();
        for (UpgradeKind kind : UpgradeKind.BUYABLE)
        {
            Integer tier = DataUtils.GetPersistentData(item, key(kind), PersistentDataType.INTEGER);
            if (tier != null)
                panel.set(kind, tier);
        }
        return panel;
    }
}
