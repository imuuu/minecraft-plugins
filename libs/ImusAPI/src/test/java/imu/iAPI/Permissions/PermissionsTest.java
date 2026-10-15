package imu.iAPI.Permissions;

import imu.iAPI.Menus.ClickKind;
import imu.iAPI.Menus.ConfirmMenu;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Testing.MenuTester;
import imu.iAPI.Menus.Testing.TestViewer;
import imu.iAPI.Menus.Viewer;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.PermissionDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PermissionsTest
{
    private static final String PLUGIN_YML = """
            name: imusTNT
            permissions:
              imustnt.use:
                description: Light special TNT
                default: true
                level: low
              imustnt.admin:
                description: Take and give special TNT, change the settings
                level: high
                icon: tnt
                children:
                  imustnt.use: true
              imustnt.everything:
                description: All of it
                default: false
                level: Very high
                children: [imustnt.admin]
              it.inv:
                description: Same as imustnt.admin
                hidden: true
              imustnt.odd:
                level: extreme
                default: sometimes
            """;

    /**
     * Players with permissions worked out like Bukkit: a grant, else a given parent's child, else the default.
     */
    private static class FakeGame implements PermissionGame
    {
        final PermissionGrants grants = new PermissionGrants();
        final List<PermissionTarget> players = new ArrayList<>();
        final Map<String, PermissionTarget> known = new HashMap<>();
        final List<String> changes = new ArrayList<>();
        PluginPermissions plugin;

        PermissionTarget add(String name, boolean op, boolean nearby)
        {
            PermissionTarget target = new PermissionTarget(UUID.nameUUIDFromBytes(name.getBytes()), name, op, true,
                    Icon.of(Material.PLAYER_HEAD));
            if (nearby)
                players.add(target);
            known.put(name.toLowerCase(), target);
            return target;
        }

        public List<PermissionTarget> nearby(Viewer viewer) { return players; }

        public PermissionTarget find(String name) { return known.get(name.toLowerCase()); }

        public boolean has(PermissionTarget target, PermissionInfo permission)
        {
            Boolean grant = grants.get(target.id(), permission.node());
            if (grant != null)
                return grant;
            for (PermissionInfo parent : plugin.permissions())
            {
                if (Boolean.TRUE.equals(grants.get(target.id(), parent.node())) && parent.children().contains(permission.node()))
                    return true;
            }
            return permission.hasByDefault(target.op());
        }

        public Boolean grant(PermissionTarget target, String node) { return grants.get(target.id(), node); }

        public void setGrant(PermissionTarget target, String node, Boolean value, Viewer by)
        {
            grants.set(target.id(), target.name(), node, value);
            changes.add(by.name() + " " + node + "=" + value + " " + target.name());
        }
    }

    private final List<String> _problems = new ArrayList<>();
    private PluginPermissions _tnt;
    private FakeGame _game;
    private PermissionTarget _steve;

    @BeforeEach
    void setUp()
    {
        _tnt = PluginPermissions.fromYaml("imusTNT", PLUGIN_YML, _problems::add);
        _game = new FakeGame();
        _game.plugin = _tnt;
        _steve = _game.add("Steve", false, true);
        _game.add("Alex", false, true);
        _game.add("Faraway", false, false);
    }

    @AfterEach
    void tearDown()
    {
        Permissions.clear();
    }

    // Reading plugin.yml =============================================================================================

    @Test
    void readsLevelsDefaultsIconsAndChildren()
    {
        assertEquals(List.of("imustnt.use", "imustnt.admin", "imustnt.everything", "imustnt.odd"),
                _tnt.permissions().stream().map(PermissionInfo::node).toList(), "in order, hidden left out");

        PermissionInfo admin = _tnt.get("imustnt.admin");
        assertEquals(PermissionLevel.HIGH, admin.level());
        assertEquals(PermissionDefault.OP, admin.byDefault(), "op when not set, like Bukkit");
        assertEquals("tnt", admin.icon());
        assertEquals(List.of("imustnt.use"), admin.children());

        assertEquals(PermissionDefault.TRUE, _tnt.get("imustnt.use").byDefault());
        assertEquals(PermissionLevel.VERY_HIGH, _tnt.get("IMUSTNT.EVERYTHING").level());
        assertEquals(List.of("imustnt.admin"), _tnt.get("imustnt.everything").children());
    }

    @Test
    void badValuesAreReportedButTheRestIsRead()
    {
        PermissionInfo odd = _tnt.get("imustnt.odd");
        assertNull(odd.level());
        assertEquals(PermissionDefault.OP, odd.byDefault());
        assertEquals(2, _problems.size(), _problems.toString());
        assertTrue(_problems.get(0).contains("extreme"));
        assertTrue(_problems.get(1).contains("sometimes"));
    }

    @Test
    void levelsParseInAnySpelling()
    {
        assertEquals(PermissionLevel.VERY_HIGH, PermissionLevel.parse("very-high"));
        assertEquals(PermissionLevel.MEDIUM, PermissionLevel.parse(" Medium "));
        assertNull(PermissionLevel.parse(null));
        assertThrows(IllegalArgumentException.class, () -> PermissionLevel.parse("huge"));
    }

    @Test
    void registryFindsPluginsAndNodesInAnyCase()
    {
        List<List<String>> changes = new ArrayList<>();
        Permissions.onChange(changes::add);
        Permissions.register(_tnt);
        Permissions.register(PluginPermissions.of("HomeTele", PermissionInfo.of("hometele.use", "Homes", PermissionLevel.LOW)));
        assertSame(_tnt, Permissions.get("IMUSTNT"));
        assertEquals(List.of("HomeTele", "imusTNT"), Permissions.names());
        assertEquals("imustnt.admin", Permissions.find("imustnt.ADMIN").node());

        Permissions.unregister("imustnt");
        assertNull(Permissions.get("imusTNT"));
        assertEquals(List.of("HomeTele"), changes.getLast());
        Permissions.onChange(null);
    }

    // Grants =========================================================================================================

    @Test
    void grantsSaveAndLoad()
    {
        PermissionGrants grants = new PermissionGrants();
        UUID steve = UUID.randomUUID();
        assertTrue(grants.set(steve, "Steve", "imustnt.Admin", true));
        assertFalse(grants.set(steve, "Steve", "imustnt.admin", true), "nothing changed");
        grants.set(steve, "Steve", "hometele.use", false);

        YamlConfiguration file = new YamlConfiguration();
        grants.save(file);
        assertEquals(List.of("imustnt.admin", "-hometele.use"), file.getStringList("players." + steve + ".grants"));

        PermissionGrants loaded = new PermissionGrants();
        loaded.load(file);
        assertEquals(Boolean.TRUE, loaded.get(steve, "IMUSTNT.ADMIN"));
        assertEquals(Boolean.FALSE, loaded.get(steve, "hometele.use"));
        assertEquals("Steve", loaded.nameOf(steve));

        loaded.set(steve, "Steve", "imustnt.admin", null);
        loaded.set(steve, "Steve", "hometele.use", null);
        assertTrue(loaded.of(steve).isEmpty());
        assertNull(loaded.nameOf(steve), "a player with nothing left is forgotten");
    }

    // Menus ==========================================================================================================

    @Test
    void pluginsMenuListsRegisteredPluginsWithTheirLevels()
    {
        Permissions.register(_tnt);
        Permissions.register(PluginPermissions.of("Empty"));
        MenuTester menu = MenuTester.open(new PermissionPluginsMenu(_game));
        assertEquals("imusTNT", menu.name(0));
        assertEquals(-1, menu.find("Empty"), "nothing to change there");
        assertTrue(menu.lore(0).contains("  High: 1"), menu.lore(0).toString());
        assertTrue(menu.lore(0).contains("  Not set: 1"));

        menu.click("imusTNT").assertShowing(PermissionPlayersMenu.class);
        assertEquals("Steve", menu.name(0));
        assertEquals("Alex", menu.name(1));
        assertEquals(-1, menu.find("Faraway"));
    }

    @Test
    void searchedPlayerComesFirst()
    {
        MenuTester menu = MenuTester.open(new PermissionPlayersMenu(_game, _tnt));
        menu.click(PermissionPlayersMenu.SEARCH);
        assertEquals("Nobody called Nobody has played here", menu.answer("Nobody"));
        assertNull(menu.answer(" faraway "));
        assertEquals("Faraway", menu.name(0));
        assertTrue(menu.lore(0).contains("Searched"));

        menu.click("Faraway").assertShowing(PlayerPermissionsMenu.class);
        assertEquals("imusTNT: Faraway", menu.title());
    }

    @Test
    void nobodyNearby()
    {
        _game.players.clear();
        MenuTester menu = MenuTester.open(new PermissionPlayersMenu(_game, _tnt));
        assertEquals("Nobody close by", menu.name(PermissionPlayersMenu.NOBODY));
    }

    @Test
    void permissionShowsWhatItGivesAndHowDangerous()
    {
        MenuTester menu = MenuTester.open(new PlayerPermissionsMenu(_game, _tnt, _steve));
        int use = menu.slotOf("imustnt.use");
        assertEquals("✔ imustnt.use", menu.name(use));
        assertEquals("minecraft:lime_dye", menu.icon(use).material());
        assertTrue(menu.lore(use).contains("Danger: Low"));
        assertTrue(menu.lore(use).contains("Has it: Yes (default: everyone)"));

        int admin = menu.slotOf("imustnt.admin");
        assertEquals("✘ imustnt.admin", menu.name(admin));
        assertEquals("minecraft:tnt", menu.icon(admin).material(), "its own icon");
        assertTrue(menu.lore(admin).contains("Take and give special TNT, change the"), menu.lore(admin).toString());
        assertTrue(menu.lore(admin).contains("Danger: High"));
        assertTrue(menu.lore(admin).contains("Gives also: imustnt.use"));
        assertTrue(menu.lore(PlayerPermissionsMenu.PLAYER).contains("Has 1/4 of imusTNT's permissions"));
    }

    @Test
    void clickTakesAwayAndGivesBack()
    {
        MenuTester menu = MenuTester.open(new PlayerPermissionsMenu(_game, _tnt, _steve), TestViewer.admin("Admin"));
        menu.click("imustnt.use");
        assertEquals(Boolean.FALSE, _game.grants.get(_steve.id(), "imustnt.use"), "everyone has it, so taken away");
        assertEquals("Steve doesn't have imustnt.use now", menu.viewer().lastMessage());
        assertTrue(menu.lore(menu.slotOf("imustnt.use")).contains("Has it: No (taken away here)"));

        menu.click("imustnt.use");
        assertNull(_game.grants.get(_steve.id(), "imustnt.use"), "back to the default instead of a grant");
        assertEquals(List.of("Admin imustnt.use=false Steve", "Admin imustnt.use=null Steve"), _game.changes);
    }

    @Test
    void highLevelsAskFirst()
    {
        MenuTester menu = MenuTester.open(new PlayerPermissionsMenu(_game, _tnt, _steve), TestViewer.admin("Admin"));
        menu.click("imustnt.admin").assertShowing(ConfirmMenu.class);
        menu.click(ConfirmMenu.NO).assertShowing(PlayerPermissionsMenu.class);
        assertNull(_game.grants.get(_steve.id(), "imustnt.admin"));

        menu.click("imustnt.admin").click(ConfirmMenu.YES).assertShowing(PlayerPermissionsMenu.class);
        assertEquals(Boolean.TRUE, _game.grants.get(_steve.id(), "imustnt.admin"));
        assertTrue(menu.lore(menu.slotOf("imustnt.admin")).contains("Has it: Yes (given here)"));
        assertTrue(menu.icon(menu.slotOf("imustnt.admin")).glow());

        menu.click("imustnt.admin");
        menu.assertShowing(PlayerPermissionsMenu.class);
        assertNull(_game.grants.get(_steve.id(), "imustnt.admin"), "taking away doesn't ask; Steve isn't op, so back to default");
        assertTrue(menu.lore(menu.slotOf("imustnt.admin")).contains("Has it: No (default: ops)"));
    }

    @Test
    void takingAChildGivenByAParentIsKeptAsTakenAway()
    {
        _game.grants.set(_steve.id(), "Steve", "imustnt.everything", true);
        MenuTester menu = MenuTester.open(new PlayerPermissionsMenu(_game, _tnt, _steve), TestViewer.admin("Admin"));
        assertTrue(menu.lore(menu.slotOf("imustnt.admin")).contains("Has it: Yes (default: ops)"), "through its parent");

        menu.click("imustnt.admin");
        assertEquals(Boolean.FALSE, _game.grants.get(_steve.id(), "imustnt.admin"),
                "the default would leave it on through the parent, so it is taken away");
        assertTrue(menu.lore(menu.slotOf("imustnt.admin")).contains("Has it: No (taken away here)"));
    }

    @Test
    void nobodyGivesWhatTheyDontHave()
    {
        TestViewer helper = TestViewer.with("Helper", "ia.permissions", "imustnt.use");
        MenuTester menu = MenuTester.open(new PlayerPermissionsMenu(_game, _tnt, _steve), helper);
        menu.click("imustnt.admin");
        assertEquals("You can only give permissions you have yourself.", helper.lastMessage());
        assertNull(_game.grants.get(_steve.id(), "imustnt.admin"));
        assertTrue(_game.changes.isEmpty());

        menu.click("imustnt.use");
        assertEquals(Boolean.FALSE, _game.grants.get(_steve.id(), "imustnt.use"), "taking away is fine");
    }

    @Test
    void rightClickAndResetAllGoBackToDefaults()
    {
        _game.grants.set(_steve.id(), "Steve", "imustnt.admin", true);
        _game.grants.set(_steve.id(), "Steve", "imustnt.everything", true);
        MenuTester menu = MenuTester.open(new PlayerPermissionsMenu(_game, _tnt, _steve), TestViewer.admin("Admin"));
        assertTrue(menu.lore(menu.slotOf("imustnt.admin")).contains("Right click to reset to the default (ops)"));

        menu.click("imustnt.admin", ClickKind.RIGHT);
        assertNull(_game.grants.get(_steve.id(), "imustnt.admin"));

        menu.click("Reset all").click(ConfirmMenu.YES);
        assertTrue(_game.grants.of(_steve.id()).isEmpty());
        assertEquals("1 of Steve's imusTNT permissions back to default", menu.viewer().lastMessage());
        assertEquals(-1, menu.find("Reset all"), "nothing left to reset");
    }

    @Test
    void wrapsDescriptions()
    {
        assertEquals(List.of("one two", "three", "averyveryverylongword"),
                PlayerPermissionsMenu.wrap(" one two  three averyveryverylongword", 9));
        assertEquals(List.of(), PlayerPermissionsMenu.wrap("", 9));
    }
}
