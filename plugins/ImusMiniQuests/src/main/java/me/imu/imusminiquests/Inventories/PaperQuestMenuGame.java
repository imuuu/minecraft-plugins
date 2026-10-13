package me.imu.imusminiquests.Inventories;

import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Paper.PaperIcons;
import imu.iAPI.Menus.Viewer;
import imu.iAPI.Utilities.InvUtil;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Managers.ManagerEconomy;
import me.imu.imusminiquests.Managers.ManagerRewardPools;
import me.imu.imusminiquests.Quests.Quest;
import me.imu.imusminiquests.Quests.QuestItem;
import me.imu.imusminiquests.Quests.QuestReward;
import me.imu.imusminiquests.Quests.RewardTier;
import me.imu.imusminiquests.Quests.RolledReward;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * The quest menus' {@link QuestMenuGame} on the server.
 */
public final class PaperQuestMenuGame implements QuestMenuGame
{
    private final ImusMiniQuests _plugin;

    public PaperQuestMenuGame(ImusMiniQuests plugin)
    {
        _plugin = plugin;
    }

    private static void give(Viewer viewer, ItemStack item)
    {
        Player player = Bukkit.getPlayer(viewer.id());
        if (player != null)
            InvUtil.AddItemToInventoryOrDrop(player, item.clone());
    }

    @Override
    public List<Quest> quests()
    {
        return new ArrayList<>(_plugin.getQuests().getQuests());
    }

    @Override
    public int totalDropWeight()
    {
        return _plugin.getQuests().getTotalDropWeight();
    }

    @Override
    public Quest rollDrop()
    {
        return _plugin.getQuests().rollDrop();
    }

    @Override
    public Icon panelIcon(Quest quest)
    {
        return PaperIcons.of(QuestItem.create(quest, _plugin.getRarities().getCommon()));
    }

    @Override
    public void givePanel(Viewer viewer, Quest quest, boolean finished)
    {
        ItemStack panel = QuestItem.create(quest);
        if (finished)
            QuestItem.setProgress(panel, quest, QuestItem.getRequiredAmount(panel, quest));
        give(viewer, panel);
    }

    @Override
    public double maxLuck()
    {
        return _plugin.getQuestPoints().getMaxLuck();
    }

    @Override
    public Opening open(Quest quest, double luck)
    {
        List<Prize> prizes = new ArrayList<>();
        boolean commands = false;
        for (RolledReward rolled : quest.rollRewards(luck))
        {
            QuestReward reward = rolled.reward();
            for (ItemStack item : reward.createItems(luck))
                prizes.add(new Prize(withTier(PaperIcons.of(item), rolled.tier()), viewer -> give(viewer, item)));
            double money = reward.rollMoney(luck);
            if (money > 0)
            {
                Icon icon = Icon.of(Material.GOLD_INGOT, "&6+" + ManagerEconomy.format(money), "&8Money, paid straight to the balance");
                prizes.add(new Prize(withTier(icon, rolled.tier()), null));
            }
            commands |= reward.hasCommands();
        }
        return new Opening(prizes, commands);
    }

    // Random rewards show their rarity
    private static Icon withTier(Icon icon, RewardTier tier)
    {
        return tier == null ? icon : icon.addLore("&8Random reward: " + tier.display());
    }

    @Override
    public int randomRolls(Quest quest)
    {
        return quest.getRandomRolls();
    }

    @Override
    public List<TierChance> tiers(double luck)
    {
        ManagerRewardPools pools = _plugin.getRewardPools();
        return pools.getTiers().stream().map(tier -> new TierChance(tier.display(), pools.getTierChancePercent(tier, luck))).toList();
    }
}
