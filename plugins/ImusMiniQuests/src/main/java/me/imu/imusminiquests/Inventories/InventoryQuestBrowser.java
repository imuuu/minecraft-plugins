package me.imu.imusminiquests.Inventories;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.InvUtil;
import imu.iAPI.Utilities.ItemUtils;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Managers.ManagerQuests;
import me.imu.imusminiquests.Quests.Quest;
import me.imu.imusminiquests.Quests.QuestItem;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * /imq menu: every quest as the panel players would find, to see what the quests look like and
 * what they give. Left-click takes a panel, shift-click a finished one, right-click opens a
 * preview of rolled rewards. The bottom row rolls a random panel the way chests do.
 */
public class InventoryQuestBrowser extends CustomInventory
{
    private static final int PAGE_SIZE = 45;
    private static final int SLOT_PREVIOUS = 45;
    private static final int SLOT_INFO = 48;
    private static final int SLOT_RANDOM = 49;
    private static final int SLOT_NEXT = 53;

    private final ManagerQuests _quests;
    private int _page = 0;

    public InventoryQuestBrowser()
    {
        super(ImusMiniQuests.getInstance(), "&6MiniQuests", 54);
        _quests = ImusMiniQuests.getInstance().getQuests();
    }

    @Override
    public INVENTORY_AREA setInventoryLock()
    {
        return INVENTORY_AREA.UPPER_INV;
    }

    @Override
    public void onAwake()
    {
        // Built on every open, so a reload shows up the next time the menu is opened
    }

    @Override
    public void onOpen()
    {
        super.onOpen();
        initButtons();
    }

    private void initButtons()
    {
        clearButtons();
        List<Quest> quests = new ArrayList<>(_quests.getQuests());
        int pages = Math.max(1, (quests.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        _page = Math.min(_page, pages - 1);

        int totalWeight = _quests.getTotalDropWeight();
        for (int i = 0; i < PAGE_SIZE; i++)
        {
            int index = _page * PAGE_SIZE + i;
            if (index >= quests.size()) break;

            Quest quest = quests.get(index);
            addButton(new Button(i, createIcon(quest, totalWeight), event -> onQuestClick(quest, event)));
        }

        for (int slot = PAGE_SIZE; slot < getSize(); slot++)
        {
            addButton(getEmptyButton(slot, Material.BLACK_STAINED_GLASS_PANE));
        }

        ItemStack info = new ItemStack(Material.BOOK);
        ItemUtils.SetDisplayName(info, "&6" + quests.size() + " quests &7(page " + (_page + 1) + "/" + pages + ")");
        ItemUtils.AddLore(info, "&eLeft-click &7a panel to take it", true);
        ItemUtils.AddLore(info, "&eShift-click &7to take it finished", true);
        ItemUtils.AddLore(info, "&eRight-click &7to preview rolled rewards", true);
        ItemUtils.AddLore(info, "&8Edit quests.yml and /imq reload to change them", true);
        addButton(new Button(SLOT_INFO, info));

        ItemStack random = new ItemStack(Material.CHEST);
        ItemUtils.SetDisplayName(random, "&6Roll a random panel");
        ItemUtils.AddLore(random, "&7Picks a quest by drop-weight,", true);
        ItemUtils.AddLore(random, "&7like a loot chest or a mob drop does", true);
        addButton(new Button(SLOT_RANDOM, random, event -> onRandomClick()));

        if (_page > 0)
            addButton(new Button(SLOT_PREVIOUS, arrow("&ePrevious page"), event -> changePage(-1)));
        if (_page < pages - 1)
            addButton(new Button(SLOT_NEXT, arrow("&eNext page"), event -> changePage(1)));

        updateButtons(true);
    }

    private static ItemStack arrow(String name)
    {
        ItemStack stack = new ItemStack(Material.ARROW);
        ItemUtils.SetDisplayName(stack, name);
        return stack;
    }

    /**
     * The panel as players see it, plus the admin details below it.
     */
    private ItemStack createIcon(Quest quest, int totalWeight)
    {
        // The icon shows the quest at its base size; taking one rolls a rarity like a found panel
        ItemStack icon = QuestItem.create(quest, ImusMiniQuests.getInstance().getRarities().getCommon());
        ItemUtils.AddLore(icon, "&8&m                              ", true);
        ItemUtils.AddLore(icon, "&7Id: &f" + quest.getId(), true);
        if (quest.getDropWeight() > 0)
            ItemUtils.AddLore(icon, String.format("&7Found: &f%.1f%% &8(weight %d)", quest.getDropChancePercent(totalWeight), quest.getDropWeight()), true);
        else
            ItemUtils.AddLore(icon, "&7Found: &cnever &8(only /imq give)", true);
        ItemUtils.AddLore(icon, "&eLeft &7take  &eShift &7take finished  &eRight &7preview", true);
        return icon;
    }

    private void onQuestClick(Quest quest, InventoryClickEvent event)
    {
        ClickType click = event.getClick();
        if (click.isRightClick())
        {
            openPage(new InventoryRewardPreview(quest));
            return;
        }

        ItemStack panel = QuestItem.create(quest);
        if (click.isShiftClick()) QuestItem.setProgress(panel, quest, QuestItem.getRequiredAmount(panel, quest));
        InvUtil.AddItemToInventoryOrDrop(getPlayer(), panel);
    }

    private void onRandomClick()
    {
        Quest quest = _quests.rollDrop();
        if (quest == null)
        {
            getPlayer().sendMessage(Metods.msgC("&cNo quest has a drop-weight above 0"));
            return;
        }
        InvUtil.AddItemToInventoryOrDrop(getPlayer(), QuestItem.create(quest));
    }

    private void changePage(int delta)
    {
        _page += delta;
        initButtons();
    }
}
