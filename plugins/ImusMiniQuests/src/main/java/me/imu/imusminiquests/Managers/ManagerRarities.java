package me.imu.imusminiquests.Managers;

import imu.iAPI.Other.Metods;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Quests.Luck;
import me.imu.imusminiquests.Quests.PanelRarity;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Panel rarities from config.yml (rarities). Every new panel rolls one by weight. Panels made
 * before rarities existed, or whose rarity was removed from the config, count as the most
 * common one.
 */
public class ManagerRarities
{
    private static final PanelRarity FALLBACK = new PanelRarity("common", Metods.msgC("&fCommon"), 1, 1, 0, 0);

    private final ImusMiniQuests _plugin;
    private final Map<String, PanelRarity> _rarities = new LinkedHashMap<>();
    private PanelRarity _common = FALLBACK;

    public ManagerRarities(ImusMiniQuests plugin)
    {
        _plugin = plugin;
    }

    public void load()
    {
        _rarities.clear();
        ConfigurationSection section = _plugin.getConfig().getConfigurationSection("rarities");
        if (section != null)
        {
            for (String name : section.getKeys(false))
            {
                ConfigurationSection entry = section.getConfigurationSection(name);
                if (entry == null) continue;

                int weight = entry.getInt("weight", 0);
                if (weight <= 0)
                {
                    _plugin.getLogger().warning("config.yml rarities." + name + ": weight must be above 0, skipped");
                    continue;
                }
                String key = name.toLowerCase(Locale.ROOT);
                _rarities.put(key, new PanelRarity(key,
                        Metods.msgC(entry.getString("display", name)),
                        weight,
                        Math.max(0.01, entry.getDouble("amount-multiplier", 1)),
                        Math.max(0, entry.getDouble("bonus-luck", 0)),
                        Math.max(0, entry.getInt("extra-random-rewards", 0))));
            }
        }

        _common = _rarities.values().stream().max(Comparator.comparingInt(PanelRarity::weight)).orElse(FALLBACK);
    }

    /** A random rarity by weight. Luck isn't used: who finds a panel doesn't change what it is. */
    public PanelRarity roll()
    {
        if (_rarities.isEmpty()) return _common;
        return Luck.pick(new ArrayList<>(_rarities.values()), PanelRarity::weight, 0);
    }

    /** The rarity with this name, or the most common one when there is none. */
    public PanelRarity get(String name)
    {
        PanelRarity rarity = name == null ? null : _rarities.get(name.toLowerCase(Locale.ROOT));
        return rarity != null ? rarity : _common;
    }

    public PanelRarity getCommon() {return _common;}

    public Collection<PanelRarity> getRarities() {return Collections.unmodifiableCollection(_rarities.values());}

    public Set<String> getNames() {return Collections.unmodifiableSet(_rarities.keySet());}
}
