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
import imu.GS.engine.shop.Shop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
    private long _nextFlashMillis;
    private long _nextRotationMillis;
    private boolean _dealsDirty;

    public LiveEvents(GsService service)
    {
        _service = service;
        _deals.addListener(shopId -> _dealsDirty = true);
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
                for (Shop shop : liveShops())
                {
                    if (_deals.active(shop.getId(), now) == null)
                        startFlashDeal(shop, now, true);
                }
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

    private long nextFlashDelay(ConfigurationSection flash)
    {
        int min = Math.max(1, flash.getInt("interval-minutes-min", 120));
        int max = Math.max(min, flash.getInt("interval-minutes-max", 300));
        return between(min, max) * MINUTE;
    }

    // Rotating stock =================================================================================================

    /**
     * Brings a few random items of the rotation tags to the shop. Returns how many kinds came.
     */
    public int rotate(Shop shop)
    {
        ConfigurationSection rotation = config("rotation");
        int count = between(rotation.getInt("items-min", 3), rotation.getInt("items-max", 6));
        double budget = rotation.getDouble("budget", 2000);
        List<Material> picked = ItemPool.pick(_service.getMaterials(), rotation.getStringList("tags"),
                rotation.getDouble("value-scale", 5), count, _random);
        int added = 0;
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
     * Starts a new flash deal in the shop, replacing one that is on.
     *
     * @param announce tell every player that deals are on
     */
    public FlashDeal startFlashDeal(Shop shop, long now, boolean announce)
    {
        ConfigurationSection flash = config("flash-deals");
        int count = between(flash.getInt("offers-min", 4), flash.getInt("offers-max", 7));
        int gearCount = (int) Math.round(count * flash.getDouble("gear-share", 0.4));

        List<FlashOffer> offers = new ArrayList<>();
        List<Material> gear = materials(flash.getStringList("gear"));
        List<Material> specials = materials(flash.getStringList("special-items"));
        for (int i = 0; i < gearCount; i++)
        {
            FlashOffer offer;
            if (!specials.isEmpty() && _random.nextDouble() < flash.getDouble("special-chance", 0.2))
            {
                ItemStack special = new ItemStack(specials.get(_random.nextInt(specials.size())));
                offer = offer(shop, special, between(flash.getInt("special-stock-min", 2), flash.getInt("special-stock-max", 4)),
                        flash.getInt("special-player-limit", 2), flash);
            } else if (!gear.isEmpty())
            {
                ItemStack item = GearGenerator.generate(gear.get(_random.nextInt(gear.size())), flash.getInt("gear-enchants-min", 2),
                        flash.getInt("gear-enchants-max", 5), flash.getDouble("over-max-level-chance", 0.15), _random);
                offer = offer(shop, item, between(flash.getInt("gear-stock-min", 2), flash.getInt("gear-stock-max", 5)),
                        flash.getInt("gear-player-limit", 2), flash);
            } else
                offer = null;
            if (offer != null)
                offers.add(offer);
        }

        List<Material> blocks = ItemPool.pick(_service.getMaterials(), flash.getStringList("tags"),
                config("rotation").getDouble("value-scale", 5), count - offers.size(), _random);
        for (Material material : blocks)
        {
            int stock = between(flash.getInt("block-stacks-min", 2), flash.getInt("block-stacks-max", 6)) * material.getMaxStackSize();
            FlashOffer offer = offer(shop, new ItemStack(material), stock, flash.getInt("block-player-limit", 128), flash);
            if (offer != null)
                offers.add(offer);
        }
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

    private static List<Material> materials(List<String> names)
    {
        List<Material> materials = new ArrayList<>();
        for (String name : names)
        {
            Material material = Material.matchMaterial(name);
            if (material != null && material.isItem())
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
        Component shopName = LegacyComponentSerializer.legacyAmpersand().deserialize(shop.getDisplayName());
        Component message = Component.text("⚡ ", NamedTextColor.GOLD)
                .append(Component.text("Flash deals are on in ", NamedTextColor.YELLOW))
                .append(shopName)
                .append(Component.text("! " + minutes + " min left. ", NamedTextColor.YELLOW))
                .append(Component.text("[Peek]", NamedTextColor.AQUA, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/gs peek " + shop.getName()))
                        .hoverEvent(HoverEvent.showText(Component.text("See a few of the deals", NamedTextColor.GRAY))));
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
        Shop shop = shops.get(_random.nextInt(shops.size()));
        ItemKey item = ItemKeys.of(stack.asQuantity(1));
        LiveRules.restock(shop, item, stack.getAmount(), found.getInt("max-per-item", 64), _service.getPrices().priceForSoldItem(item));
    }
}
