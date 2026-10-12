package imu.GS;

import imu.GS.bukkit.ItemKeys;
import imu.GS.command.GetPriceCommand;
import imu.GS.command.GsCommands;
import imu.GS.command.LiveCommands;
import imu.GS.command.PricingCommands;
import imu.GS.command.ItemSelection;
import imu.GS.command.OpenForCommand;
import imu.GS.command.StatsCommands;
import imu.GS.command.SubCommand;
import imu.GS.text.Messages;
import imu.GS.npc.ShopNpcAction;
import imu.iAPI.Config.ConfigMenu;
import imu.iAPI.Handelers.CommandHandler;
import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.ImusTabCompleter;
import imu.iAPI.Other.MySQL;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * imusGS: general stores that buy and sell items. The shop logic is in {@link imu.GS.engine}; this wires it to the
 * server: {@link GsService} runs the shops, menus are in {@link imu.GS.menu}, commands in {@link imu.GS.command}.
 */
public class ImusGS extends JavaPlugin
{
    private GsService _service;
    private ConfigMenu _configMenu;
    private GsCommands _commands;
    private ImusTabCompleter _tabCompleter;

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        // Settings added in an update go into an existing config.yml
        getConfig().options().copyDefaults(true);
        saveConfig();
        ItemKeys.setLogger(getLogger());
        Messages.load(this);

        _service = new GsService(this, new MySQL(this, 10, "imusGS"));
        getServer().getPluginManager().registerEvents(_service, this);
        getServer().getPluginManager().registerEvents(_service.getLive(), this);
        getServer().getPluginManager().registerEvents(_service.getLive().getTracker(), this);

        ImusAPI._instance.GetNpcManager().registerAction(new ShopNpcAction(_service));

        _configMenu = createConfigMenu();
        _configMenu.register();
        _commands = new GsCommands(_service, _configMenu);
        registerCommands();

        _service.start();
    }

    @Override
    public void onDisable()
    {
        if (_configMenu != null)
            _configMenu.unregister();
        if (ImusAPI._instance != null && ImusAPI._instance.GetNpcManager() != null)
            ImusAPI._instance.GetNpcManager().unregisterAction(ShopNpcAction.ID);
        // Saves every changed shop and waits for the database; ImusAPI closes the connection pool after this
        if (_service != null)
            _service.stop();
    }

    public GsService getService()
    {
        return _service;
    }

    public void reloadSettings()
    {
        reloadConfig();
        Messages.load(this);
        _service.getMarket().reload();
        _service.restartTimers();
        _service.refreshMenus();
    }

    // Commands =======================================================================================================

    private void registerCommands()
    {
        CommandHandler handler = new CommandHandler(this);
        String gs = "gs";
        handler.registerCmd(gs, new CommandInterface()
        {
            @Override
            public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
            {
                if (args.length > 0)
                    return false; // a sub command
                _commands.help((Player) sender);
                return true;
            }

            @Override
            public void FailedMsg(CommandSender sender, String msg)
            {
            }
        });

        sub(handler, gs, "create shop", "gs.create.shop", _commands::createShop);
        sub(handler, gs, "open shop", "gs.open.shop", _commands::openShop);
        sub(handler, gs, "modify shop", "gs.modify.shop", _commands::modifyShop);
        sub(handler, gs, "create npc", "gs.create.npc", _commands::createNpc);
        sub(handler, gs, "add", "gs.add", _commands::add);
        sub(handler, gs, "create unique", "gs.create.uniques", _commands::createUnique);
        sub(handler, gs, "modify uniques", "gs.modify.uniques", _commands::modifyUniques);
        sub(handler, gs, "modify enchantments", "gs.modify.enchants", _commands::modifyEnchantments);
        sub(handler, gs, "set material price", "gs.set.material.price", _commands::setMaterialPrice);
        sub(handler, gs, "set material overflow", "gs.set.material.overflow", _commands::setMaterialOverflow);
        sub(handler, gs, "unset material overflow", "gs.unset.material.overflow", _commands::unsetMaterialOverflow);
        sub(handler, gs, "set material smart_price", "gs.set.material.smart_price", _commands::setSmartPrice);
        sub(handler, gs, "unset material smart_price", "gs.unset.material.smart_price", _commands::unsetSmartPrice);
        sub(handler, gs, "tag", "gs.tag", _commands::tag);
        sub(handler, gs, "config", "gs.config", _commands::config);
        PricingCommands pricing = new PricingCommands(_service);
        consoleSub(handler, gs, "profile", "gs.profile", pricing::profile);
        consoleSub(handler, gs, "limits", "gs.limits", pricing::limits);
        consoleSub(handler, gs, "reset limits", "gs.limits", pricing::resetLimits);
        LiveCommands live = new LiveCommands(_service);
        sub(handler, gs, "peek", "gs.peek", live::peek);
        consoleSub(handler, gs, "flash", "gs.flash", live::flash);
        consoleSub(handler, gs, "restock", "gs.flash", live::restock);
        consoleSub(handler, gs, "wanted", "gs.flash", live::wanted);
        consoleSub(handler, gs, "progress", "gs.flash", live::progress);
        consoleSub(handler, gs, "stats", "gs.stats", new StatsCommands(_service)::stats);
        getCommand(gs).setExecutor(handler);

        Map<String, String[]> arguments = new HashMap<>();
        arguments.put(gs, new String[] { "create", "open", "modify", "add", "set", "unset", "tag", "config", "profile", "limits", "reset", "peek", "flash", "wanted", "restock", "progress", "stats" });
        arguments.put("profile", new String[] { "list", "apply" });
        arguments.put("create", new String[] { "shop", "unique", "npc" });
        arguments.put("open", new String[] { "shop" });
        arguments.put("modify", new String[] { "shop", "uniques", "enchantments" });
        arguments.put("add", ItemSelection.NAMES.toArray(new String[0]));
        arguments.put("set", new String[] { "material" });
        arguments.put("unset", new String[] { "material" });
        arguments.put("tag", new String[] { "materials", "shopitems" });
        arguments.put("reset", new String[] { "limits" });
        arguments.put("flash", new String[] { "start", "end" });
        arguments.put("wanted", new String[] { "start", "end" });
        arguments.put("progress", new String[] { "scan", "reset" });
        _tabCompleter = new ImusTabCompleter(gs, new HashMap<>(arguments), "gs.tabcompleter");
        getCommand(gs).setTabCompleter(_tabCompleter);

        String g = "g";
        handler.registerCmd(g, new CommandInterface()
        {
            @Override
            public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
            {
                return args.length > 0 ? false : new GetPriceCommand(_service).onCommand(sender, cmd, label, args);
            }

            @Override
            public void FailedMsg(CommandSender sender, String msg)
            {
            }
        });
        handler.registerSubCmd(g, "getprice", new GetPriceCommand(_service));
        getCommand(g).setExecutor(handler);
        Map<String, String[]> gArguments = new HashMap<>();
        gArguments.put(g, new String[] { "getprice" });
        gArguments.put("getprice", ItemSelection.NAMES.toArray(new String[0]));
        getCommand(g).setTabCompleter(new ImusTabCompleter(g, new HashMap<>(gArguments), null));

        OpenForCommand openFor = new OpenForCommand(_service);
        getCommand("gsopenfor").setExecutor(openFor);
        getCommand("gsopenfor").setTabCompleter(openFor);

        updateTabCompletion();
    }

    private void sub(CommandHandler handler, String root, String name, String permission, java.util.function.BiConsumer<Player, String[]> action)
    {
        handler.registerSubCmd(root, name, SubCommand.players(action));
        handler.setPermissionOnLastCmd(permission);
    }

    /**
     * A sub command the console can run too.
     */
    private void consoleSub(CommandHandler handler, String root, String name, String permission, java.util.function.BiConsumer<CommandSender, String[]> action)
    {
        handler.registerSubCmd(root, name, new SubCommand(action));
        handler.setPermissionOnLastCmd(permission);
        handler.allowConsole(root, name);
    }

    /**
     * Shop names and tags in tab completion; call it when they change.
     */
    public void updateTabCompletion()
    {
        if (_tabCompleter == null || _service == null)
            return;
        List<String> shops = _service.getShopNames();
        List<String> options = ItemSelection.NAMES;
        _tabCompleter.setArgumenrs("shop", shops.toArray(new String[0]));
        _tabCompleter.SetRule("/gs create npc", 3, shops);
        _tabCompleter.SetRule("/gs add", 3, shops);
        _tabCompleter.SetRule("/gs limits", 2, shops);
        _tabCompleter.SetRule("/gs reset limits", 3, shops);
        _tabCompleter.SetRule("/gs peek", 2, shops);
        _tabCompleter.SetRule("/gs restock", 2, shops);
        _tabCompleter.SetRule("/gs flash start", 3, shops);
        _tabCompleter.SetRule("/gs flash end", 3, shops);
        _tabCompleter.SetRule("/gs wanted start", 3, shops);
        _tabCompleter.SetRule("/gs wanted end", 3, shops);

        _tabCompleter.SetRule("/gs profile apply", 3, _service.getProfiles().names());
        _tabCompleter.SetRule("/gs profile apply", 4, List.of("1000", "confirm"));
        _tabCompleter.SetRule("/gs profile apply", 5, List.of("confirm"));

        _tabCompleter.SetRule("/gs set material", 3, List.of("price", "overflow", "smart_price"));
        _tabCompleter.SetRule("/gs set material", 4, options);
        _tabCompleter.SetRule("/gs unset material", 3, List.of("overflow", "smart_price"));
        _tabCompleter.SetRule("/gs unset material", 4, options);

        _tabCompleter.SetRule("/gs tag materials", 3, List.of("add", "remove", "remove_all_tags", "set_price", "increase_price"));
        for (String action : List.of("add", "remove", "remove_all_tags"))
            _tabCompleter.SetRule("/gs tag materials " + action, 4, options);
        List<String> materialTags = new ArrayList<>(_service.getMaterials().allTags());
        _tabCompleter.SetRule("/gs tag materials set_price", 4, materialTags);
        _tabCompleter.SetRule("/gs tag materials increase_price", 4, materialTags);
        _tabCompleter.SetRule("/gs tag shopitems", 3, List.of("set_price", "increase_price"));
        List<String> itemTags = new ArrayList<>(_commands.shopItemTags());
        _tabCompleter.SetRule("/gs tag shopitems set_price", 4, itemTags);
        _tabCompleter.SetRule("/gs tag shopitems increase_price", 4, itemTags);
    }

    private ConfigMenu createConfigMenu()
    {
        ConfigMenu menu = new ConfigMenu(this, "imusGS", "gs.config", this::reloadSettings);
        menu.addString("language", "Language", Material.BOOK)
                .description("Texts players see: en, fi, or your own")
                .description("messages_<code>.yml in the plugin folder")
                .note("now");
        menu.addInt("shop.check-interval-seconds", "Shop check interval", Material.CLOCK, 1, 3600)
                .description("Seconds between stockable refills and expire checks")
                .note("now");
        menu.addInt("shop.save-delay-seconds", "Save delay", Material.WRITABLE_BOOK, 1, 600)
                .description("Changed shops are saved at most")
                .description("this many seconds later")
                .note("now");
        menu.addDouble("shop.click-cooldown-seconds", "Click cooldown", Material.STONE_BUTTON, 0, 5)
                .description("Shortest time between two clicks in a shop")
                .note("now");
        menu.addBoolean("shop.allow-zero-price-sells", "Sell for nothing", Material.CAULDRON)
                .description("Players may sell items worth 0")
                .description("to empty their inventory")
                .note("now");
        menu.addInt("shop.trade-log-days", "Trade log days", Material.BOOK, 0, 3650)
                .description("Trades older than this are deleted")
                .description("0 = keep forever")
                .note("on the next start");
        menu.addBoolean("live.flash-deals.enabled", "Flash deals", Material.NETHER_STAR)
                .description("Limited time offers every few hours")
                .note("now");
        menu.addBoolean("live.progress.flash-deals", "Flash deals follow progress", Material.EXPERIENCE_BOTTLE)
                .description("Only items and enchants non-op players")
                .description("have had can be in flash deals")
                .note("now");
        menu.addBoolean("live.wanted.enabled", "Wanted requests", Material.GOAT_HORN)
                .description("Shops pay a bonus for some items for a while")
                .note("now");
        menu.addBoolean("live.mood.enabled", "Market mood", Material.FILLED_MAP)
                .description("Prices of each kind of item move a little every day")
                .note("now");
        menu.addBoolean("live.rotation.enabled", "Rotating stock", Material.CHEST)
                .description("Random items come to shops every few hours")
                .note("now");
        menu.addBoolean("live.found-items.enabled", "Found items", Material.DIAMOND_PICKAXE)
                .description("What players gather turns up in shops now and then")
                .note("now");
        menu.addDouble("pricing.durability-penalty", "Durability penalty", Material.DAMAGED_ANVIL, 0, 1)
                .description("Damaged items are worth durability left - this")
                .description("0.1 = an item at 80% sells for 70%")
                .note("now");
        menu.addDouble("new-shop.sell-multiplier", "New shop: sell multiplier", Material.EMERALD, 0, 1000)
                .description("Players buy from the shop for price * this")
                .note("shops created from now on");
        menu.addDouble("new-shop.buy-multiplier", "New shop: buy multiplier", Material.GOLD_INGOT, 0, 1000)
                .description("Players sell to the shop for price * this")
                .note("shops created from now on");
        menu.addDouble("new-shop.expire-percent", "New shop: expire share", Material.ROTTEN_FLESH, 0, 1)
                .description("Share of stock that disappears every cooldown")
                .description("0.1 = 10%")
                .note("shops created from now on");
        menu.addInt("new-shop.expire-cooldown-minutes", "New shop: expire cooldown", Material.HOPPER, 0, 10080)
                .description("0 = never")
                .note("shops created from now on");
        return menu;
    }
}
