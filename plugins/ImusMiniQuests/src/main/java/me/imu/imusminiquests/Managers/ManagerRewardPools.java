package me.imu.imusminiquests.Managers;

import imu.iAPI.Other.Metods;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Quests.Luck;
import me.imu.imusminiquests.Quests.QuestReward;
import me.imu.imusminiquests.Quests.RewardTier;
import me.imu.imusminiquests.Quests.RolledReward;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * The shared random rewards from rewards.yml. Every quest rolls these on top of its own rewards
 * (random-rewards.rolls-per-quest in config.yml, or rewards.random-rolls on a quest): first a
 * rarity tier by the tiers' weights, then an entry of that tier by the entries' weights. Luck
 * from quest points makes the rare tiers and entries likelier.
 */
public class ManagerRewardPools
{
    private static final String FILE_NAME = "rewards.yml";

    private final ImusMiniQuests _plugin;
    private final List<RewardTier> _tiers = new ArrayList<>();

    public ManagerRewardPools(ImusMiniQuests plugin)
    {
        _plugin = plugin;
    }

    public void load()
    {
        File file = new File(_plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) _plugin.saveResource(FILE_NAME, false);

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Logger log = _plugin.getLogger();
        _tiers.clear();

        ConfigurationSection tiers = config.getConfigurationSection("tiers");
        if (tiers == null)
        {
            log.warning(FILE_NAME + " has no tiers section, there are no random rewards");
            return;
        }

        for (String name : tiers.getKeys(false))
        {
            ConfigurationSection section = tiers.getConfigurationSection(name);
            if (section == null) continue;

            String context = FILE_NAME + " tier " + name;
            int weight = section.getInt("weight", 0);
            List<QuestReward> rewards = QuestReward.listFromConfig(section, "rewards", log, context);
            if (weight <= 0 || rewards.isEmpty())
            {
                log.warning(context + ": needs a weight above 0 and at least one reward, skipped");
                continue;
            }

            _tiers.add(new RewardTier(name, Metods.msgC(section.getString("display", name)), weight,
                    section.getBoolean("announce", false), rewards));
        }
    }

    public List<RewardTier> getTiers() {return Collections.unmodifiableList(_tiers);}

    /**
     * Picks a tier, then one of its rewards. Null when rewards.yml has no tiers.
     */
    public RolledReward roll(double luck)
    {
        // Locked entries sit out; a tier with nothing unlocked left isn't picked at all
        List<RewardTier> open = _tiers.stream()
                .filter(tier -> tier.rewards().stream().anyMatch(QuestReward::isAvailable))
                .toList();
        RewardTier tier = Luck.pick(open, RewardTier::weight, luck);
        if (tier == null) return null;

        List<QuestReward> rewards = tier.rewards().stream().filter(QuestReward::isAvailable).toList();
        return new RolledReward(Luck.pick(rewards, QuestReward::weight, luck), tier);
    }

    /** How many random rewards a quest without its own rewards.random-rolls gets. */
    public int getDefaultRolls()
    {
        return Math.max(0, _plugin.getConfig().getInt("random-rewards.rolls-per-quest", 2));
    }

    /**
     * The tier names from most to least common, coloured, for the panel lore: "Common to Legendary".
     */
    public String describeRange()
    {
        if (_tiers.isEmpty()) return "";

        List<RewardTier> sorted = new ArrayList<>(_tiers);
        sorted.sort((a, b) -> Integer.compare(b.weight(), a.weight()));
        return sorted.size() == 1
                ? sorted.getFirst().display()
                : sorted.getFirst().display() + Metods.msgC(" &8to ") + sorted.getLast().display();
    }

    /** Chance of a tier in percent at the given luck, for the reward list in /imq menu. */
    public double getTierChancePercent(RewardTier tier, double luck)
    {
        if (tier.rewards().stream().noneMatch(QuestReward::isAvailable)) return 0;

        double total = 0;
        for (RewardTier t : _tiers)
        {
            if (t.rewards().stream().anyMatch(QuestReward::isAvailable)) total += Luck.effectiveWeight(t.weight(), luck);
        }
        return total <= 0 ? 0 : Luck.effectiveWeight(tier.weight(), luck) * 100 / total;
    }
}
