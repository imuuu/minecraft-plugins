package imu.waystones;

import imu.iAPI.Menus.ClickKind;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Testing.MenuTester;
import imu.iAPI.Menus.Testing.TestViewer;
import imu.iAPI.Menus.Viewer;
import imu.waystones.engine.Cost;
import imu.waystones.engine.Panel;
import imu.waystones.engine.Rules;
import imu.waystones.engine.UpgradeKind;
import imu.waystones.engine.Visibility;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The waystone menus clicked through with the texts of the real config.yml and a stand-in for the server.
 */
class WaystoneMenusTest
{
    private static final YamlConfiguration CONFIG = YamlConfiguration.loadConfiguration(new File("src/main/resources/config.yml"));
    private static final Rules RULES = new Rules(30, 3, 1, List.of(5400.0, 60.0), 5, 0, 1, Map.of(
            UpgradeKind.CAST_TIME, List.of(Cost.parse("LAPIS_BLOCK:10"), Cost.parse("LAPIS_BLOCK:20")),
            UpgradeKind.FOUNDATION, List.of(Cost.parse("GOLD_BLOCK"))));

    private final TestViewer _player = TestViewer.with("Alex", "imuswaystones.use");
    private final TestViewer _admin = TestViewer.admin("Admin");
    private final Game _game = new Game();
    private final Waystone _home = _game.add("&6Home", _player.id());

    /**
     * The server as the menus see it: what they asked for is written down.
     */
    private static final class Game implements WaystoneGame
    {
        final Map<UUID, Waystone> waystones = new LinkedHashMap<>();
        final List<String> done = new ArrayList<>();
        long cooldown;
        boolean canPay = true;
        boolean worldLoaded = true;

        Waystone add(String name, UUID owner)
        {
            Waystone ws = new Waystone(UUID.randomUUID(), "world", waystones.size() * 100, 64, 0);
            ws.name = name;
            ws.owner = owner;
            ws.ownerName = "Owner";
            waystones.put(ws.id, ws);
            return ws;
        }

        @Override
        public String text(String key, Object... placeholders)
        {
            String text = CONFIG.getString("messages." + key, key);
            for (int i = 0; i + 1 < placeholders.length; i += 2)
                text = text.replace("{" + placeholders[i] + "}", String.valueOf(placeholders[i + 1]));
            return text;
        }

        @Override
        public List<String> textList(String key)
        {
            return CONFIG.getStringList("messages." + key);
        }

        @Override
        public Rules rules()
        {
            return RULES;
        }

        @Override
        public Collection<Waystone> all()
        {
            return waystones.values();
        }

        @Override
        public Waystone get(UUID id)
        {
            return waystones.get(id);
        }

        @Override
        public List<Waystone> destinations(Waystone from, UUID player)
        {
            return waystones.values().stream().filter(ws -> ws != from).toList();
        }

        @Override
        public long cooldownLeft(Viewer viewer, Waystone from)
        {
            return cooldown;
        }

        @Override
        public Icon icon(Waystone ws)
        {
            return Icon.of("lodestone");
        }

        @Override
        public void save(Waystone ws)
        {
            done.add("save " + ws.name);
        }

        @Override
        public void savePanel(Waystone ws, UUID player)
        {
            done.add("save panel " + ws.panel(player).tiers());
        }

        @Override
        public void remove(Waystone ws)
        {
            waystones.remove(ws.id);
            done.add("remove " + ws.name);
        }

        @Override
        public void travel(Viewer viewer, Waystone from, Waystone to)
        {
            done.add(viewer.name() + " travels to " + to.name);
        }

        @Override
        public boolean teleport(Viewer viewer, Waystone to)
        {
            done.add(viewer.name() + " teleports to " + to.name);
            return worldLoaded;
        }

        @Override
        public boolean take(Viewer viewer, List<Cost> cost)
        {
            done.add("take " + cost);
            return canPay;
        }

        @Override
        public void giveUpgrades(Viewer viewer, Panel panel)
        {
            done.add("give " + panel.tiers());
        }

        @Override
        public void sound(Viewer viewer, String sound, float volume, float pitch)
        {
            done.add("sound " + sound);
        }
    }

    private MenuTester waystoneMenu(TestViewer viewer)
    {
        return MenuTester.open(new WaystoneMenu(_game, _home), viewer);
    }

    // The waystone menu ==============================================================================================

    @Test
    void destinationsAreListedAndAClickStartsTheTrip()
    {
        Waystone mine = _game.add("&bMine", _player.id());
        MenuTester menu = waystoneMenu(_player);
        assertEquals("Home", menu.title());
        assertEquals("Mine", menu.name(0));
        assertEquals(List.of("Owner: Owner", "World: world", " ", "(Click) to teleport"), menu.lore(0));

        menu.click(0);
        assertTrue(menu.isClosed());
        assertEquals(List.of("Alex travels to " + mine.name), _game.done);
    }

    @Test
    void manyDestinationsGoOnPages()
    {
        for (int i = 1; i <= 30; i++)
            _game.add("Stone " + i, _player.id());
        MenuTester menu = waystoneMenu(_player);
        assertEquals("Stone 27", menu.name(26));
        assertEquals(-1, menu.find("Previous"));
        menu.click(WaystoneMenu.NEXT);
        assertEquals("Stone 28", menu.name(0));
        assertNull(menu.icon(3));
        assertTrue(menu.lore(WaystoneMenu.PREVIOUS).contains("Page 2/2"));
    }

    @Test
    void noDestinationsYet()
    {
        MenuTester menu = waystoneMenu(_player);
        assertEquals("Nowhere to go yet", menu.name(13));
    }

    @Test
    void aClosedWaystoneSaysWhy()
    {
        _game.add("Elsewhere", _player.id());
        _game.cooldown = 90_000;
        assertTrue(waystoneMenu(_player).name(13).startsWith("You can leave from here again in"));

        _game.cooldown = 0;
        _home.visibility = Visibility.TO_ALL_ONE_WAY;
        assertEquals("You can only arrive at this waystone", waystoneMenu(_player).name(13));

        _home.enabled = false;
        assertEquals("This waystone is off", waystoneMenu(_player).name(13));
    }

    @Test
    void onlyAdminsSeeTheSettings()
    {
        assertNull(waystoneMenu(_player).icon(WaystoneMenu.VISIBILITY).name());
        MenuTester menu = waystoneMenu(_admin);
        assertEquals("Who sees it", menu.name(WaystoneMenu.VISIBILITY));
        assertTrue(menu.lore(WaystoneMenu.VISIBILITY).contains("Now: players who touched it"));

        menu.click(WaystoneMenu.VISIBILITY).click(WaystoneMenu.UNBREAKABLE).click(WaystoneMenu.MAX_OUT).click(WaystoneMenu.ENABLED);
        assertEquals(Visibility.BY_TOUCH.next(), _home.visibility);
        assertTrue(_home.unbreakable);
        assertTrue(_home.maxedOut);
        assertFalse(_home.enabled);
        assertEquals(4, _game.done.size(), "saved after each: " + _game.done);
        assertTrue(menu.lore(WaystoneMenu.UNBREAKABLE).contains("Now: yes"));
    }

    @Test
    void removingTakesAShiftClick()
    {
        MenuTester menu = waystoneMenu(_admin);
        menu.click(WaystoneMenu.REMOVE);
        assertFalse(menu.isClosed());
        assertTrue(_game.done.isEmpty());

        menu.click(WaystoneMenu.REMOVE, ClickKind.SHIFT_LEFT);
        assertTrue(menu.isClosed());
        assertNull(_game.get(_home.id));
        assertEquals("Waystone Home removed", _admin.lastMessage());
    }

    // Upgrades =======================================================================================================

    private MenuTester upgradeMenu(TestViewer viewer)
    {
        MenuTester menu = waystoneMenu(viewer);
        menu.click(WaystoneMenu.UPGRADES);
        menu.assertShowing(UpgradeMenu.class);
        return menu;
    }

    @Test
    void anUpgradeShowsItsTierAndCost()
    {
        MenuTester menu = upgradeMenu(_player);
        assertEquals("Upgrades: Home", menu.title());
        int slot = UpgradeMenu.SLOTS.get(UpgradeKind.CAST_TIME);
        assertEquals("Cast Time", menu.name(slot));
        List<String> lore = menu.lore(slot);
        assertTrue(lore.contains("Now: 30s"), lore.toString());
        assertTrue(lore.contains("Tier 0/2"));
        assertTrue(lore.contains(" 10 x Lapis Block"));
        assertFalse(lore.contains("(Shift right click) upgrade free (admin)"));
        assertEquals("Dimensions is off", menu.name(UpgradeMenu.SLOTS.get(UpgradeKind.DIMENSION)));
    }

    @Test
    void buyingTakesTheItemsAndRaisesTheTier()
    {
        MenuTester menu = upgradeMenu(_player);
        int slot = UpgradeMenu.SLOTS.get(UpgradeKind.CAST_TIME);
        menu.click(slot);
        assertEquals(1, _home.panel(_player.id()).tier(UpgradeKind.CAST_TIME));
        assertEquals(List.of("take [Cost[material=LAPIS_BLOCK, amount=10]]", "save panel {CAST_TIME=1}", "sound block.anvil.use"), _game.done);
        assertTrue(menu.lore(slot).contains("Now: 27s"));
        assertTrue(menu.lore(slot).contains(" 20 x Lapis Block"));

        _game.canPay = false;
        menu.click(slot);
        assertEquals(1, _home.panel(_player.id()).tier(UpgradeKind.CAST_TIME));
        assertEquals("You don't have the items for that", _player.lastMessage());

        menu.click(slot, ClickKind.SHIFT_RIGHT);
        assertEquals(1, _home.panel(_player.id()).tier(UpgradeKind.CAST_TIME), "only admins get it free");
    }

    @Test
    void adminsUpgradeFreeAndTheLastTierIsMaxed()
    {
        MenuTester menu = upgradeMenu(_admin);
        int slot = UpgradeMenu.SLOTS.get(UpgradeKind.FOUNDATION);
        menu.click(slot, ClickKind.SHIFT_RIGHT);
        assertEquals(1, _home.panel(_admin.id()).tier(UpgradeKind.FOUNDATION));
        assertFalse(_game.done.stream().anyMatch(line -> line.startsWith("take")));
        assertTrue(menu.lore(slot).contains("Maxed out!"));
    }

    @Test
    void upgradesCanBeTakenOntoAnItem()
    {
        MenuTester menu = upgradeMenu(_player);
        menu.click(UpgradeMenu.EXTRACT, ClickKind.SHIFT_RIGHT);
        assertEquals("You have no upgrades here", _player.lastMessage());

        _home.panel(_player.id()).set(UpgradeKind.CAST_TIME, 2);
        menu.click(UpgradeMenu.EXTRACT);
        assertTrue(_home.panel(_player.id()).tiers().containsValue(2), "a plain click does nothing");
        menu.click(UpgradeMenu.EXTRACT, ClickKind.SHIFT_RIGHT);
        assertTrue(_game.done.contains("give {CAST_TIME=2}"));
        assertTrue(_home.panel(_player.id()).isEmpty());
    }

    @Test
    void theOwnerRenamesTheWaystone()
    {
        MenuTester menu = upgradeMenu(_player);
        menu.click(UpgradeMenu.RENAME);
        assertEquals("&6Home", menu.prompt().initial());
        assertEquals("The name can't be empty", menu.answer("  "));
        assertNull(menu.answer(" &aGarden "));
        assertEquals("&aGarden", _home.name);
        assertEquals("The waystone is now Garden", _player.lastMessage());
        menu.assertShowing(UpgradeMenu.class);

        assertEquals(-1, upgradeMenu(TestViewer.with("Stranger")).find("Rename"), "only the owner and admins");
    }

    @Test
    void backGoesToTheWaystone()
    {
        MenuTester menu = upgradeMenu(_player);
        menu.click(UpgradeMenu.BACK);
        menu.assertShowing(WaystoneMenu.class);
    }

    @Test
    void aMaxedOutWaystoneHasNothingToBuy()
    {
        _home.maxedOut = true;
        MenuTester menu = upgradeMenu(_player);
        assertEquals("Maxed out!", menu.name(13));
        assertEquals(-1, menu.find("Cast Time"));
    }

    // The admin list =================================================================================================

    @Test
    void theListGoesToAWaystoneOrRemovesIt()
    {
        Waystone far = _game.add("Far", _player.id());
        MenuTester menu = MenuTester.open(new ListMenu(_game), _admin);
        assertEquals("Waystones (2)", menu.title());
        assertTrue(menu.lore(1).contains("At: world 100 64 0"));

        menu.click(1, ClickKind.SHIFT_RIGHT);
        assertNull(_game.get(far.id));
        assertNull(menu.icon(1), "gone from the list");

        menu.click(0);
        assertTrue(menu.isClosed());
        assertTrue(_game.done.contains("Admin teleports to &6Home"));
    }

    @Test
    void anUnloadedWorldIsTold()
    {
        _game.worldLoaded = false;
        MenuTester.open(new ListMenu(_game), _admin).click(0);
        assertEquals("The world world isn't loaded", _admin.lastMessage());
    }
}
