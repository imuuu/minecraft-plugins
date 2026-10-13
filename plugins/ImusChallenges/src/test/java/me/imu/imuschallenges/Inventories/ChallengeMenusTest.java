package me.imu.imuschallenges.Inventories;

import imu.iAPI.Menus.ClickKind;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Testing.MenuTester;
import imu.iAPI.Menus.Testing.TestViewer;
import imu.iAPI.Menus.Viewer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The challenge shop and the collection view, with stand-ins for the server.
 */
class ChallengeMenusTest
{
    // The shop =======================================================================================================

    private static final class Shop implements ShopMenuGame
    {
        final List<String> done = new ArrayList<>();
        Consumer<List<Tier>> pending;
        boolean canBuy = true;
        Tier special = new Tier(true, false, List.of(Icon.of("totem_of_undying", "Totem"), Icon.of("elytra", "Elytra")),
                List.of(50, 80), 1, 0, 1000);
        Tier normal = new Tier(false, false, List.of(Icon.of("bread", "Bread"), Icon.of("apple", "Apple"), Icon.of("coal", "Coal")),
                List.of(5, 6, 7), 3, 0, 500);

        @Override
        public String resetsIn(boolean special)
        {
            return special ? "3d" : "2h";
        }

        @Override
        public void load(Viewer viewer, Consumer<List<Tier>> done)
        {
            pending = done;
        }

        // The database answers
        void answer()
        {
            pending.accept(List.of(special, normal));
        }

        @Override
        public boolean buyItem(Viewer viewer, boolean special, int index)
        {
            done.add("item " + special + " " + index);
            return canBuy;
        }

        @Override
        public boolean buySlot(Viewer viewer, boolean special, int nextSlot)
        {
            done.add("slot " + special + " " + nextSlot);
            return canBuy;
        }

        @Override
        public void opened(Viewer viewer)
        {
            done.add("opened");
        }

        @Override
        public void closed(Viewer viewer)
        {
            done.add("closed");
        }
    }

    private final Shop _shop = new Shop();

    private MenuTester shop()
    {
        MenuTester menu = MenuTester.open(new InventoryChallengeShop(_shop));
        _shop.answer();
        return menu;
    }

    @Test
    void theShopFillsWhenTheSlotsAreRead()
    {
        MenuTester menu = MenuTester.open(new InventoryChallengeShop(_shop));
        assertEquals("Resets in 3d", menu.name(1));
        assertEquals("Resets in 2h", menu.name(12));
        assertEquals("Resets in 2h", menu.name(39));
        assertTrue(menu.icon(21).hideTooltip(), "nothing yet");

        _shop.answer();
        assertEquals("Bread", menu.name(21));
        assertTrue(menu.lore(21).contains("Cost: 5 Challenge points"));
        assertEquals("Coal", menu.name(23));
        assertTrue(menu.icon(24).hideTooltip(), "no fourth item");
        assertEquals("Totem", menu.name(10));
        assertEquals("Buy Special slot", menu.name(19), "the second special slot is for sale");
        assertTrue(menu.lore(19).contains("Cost: 1000 $"));
        assertEquals(InventoryChallengeShop.slot(false, 4), 30, "the second row of the normal side");
    }

    @Test
    void buyingAnItemClosesTheShop()
    {
        MenuTester menu = shop();
        menu.click(22);
        assertEquals(List.of("opened", "item false 1", "closed"), _shop.done);
        assertTrue(menu.isClosed());

        _shop.canBuy = false;
        MenuTester again = shop();
        again.click(10);
        assertFalse(again.isClosed(), "it stays open when the item can't be bought");
    }

    @Test
    void aSlotIsBoughtWithAClickAndARightClick()
    {
        MenuTester menu = shop();
        menu.click(19, ClickKind.RIGHT);
        assertFalse(_shop.done.stream().anyMatch(line -> line.startsWith("slot")), "not before the first click");

        menu.click(19);
        assertTrue(menu.name(19).contains("(Confirm by M2)"));
        assertTrue(menu.icon(19).glow());
        menu.click(19, ClickKind.RIGHT);
        assertTrue(_shop.done.contains("slot true 0"));
        assertTrue(menu.isClosed());
    }

    @Test
    void aClosedSideIsRed()
    {
        _shop.normal = new ShopMenuGame.Tier(false, true, _shop.normal.items(), _shop.normal.costs(), 3, 0, 500);
        MenuTester menu = shop();
        assertEquals("minecraft:red_stained_glass_pane", menu.icon(21).material());
        menu.click(21);
        assertFalse(_shop.done.stream().anyMatch(line -> line.startsWith("item")));
    }

    // The collection view ============================================================================================

    private static final class Collection implements CollectMenuGame
    {
        final List<String> done = new ArrayList<>();
        final UUID alex = UUID.randomUUID();

        @Override
        public void playerCounts(Consumer<List<PlayerCount>> done)
        {
            done.accept(List.of(new PlayerCount(alex, "Alex", 2)));
        }

        @Override
        public void collected(UUID player, Consumer<List<Collected>> done)
        {
            done.accept(player.equals(alex) ? List.of(new Collected("DIAMOND", 0), new Collected("EMERALD", 0)) : null);
        }

        @Override
        public void uncollected(Consumer<List<String>> done)
        {
            List<String> materials = new ArrayList<>();
            for (int i = 0; i < 50; i++)
                materials.add(i == 0 ? "NETHER_STAR" : "STONE");
            done.accept(materials);
        }

        @Override
        public void removeCollected(UUID player, String material)
        {
            done.add("remove " + material);
        }

        @Override
        public void exclude(String material, Runnable done)
        {
            this.done.add("exclude " + material);
            done.run();
        }
    }

    private final Collection _collection = new Collection();

    @Test
    void theViewStartsWithThePlayers()
    {
        MenuTester menu = MenuTester.open(new InventoryCCollectMaterial(_collection));
        assertEquals("Alex", menu.name(0));
        assertTrue(menu.lore(0).contains("Materials: 2"));

        menu.click(0);
        assertEquals("minecraft:diamond", menu.icon(0).material());
        assertTrue(menu.lore(0).contains("===> 2"));
        assertTrue(menu.lore(1).contains("===> 1"));
    }

    @Test
    void anAdminRemovesACollectedMaterial()
    {
        MenuTester menu = MenuTester.open(new InventoryCCollectMaterial(_collection));
        menu.click(0);
        menu.click(0);
        assertTrue(_collection.done.isEmpty(), "a left click does nothing");
        menu.click(0, ClickKind.RIGHT);
        assertEquals(List.of("remove DIAMOND"), _collection.done);
        assertEquals("Removed", menu.name(0));
        menu.click(0, ClickKind.RIGHT);
        assertEquals(1, _collection.done.size(), "only once");
    }

    @Test
    void playersSeeTheListsButChangeNothing()
    {
        MenuTester menu = MenuTester.open(new InventoryCCollectMaterial(_collection), TestViewer.with("Guest"));
        menu.click(InventoryCCollectMaterial.UNKNOWN);
        assertFalse(menu.lore(0).contains("=== OP ONLY ==="));
        menu.click(0, ClickKind.RIGHT);
        assertTrue(_collection.done.isEmpty());
    }

    @Test
    void unknownMaterialsGoOnPagesAndCanBeExcluded()
    {
        MenuTester menu = MenuTester.open(new InventoryCCollectMaterial(_collection));
        menu.click(InventoryCCollectMaterial.UNKNOWN);
        assertTrue(menu.lore(0).contains("=> Called NETHER_STAR"));
        assertTrue(menu.lore(0).contains("=> 50"));
        menu.click(0, ClickKind.RIGHT);
        assertEquals(List.of("exclude NETHER_STAR"), _collection.done);
        assertEquals("Added NETHER_STAR to excluded database!", menu.viewer().lastMessage());
        assertEquals("Excluded", menu.name(0));

        menu.click(InventoryCCollectMaterial.NEXT);
        assertTrue(menu.lore(0).contains("=> 5"));
    }

    @Test
    void anOfflinePlayersMaterialsAreNotShown()
    {
        MenuTester menu = MenuTester.open(new InventoryCCollectMaterial(_collection));
        menu.click(InventoryCCollectMaterial.OWN);
        assertEquals("Tester is not online!", menu.viewer().lastMessage());
    }
}
