package imu.GS.live;

import imu.GS.GsService;
import imu.GS.bukkit.FlashItems;
import imu.GS.bukkit.ItemKeys;
import imu.GS.data.FlashDealCodec;
import imu.GS.engine.ItemKey;
import imu.GS.engine.live.FlashDeal;
import imu.GS.engine.live.FlashDeals;
import imu.GS.engine.live.FlashOffer;
import imu.GS.engine.live.LiveRules;
import imu.GS.engine.live.ServerProgress;
import imu.GS.engine.shop.Shop;
import imu.GS.presets.ShopPreset;
import imu.GS.text.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * Makes shops feel alive: flash deals, rotating stock, and items players gather turning up in a shop now and
 * then. Settings are under "live" in config.yml; a shop takes part when its own Live Events switch is on.
 */
public class LiveEvents implements Listener
{
    private static final long MINUTE = 60_000L;

    private final GsService _service;
    private final FlashDeals _deals = new FlashDeals();
    private final PlacedBlocks _placed = new PlacedBlocks(50_000);
    private final Random _random = new Random();
    private final ProgressTracker _tracker;
    private long _nextFlashMillis;
    private long _nextRotationMillis;
    private int _flashTurn;
    private boolean _dealsDirty;

    public LiveEvents(GsService service)
    {
        _service = service;
        _deals.addListener(shopId -> _dealsDirty = true);
        _tracker = new ProgressTracker(service);
    }

    /**
     * When the next flash deals start, 0 before the first tick.
     */
    public long getNextFlashMillis()
    {
        return _nextFlashMillis;
    }

    public long getNextRotationMillis()
    {
        return _nextRotationMillis;
    }

    public ProgressTracker getTracker()
    {
        return _tracker;
    }

    public FlashDeals getDeals()
    {
        return _deals;
    }

    private ConfigurationSection config(String part)
    {
        ConfigurationSection section = _service.getPlugin().getConfig().getConfigurationSection("live." + part);
        return section != null ? section : new org.bukkit.configuration.MemoryConfiguration();
    }

    /**
     * Shops that take part in live events: their switch is on and they aren't locked.
     */
    public List<Shop> liveShops()
    {
        List<Shop> shops = new ArrayList<>();
        for (Shop shop : _service.getShops())
        {
            if (shop.getSettings().liveEvents() && !shop.getSettings().locked())
                shops.add(shop);
        }
        return shops;
    }

    private int between(int min, int max)
    {
        return min >= max ? min : min + _random.nextInt(max - min + 1);
    }

    // Saving =========================================================================================================

    /**
     * Restores saved flash deals that are still on. Call it once the shops are loaded.
     */
    public void load(Map<UUID, String> saved)
    {
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, String> entry : saved.entrySet())
        {
            try
            {
                FlashDeal deal = FlashDealCodec.fromJson(entry.getKey(), entry.getValue());
                if (_service.getShop(entry.getKey()) != null && deal.isActive(now))
                    _deals.start(deal);
            } catch (RuntimeException e)
            {
                _service.getPlugin().getLogger().log(Level.WARNING, "Skipped a broken flash deal of shop " + entry.getKey(), e);
            }
        }
        _dealsDirty = false;
    }

    /**
     * Saves the flash deals when they changed.
     */
    public void save(Runnable onFailure)
    {
        if (!_dealsDirty)
            return;
        _dealsDirty = false;
        Map<UUID, String> rows = new LinkedHashMap<>();
        for (FlashDeal deal : _deals.all())
            rows.put(deal.shopId(), FlashDealCodec.toJson(deal));
        _service.getDatabase().saveFlashDeals(rows, onFailure);
    }

    public void markDirty()
    {
        _dealsDirty = true;
    }

    // Timers =========================================================================================================

    /**
     * Ends flash deals whose time is over and starts new deals and rotations when their time has come.
     */
    public void tick(long now)
    {
        _deals.removeEnded(now);

        ConfigurationSection flash = config("flash-deals");
        if (flash.getBoolean("enabled", true))
        {
            if (_nextFlashMillis == 0)
                _nextFlashMillis = now + nextFlashDelay(flash);
            else if (now >= _nextFlashMillis)
            {
                _nextFlashMillis = now + nextFlashDelay(flash);
                startFlashDeals(flash, now);
            }
        }

        ConfigurationSection rotation = config("rotation");
        if (rotation.getBoolean("enabled", true))
        {
            long interval = Math.max(1, rotation.getInt("interval-minutes", 180)) * MINUTE;
            if (_nextRotationMillis == 0)
                _nextRotationMillis = now + interval;
            else if (now >= _nextRotationMillis)
            {
                _nextRotationMillis = now + interval;
                liveShops().forEach(this::rotate);
            }
        }
    }

    /**
     * In "one-at-a-time" mode the next shop in turn gets a deal, otherwise every shop without one.
     */
    private void startFlashDeals(ConfigurationSection flash, long now)
    {
        List<Shop> shops = new ArrayList<>(liveShops());
        shops.removeIf(shop -> _deals.active(shop.getId(), now) != null);
        if (shops.isEmpty())
            return;
        if (!flash.getString("mode", "one-at-a-time").equalsIgnoreCase("one-at-a-time"))
        {
            for (Shop shop : shops)
                startFlashDeal(shop, now, true);
            return;
        }
        // Each shop in turn; a shop with nothing to offer passes its turn on
        for (int tries = 0; tries < shops.size(); tries++)
        {
            Shop shop = shops.get(Math.floorMod(_flashTurn++, shops.size()));
            if (startFlashDeal(shop, now, true) != null)
                return;
        }
    }

    private long nextFlashDelay(ConfigurationSection flash)
    {
        int min = Math.max(1, flash.getInt("interval-minutes-min", 120));
        int max = Math.max(min, flash.getInt("interval-minutes-max", 300));
        return between(min, max) * MINUTE;
    }

    // Rotating stock =================================================================================================

    /**
     * The shop's preset, null for a general store (which follows config.yml).
     */
    public ShopPreset presetOf(Shop shop)
    {
        return shop.getSettings().isGeneral() ? null : _service.getPresets().get(shop.getSettings().kind());
    }

    /**
     * The material tags the shop's rotating stock and flash deals come from.
     */
    private List<String> tagsOf(ShopPreset preset, ConfigurationSection fallback)
    {
        return preset != null && !preset.tags().isEmpty() ? preset.tags() : fallback.getStringList("tags");
    }

    private double valueScale(ShopPreset preset)
    {
        return config("rotation").getDouble("value-scale", 5) * (preset == null ? 1 : preset.quality());
    }

    /**
     * Enchanted gear and books follow the flash deal progress setting, wherever they are sold.
     */
    private ServerProgress gearProgress()
    {
        return _tracker.appliesTo("flash-deals") ? _tracker.getProgress() : null;
    }

    private List<Material> gearOf(ShopPreset preset, Predicate<Material> allowed)
    {
        if (preset != null && !preset.gear().isEmpty())
            return preset.gear().stream().filter(allowed).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        return materials(config("flash-deals").getStringList("gear"), allowed);
    }

    /**
     * Brings a few random items to the shop: of its preset's tags (or the rotation tags), and for a blacksmith or
     * a librarian enchanted gear or books. Returns how many kinds came.
     */
    public int rotate(Shop shop)
    {
        ConfigurationSection rotation = config("rotation");
        ShopPreset preset = presetOf(shop);
        int count = preset != null ? between(preset.rotationMin(), preset.rotationMax())
                : between(rotation.getInt("items-min", 3), rotation.getInt("items-max", 6));
        Predicate<Material> allowed = m -> _tracker.allows("rotation", m);
        int added = 0;

        int special = preset == null ? 0 : preset.books() ? count : (int) Math.round(count * preset.rotationGearShare());
        List<Material> gear = gearOf(preset, m -> _tracker.allows("flash-deals", m));
        ConfigurationSection flash = config("flash-deals");
        for (int i = 0; i < special; i++)
        {
            ItemStack item = preset.books() ? GearGenerator.book(gearProgress(), flash.getDouble("over-max-level-chance", 0.15), _random)
                    : gear.isEmpty() ? null : GearGenerator.generate(gear.get(_random.nextInt(gear.size())), flash.getInt("gear-enchants-min", 2),
                    flash.getInt("gear-enchants-max", 5), flash.getDouble("over-max-level-chance", 0.15), gearProgress(), _random);
            if (item == null)
                continue;
            ItemKey key = ItemKeys.of(item);
            if (LiveRules.restock(shop, key, 1, 1, _service.getPrices().priceForSoldItem(key)) > 0)
                added++;
        }

        double budget = rotation.getDouble("budget", 2000) * (preset == null ? 1 : preset.quality());
        List<Material> picked = preset != null && preset.books() ? List.of()
                : ItemPool.pick(_service.getMaterials(), tagsOf(preset, rotation), valueScale(preset), count - special, allowed, _random);
        for (Material material : picked)
        {
            ItemStack stack = new ItemStack(material);
            ItemKey item = ItemKeys.of(stack);
            int amount = LiveRules.restockAmount(_service.getRules().value(stack), budget, material.getMaxStackSize());
            if (LiveRules.restock(shop, item, amount, amount, _service.getPrices().priceForSoldItem(item)) > 0)
                added++;
        }
        return added;
    }

    // Flash deals ====================================================================================================

    /**
     * Starts a new flash deal in the shop, replacing one that is on. With the progress check on, it offers only
     * what players have had. Returns null, and starts nothing, when there is nothing to offer.
     *
     * @param announce tell every player that deals are on
     */
    public FlashDeal startFlashDeal(Shop shop, long now, boolean announce)
    {
        ConfigurationSection flash = config("flash-deals");
        ShopPreset preset = presetOf(shop);
        int count = between(flash.getInt("offers-min", 4), flash.getInt("offers-max", 7));
        double gearShare = preset != null && preset.flashGearShare() != null ? preset.flashGearShare() : flash.getDouble("gear-share", 0.4);
        int gearCount = preset != null && preset.books() ? count : (int) Math.round(count * gearShare);
        Predicate<Material> allowed = m -> _tracker.allows("flash-deals", m);
        ServerProgress progress = gearProgress();

        List<FlashOffer> offers = new ArrayList<>();
        List<Material> gear = gearOf(preset, allowed);
        List<Material> specials = preset == null || preset.specials() ? materials(flash.getStringList("special-items"), allowed) : List.of();
        for (int i = 0; i < gearCount; i++)
        {
            FlashOffer offer = null;
            if (preset != null && preset.books())
            {
                ItemStack book = GearGenerator.book(progress, flash.getDouble("over-max-level-chance", 0.15), _random);
                if (book != null)
                    offer = offer(shop, book, between(flash.getInt("gear-stock-min", 2), flash.getInt("gear-stock-max", 5)),
                            flash.getInt("gear-player-limit", 2), flash);
            } else if (!specials.isEmpty() && (gear.isEmpty() || _random.nextDouble() < flash.getDouble("special-chance", 0.2)))
            {
                ItemStack special = new ItemStack(specials.get(_random.nextInt(specials.size())));
                offer = offer(shop, special, between(flash.getInt("special-stock-min", 2), flash.getInt("special-stock-max", 4)),
                        flash.getInt("special-player-limit", 2), flash);
            } else if (!gear.isEmpty())
            {
                ItemStack item = GearGenerator.generate(gear.get(_random.nextInt(gear.size())), flash.getInt("gear-enchants-min", 2),
                        flash.getInt("gear-enchants-max", 5), flash.getDouble("over-max-level-chance", 0.15), progress, _random);
                offer = offer(shop, item, between(flash.getInt("gear-stock-min", 2), flash.getInt("gear-stock-max", 5)),
                        flash.getInt("gear-player-limit", 2), flash);
            }
            if (offer != null)
                offers.add(offer);
        }

        // The rest are blocks and items, now and then a fun one (TNT, fireworks...)
        int blockCount = count - offers.size();
        List<Material> fun = preset == null || preset.fun() ? materials(flash.getStringList("fun-items"), allowed) : new ArrayList<>();
        Collections.shuffle(fun, _random);
        List<Material> picked = new ArrayList<>();
        for (int i = 0; i < blockCount && !fun.isEmpty(); i++)
        {
            if (_random.nextDouble() < flash.getDouble("fun-chance", 0.3))
                picked.add(fun.removeFirst());
        }
        if (preset == null || !preset.books())
            picked.addAll(ItemPool.pick(_service.getMaterials(), tagsOf(preset, flash), valueScale(preset),
                    blockCount - picked.size(), allowed.and(m -> !picked.contains(m)), _random));
        for (Material material : picked)
        {
            int stock = between(flash.getInt("block-stacks-min", 2), flash.getInt("block-stacks-max", 6)) * material.getMaxStackSize();
            FlashOffer offer = offer(shop, new ItemStack(material), stock, flash.getInt("block-player-limit", 128), flash);
            if (offer != null)
                offers.add(offer);
        }
        if (offers.isEmpty())
            return null;
        Collections.shuffle(offers, _random);

        long duration = Math.max(1, flash.getInt("duration-minutes", 20)) * MINUTE;
        FlashDeal deal = new FlashDeal(UUID.randomUUID(), shop.getId(), now, now + duration, offers);
        _deals.start(deal);
        if (announce)
            announce(shop, deal, now);
        return deal;
    }

    public void endFlashDeal(Shop shop)
    {
        _deals.remove(shop.getId());
    }

    private FlashOffer offer(Shop shop, ItemStack item, int stock, int playerLimit, ConfigurationSection flash)
    {
        double normal = _service.getRules().value(item) * shop.getSettings().sellMultiplier();
        if (normal <= 0 || stock <= 0)
            return null;
        double sellBack = _service.getPrices().sellUnitPrice(shop, ItemKeys.of(item));
        double discount = between(flash.getInt("discount-min", 20), flash.getInt("discount-max", 50)) / 100.0;
        double price = LiveRules.flashPrice(normal, sellBack, discount);
        ItemKey marked = ItemKeys.of(FlashItems.mark(item));
        return new FlashOffer(UUID.randomUUID(), marked, stock, price, Math.min(stock, Math.max(1, playerLimit)));
    }

    private static List<Material> materials(List<String> names, Predicate<Material> allowed)
    {
        List<Material> materials = new ArrayList<>();
        for (String name : names)
        {
            Material material = Material.matchMaterial(name);
            if (material != null && material.isItem() && allowed.test(material))
                materials.add(material);
        }
        return materials;
    }

    /**
     * Tells everyone deals are on, without saying what they are, with a [Peek] at a couple of them.
     */
    private void announce(Shop shop, FlashDeal deal, long now)
    {
        long minutes = Math.max(1, Math.round(deal.millisLeft(now) / (double) MINUTE));
        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();
        Component message = legacy.deserialize(Messages.get("flash.announce", "shop", shop.getDisplayName(), "minutes", minutes))
                .append(legacy.deserialize(Messages.get("flash.peek-button"))
                        .clickEvent(ClickEvent.runCommand("/gs peek " + shop.getName()))
                        .hoverEvent(HoverEvent.showText(legacy.deserialize(Messages.get("flash.peek-hover")))));
        Bukkit.broadcast(message);
        for (Player player : Bukkit.getOnlinePlayers())
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.5f);
    }

    // Found items ====================================================================================================

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e)
    {
        // Crops are planted to be harvested; harvesting them counts
        if (!(e.getBlockPlaced().getBlockData() instanceof Ageable))
            _placed.add(e.getBlockPlaced());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDrops(BlockDropItemEvent e)
    {
        Block block = e.getBlock();
        if (_placed.remove(block) || !config("found-items").getBoolean("mining", true))
            return;
        for (Item item : e.getItems())
            found(item.getItemStack());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMobDeath(EntityDeathEvent e)
    {
        if (e.getEntity() instanceof Player || e.getEntity().getKiller() == null || !config("found-items").getBoolean("mobs", true))
            return;
        for (ItemStack drop : e.getDrops())
            found(drop);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(PlayerFishEvent e)
    {
        if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH && e.getCaught() instanceof Item item
                && config("found-items").getBoolean("fishing", true))
            found(item.getItemStack());
    }

    private void found(ItemStack stack)
    {
        ConfigurationSection found = config("found-items");
        if (stack == null || stack.getType().isAir() || !found.getBoolean("enabled", true) || !_service.isLoaded())
            return;
        double value = _service.getRules().value(stack);
        if (value <= 0)
            return;
        if (_random.nextDouble() >= LiveRules.foundChance(value, found.getDouble("base-chance", 0.006), found.getDouble("value-scale", 5)))
            return;

        List<Shop> shops = liveShops();
        if (shops.isEmpty())
            return;
        ItemKey item = ItemKeys.of(stack.asQuantity(1));
        int cap = found.getInt("max-per-item", 64);

        // The shops the item belongs to by its tags, and the general stores
        java.util.Set<String> itemTags = _service.getMaterials().get(stack.getType()).getTags();
        List<Shop> specialists = new ArrayList<>();
        List<Shop> general = new ArrayList<>();
        List<String> matchedTags = new ArrayList<>();
        for (Shop shop : shops)
        {
            ShopPreset preset = presetOf(shop);
            if (preset == null)
            {
                general.add(shop);
                continue;
            }
            for (String tag : preset.foundOrShopTags())
            {
                if (itemTags.contains(tag))
                {
                    specialists.add(shop);
                    matchedTags.add(tag);
                    break;
                }
            }
        }

        boolean toGeneral = specialists.isEmpty() || (!general.isEmpty() && _random.nextDouble() < found.getDouble("general-share", 0.3));
        List<Shop> targets = toGeneral ? (general.isEmpty() ? shops : general) : specialists;
        Shop shop = targets.get(_random.nextInt(targets.size()));
        LiveRules.restock(shop, item, stack.getAmount(), cap, _service.getPrices().priceForSoldItem(item));

        // Now and then the general store gets something else of the same kind, so it always has a bit of everything
        if (!toGeneral && !general.isEmpty() && _random.nextDouble() < found.getDouble("echo-chance", 0.2))
        {
            Material original = stack.getType();
            List<Material> echo = ItemPool.pick(_service.getMaterials(), matchedTags, valueScale(null), 1,
                    m -> m != original && _tracker.getProgress().hasSeen(m.name()), _random);
            if (!echo.isEmpty())
            {
                ItemKey echoItem = ItemKeys.of(new ItemStack(echo.getFirst()));
                LiveRules.restock(general.get(_random.nextInt(general.size())), echoItem, 1, cap, _service.getPrices().priceForSoldItem(echoItem));
            }
        }
    }
}
