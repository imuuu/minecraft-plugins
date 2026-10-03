package me.imu.imusminiquests.Inventories;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Utilities.InvUtil;
import imu.iAPI.Utilities.ItemUtils;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Managers.ManagerEconomy;
import me.imu.imusminiquests.Managers.ManagerQuestPoints;
import me.imu.imusminiquests.Managers.ManagerRewardPools;
import me.imu.imusminiquests.Quests.Quest;
import me.imu.imusminiquests.Quests.QuestReward;
import me.imu.imusminiquests.Quests.RewardTier;
import me.imu.imusminiquests.Quests.RolledReward;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Opens a quest many times without giving anything and shows what came out, so the spread of
 * rewards (which tool tiers, which books...) can be seen at a glance. Clicking an item takes it.
 */
public class InventoryRewardPreview extends CustomInventory
{
    private static final int ITEM_SLOTS = 45;
    private static final int SLOT_BACK = 45;
    private static final int SLOT_INFO = 48;
    private static final int SLOT_REROLL = 49;
    private static final int SLOT_LUCK = 50;

    private final Quest _quest;
    // Which luck the rolls use: 0 = none, 1 = half of the most, 2 = the most
    private int _luckStep = 0;

    /**
     * An item in the preview. display is what the slot shows (with the rarity of random rewards
     * in its lore), item what clicking gives; null for money, which can't be taken.
     */
    private record Shown(ItemStack display, ItemStack item) {}

    private static ItemStack withTier(ItemStack stack, RewardTier tier)
    {
        if (tier == null) return stack;
        ItemStack display = stack.clone();
        ItemUtils.AddLore(display, "&8Random reward: " + tier.display(), true);
        return display;
    }

    public InventoryRewardPreview(Quest quest)
    {
        super(ImusMiniQuests.getInstance(), "&6Rewards: " + quest.getId(), 54);
        _quest = quest;
    }

    @Override
    public INVENTORY_AREA setInventoryLock()
    {
        return INVENTORY_AREA.UPPER_INV;
    }

    @Override
    public void onAwake()
    {
    }

    @Override
    public void onOpen()
    {
        super.onOpen();
        roll();
    }

    private double getLuck()
    {
        return ImusMiniQuests.getInstance().getQuestPoints().getMaxLuck() * _luckStep / 2.0;
    }

    private static ItemStack moneyIcon(double money)
    {
        ItemStack icon = new ItemStack(Material.GOLD_INGOT);
        ItemUtils.SetDisplayName(icon, "&6+" + ManagerEconomy.format(money));
        ItemUtils.AddLore(icon, "&8Money, paid straight to the balance", true);
        return icon;
    }

    /**
     * Opens the quest until the item slots are full and lays the items out, one opening after
     * another.
     */
    private void roll()
    {
        clearButtons();

        double luck = getLuck();
        List<Shown> items = new ArrayList<>();
        int openings = 0;
        boolean commands = false;
        // An opening that only runs commands gives no items; stop instead of looping forever
        for (int attempt = 0; attempt < ITEM_SLOTS * 4 && items.size() < ITEM_SLOTS; attempt++)
        {
            List<Shown> opened = new ArrayList<>();
            for (RolledReward rolled : _quest.rollRewards(luck))
            {
                QuestReward reward = rolled.reward();
                reward.createItems(luck).forEach(item -> opened.add(new Shown(withTier(item, rolled.tier()), item)));
                double money = reward.rollMoney(luck);
                if (money > 0) opened.add(new Shown(withTier(moneyIcon(money), rolled.tier()), null));
                commands |= reward.hasCommands();
            }
            if (items.size() + opened.size() > ITEM_SLOTS && !items.isEmpty()) break;

            items.addAll(opened);
            openings++;
        }

        for (int slot = 0; slot < items.size() && slot < ITEM_SLOTS; slot++)
        {
            Shown shown = items.get(slot);
            ItemStack item = shown.item();
            addButton(item != null
                    ? new Button(slot, shown.display(), event -> InvUtil.AddItemToInventoryOrDrop(getPlayer(), item.clone()))
                    : new Button(slot, shown.display()));
        }

        for (int slot = ITEM_SLOTS; slot < getSize(); slot++)
        {
            addButton(getEmptyButton(slot, Material.BLACK_STAINED_GLASS_PANE));
        }

        ItemStack back = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemUtils.SetDisplayName(back, "&bGo Back");
        addButton(new Button(SLOT_BACK, back, event -> back()));

        ItemStack info = new ItemStack(Material.BOOK);
        ItemUtils.SetDisplayName(info, "&6" + _quest.getName());
        ItemUtils.AddLore(info, "&7Items from &e" + openings + "&7 openings", true);
        ItemUtils.AddLore(info, "&7Pool rolls per opening: &e" + _quest.getRolls(), true);
        ManagerRewardPools pools = ImusMiniQuests.getInstance().getRewardPools();
        if (_quest.getRandomRolls() > 0 && !pools.getTiers().isEmpty())
        {
            ItemUtils.AddLore(info, "&7Random rewards per opening: &e" + _quest.getRandomRolls(), true);
            for (RewardTier tier : pools.getTiers())
            {
                ItemUtils.AddLore(info, String.format("&8 • %s &7%.1f%%", tier.display(), pools.getTierChancePercent(tier, luck)), true);
            }
        }
        if (commands) ItemUtils.AddLore(info, "&8Command rewards ran nothing here", true);
        ItemUtils.AddLore(info, "&eClick &7an item to take it", true);
        addButton(new Button(SLOT_INFO, info));

        ItemStack reroll = new ItemStack(Material.ENDER_EYE);
        ItemUtils.SetDisplayName(reroll, "&6Roll again");
        addButton(new Button(SLOT_REROLL, reroll, event -> roll()));

        ItemStack luckIcon = new ItemStack(Material.RABBIT_FOOT);
        ItemUtils.SetDisplayName(luckIcon, "&6Luck: &e" + ManagerQuestPoints.formatLuck(luck));
        ItemUtils.AddLore(luckIcon, "&7Rolls as a player with this much luck", true);
        ItemUtils.AddLore(luckIcon, "&7from quest points would get them", true);
        ItemUtils.AddLore(luckIcon, "&eClick &7to switch: none, half, most", true);
        addButton(new Button(SLOT_LUCK, luckIcon, event ->
        {
            _luckStep = (_luckStep + 1) % 3;
            roll();
        }));

        updateButtons(true);
    }
}
