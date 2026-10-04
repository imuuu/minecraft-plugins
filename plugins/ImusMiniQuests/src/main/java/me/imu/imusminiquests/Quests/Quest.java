package me.imu.imusminiquests.Quests;

import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Managers.ManagerRewardPools;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * One quest definition from quests.yml. The quest items only store the quest id and their
 * progress, everything else is read from here, so editing quests.yml changes existing items too.
 */
public class Quest
{
    private final String _id;
    private final String _name;
    private final Material _material;
    private final NamespacedKey _model;
    private final List<String> _lore;
    private final int _dropWeight;
    private final QuestObjective _objective;
    private final int _rolls;
    private final List<QuestReward> _poolRewards;
    private final List<QuestReward> _guaranteedRewards;
    // Random rewards from rewards.yml, -1 = random-rewards.rolls-per-quest of config.yml
    private final int _randomRolls;

    public Quest(String id, String name, Material material, NamespacedKey model, List<String> lore, int dropWeight,
                 QuestObjective objective, int rolls, List<QuestReward> poolRewards, List<QuestReward> guaranteedRewards,
                 int randomRolls)
    {
        _id = id;
        _name = name;
        _material = material;
        _model = model;
        _lore = List.copyOf(lore);
        _dropWeight = dropWeight;
        _objective = objective;
        _rolls = rolls;
        _poolRewards = List.copyOf(poolRewards);
        _guaranteedRewards = List.copyOf(guaranteedRewards);
        _randomRolls = randomRolls;
    }

    public String getId() {return _id;}

    /** The display name with colours already translated. */
    public String getName() {return _name;}

    public Material getMaterial() {return _material;}

    /** Item model to show instead of the material's own, or null. */
    public NamespacedKey getModel() {return _model;}

    /** Lore lines with colours already translated. */
    public List<String> getLore() {return _lore;}

    public int getDropWeight() {return _dropWeight;}

    public QuestObjective getObjective() {return _objective;}

    public int getRequiredAmount() {return _objective.amount();}

    /** How many times the reward pool is rolled. */
    public int getRolls() {return _rolls;}

    /** The weighted rewards, of which {@link #getRolls()} are picked. */
    public List<QuestReward> getPoolRewards() {return _poolRewards;}

    public List<QuestReward> getGuaranteedRewards() {return _guaranteedRewards;}

    /**
     * Rolls the reward pool and hands out the result and the guaranteed rewards. Luck (0-1, from
     * the player's quest points) makes rare entries, high amounts and better tool tiers likelier.
     */
    public void giveRewards(Player player, double luck, int extraRandomRolls)
    {
        ImusMiniQuests plugin = ImusMiniQuests.getInstance();
        for (RolledReward rolled : rollRewards(luck, extraRandomRolls))
        {
            String given = rolled.reward().give(player, luck);
            RewardTier tier = rolled.tier();
            if (tier == null || given.isEmpty()) continue;

            // Random rewards are a surprise, so say what came out and how rare it was
            player.sendMessage(plugin.getMessage("random-reward")
                    .replace("%tier%", tier.display())
                    .replace("%reward%", given));
            if (tier.announce())
            {
                Bukkit.broadcastMessage(plugin.getMessage("random-reward-announce")
                        .replace("%player%", player.getName())
                        .replace("%tier%", tier.display())
                        .replace("%reward%", given)
                        .replace("%quest%", _name));
            }
        }
    }

    /**
     * How many random rewards from rewards.yml one opening gives: rewards.random-rolls of the
     * quest, or random-rewards.rolls-per-quest of config.yml when the quest doesn't set it.
     */
    public int getRandomRolls()
    {
        return _randomRolls >= 0 ? _randomRolls : ImusMiniQuests.getInstance().getRewardPools().getDefaultRolls();
    }

    /**
     * The entries one opening would give: every guaranteed reward, the quest's pool rolled
     * {@link #getRolls()} times and {@link #getRandomRolls()} random rewards from rewards.yml.
     */
    public List<RolledReward> rollRewards(double luck)
    {
        return rollRewards(luck, 0);
    }

    /** As rollRewards(luck), with extra random rewards, e.g. from a rare panel. */
    public List<RolledReward> rollRewards(double luck, int extraRandomRolls)
    {
        List<RolledReward> rewards = new ArrayList<>();
        for (QuestReward reward : _guaranteedRewards)
        {
            if (reward.isAvailable()) rewards.add(new RolledReward(reward, null));
        }

        // Entries whose items are all still locked (netherite before anyone found it...) sit out
        List<QuestReward> pool = _poolRewards.stream().filter(QuestReward::isAvailable).toList();
        if (!pool.isEmpty())
        {
            for (int i = 0; i < _rolls; i++)
            {
                rewards.add(new RolledReward(Luck.pick(pool, QuestReward::weight, luck), null));
            }
        }

        ManagerRewardPools pools = ImusMiniQuests.getInstance().getRewardPools();
        for (int i = 0; i < getRandomRolls() + extraRandomRolls; i++)
        {
            RolledReward random = pools.roll(luck);
            if (random != null) rewards.add(random);
        }
        return rewards;
    }

    /**
     * Chance in percent that a found panel is this quest, from its share of all drop weights.
     */
    public double getDropChancePercent(int totalDropWeight)
    {
        return totalDropWeight <= 0 ? 0 : _dropWeight * 100.0 / totalDropWeight;
    }
}
