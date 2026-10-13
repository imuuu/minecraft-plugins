package me.imu.imusenchants.Inventories;

import imu.iAPI.Config.ConfigMenu;
import imu.iAPI.Enums.ENCHANTMENT_TIER;
import imu.iAPI.Enums.ITEM_CATEGORY;
import imu.iAPI.Menus.ClickKind;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Testing.MenuTester;
import imu.iAPI.Menus.Viewer;
import me.imu.imusenchants.CONSTANTS;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * /ien admin and the enchant shop, with stand-ins for the server.
 */
class EnchantMenusTest
{
    // /ien admin =====================================================================================================

    private static final class Admin implements EnchantAdminGame
    {
        final Set<String> disabled = new HashSet<>();
        final List<String> given = new ArrayList<>();
        final List<Entry> vanilla = new ArrayList<>();

        Admin()
        {
            for (int i = 0; i < 40; i++)
                vanilla.add(new Entry("vanilla:e" + i, Icon.of("enchanted_book"), "Enchant " + i, "&e", List.of("&7Max level: &fV")));
        }

        @Override
        public List<Entry> vanilla()
        {
            return vanilla;
        }

        @Override
        public List<Entry> custom()
        {
            return List.of(new Entry("custom:lifesteal", Icon.of("enchanted_book"), "Lifesteal", "&d", List.of("&7Heals")));
        }

        @Override
        public boolean isEnabled(String key)
        {
            return !disabled.contains(key);
        }

        @Override
        public void setEnabled(String key, boolean enabled)
        {
            if (enabled)
                disabled.remove(key);
            else
                disabled.add(key);
        }

        @Override
        public void giveBook(Viewer viewer, String key)
        {
            given.add("book " + key);
        }

        @Override
        public Icon coreIcon()
        {
            return Icon.of("amethyst_shard", "Slot Core");
        }

        @Override
        public void giveCores(Viewer viewer, int amount)
        {
            given.add(amount + " cores");
        }

        @Override
        public ConfigMenu settings()
        {
            return null;
        }
    }

    private final Admin _admin = new Admin();

    @Test
    void enchantsAreSwitchedOnAndOff()
    {
        MenuTester menu = MenuTester.open(new InventoryEnchantAdmin(_admin));
        assertEquals("Enchant 0", menu.name(9));
        assertTrue(menu.lore(9).contains("ENABLED"));

        menu.click(9);
        assertFalse(_admin.isEnabled("vanilla:e0"));
        assertEquals("minecraft:gray_dye", menu.icon(9).material());
        assertTrue(menu.lore(9).contains("Click » enable"));

        menu.click(9, ClickKind.MIDDLE).click(9, ClickKind.SHIFT_RIGHT);
        assertEquals(List.of("book vanilla:e0", "book vanilla:e0"), _admin.given);
        assertFalse(_admin.isEnabled("vanilla:e0"), "getting a book doesn't switch it");
    }

    @Test
    void theTabsAndPages()
    {
        MenuTester menu = MenuTester.open(new InventoryEnchantAdmin(_admin));
        menu.click(InventoryEnchantAdmin.SLOT_NEXT);
        assertEquals("Enchant 36", menu.name(9));

        menu.click(InventoryEnchantAdmin.SLOT_TAB_CUSTOM);
        assertEquals("Lifesteal", menu.name(9), "a new tab starts on its first page");
        assertTrue(menu.icon(InventoryEnchantAdmin.SLOT_TAB_CUSTOM).glow());
        assertTrue(menu.icon(10).hideTooltip());
    }

    @Test
    void allOnATabAtOnce()
    {
        MenuTester menu = MenuTester.open(new InventoryEnchantAdmin(_admin));
        menu.click(InventoryEnchantAdmin.SLOT_DISABLE_ALL);
        assertEquals(40, _admin.disabled.size());
        assertEquals("Disabled all vanilla enchants", menu.viewer().lastMessage());
        assertTrue(_admin.isEnabled("custom:lifesteal"), "only this tab");
        menu.click(InventoryEnchantAdmin.SLOT_ENABLE_ALL);
        assertTrue(_admin.disabled.isEmpty());
    }

    @Test
    void slotCoresOneOrAStack()
    {
        MenuTester menu = MenuTester.open(new InventoryEnchantAdmin(_admin));
        menu.click(InventoryEnchantAdmin.SLOT_CORE).click(InventoryEnchantAdmin.SLOT_CORE, ClickKind.SHIFT_LEFT);
        assertEquals(List.of("1 cores", "64 cores"), _admin.given);
        menu.click(InventoryEnchantAdmin.SLOT_CLOSE);
        assertTrue(menu.isClosed());
    }

    // The shop =======================================================================================================

    private static final class Shop implements EnchantShopGame
    {
        int level = 100;
        boolean creative;
        boolean available = true;
        double roll = 0.99;
        final List<String> given = new ArrayList<>();

        @Override
        public int level(Viewer viewer)
        {
            return level;
        }

        @Override
        public boolean isCreative(Viewer viewer)
        {
            return creative;
        }

        @Override
        public void setLevel(Viewer viewer, int level)
        {
            this.level = level;
        }

        @Override
        public double roll()
        {
            return roll;
        }

        @Override
        public boolean giveBook(Viewer viewer, ENCHANTMENT_TIER tier, ITEM_CATEGORY category)
        {
            if (available)
                given.add(tier + " " + category);
            return available;
        }

        @Override
        public void giveBooster(Viewer viewer, int directions)
        {
            given.add("booster " + directions);
        }

        @Override
        public void openTable(Viewer viewer)
        {
            given.add("table");
        }
    }

    private final Shop _shop = new Shop();

    @Test
    void aBookCostsLevelsAndFollowsTheRolls()
    {
        MenuTester menu = MenuTester.open(new InventoryBuyEnchants(_shop));
        // A high roll: never the likelier side, and no higher tier
        menu.click(InventoryBuyEnchants.TOOLS);
        assertEquals(List.of("TIER_1 ARMOR"), _shop.given);
        assertEquals(100 - CONSTANTS.COST_FIRST_1_ENCHANTS, _shop.level);

        _shop.roll = 0;
        menu.click(InventoryBuyEnchants.TOOLS + 18);
        assertEquals("TIER_3 TOOL", _shop.given.getLast());
        menu.click(InventoryBuyEnchants.ARMOR + 9);
        assertEquals("TIER_2 ARMOR", _shop.given.getLast());
    }

    @Test
    void withoutLevelsNothingIsBought()
    {
        _shop.level = CONSTANTS.CAP_FIRST_3_ENCHANTS - 1;
        MenuTester menu = MenuTester.open(new InventoryBuyEnchants(_shop));
        menu.click(InventoryBuyEnchants.ARMOR + 18);
        assertTrue(_shop.given.isEmpty());
        assertEquals("Not Enough Levels", menu.viewer().lastMessage());

        _shop.creative = true;
        _shop.level = 2;
        menu.click(InventoryBuyEnchants.ARMOR + 18);
        assertEquals(1, _shop.given.size(), "creative players buy without levels");
        assertEquals(0, _shop.level, "and never go below zero");
    }

    @Test
    void nothingIsChargedWhenNoEnchantIsOn()
    {
        _shop.available = false;
        MenuTester menu = MenuTester.open(new InventoryBuyEnchants(_shop));
        menu.click(InventoryBuyEnchants.ARMOR);
        assertEquals(100, _shop.level);
        assertEquals("No enchants of this kind are available right now", menu.viewer().lastMessage());
    }

    @Test
    void boostersHaveMoreDirectionsByTier()
    {
        MenuTester menu = MenuTester.open(new InventoryBuyEnchants(_shop));
        menu.click(InventoryBuyEnchants.BOOSTERS).click(InventoryBuyEnchants.BOOSTERS + 9).click(InventoryBuyEnchants.BOOSTERS + 18);
        assertEquals(List.of("booster 1", "booster 4", "booster 4"), _shop.given);
        assertEquals(100 - 3 * CONSTANTS.COST_FIRST_1_BOOSTER, _shop.level);
    }

    @Test
    void backOpensTheTable()
    {
        MenuTester menu = MenuTester.open(new InventoryBuyEnchants(_shop));
        menu.click(InventoryBuyEnchants.BACK);
        assertTrue(menu.isClosed());
        assertEquals(List.of("table"), _shop.given);
    }
}
