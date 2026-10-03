package me.imu.imusminiquests.Inventories;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Utilities.InvUtil;
import imu.iAPI.Utilities.ItemUtils;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Managers.ManagerEconomy;
import me.imu.imusminiquests.Quests.Quest;
import me.imu.imusminiquests.Quests.QuestReward;
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

    private final Quest _quest;

    /** An item in the preview; money is shown as an icon that can't be taken. */
    private record Shown(ItemStack stack, boolean takeable) {}

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

        List<Shown> items = new ArrayList<>();
        int openings = 0;
        boolean commands = false;
        // An opening that only runs commands gives no items; stop instead of looping forever
        for (int attempt = 0; attempt < ITEM_SLOTS * 4 && items.size() < ITEM_SLOTS; attempt++)
        {
            List<Shown> opened = new ArrayList<>();
            for (QuestReward reward : _quest.rollRewards())
            {
                reward.createItems().forEach(item -> opened.add(new Shown(item, true)));
                double money = reward.rollMoney();
                if (money > 0) opened.add(new Shown(moneyIcon(money), false));
                commands |= reward.hasCommands();
            }
            if (items.size() + opened.size() > ITEM_SLOTS && !items.isEmpty()) break;

            items.addAll(opened);
            openings++;
        }

        for (int slot = 0; slot < items.size() && slot < ITEM_SLOTS; slot++)
        {
            Shown shown = items.get(slot);
            ItemStack item = shown.stack();
            addButton(shown.takeable()
                    ? new Button(slot, item, event -> InvUtil.AddItemToInventoryOrDrop(getPlayer(), item.clone()))
                    : new Button(slot, item));
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
        if (commands) ItemUtils.AddLore(info, "&8Command rewards ran nothing here", true);
        ItemUtils.AddLore(info, "&eClick &7an item to take it", true);
        addButton(new Button(SLOT_INFO, info));

        ItemStack reroll = new ItemStack(Material.ENDER_EYE);
        ItemUtils.SetDisplayName(reroll, "&6Roll again");
        addButton(new Button(SLOT_REROLL, reroll, event -> roll()));

        updateButtons(true);
    }
}
