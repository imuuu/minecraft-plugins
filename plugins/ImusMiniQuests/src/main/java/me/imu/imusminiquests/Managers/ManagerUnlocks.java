package me.imu.imusminiquests.Managers;

import imu.iAPI.Other.Metods;
import me.imu.imusminiquests.CONSTANTS;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Quests.TargetSet;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Server-wide unlocks, so quests don't hand out the best things before anyone has earned them.
 * Each unlock in config.yml (netherite, elytra, mending...) names materials, enchants and
 * advancements. Until a player gets one of those in normal play, rewards made of those
 * materials or carrying those enchants are left out of every roll. The first player to get one
 * unlocks it for everyone, silently: only admins are told. It is saved in unlocks.yml.
 * <p>
 * Admins never count: ops, players with imq.unlocks.exempt, and anyone in creative or
 * spectator. Inventories are checked every few seconds, which catches every way of getting an
 * item (mining, chests, trades, crafting...), and advancements unlock right away.
 */
public class ManagerUnlocks implements Listener
{
    private static final String FILE_NAME = "unlocks.yml";
    private static final long SCAN_TICKS = 20L * 5;

    public record Unlock(String name, String display, Set<Material> materials, Set<Enchantment> enchants,
                         Set<NamespacedKey> advancements) {}

    private final ImusMiniQuests _plugin;
    private final Map<String, Unlock> _unlocks = new LinkedHashMap<>();
    private final Set<Material> _lockedMaterials = EnumSet.noneOf(Material.class);
    private final Set<Enchantment> _lockedEnchants = new HashSet<>();
    private YamlConfiguration _data = new YamlConfiguration();
    private BukkitTask _scanTask;

    public ManagerUnlocks(ImusMiniQuests plugin)
    {
        _plugin = plugin;
    }

    private File dataFile() {return new File(_plugin.getDataFolder(), FILE_NAME);}

    public void load()
    {
        _data = YamlConfiguration.loadConfiguration(dataFile());
        _unlocks.clear();

        ConfigurationSection section = _plugin.getConfig().getConfigurationSection("unlocks.list");
        if (section != null)
        {
            for (String name : section.getKeys(false))
            {
                ConfigurationSection entry = section.getConfigurationSection(name);
                if (entry == null) continue;
                String context = "config.yml unlocks.list." + name;

                Set<Material> materials = EnumSet.noneOf(Material.class);
                List<String> items = entry.getStringList("items");
                if (!items.isEmpty())
                    materials.addAll(TargetSet.parseMaterials(items, _plugin.getLogger(), context).getMaterials());

                Set<Enchantment> enchants = new HashSet<>();
                for (String raw : entry.getStringList("enchants"))
                {
                    NamespacedKey key = NamespacedKey.fromString(raw.toLowerCase(Locale.ROOT));
                    Enchantment enchant = key == null ? null : Registry.ENCHANTMENT.get(key);
                    if (enchant == null) _plugin.getLogger().warning(context + ": unknown enchant " + raw);
                    else enchants.add(enchant);
                }

                Set<NamespacedKey> advancements = new HashSet<>();
                for (String raw : entry.getStringList("advancements"))
                {
                    NamespacedKey key = NamespacedKey.fromString(raw.toLowerCase(Locale.ROOT));
                    if (key == null || Bukkit.getAdvancement(key) == null)
                        _plugin.getLogger().warning(context + ": unknown advancement " + raw);
                    else advancements.add(key);
                }

                _unlocks.put(name.toLowerCase(Locale.ROOT), new Unlock(name.toLowerCase(Locale.ROOT),
                        entry.getString("display", name), materials, enchants, advancements));
            }
        }

        rebuildLocked();
        restartScan();
    }

    public void stop()
    {
        if (_scanTask != null) _scanTask.cancel();
        _scanTask = null;
    }

    private boolean isEnabled()
    {
        return _plugin.getConfig().getBoolean("unlocks.enabled", true);
    }

    private void rebuildLocked()
    {
        _lockedMaterials.clear();
        _lockedEnchants.clear();
        if (!isEnabled()) return;

        for (Unlock unlock : _unlocks.values())
        {
            if (isUnlocked(unlock.name())) continue;
            _lockedMaterials.addAll(unlock.materials());
            _lockedEnchants.addAll(unlock.enchants());
        }
    }

    private void restartScan()
    {
        stop();
        if (isEnabled() && hasLocked())
            _scanTask = Bukkit.getScheduler().runTaskTimer(_plugin, this::scanPlayers, SCAN_TICKS, SCAN_TICKS);
    }

    public Collection<Unlock> getUnlocks() {return Collections.unmodifiableCollection(_unlocks.values());}

    public Unlock getUnlock(String name) {return name == null ? null : _unlocks.get(name.toLowerCase(Locale.ROOT));}

    public boolean isUnlocked(String name)
    {
        return _data.getBoolean(name + ".unlocked", false);
    }

    /** Who unlocked it and when, or null when it is still locked or was unlocked by an admin. */
    public String getUnlockedBy(String name)
    {
        return _data.getString(name + ".by");
    }

    public long getUnlockedAt(String name)
    {
        return _data.getLong(name + ".at", 0);
    }

    private boolean hasLocked()
    {
        return _unlocks.keySet().stream().anyMatch(name -> !isUnlocked(name));
    }

    /** True when rewards must not give this material yet. */
    public boolean isLocked(Material material)
    {
        return _lockedMaterials.contains(material);
    }

    /** True when rewards must not give this enchant yet. */
    public boolean isLocked(Enchantment enchantment)
    {
        return _lockedEnchants.contains(enchantment);
    }

    /**
     * Unlocks or locks again. With a player, the server is told they were the first to find it.
     */
    public void setUnlocked(String name, boolean unlocked, Player by)
    {
        Unlock unlock = getUnlock(name);
        if (unlock == null) return;

        if (unlocked)
        {
            _data.set(unlock.name() + ".unlocked", true);
            _data.set(unlock.name() + ".by", by == null ? null : by.getName());
            _data.set(unlock.name() + ".at", System.currentTimeMillis());
        }
        else
        {
            _data.set(unlock.name(), null);
        }

        try
        {
            _data.save(dataFile());
        }
        catch (IOException e)
        {
            _plugin.getLogger().log(Level.WARNING, "Couldn't save " + FILE_NAME, e);
        }

        rebuildLocked();
        restartScan();

        if (unlocked && by != null)
        {
            // A silent upgrade: only admins hear about it, players just start seeing the rewards
            String message = _plugin.getMessage("unlocked")
                    .replace("%player%", by.getName())
                    .replace("%unlock%", Metods.msgC(unlock.display()));
            Bukkit.broadcast(message, CONSTANTS.PERM_UNLOCKS_ADMIN);
            _plugin.getLogger().info(by.getName() + " unlocked " + unlock.name() + " for quest rewards");
        }
    }

    /** Admins and players not playing survival never unlock anything. */
    private static boolean counts(Player player)
    {
        if (player.isOp() || player.hasPermission(CONSTANTS.PERM_UNLOCKS_EXEMPT)) return false;
        return player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE;
    }

    private void scanPlayers()
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (!counts(player)) continue;

            for (ItemStack stack : player.getInventory().getContents())
            {
                if (stack == null || stack.isEmpty()) continue;

                for (Unlock unlock : _unlocks.values())
                {
                    if (!isUnlocked(unlock.name()) && matches(unlock, stack))
                    {
                        setUnlocked(unlock.name(), true, player);
                        if (_scanTask == null) return;
                    }
                }
            }
        }
    }

    private static boolean matches(Unlock unlock, ItemStack stack)
    {
        if (unlock.materials().contains(stack.getType())) return true;
        if (unlock.enchants().isEmpty()) return false;

        // Enchants on the item are read without copying its meta; only enchanted books need it
        for (Enchantment enchant : unlock.enchants())
        {
            if (stack.containsEnchantment(enchant)) return true;
        }
        if (stack.getType() != Material.ENCHANTED_BOOK) return false;

        ItemMeta meta = stack.getItemMeta();
        if (!(meta instanceof EnchantmentStorageMeta storage)) return false;
        for (Enchantment enchant : unlock.enchants())
        {
            if (storage.hasStoredEnchant(enchant)) return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAdvancement(PlayerAdvancementDoneEvent event)
    {
        Player player = event.getPlayer();
        if (!isEnabled() || !counts(player)) return;

        NamespacedKey key = event.getAdvancement().getKey();
        for (Unlock unlock : _unlocks.values())
        {
            if (!isUnlocked(unlock.name()) && unlock.advancements().contains(key))
                setUnlocked(unlock.name(), true, player);
        }
    }
}
