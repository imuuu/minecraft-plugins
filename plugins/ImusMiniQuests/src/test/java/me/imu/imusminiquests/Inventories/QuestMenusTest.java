package me.imu.imusminiquests.Inventories;

import imu.iAPI.Menus.ClickKind;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Testing.MenuTester;
import imu.iAPI.Menus.Viewer;
import me.imu.imusminiquests.Quests.Quest;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * /imq menu and the reward preview, with a stand-in for the server.
 */
class QuestMenusTest
{
    private final Game _game = new Game();

    private static Quest quest(String id, int dropWeight)
    {
        return new Quest(id, "Quest " + id, Material.PAPER, null, List.of(), dropWeight, null, 2, List.of(), List.of(), 1);
    }

    private static final class Game implements QuestMenuGame
    {
        final List<Quest> quests = new ArrayList<>();
        final List<String> given = new ArrayList<>();
        Quest drop;
        int opened;
        // Items each opening gives
        int perOpening = 4;

        @Override
        public List<Quest> quests()
        {
            return quests;
        }

        @Override
        public int totalDropWeight()
        {
            return quests.stream().mapToInt(Quest::getDropWeight).sum();
        }

        @Override
        public Quest rollDrop()
        {
            return drop;
        }

        @Override
        public Icon panelIcon(Quest quest)
        {
            return Icon.of("paper", quest.getName(), "Panel lore");
        }

        @Override
        public void givePanel(Viewer viewer, Quest quest, boolean finished)
        {
            given.add(quest.getId() + (finished ? " finished" : ""));
        }

        @Override
        public double maxLuck()
        {
            return 0.5;
        }

        @Override
        public Opening open(Quest quest, double luck)
        {
            int number = ++opened;
            List<Prize> prizes = new ArrayList<>();
            for (int i = 0; i < perOpening - 1; i++)
            {
                String name = "Item " + number + "." + i + " luck " + luck;
                prizes.add(new Prize(Icon.of("diamond", name), viewer -> given.add(name)));
            }
            prizes.add(new Prize(Icon.of("gold_ingot", "+10"), null));
            return new Opening(prizes, true);
        }

        @Override
        public int randomRolls(Quest quest)
        {
            return 1;
        }

        @Override
        public List<TierChance> tiers(double luck)
        {
            return List.of(new TierChance("Rare", 10), new TierChance("Common", 90));
        }
    }

    // The browser ====================================================================================================

    @Test
    void everyQuestIsAPanelWithItsDetails()
    {
        _game.quests.add(quest("fish", 3));
        _game.quests.add(quest("secret", 0));
        _game.quests.add(quest("mine", 1));
        MenuTester menu = MenuTester.open(new InventoryQuestBrowser(_game));

        assertEquals("Quest fish", menu.name(0));
        List<String> lore = menu.lore(0);
        assertEquals("Panel lore", lore.getFirst(), "the panel's own lore first");
        assertTrue(lore.contains("Id: fish"));
        // String.format writes the decimal the way the server's locale does
        assertTrue(lore.contains(String.format("Found: %.1f%% (weight 3)", 75.0)), lore.toString());
        assertTrue(menu.lore(1).contains("Found: never (only /imq give)"));
        assertEquals("3 quests (page 1/1)", menu.name(InventoryQuestBrowser.SLOT_INFO));
    }

    @Test
    void clicksGivePanels()
    {
        _game.quests.add(quest("fish", 1));
        MenuTester menu = MenuTester.open(new InventoryQuestBrowser(_game));
        menu.click(0).click(0, ClickKind.SHIFT_LEFT);
        assertEquals(List.of("fish", "fish finished"), _game.given);

        menu.click(InventoryQuestBrowser.SLOT_RANDOM);
        assertEquals("No quest has a drop-weight above 0", menu.viewer().lastMessage());
        _game.drop = _game.quests.getFirst();
        menu.click(InventoryQuestBrowser.SLOT_RANDOM);
        assertEquals(List.of("fish", "fish finished", "fish"), _game.given);
    }

    @Test
    void manyQuestsGoOnPages()
    {
        for (int i = 0; i < 50; i++)
            _game.quests.add(quest("q" + i, 1));
        MenuTester menu = MenuTester.open(new InventoryQuestBrowser(_game));
        menu.click(InventoryQuestBrowser.SLOT_NEXT);
        assertEquals("Quest q45", menu.name(0));
        assertEquals("50 quests (page 2/2)", menu.name(InventoryQuestBrowser.SLOT_INFO));
    }

    // The reward preview =============================================================================================

    private MenuTester preview()
    {
        _game.quests.add(quest("fish", 1));
        MenuTester menu = MenuTester.open(new InventoryQuestBrowser(_game));
        menu.click(0, ClickKind.RIGHT);
        menu.assertShowing(InventoryRewardPreview.class);
        return menu;
    }

    @Test
    void thePreviewFillsTheSlotsWithWholeOpenings()
    {
        _game.perOpening = 4;
        MenuTester menu = preview();
        assertEquals("Rewards: fish", menu.title());
        // 11 openings of 4 make 44; a 12th wouldn't fit
        assertEquals("Item 1.0 luck 0.0", menu.name(0));
        assertEquals("+10", menu.name(43));
        assertNull(menu.icon(44));
        List<String> info = menu.lore(InventoryRewardPreview.SLOT_INFO);
        assertTrue(info.contains("Items from 11 openings"), info.toString());
        assertTrue(info.contains("Pool rolls per opening: 2"));
        assertTrue(info.contains(String.format(" • Rare %.1f%%", 10.0)));
        assertTrue(info.contains("Command rewards ran nothing here"));
    }

    @Test
    void itemsCanBeTakenButMoneyCant()
    {
        MenuTester menu = preview();
        menu.click(0).click(3);
        assertEquals(List.of("Item 1.0 luck 0.0"), _game.given);
        assertEquals("Item 1.0 luck 0.0", menu.name(0), "the same items stay until rolled again");
    }

    @Test
    void rollingAgainAndLuck()
    {
        MenuTester menu = preview();
        int before = _game.opened;
        menu.click(InventoryRewardPreview.SLOT_REROLL);
        assertTrue(_game.opened > before);
        assertNotEquals("Item 1.0 luck 0.0", menu.name(0));

        menu.click(InventoryRewardPreview.SLOT_LUCK);
        assertEquals("Luck: 25%", menu.name(InventoryRewardPreview.SLOT_LUCK));
        assertTrue(menu.name(0).endsWith("luck 0.25"));
        menu.click(InventoryRewardPreview.SLOT_LUCK).click(InventoryRewardPreview.SLOT_LUCK);
        assertEquals("Luck: 0%", menu.name(InventoryRewardPreview.SLOT_LUCK));
    }

    @Test
    void backGoesToTheBrowser()
    {
        MenuTester menu = preview();
        menu.click("Go Back");
        menu.assertShowing(InventoryQuestBrowser.class);
    }
}
