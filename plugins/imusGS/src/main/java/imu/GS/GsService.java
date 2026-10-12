package imu.GS;

import imu.GS.text.Messages;
import imu.GS.bukkit.ItemKeys;
import imu.GS.bukkit.PlayerContext;
import imu.GS.bukkit.PlayerInventoryAdapter;
import imu.GS.bukkit.VaultWallet;
import imu.GS.data.GsDatabase;
import imu.GS.data.ListingCodec;
import imu.GS.engine.ItemKey;
import imu.GS.engine.customer.Customer;
import imu.GS.engine.pricing.PriceCalculator;
import imu.GS.engine.pricing.SellLimit;
import imu.GS.engine.pricing.SellPressure;
import imu.GS.engine.live.ServerProgress;
import imu.GS.engine.live.WantedRequests;
import imu.GS.live.LiveEvents;
import imu.GS.live.MarketEvents;
import imu.GS.presets.ShopPreset;
import imu.iAPI.CustomItems.CustomItemProvider;
import imu.iAPI.CustomItems.CustomItems;
import imu.GS.presets.ShopPresets;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Shop;
import org.bukkit.inventory.ItemStack;
import imu.GS.engine.shop.StockRules;
import imu.GS.engine.pricing.PriceRule;
import imu.GS.engine.shop.ShopListener;
import imu.GS.engine.shop.ShopSettings;
import imu.GS.engine.trade.TradeService;
import imu.GS.menu.ShopMenu;
import imu.GS.prices.EnchantPrices;
import imu.GS.prices.MaterialPrices;
import imu.GS.prices.ServerPriceRules;
import imu.GS.prices.UniqueItems;
import imu.GS.prices.profile.PriceProfiles;
import imu.GS.prices.profile.ProfileCalculator;
import imu.iAPI.Other.Methods;
import imu.iAPI.Other.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * The running shops: loads them, ticks them, saves them, and holds the prices and the trade engine.
 * Main thread only, apart from the startup load.
 */
public class GsService implements Listener
{
    private final ImusGS _plugin;
    private final GsDatabase _db;
    private final MaterialPrices _materials = new MaterialPrices();
    private final EnchantPrices _enchants = new EnchantPrices();
    private final UniqueItems _uniques = new UniqueItems();
    private final ServerPriceRules _rules;
    private final SellPressure _pressure = new SellPressure();
    private final PriceCalculator _prices;
    private final VaultWallet _wallet = new VaultWallet();
    private final TradeService _trades;
    private final PriceProfiles _profiles;
    private final ShopPresets _presets;
    private final LiveEvents _live;
    private final MarketEvents _market;

    private final Map<UUID, Shop> _shops = new LinkedHashMap<>();
    private final Set<UUID> _dirty = new HashSet<>();
    private final Map<UUID, ShopMenu> _openMenus = new HashMap<>();
    private final ShopListener _dirtyMarker = new ShopListener()
    {
        @Override
        public void listingChanged(Shop shop, Listing listing)
        {
            _dirty.add(shop.getId());
        }

        @Override
        public void layoutChanged(Shop shop)
        {
            _dirty.add(shop.getId());
        }

        @Override
        public void settingsChanged(Shop shop)
        {
            _dirty.add(shop.getId());
        }
    };

    private boolean _loaded;
    private boolean _pressureDirty;
    private BukkitTask _tickTask;
    private BukkitTask _saveTask;

    public GsService(ImusGS plugin, MySQL sql)
    {
        _plugin = plugin;
        _db = new GsDatabase(sql, plugin.getLogger());
        _rules = new ServerPriceRules(_materials, _enchants, _uniques,
                () -> plugin.getConfig().getDouble("pricing.durability-penalty"));
        _rules.setTierFactors(setting -> plugin.getConfig().getDoubleList("pricing." + setting));
        _prices = new PriceCalculator(_rules, _pressure, System::currentTimeMillis);
        _trades = new TradeService(_prices, _wallet, () -> plugin.getConfig().getBoolean("shop.allow-zero-price-sells", true));
        _trades.addListener((shop, customer, trade) ->
        {
            _db.logTrade(trade, shop.getName());
            _pressureDirty = true;
        });
        _profiles = new PriceProfiles(plugin);
        _presets = new ShopPresets(plugin);
        _live = new LiveEvents(this);
        _market = new MarketEvents(this);
        _prices.setMarket(_market);
    }

    // Lifecycle ======================================================================================================

    /**
     * Reads everything from the database in the background; shops appear when it is done.
     */
    public void start()
    {
        Bukkit.getScheduler().runTaskAsynchronously(_plugin, () ->
        {
            try
            {
                _db.createTables();
                int tradeLogDays = _plugin.getConfig().getInt("shop.trade-log-days", 90);
                if (tradeLogDays > 0)
                {
                    int deleted = _db.deleteOldTrades(tradeLogDays);
                    if (deleted > 0)
                        _plugin.getLogger().info("Deleted " + deleted + " logged trades older than " + tradeLogDays + " days");
                }
                List<GsDatabase.ShopRow> shops = _db.loadShops();
                List<GsDatabase.MaterialRow> materials = _db.loadMaterials();
                EnchantPrices enchants = new EnchantPrices();
                _db.loadEnchants(enchants);
                List<GsDatabase.UniqueRow> uniques = _db.loadUniques();
                Map<String, SellLimit> limits = _db.loadSellLimits();
                List<SellPressure.Entry> pressure = _db.loadSellPressure();
                Map<UUID, String> flashDeals = _db.loadFlashDeals();
                List<WantedRequests.Wanted> wanted = _db.loadWanted();
                ServerProgress progress = new ServerProgress();
                _db.loadProgress(progress);
                Bukkit.getScheduler().runTask(_plugin, () ->
                {
                    _materials.setLimits(limits);
                    pressure.forEach(_pressure::restore);
                    apply(shops, materials, enchants, uniques);
                    _live.load(flashDeals);
                    _market.load(wanted);
                    ServerProgress tracked = _live.getTracker().getProgress();
                    progress.items().forEach(tracked::seeItem);
                    progress.enchants().forEach(tracked::seeEnchant);
                    tracked.takeDirty();
                });
            } catch (SQLException e)
            {
                _plugin.getLogger().log(Level.SEVERE, "Couldn't load the shops from the database; shops are off until a restart", e);
            }
        });
    }

    private void apply(List<GsDatabase.ShopRow> shopRows, List<GsDatabase.MaterialRow> materialRows, EnchantPrices enchants, List<GsDatabase.UniqueRow> uniqueRows)
    {
        for (GsDatabase.MaterialRow row : materialRows)
        {
            Material material = Material.matchMaterial(row.material());
            if (material == null)
                continue;
            _materials.setPrice(material, row.price());
            _materials.setSmartMultiplier(material, row.smartMultiplier());
            _materials.setOverflow(material, row.overflow());
            _materials.setTags(material, row.tags());
            _materials.setLimitGroup(material, row.limitGroup());
        }

        _enchants.getValues().putAll(enchants.getValues());
        _enchants.setEnchantCountPercent(enchants.getEnchantCountPercent());
        _enchants.getCustomPrices().putAll(enchants.getCustomPrices());
        for (imu.GS.prices.GearTier tier : imu.GS.prices.GearTier.values())
            _enchants.setGearMultiplier(tier, enchants.getGearMultiplier(tier));

        for (GsDatabase.UniqueRow row : uniqueRows)
        {
            ItemKey key = new ItemKey(row.item());
            if (ItemKeys.isValid(key))
                _uniques.put(new UniqueItems.Unique(key, ItemKeys.template(key), row.price()));
            else
                _plugin.getLogger().warning("Skipped a unique item that isn't a valid item any more: " + key);
        }

        for (GsDatabase.ShopRow row : shopRows)
        {
            Shop shop = new Shop(row.id(), row.name(), row.displayName(), ListingCodec.settingsFromJson(row.settings()));
            for (GsDatabase.ListingRow listingRow : row.listings())
            {
                try
                {
                    ListingCodec.ListingData data = ListingCodec.fromJson(listingRow.data());
                    Listing listing = new Listing(listingRow.id(), new ItemKey(listingRow.item()), listingRow.amount(), data.price());
                    shop.placeListing(listing, listingRow.page(), listingRow.slot());
                    shop.setStock(listing, data.stock());
                    shop.setRestrictions(listing, data.restrictions());
                    shop.setTags(listing, data.tags());
                    shop.setAmount(listing, listingRow.amount());
                } catch (RuntimeException e)
                {
                    _plugin.getLogger().log(Level.WARNING, "Skipped a broken listing " + listingRow.id() + " of shop " + row.name(), e);
                }
            }
            addShop(shop);
        }
        _dirty.clear();
        _loaded = true;
        if (materialRows.isEmpty())
            applyFirstStartProfile();
        _plugin.getLogger().info("Loaded " + _shops.size() + " shops");
        _plugin.updateTabCompletion();
        restartTimers();
    }

    /**
     * With no material prices saved yet, prices everything with the profile config.yml names, if any.
     */
    private void applyFirstStartProfile()
    {
        String name = _plugin.getConfig().getString("pricing.first-start-profile", "");
        if (name == null || name.isBlank())
            return;
        try
        {
            double diamond = _plugin.getConfig().getDouble("pricing.first-start-diamond-price", 1000);
            ProfileCalculator.Result result = PriceProfiles.calculate(_profiles.load(name), _materials, diamond);
            applyProfile(result);
            _plugin.getLogger().info("No material prices yet: priced " + result.prices().size() + " materials with the profile '"
                    + name + "' (diamond = " + diamond + ")");
        } catch (RuntimeException e)
        {
            _plugin.getLogger().log(Level.WARNING, "Couldn't apply the first start price profile '" + name + "'", e);
        }
    }

    public void restartTimers()
    {
        if (_tickTask != null)
            _tickTask.cancel();
        if (_saveTask != null)
            _saveTask.cancel();

        FileConfiguration config = _plugin.getConfig();
        long tickTicks = Math.max(1, config.getInt("shop.check-interval-seconds")) * 20L;
        Runnable tick = () ->
        {
            long now = System.currentTimeMillis();
            for (Shop shop : _shops.values())
                shop.tick(now);
            _live.tick(now);
            // Only shops that buy can want something
            List<Shop> buying = new ArrayList<>(_live.liveShops());
            buying.removeIf(shop -> shop.getSettings().customersCanOnlyBuy());
            _market.tick(now, buying);
        };
        // Once now, so the live events have their times from the start
        tick.run();
        _tickTask = Bukkit.getScheduler().runTaskTimer(_plugin, tick, tickTicks, tickTicks);

        _live.getTracker().start();

        long saveTicks = Math.max(1, config.getInt("shop.save-delay-seconds")) * 20L;
        _saveTask = Bukkit.getScheduler().runTaskTimer(_plugin, this::saveDirty, saveTicks, saveTicks);
    }

    /**
     * Closes every shop menu and saves every changed shop before returning. Call it in onDisable.
     */
    public void stop()
    {
        if (_tickTask != null)
            _tickTask.cancel();
        if (_saveTask != null)
            _saveTask.cancel();
        for (ShopMenu menu : new ArrayList<>(_openMenus.values()))
            menu.getPlayer().closeInventory();
        _live.getTracker().stop();
        if (_loaded)
            _live.getTracker().scanAll();
        saveDirty();
        _db.close();
    }

    public boolean isLoaded()
    {
        return _loaded;
    }

    // Shops ==========================================================================================================

    public Collection<Shop> getShops()
    {
        return Collections.unmodifiableCollection(_shops.values());
    }

    public Shop getShop(UUID id)
    {
        return _shops.get(id);
    }

    /**
     * The shop with exactly this name, ignoring case and colour codes.
     */
    public Shop getShop(String name)
    {
        String search = plainName(name);
        for (Shop shop : _shops.values())
        {
            if (shop.getName().equalsIgnoreCase(search))
                return shop;
        }
        return null;
    }

    public List<String> getShopNames()
    {
        List<String> names = new ArrayList<>();
        for (Shop shop : _shops.values())
            names.add(shop.getName());
        return names;
    }

    /**
     * Name without colour codes, used to find shops.
     */
    public static String plainName(String displayName)
    {
        return ChatColor.stripColor(Methods.msgC(displayName)).trim();
    }

    /**
     * A new empty shop with the settings new shops start with in config.yml.
     */
    public Shop createShop(String displayName)
    {
        return createShop(plainName(displayName), displayName);
    }

    /**
     * @param name the plain name commands use
     */
    public Shop createShop(String name, String displayName)
    {
        FileConfiguration config = _plugin.getConfig();
        ShopSettings settings = new ShopSettings(
                config.getDouble("new-shop.sell-multiplier"),
                config.getDouble("new-shop.buy-multiplier"),
                config.getDouble("new-shop.expire-percent"),
                config.getInt("new-shop.expire-cooldown-minutes"),
                false, false, true, true);
        Shop shop = new Shop(UUID.randomUUID(), name, displayName, settings);
        addShop(shop);
        saveNow(shop);
        _plugin.updateTabCompletion();
        return shop;
    }

    private void addShop(Shop shop)
    {
        _shops.put(shop.getId(), shop);
        shop.addListener(_dirtyMarker);
    }

    /**
     * A new shop from a preset: its kind, whether players can sell to it, its lasting stock, and a first rotation.
     */
    public Shop createShop(ShopPreset preset, String name, String displayName)
    {
        Shop shop = createShop(name, displayName);
        ShopSettings settings = shop.getSettings();
        shop.setSettings(new ShopSettings(settings.sellMultiplier(), settings.buyMultiplier(), settings.expirePercent(),
                settings.expireCooldownMinutes(), false, preset.buyOnly(), settings.absolutePositions(), true,
                preset.isGeneral() ? null : preset.id()));
        for (ShopPreset.StockItem item : preset.stock())
        {
            ItemStack stack = stockStack(item.item());
            if (stack == null)
            {
                _plugin.getLogger().warning("Preset " + preset.id() + ": no item " + item.item() + " (is its plugin on?)");
                continue;
            }
            Listing listing = new Listing(UUID.randomUUID(), ItemKeys.of(stack), item.max(), PriceRule.MATERIAL);
            shop.addListing(listing);
            shop.setStock(listing, new StockRules(item.max(), item.fill(), item.minutes()));
        }
        _live.rotate(shop);
        saveNow(shop);
        return shop;
    }

    /**
     * A material, or a custom item of another plugin by its id; null when neither.
     */
    private static ItemStack stockStack(String item)
    {
        Material material = Material.matchMaterial(item);
        if (material != null && material.isItem())
            return new ItemStack(material);
        CustomItemProvider.CustomItem custom = CustomItems.item(item);
        return custom == null ? null : custom.stack();
    }

    public ShopPresets getPresets()
    {
        return _presets;
    }

    public void renameShop(Shop shop, String displayName)
    {
        shop.rename(plainName(displayName), displayName);
        _plugin.updateTabCompletion();
    }

    public void deleteShop(Shop shop)
    {
        closeMenus(shop);
        _shops.remove(shop.getId());
        _dirty.remove(shop.getId());
        shop.removeListener(_dirtyMarker);
        _db.deleteShop(shop.getId());
        _live.endFlashDeal(shop);
        _market.endWanted(shop);
        _plugin.updateTabCompletion();
    }

    // Saving =========================================================================================================

    public void saveNow(Shop shop)
    {
        UUID id = shop.getId();
        _dirty.remove(id);
        _db.saveShop(GsDatabase.ShopRow.of(shop), retryLater(() -> _dirty.add(id)));
    }

    private void saveDirty()
    {
        for (UUID id : new ArrayList<>(_dirty))
        {
            Shop shop = _shops.get(id);
            if (shop != null)
                saveNow(shop);
        }
        _dirty.clear();
        if (_pressureDirty)
        {
            _pressureDirty = false;
            _pressure.forgetRefilled(_materials.getLimits(), System.currentTimeMillis());
            _db.saveSellPressure(_pressure.entries(), retryLater(() -> _pressureDirty = true));
        }
        _live.save(retryLater(_live::markDirty));
        _market.save(retryLater(_market::markDirty));
        ServerProgress progress = _live.getTracker().getProgress();
        if (progress.takeDirty())
            _db.saveProgress(progress.items(), progress.enchants(), retryLater(progress::markDirty));
    }

    /**
     * Tells the admin when players can buy items of the shop and sell them back for more, e.g. after a price change.
     */
    public void warnAboutBuyBackProfit(CommandSender admin, Shop shop)
    {
        List<Listing> profitable = new ArrayList<>();
        for (Listing listing : shop.getListings())
        {
            if (_prices.buyBackProfit(shop, listing) > 0)
                profitable.add(listing);
        }
        if (profitable.isEmpty())
            return;
        Listing example = profitable.getFirst();
        admin.sendMessage(Methods.msgC("&c&lWarning: &e" + profitable.size() + " &citem(s) of " + shop.getName()
                + " &csell back for more than they cost, e.g. &e" + ItemKeys.template(example.getItem()).getType()
                + "&c: buy &e" + Methods.Round(_prices.buyUnitPrice(shop, example)) + "&c, sells back for &e"
                + Methods.Round(_prices.sellUnitPrice(shop, example.getItem())) + "&c. Players can make money from nothing."));
    }

    /**
     * For a failed save: marks the data changed again on the main thread, so the next autosave tries again.
     * Nothing to do once the plugin is shutting down; the error is in the log.
     */
    private Runnable retryLater(Runnable markChanged)
    {
        return () ->
        {
            if (!_plugin.isEnabled())
                return;
            try
            {
                Bukkit.getScheduler().runTask(_plugin, markChanged);
            } catch (org.bukkit.plugin.IllegalPluginAccessException e)
            {
                // Disabled in the meantime
            }
        };
    }

    /**
     * Gives every material the price, tags and sell limit group a profile worked out, and replaces the sell limits.
     * Smart prices are removed; overflows stay.
     */
    public void applyProfile(ProfileCalculator.Result result)
    {
        List<Material> changed = new ArrayList<>();
        for (Map.Entry<String, Double> entry : result.prices().entrySet())
        {
            Material material = Material.matchMaterial(entry.getKey());
            if (material == null)
                continue;
            _materials.setPrice(material, entry.getValue());
            _materials.setSmartMultiplier(material, null);
            _materials.setTags(material, result.tags().getOrDefault(entry.getKey(), Set.of()));
            _materials.setLimitGroup(material, result.limitGroups().get(entry.getKey()));
            changed.add(material);
        }
        _materials.setLimits(result.limits());
        _db.saveSellLimits(_materials.getLimits());
        saveMaterials(changed);
        _plugin.updateTabCompletion();
    }

    /**
     * How much of each sell limit group the shop has bought lately: group -> value, refills included.
     */
    public Map<String, Double> sellPressure(Shop shop)
    {
        Map<String, Double> pressure = new java.util.TreeMap<>();
        long now = System.currentTimeMillis();
        _materials.getLimits().forEach((group, limit) -> pressure.put(group, _pressure.get(shop.getId(), group, limit, now)));
        return pressure;
    }

    /**
     * Gives the shop back the full allowance of every sell limit group.
     */
    public void resetSellPressure(Shop shop)
    {
        _pressure.reset(shop.getId());
        _pressureDirty = true;
        saveDirty();
        refreshMenus();
    }

    public PriceProfiles getProfiles()
    {
        return _profiles;
    }

    public void saveMaterials(Collection<Material> materials)
    {
        List<GsDatabase.MaterialRow> rows = new ArrayList<>();
        for (Material material : materials)
            rows.add(GsDatabase.materialRow(material, _materials.get(material)));
        _db.saveMaterials(rows);
        // Tags may have changed, and the mood follows them
        _market.reload();
        refreshMenus();
    }

    public GsDatabase getDatabase()
    {
        return _db;
    }

    // Customers ======================================================================================================

    public Customer customer(Player player, PlayerInventoryAdapter inventory)
    {
        return new Customer(player.getUniqueId(), player.getName(), inventory, new PlayerContext(player));
    }

    /**
     * Opens the shop menu, unless the shop is locked; admins get in anyway.
     */
    public void openShop(Player player, Shop shop)
    {
        if (!_loaded)
        {
            Messages.send(player, "shop.still-opening");
            return;
        }
        if (shop.getSettings().locked() || isBeingEdited(shop))
        {
            Messages.send(player, "shop.closed");
            if (!player.isOp())
                return;
        }
        new ShopMenu(this, shop).open(player);
    }

    public void menuOpened(ShopMenu menu)
    {
        _openMenus.put(menu.getPlayer().getUniqueId(), menu);
    }

    public void menuClosed(ShopMenu menu)
    {
        _openMenus.remove(menu.getPlayer().getUniqueId(), menu);
    }

    /**
     * Closes every customer's menu of the shop, e.g. before an admin changes it.
     */
    public void closeMenus(Shop shop)
    {
        for (ShopMenu menu : new ArrayList<>(_openMenus.values()))
        {
            if (menu.getShop() == shop)
                menu.getPlayer().closeInventory();
        }
    }

    /**
     * Redraws every open shop menu, e.g. after prices changed.
     */
    public void refreshMenus()
    {
        for (ShopMenu menu : new ArrayList<>(_openMenus.values()))
            menu.refresh();
    }

    // Admins editing a shop keep customers out, like a locked shop
    private final Map<UUID, Integer> _editors = new HashMap<>();

    public void startEditing(Shop shop)
    {
        _editors.merge(shop.getId(), 1, Integer::sum);
        closeMenus(shop);
    }

    public void stopEditing(Shop shop)
    {
        _editors.computeIfPresent(shop.getId(), (id, count) -> count <= 1 ? null : count - 1);
    }

    public boolean isBeingEdited(Shop shop)
    {
        return _editors.containsKey(shop.getId());
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e)
    {
        // Items that didn't fit were dropped at the customer's feet; they pick them up after closing the shop
        if (e.getEntity() instanceof Player player && _openMenus.containsKey(player.getUniqueId()))
            e.setCancelled(true);
    }

    // Getters ========================================================================================================

    public ImusGS getPlugin()
    {
        return _plugin;
    }

    public MaterialPrices getMaterials()
    {
        return _materials;
    }

    public EnchantPrices getEnchants()
    {
        return _enchants;
    }

    public UniqueItems getUniques()
    {
        return _uniques;
    }

    public ServerPriceRules getRules()
    {
        return _rules;
    }

    public PriceCalculator getPrices()
    {
        return _prices;
    }

    public LiveEvents getLive()
    {
        return _live;
    }

    public MarketEvents getMarket()
    {
        return _market;
    }

    public TradeService getTrades()
    {
        return _trades;
    }

    public VaultWallet getWallet()
    {
        return _wallet;
    }
}
