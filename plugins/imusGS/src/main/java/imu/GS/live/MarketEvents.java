package imu.GS.live;

import imu.GS.GsService;
import imu.GS.bukkit.ItemKeys;
import imu.GS.engine.ItemKey;
import imu.GS.engine.live.DailyMood;
import imu.GS.engine.live.WantedRequests;
import imu.GS.engine.pricing.Market;
import imu.GS.engine.shop.Shop;
import imu.GS.text.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * The market side of the live events: wanted requests, where a shop pays a bonus for some items for a while, and
 * the daily mood, where every kind of item is a little up or down in price each day. Settings are under
 * "live.wanted" and "live.mood" in config.yml.
 */
public class MarketEvents implements Market
{
    private static final long MINUTE = 60_000L;

    private final GsService _service;
    private final WantedRequests _wanted = new WantedRequests();
    private final Random _random = new Random();
    private DailyMood _mood;
    private LocalDate _moodDay;
    private final Map<Material, Double> _moodCache = new EnumMap<>(Material.class);
    private long _nextWantedMillis;
    private boolean _dirty;

    public MarketEvents(GsService service)
    {
        _service = service;
    }

    private ConfigurationSection config(String part)
    {
        ConfigurationSection section = _service.getPlugin().getConfig().getConfigurationSection("live." + part);
        return section != null ? section : new MemoryConfiguration();
    }

    /**
     * When shops next start wanting items, 0 before the first tick.
     */
    public long getNextWantedMillis()
    {
        return _nextWantedMillis;
    }

    public WantedRequests getWanted()
    {
        return _wanted;
    }

    // Daily mood =====================================================================================================

    @Override
    public double moodFactor(ItemKey item)
    {
        if (!config("mood").getBoolean("enabled", true))
            return 1.0;
        LocalDate today = LocalDate.now();
        if (!today.equals(_moodDay))
        {
            _moodDay = today;
            _moodCache.clear();
        }
        Material material = ItemKeys.template(item).getType();
        return _moodCache.computeIfAbsent(material, m ->
        {
            String tag = moodTag(m);
            return tag == null ? 1.0 : mood().factor(tag, today);
        });
    }

    /**
     * The first tag of the mood's tag list that the material has, or null.
     */
    private String moodTag(Material material)
    {
        Set<String> tags = _service.getMaterials().get(material).getTags();
        for (String tag : config("mood").getStringList("tags"))
        {
            if (tags.contains(tag.toLowerCase(Locale.ROOT)))
                return tag.toLowerCase(Locale.ROOT);
        }
        return null;
    }

    private DailyMood mood()
    {
        if (_mood == null)
        {
            ConfigurationSection mood = config("mood");
            long seed = mood.getLong("seed", 0);
            if (seed == 0)
            {
                // Each server gets moods of its own
                seed = _random.nextLong();
                _service.getPlugin().getConfig().set("live.mood.seed", seed);
                _service.getPlugin().saveConfig();
            }
            _mood = new DailyMood(seed, mood.getDouble("max-percent", 10) / 100.0);
        }
        return _mood;
    }

    /**
     * Today's factor of every mood tag, in the configured order; empty when the mood is off.
     */
    public Map<String, Double> todaysMood()
    {
        Map<String, Double> moods = new LinkedHashMap<>();
        if (!config("mood").getBoolean("enabled", true))
            return moods;
        LocalDate today = LocalDate.now();
        for (String tag : config("mood").getStringList("tags"))
            moods.put(tag.toLowerCase(Locale.ROOT), mood().factor(tag, today));
        return moods;
    }

    /**
     * Settings changed: the mood is worked out again.
     */
    public void reload()
    {
        _mood = null;
        _moodDay = null;
        _moodCache.clear();
    }

    // Wanted requests ================================================================================================

    @Override
    public double wantedBonus(UUID shopId, ItemKey item)
    {
        if (!config("wanted").getBoolean("enabled", true))
            return 0;
        return _wanted.bonus(shopId, item, System.currentTimeMillis());
    }

    public List<WantedRequests.Wanted> activeWanted(Shop shop)
    {
        if (!config("wanted").getBoolean("enabled", true))
            return List.of();
        return _wanted.active(shop.getId(), System.currentTimeMillis());
    }

    /**
     * Ends requests whose time is over, starts new ones when their time has come, and redraws the menus when the
     * day's mood changes.
     */
    public void tick(long now, List<Shop> liveShops)
    {
        boolean changed = _wanted.removeEnded(now);

        ConfigurationSection wanted = config("wanted");
        if (wanted.getBoolean("enabled", true))
        {
            if (_nextWantedMillis == 0)
                _nextWantedMillis = now + nextWantedDelay(wanted);
            else if (now >= _nextWantedMillis)
            {
                _nextWantedMillis = now + nextWantedDelay(wanted);
                for (Shop shop : liveShops)
                {
                    if (_wanted.active(shop.getId(), now).isEmpty())
                        startWanted(shop, now, true);
                }
                changed = true;
            }
        }

        if (_moodDay != null && !LocalDate.now().equals(_moodDay))
            changed = true;
        if (changed)
        {
            _dirty = true;
            _service.refreshMenus();
        }
    }

    private long nextWantedDelay(ConfigurationSection wanted)
    {
        int min = Math.max(1, wanted.getInt("interval-minutes-min", 180));
        int max = Math.max(min, wanted.getInt("interval-minutes-max", 360));
        return (min + _random.nextInt(max - min + 1)) * MINUTE;
    }

    /**
     * The shop starts wanting a few items, replacing what it wanted before.
     */
    public List<WantedRequests.Wanted> startWanted(Shop shop, long now, boolean announce)
    {
        ConfigurationSection wanted = config("wanted");
        int min = wanted.getInt("items-min", 1);
        int count = min + _random.nextInt(Math.max(1, wanted.getInt("items-max", 2) - min + 1));
        long end = now + Math.max(1, wanted.getInt("duration-minutes", 120)) * MINUTE;
        int bonusMin = wanted.getInt("bonus-min", 30);
        int bonusMax = Math.max(bonusMin, wanted.getInt("bonus-max", 80));

        _wanted.clear(shop.getId());
        List<WantedRequests.Wanted> started = new ArrayList<>();
        for (Material material : ItemPool.pick(_service.getMaterials(), wanted.getStringList("tags"), wanted.getDouble("value-scale", 5), count, m -> true, _random))
        {
            // Bonuses in steps of 5%
            double bonus = (bonusMin + 5 * _random.nextInt((bonusMax - bonusMin) / 5 + 1)) / 100.0;
            WantedRequests.Wanted request = new WantedRequests.Wanted(shop.getId(), ItemKeys.of(new ItemStack(material)), bonus, end);
            _wanted.add(request);
            started.add(request);
        }
        _dirty = true;
        _service.refreshMenus();
        if (announce && !started.isEmpty())
            announce(shop, started, end - now);
        return started;
    }

    public void endWanted(Shop shop)
    {
        _wanted.clear(shop.getId());
        _dirty = true;
        _service.refreshMenus();
    }

    /**
     * "Pumpkin" for PUMPKIN, "Oak Log" for OAK_LOG.
     */
    public static String itemName(Material material)
    {
        StringBuilder name = new StringBuilder();
        for (String word : material.name().toLowerCase(Locale.ROOT).split("_"))
        {
            if (!name.isEmpty())
                name.append(' ');
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.toString();
    }

    private void announce(Shop shop, List<WantedRequests.Wanted> requests, long millis)
    {
        List<String> items = new ArrayList<>();
        for (WantedRequests.Wanted request : requests)
            items.add(Messages.get("market.wanted-item", "item", itemName(ItemKeys.template(request.item()).getType()),
                    "percent", Math.round(request.bonus() * 100)));
        long hours = Math.max(1, Math.round(millis / 3_600_000.0));
        Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(Messages.get("market.wanted-announce",
                "shop", shop.getDisplayName(), "items", String.join(", ", items), "hours", hours));
        Bukkit.broadcast(message);
        for (Player player : Bukkit.getOnlinePlayers())
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 0.8f, 1.2f);
    }

    // Saving =========================================================================================================

    public void load(List<WantedRequests.Wanted> saved)
    {
        long now = System.currentTimeMillis();
        for (WantedRequests.Wanted wanted : saved)
        {
            if (now < wanted.endMillis() && _service.getShop(wanted.shopId()) != null)
                _wanted.add(wanted);
        }
        _dirty = false;
    }

    public void save(Runnable onFailure)
    {
        if (!_dirty)
            return;
        _dirty = false;
        _service.getDatabase().saveWanted(new ArrayList<>(_wanted.all()), onFailure);
    }

    public void markDirty()
    {
        _dirty = true;
    }
}
