package imu.iAPI.Config;

import imu.iAPI.Menus.ClickKind;
import imu.iAPI.Menus.Testing.MenuTester;
import imu.iAPI.Menus.Testing.TestViewer;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class ConfigMenusTest
{
    private final YamlConfiguration _config = new YamlConfiguration();
    private int _saves;
    private int _changes;
    private ConfigMenu _menu;

    @BeforeEach
    void setUp()
    {
        YamlConfiguration defaults = new YamlConfiguration();
        defaults.set("shop.enabled", true);
        defaults.set("shop.seconds", 30);
        defaults.set("shop.mode", "calm");
        _config.setDefaults(defaults);
        _config.set("shop.enabled", true);
        _config.set("shop.seconds", 45);
        _config.set("shop.mode", "calm");

        _menu = new ConfigMenu(new ConfigMenu.Store()
        {
            public String pluginName() { return "TestPlugin"; }

            public FileConfiguration config() { return _config; }

            public void save() { _saves++; }

            public Logger logger() { return Logger.getLogger("test"); }
        }, "Test settings", "test.config", () -> _changes++);
        _menu.addBoolean("shop.enabled", "Shop on", Material.CHEST).note("now");
        _menu.addInt("shop.seconds", "Seconds", Material.CLOCK, 1, 600).description("How long");
        _menu.addChoice("shop.mode", "Mode", Material.COMPASS, List.of("calm", "busy"));
        _menu.addString("shop.name", "Name", Material.NAME_TAG);
    }

    @Test
    void entriesShowTheirValues()
    {
        MenuTester menu = MenuTester.open(new ConfigEntriesMenu(_menu));
        assertEquals("Test settings", menu.title());
        assertEquals("Shop on", menu.name(0));
        assertEquals("minecraft:lime_dye", menu.icon(0).material());
        assertTrue(menu.lore(1).contains("Value: 45"));
        assertTrue(menu.lore(1).contains("Default: 30"));
        assertTrue(menu.lore(1).contains("Allowed: 1 - 600"));
        assertTrue(menu.lore(1).contains("How long"));
        assertEquals("minecraft:clock", menu.icon(1).material());
        assertEquals(-1, menu.find("Go Back"), "opened on its own");
    }

    @Test
    void togglingAndCyclingSaveAtOnce()
    {
        MenuTester menu = MenuTester.open(new ConfigEntriesMenu(_menu));
        menu.click("Shop on");
        assertFalse(_config.getBoolean("shop.enabled"));
        assertEquals("minecraft:gray_dye", menu.icon(0).material());
        assertEquals(1, _saves);
        assertEquals(1, _changes);
        assertEquals("Shop on changed true -> false", menu.viewer().lastMessage());

        menu.click("Mode");
        assertEquals("busy", _config.getString("shop.mode"));
    }

    @Test
    void numbersAreTypedAndChecked()
    {
        MenuTester menu = MenuTester.open(new ConfigEntriesMenu(_menu));
        menu.click("Seconds");
        assertEquals("45", menu.prompt().initial());
        assertEquals("0 is outside 1 - 600", menu.answer("0"));
        assertEquals(45, _config.getInt("shop.seconds"));
        assertNull(menu.answer("90"));
        assertEquals(90, _config.getInt("shop.seconds"));
        assertTrue(menu.lore(menu.slotOf("Seconds")).contains("Value: 90"), "the menu came back with the new value");
    }

    @Test
    void rightClickResetsToTheDefault()
    {
        MenuTester menu = MenuTester.open(new ConfigEntriesMenu(_menu));
        menu.click("Seconds", ClickKind.RIGHT);
        assertEquals(30, _config.getInt("shop.seconds"));
        assertFalse(menu.lore(menu.slotOf("Name")).contains("Right click to reset to default"), "no default for the name");
    }

    @Test
    void withoutPermissionNothingChanges()
    {
        MenuTester menu = MenuTester.open(new ConfigEntriesMenu(_menu), TestViewer.with("Guest"));
        menu.click("Shop on");
        assertTrue(_config.getBoolean("shop.enabled"));
        assertEquals(0, _saves);
        assertEquals("You don't have permission to change these settings!", menu.viewer().lastMessage());
    }

    @Test
    void theListShowsOnlyMenusThePlayerMayEdit()
    {
        ConfigMenu open = new ConfigMenu(_menuStore("Other"), "Other settings", null, null);
        MenuTester guest = MenuTester.open(new ConfigListMenu(List.of(_menu, open)), TestViewer.with("Guest"));
        assertEquals(-1, guest.find("Test settings"));
        assertEquals(0, guest.slotOf("Other settings"));

        MenuTester admin = MenuTester.open(new ConfigListMenu(List.of(_menu, open)));
        admin.click("Test settings");
        admin.assertShowing(ConfigEntriesMenu.class);
        admin.click("Go Back");
        admin.assertShowing(ConfigListMenu.class);
    }

    private ConfigMenu.Store _menuStore(String name)
    {
        return new ConfigMenu.Store()
        {
            public String pluginName() { return name; }

            public FileConfiguration config() { return new YamlConfiguration(); }

            public void save() {}

            public Logger logger() { return Logger.getLogger("test"); }
        };
    }
}
