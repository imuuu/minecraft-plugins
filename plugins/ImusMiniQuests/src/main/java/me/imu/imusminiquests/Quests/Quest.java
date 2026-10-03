package me.imu.imusminiquests.Quests;

import imu.iAPI.LootTables.ImusLootTable;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

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
    private final ImusLootTable _rewardPool = new ImusLootTable();
    private final List<QuestReward> _guaranteedRewards;

    public Quest(String id, String name, Material material, NamespacedKey model, List<String> lore, int dropWeight,
                 QuestObjective objective, int rolls, List<QuestReward> poolRewards, List<QuestReward> guaranteedRewards)
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
        for (QuestReward reward : _poolRewards)
        {
            _rewardPool.add(reward, reward.weight());
        }
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
     * Rolls the reward pool and hands out the result and the guaranteed rewards.
     */
    public void giveRewards(Player player)
    {
        for (QuestReward reward : _guaranteedRewards)
        {
            reward.give(player);
        }

        if (_rewardPool.getTotalWeight() <= 0) return;

        for (Object loot : _rewardPool.getLoot(_rolls))
        {
            if (loot instanceof QuestReward reward) reward.give(player);
        }
    }
}
