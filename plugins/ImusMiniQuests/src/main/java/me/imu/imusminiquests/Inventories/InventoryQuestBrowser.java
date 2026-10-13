package me.imu.imusminiquests.Inventories;

import imu.iAPI.Menus.Button;
import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Click;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.Pager;
import me.imu.imusminiquests.Quests.Quest;

import java.util.List;

/**
 * /imq menu: every quest as the panel players would find, to see what the quests look like and
 * what they give. Left-click takes a panel, shift-click a finished one, right-click opens a
 * preview of rolled rewards. The bottom row rolls a random panel the way chests do.
 */
public class InventoryQuestBrowser extends Menu
{
    static final int SLOT_PREVIOUS = 45;
    static final int SLOT_INFO = 48;
    static final int SLOT_RANDOM = 49;
    static final int SLOT_NEXT = 53;

    private final QuestMenuGame _game;
    private final Pager<Quest> _pager = Pager.<Quest>rows(0, 5)
            .arrows(Icon.of("arrow", "&ePrevious page"), Icon.of("arrow", "&eNext page"));

    public InventoryQuestBrowser(QuestMenuGame game)
    {
        super("&6MiniQuests", 6);
        _game = game;
    }

    @Override
    protected void render(Canvas canvas)
    {
        // Read on every draw, so a reload shows up at once
        List<Quest> quests = _game.quests();
        int totalWeight = _game.totalDropWeight();
        canvas.fillRow(5, Icon.filler("black_stained_glass_pane"));
        _pager.render(canvas, quests, quest -> Button.of(icon(quest, totalWeight), click -> onQuestClick(click, quest)),
                SLOT_PREVIOUS, SLOT_NEXT);

        canvas.set(SLOT_INFO, Icon.of("book", "&6" + quests.size() + " quests &7(page " + (_pager.page() + 1) + "/" + _pager.pageCount(quests.size()) + ")",
                "&eLeft-click &7a panel to take it",
                "&eShift-click &7to take it finished",
                "&eRight-click &7to preview rolled rewards",
                "&8Edit quests.yml and /imq reload to change them"));
        canvas.set(SLOT_RANDOM, Icon.of("chest", "&6Roll a random panel",
                "&7Picks a quest by drop-weight,",
                "&7like a loot chest or a mob drop does"), this::onRandomClick);
    }

    /**
     * The panel as players see it, plus the admin details below it.
     */
    private Icon icon(Quest quest, int totalWeight)
    {
        // The icon shows the quest at its base size; taking one rolls a rarity like a found panel
        String found = quest.getDropWeight() > 0
                ? String.format("&7Found: &f%.1f%% &8(weight %d)", quest.getDropChancePercent(totalWeight), quest.getDropWeight())
                : "&7Found: &cnever &8(only /imq give)";
        return _game.panelIcon(quest).addLore("&8&m                              ", "&7Id: &f" + quest.getId(), found,
                "&eLeft &7take  &eShift &7take finished  &eRight &7preview");
    }

    private void onQuestClick(Click click, Quest quest)
    {
        if (click.isRight())
        {
            click.open(new InventoryRewardPreview(_game, quest));
            return;
        }
        _game.givePanel(click.viewer(), quest, click.isShift());
    }

    private void onRandomClick(Click click)
    {
        Quest quest = _game.rollDrop();
        if (quest == null)
        {
            click.message("&cNo quest has a drop-weight above 0");
            return;
        }
        _game.givePanel(click.viewer(), quest, false);
    }
}
