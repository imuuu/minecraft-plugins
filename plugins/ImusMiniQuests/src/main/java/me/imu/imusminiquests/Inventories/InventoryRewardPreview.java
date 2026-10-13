package me.imu.imusminiquests.Inventories;

import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Click;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import me.imu.imusminiquests.Managers.ManagerQuestPoints;
import me.imu.imusminiquests.Quests.Quest;

import java.util.ArrayList;
import java.util.List;

/**
 * Opens a quest many times without giving anything and shows what came out, so the spread of
 * rewards (which tool tiers, which books...) can be seen at a glance. Clicking an item takes it.
 */
public class InventoryRewardPreview extends Menu
{
    static final int ITEM_SLOTS = 45;
    static final int SLOT_BACK = 45;
    static final int SLOT_INFO = 48;
    static final int SLOT_REROLL = 49;
    static final int SLOT_LUCK = 50;

    private final QuestMenuGame _game;
    private final Quest _quest;
    // Which luck the rolls use: 0 = none, 1 = half of the most, 2 = the most
    private int _luckStep = 0;
    private List<QuestMenuGame.Prize> _prizes = List.of();
    private int _openings;
    private boolean _commands;

    public InventoryRewardPreview(QuestMenuGame game, Quest quest)
    {
        super("&6Rewards: " + quest.getId(), 6);
        _game = game;
        _quest = quest;
        roll();
    }

    private double getLuck()
    {
        return _game.maxLuck() * _luckStep / 2.0;
    }

    /**
     * Opens the quest until the item slots are full, one opening after another.
     */
    private void roll()
    {
        double luck = getLuck();
        List<QuestMenuGame.Prize> prizes = new ArrayList<>();
        int openings = 0;
        boolean commands = false;
        // An opening that only runs commands gives no items; stop instead of looping forever
        for (int attempt = 0; attempt < ITEM_SLOTS * 4 && prizes.size() < ITEM_SLOTS; attempt++)
        {
            QuestMenuGame.Opening opened = _game.open(_quest, luck);
            commands |= opened.commands();
            if (prizes.size() + opened.prizes().size() > ITEM_SLOTS && !prizes.isEmpty())
                break;
            prizes.addAll(opened.prizes());
            openings++;
        }
        _prizes = prizes;
        _openings = openings;
        _commands = commands;
    }

    @Override
    protected void render(Canvas canvas)
    {
        for (int slot = 0; slot < _prizes.size() && slot < ITEM_SLOTS; slot++)
        {
            QuestMenuGame.Prize prize = _prizes.get(slot);
            if (prize.take() != null)
                canvas.set(slot, prize.icon(), click -> prize.take().accept(click.viewer()));
            else
                canvas.set(slot, prize.icon());
        }

        canvas.fillRow(5, Icon.filler("black_stained_glass_pane"));
        if (canvas.canGoBack())
            canvas.set(SLOT_BACK, Icon.of("red_stained_glass_pane", "&bGo Back"), Click::back);

        double luck = getLuck();
        List<String> info = new ArrayList<>();
        info.add("&7Items from &e" + _openings + "&7 openings");
        info.add("&7Pool rolls per opening: &e" + _quest.getRolls());
        List<QuestMenuGame.TierChance> tiers = _game.tiers(luck);
        int randomRolls = _game.randomRolls(_quest);
        if (randomRolls > 0 && !tiers.isEmpty())
        {
            info.add("&7Random rewards per opening: &e" + randomRolls);
            for (QuestMenuGame.TierChance tier : tiers)
                info.add(String.format("&8 • %s &7%.1f%%", tier.display(), tier.percent()));
        }
        if (_commands)
            info.add("&8Command rewards ran nothing here");
        info.add("&eClick &7an item to take it");
        canvas.set(SLOT_INFO, Icon.of("book", "&6" + _quest.getName(), info.toArray(String[]::new)));

        canvas.set(SLOT_REROLL, Icon.of("ender_eye", "&6Roll again"), click -> roll());
        canvas.set(SLOT_LUCK, Icon.of("rabbit_foot", "&6Luck: &e" + ManagerQuestPoints.formatLuck(luck),
                "&7Rolls as a player with this much luck",
                "&7from quest points would get them",
                "&eClick &7to switch: none, half, most"), click ->
        {
            _luckStep = (_luckStep + 1) % 3;
            roll();
        });
    }
}
