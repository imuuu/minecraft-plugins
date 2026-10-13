package imu.imustnt;

import imu.iAPI.Config.ConfigMenu;
import imu.iAPI.CustomItems.CustomItems;
import imu.iAPI.Utilities.DataUtils;
import imu.iAPI.Utilities.ItemUtils;
import imu.iAPI.Utilities.TextUtils;
import imu.imustnt.engine.LayerRule;
import imu.imustnt.engine.TntType;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;

/**
 * Special TNTs lit with flint and steel: Chunk TNT clears one layer of a chunk, Chunk Stone TNT turns it into
 * stone. Shops (imusGS) can sell them through ImusAPI's custom items.
 */
public class ImusTNT extends JavaPlugin
{
    /** Where an item keeps its type: imusapi:tnt_type, as the old imusTNT wrote it */
    static final String ITEM_KEY = "tnt_type";

    private final TntItems _items = new TntItems(this);
    private ConfigMenu _configMenu;
    private LayerRule _rule;

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        readRule();

        getServer().getPluginManager().registerEvents(new TntListener(this, new PlacedTnt(this)), this);
        TntCommands commands = new TntCommands(this);
        PluginCommand command = getCommand("imustnt");
        command.setExecutor(commands);
        command.setTabCompleter(commands);
        CustomItems.register(_items);

        _configMenu = new ConfigMenu(this, "imusTNT", "imustnt.admin", this::reloadSettings);
        _configMenu.addInt("fuse-ticks", "Fuse", Material.FLINT_AND_STEEL, 1, 400)
                .description("Ticks from lighting to the blast, 20 = one second");
        _configMenu.addInt("layers", "Layers", Material.LADDER, 1, 16)
                .description("Block layers the blast reaches, down from the TNT's own");
        _configMenu.addDouble("blast-resistance-limit", "Blast resistance limit", Material.OBSIDIAN, 0, 4_000_000)
                .description("Blocks this blast resistant or more stay");
        _configMenu.addBoolean("keep-usable-blocks", "Keep usable blocks", Material.CHEST)
                .description("Chests, furnaces, doors and other usable blocks stay");
        _configMenu.register();
    }

    @Override
    public void onDisable()
    {
        if (_configMenu != null)
            _configMenu.unregister();
        CustomItems.unregister(_items);
    }

    private void reloadSettings()
    {
        reloadConfig();
        readRule();
    }

    private void readRule()
    {
        _rule = new LayerRule(getConfig().getDouble("blast-resistance-limit", 1200),
                new HashSet<>(getConfig().getStringList("protected-blocks")),
                getConfig().getBoolean("keep-usable-blocks", true));
    }

    public LayerRule rule()
    {
        return _rule;
    }

    public int layers()
    {
        return Math.max(1, getConfig().getInt("layers", 1));
    }

    /**
     * A fresh stack of the TNT.
     */
    public ItemStack item(TntType type, int amount)
    {
        ItemStack stack = new ItemStack(Material.TNT, Math.max(1, Math.min(64, amount)));
        String path = "items." + type.key() + ".";
        ItemUtils.SetDisplayName(stack, getConfig().getString(path + "name", type.key()));
        String size = "16x" + layers() + "x16";
        List<String> lore = getConfig().getStringList(path + "lore").stream().map(line -> line.replace("{size}", size)).toList();
        ItemUtils.SetLores(stack, lore, false);
        DataUtils.SetPersistentData(stack, ITEM_KEY, PersistentDataType.STRING, type.name());
        return stack;
    }

    /**
     * The special TNT the item is, or null.
     */
    public TntType typeOf(ItemStack stack)
    {
        if (stack == null || stack.getType() != Material.TNT || !stack.hasItemMeta())
            return null;
        return TntType.parse(DataUtils.GetPersistentData(stack, ITEM_KEY, PersistentDataType.STRING));
    }

    public String text(String key, Object... placeholders)
    {
        String text = getConfig().getString("messages." + key, key);
        for (int i = 0; i + 1 < placeholders.length; i += 2)
            text = text.replace("{" + placeholders[i] + "}", String.valueOf(placeholders[i + 1]));
        return text;
    }

    /**
     * Sends a message of config.yml; an empty one isn't sent.
     */
    public void send(CommandSender to, String key, Object... placeholders)
    {
        String text = text(key, placeholders);
        if (!text.isEmpty())
            to.sendMessage(TextUtils.Color(text));
    }
}
