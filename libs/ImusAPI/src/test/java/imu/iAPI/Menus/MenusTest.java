package imu.iAPI.Menus;

import imu.iAPI.Menus.Testing.MenuTester;
import imu.iAPI.Menus.Testing.TestViewer;
import org.junit.jupiter.api.Test;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MenusTest
{
    /**
     * A counter and a list of 30 numbers in pages of 9.
     */
    static class CounterMenu extends Menu
    {
        int count;
        int opened;
        int closed;
        final List<Integer> numbers = new ArrayList<>();
        final Pager<Integer> pager = Pager.rows(1, 1);

        CounterMenu()
        {
            super("&6Counter", 3);
            for (int i = 1; i <= 30; i++)
                numbers.add(i);
        }

        @Override
        protected void render(Canvas canvas)
        {
            canvas.fillRow(2, Icon.filler(Material.BLACK_STAINED_GLASS_PANE));
            canvas.set(0, Icon.of(Material.EMERALD, "&aCount " + count), click -> count += click.isRight() ? -1 : 1);
            canvas.set(1, Icon.of(Material.ENDER_PEARL, "Open another"), click -> click.open(new CounterMenu()));
            canvas.set(2, Buttons.confirm(Icon.of(Material.TNT, "&cReset"), "&cReset the count?", click -> count = 0));
            canvas.set(3, Icon.of(Material.NAME_TAG, "Rename"), click -> click.prompt(Prompt.of("Count", List.of(), "" + count, 5, text ->
            {
                try
                {
                    count = Integer.parseInt(text);
                    return null;
                } catch (NumberFormatException e)
                {
                    return "Not a number";
                }
            })));
            canvas.set(4, Icon.of(Material.BARRIER, "Broken"), click -> { throw new IllegalStateException("boom"); });
            pager.render(canvas, numbers, n -> Button.display(Icon.of(Material.PAPER, "#" + n)), 18, 26);
            Buttons.back(canvas, 22);
        }

        @Override
        protected void onOpen(Viewer viewer)
        {
            opened++;
        }

        @Override
        protected void onClose(Viewer viewer)
        {
            closed++;
        }
    }

    @Test
    void aClickChangesStateAndTheMenuIsDrawnAgain()
    {
        CounterMenu counter = new CounterMenu();
        MenuTester menu = MenuTester.open(counter);
        assertEquals("Counter", menu.title());
        assertEquals("Count 0", menu.name(0));

        menu.click(0).click(0).click(0, ClickKind.RIGHT);
        assertEquals(1, counter.count);
        assertEquals("Count 1", menu.name(0));
    }

    @Test
    void onlyChangedSlotsAreSent()
    {
        List<Integer> sent = new ArrayList<>();
        MenuSession session = new MenuSession(TestViewer.admin("A"), new MenuHost()
        {
            public void show(Menu menu, Map<Integer, Icon> icons) {}

            public void set(int slot, Icon icon) { sent.add(slot); }

            public void prompt(Prompt prompt) {}

            public Object cursor() { return null; }

            public void setCursor(Object item) {}

            public void close() {}

            public void error(String what, RuntimeException error) { throw error; }
        });
        session.open(new CounterMenu());
        session.click(0, ClickKind.LEFT, -1);
        assertEquals(List.of(0), sent);
        session.refresh();
        assertEquals(List.of(0), sent, "nothing changed");
    }

    @Test
    void pagesTurnWithTheArrows()
    {
        MenuTester menu = MenuTester.open(new CounterMenu());
        assertEquals("#1", menu.name(9));
        assertEquals(-1, menu.find("Previous page"), "no previous page on the first");
        menu.click("Next page");
        assertEquals("#10", menu.name(9));
        assertTrue(menu.lore(26).contains("Page 2/4"));
        menu.click("Next page").click("Next page");
        assertEquals("#28", menu.name(9));
        assertEquals("#30", menu.name(11));
        assertNull(menu.icon(12), "the rest of the last page is empty");
        assertEquals(-1, menu.find("Next page"));
        menu.click("Previous page");
        assertEquals("#19", menu.name(9));
    }

    @Test
    void backReturnsToThePreviousMenuAndClosesTheFirst()
    {
        CounterMenu first = new CounterMenu();
        MenuTester menu = MenuTester.open(first);
        assertEquals(-1, menu.find("Go Back"), "nothing to go back to");

        menu.click("Open another");
        CounterMenu second = menu.assertShowing(CounterMenu.class);
        assertNotSame(first, second);
        menu.click(0);
        assertEquals(1, second.count);

        menu.click("Go Back");
        assertSame(first, menu.menu());
        assertEquals(2, first.opened, "shown again");
        assertEquals(1, second.closed);
        assertEquals(0, first.closed);

        menu.session().back();
        assertTrue(menu.isClosed());
        assertEquals(1, first.closed);
    }

    @Test
    void confirmingRunsTheActionAndGoesBack()
    {
        CounterMenu counter = new CounterMenu();
        MenuTester menu = MenuTester.open(counter);
        menu.click(0).click(0);

        menu.click("Reset");
        menu.assertShowing(ConfirmMenu.class);
        assertEquals("Reset the count?", menu.title());
        menu.click(ConfirmMenu.NO);
        assertSame(counter, menu.menu());
        assertEquals(2, counter.count);

        menu.click("Reset").click(ConfirmMenu.YES);
        assertSame(counter, menu.menu());
        assertEquals(0, counter.count);
        assertEquals("Count 0", menu.name(0));
    }

    @Test
    void aPromptAsksAgainUntilTheAnswerIsGood()
    {
        CounterMenu counter = new CounterMenu();
        MenuTester menu = MenuTester.open(counter);
        menu.click("Rename");
        assertEquals("0", menu.prompt().initial());
        assertThrows(AssertionError.class, () -> menu.click(0), "the menu isn't shown during a prompt");

        assertEquals("Not a number", menu.answer("ten"));
        assertNotNull(menu.prompt());
        assertNull(menu.answer("10"));
        assertSame(counter, menu.assertShowing(CounterMenu.class));
        assertEquals("Count 10", menu.name(0));

        menu.click("Rename").cancelPrompt();
        assertEquals(10, counter.count);
    }

    @Test
    void aFailingButtonFailsTheTest()
    {
        MenuTester menu = MenuTester.open(new CounterMenu());
        AssertionError error = assertThrows(AssertionError.class, () -> menu.click("Broken"));
        assertEquals("boom", error.getCause().getMessage());
    }

    @Test
    void closingTheWindowClosesEveryMenu()
    {
        CounterMenu first = new CounterMenu();
        MenuTester menu = MenuTester.open(first);
        menu.click("Open another");
        CounterMenu second = menu.assertShowing(CounterMenu.class);
        menu.closeWindow();
        assertEquals(1, first.closed);
        assertEquals(1, second.closed);
        assertTrue(menu.session().hasEnded());
    }

    @Test
    void togglesCyclesAndCounters()
    {
        class Settings extends Menu
        {
            boolean on;
            String mode = "fast";
            int amount = 5;

            Settings()
            {
                super("Settings", 1);
            }

            @Override
            protected void render(Canvas canvas)
            {
                canvas.set(0, Buttons.toggle(Icon.of(Material.LEVER, "Power"), on, value -> on = value));
                canvas.set(1, Buttons.cycle(Icon.of(Material.CLOCK, "Mode"), List.of("slow", "fast", "faster"), mode, m -> m, value -> mode = value));
                canvas.set(2, Buttons.counter(Icon.of(Material.GOLD_NUGGET, "Amount"), amount, 1, 20, value -> amount = value));
            }
        }
        Settings settings = new Settings();
        MenuTester menu = MenuTester.open(settings);

        assertTrue(menu.lore(0).contains("Off"));
        menu.click(0);
        assertTrue(settings.on);
        assertTrue(menu.icon(0).glow());

        menu.click(1);
        assertEquals("faster", settings.mode);
        menu.click(1);
        assertEquals("slow", settings.mode, "wraps around");
        menu.click(1, ClickKind.RIGHT);
        assertEquals("faster", settings.mode);
        assertTrue(menu.lore(1).contains("> faster"));

        menu.click(2, ClickKind.SHIFT_LEFT);
        assertEquals(15, settings.amount);
        menu.click(2, ClickKind.SHIFT_LEFT);
        assertEquals(20, settings.amount, "stops at the max");
        menu.click(2, ClickKind.RIGHT);
        assertEquals(19, settings.amount);
        assertEquals(19, menu.icon(2).amount());
    }

    @Test
    void theDumpShowsTheGrid()
    {
        String dump = MenuTester.open(new CounterMenu()).dump();
        assertTrue(dump.startsWith("Counter\n"));
        assertTrue(dump.contains("Count 0"));
        assertEquals(4, dump.split("\n").length, "title and three rows");
    }

    @Test
    void timersDrawTheMenuAgain()
    {
        class Clock extends Menu
        {
            int seconds;

            Clock()
            {
                super("Clock", 1);
            }

            @Override
            public int refreshTicks()
            {
                return 20;
            }

            @Override
            protected boolean onTick()
            {
                seconds++;
                return true;
            }

            @Override
            protected void render(Canvas canvas)
            {
                canvas.set(4, Icon.of(Material.CLOCK, seconds + " s"));
            }
        }
        MenuTester menu = MenuTester.open(new Clock());
        menu.tick().tick();
        assertEquals("2 s", menu.name(4));
    }

    @Test
    void iconsAreValues()
    {
        Icon icon = Icon.of("DIAMOND", "&bShiny", "&7line");
        assertEquals("minecraft:diamond", icon.material());
        assertEquals("Shiny", icon.plainName());
        assertEquals(List.of("line"), icon.plainLore());
        assertEquals(icon, Icon.of("minecraft:diamond", "&bShiny", "&7line"));
        assertNotEquals(icon, icon.amount(2));
        assertEquals(List.of("line", "more"), icon.addLore("more").plainLore());
        assertEquals(List.of("line"), icon.plainLore(), "the original stays");
    }

    @Test
    void itemsMoveBetweenTheCursorAndASlot()
    {
        // One slot that holds whatever the player puts in it
        class Box extends Menu
        {
            Object held;

            Box()
            {
                super("Box", 1);
            }

            @Override
            protected void render(Canvas canvas)
            {
                if (held == null)
                    canvas.set(4, Button.empty(click ->
                    {
                        held = click.cursor();
                        click.setCursor(null);
                    }));
                else
                    canvas.set(4, Icon.of(Material.DIAMOND, held.toString()), click ->
                    {
                        if (click.cursor() != null)
                            return;
                        click.setCursor(held);
                        held = null;
                    });
            }
        }
        Box box = new Box();
        MenuTester menu = MenuTester.open(box);
        assertNull(menu.icon(4), "an empty slot");

        menu.holding("Sword").click(4);
        assertEquals("Sword", box.held);
        assertNull(menu.cursor());
        assertEquals("Sword", menu.name(4));

        menu.holding("Apple").click(4);
        assertEquals("Apple", menu.cursor(), "a full slot doesn't swap");
        menu.holding(null).click(4);
        assertEquals("Sword", menu.cursor());
        assertNull(menu.icon(4));
    }

    // A menu that throws closes ============================================================================

    /**
     * A menu that throws where it is told to, and counts how often it was closed.
     */
    static class Fragile extends Menu
    {
        boolean throwOnRender;
        boolean throwOnClose;
        boolean throwOnTick;
        int closed;

        Fragile()
        {
            super("Fragile", 1);
        }

        @Override
        protected void render(Canvas canvas)
        {
            if (throwOnRender)
                throw new IllegalStateException("render");
            canvas.set(0, Icon.of(Material.STONE, "Break"), click -> { throw new IllegalStateException("click"); });
            canvas.set(1, Icon.of(Material.ARROW, "Deeper"), click -> click.open(new Fragile()));
            canvas.set(2, Icon.of(Material.NAME_TAG, "Ask"), click -> click.prompt(Prompt.of("Ask", List.of(), "", 5,
                    text -> { throw new IllegalStateException("answer"); })));
            canvas.set(3, Icon.of(Material.FEATHER, "Redraw fails"), click -> throwOnRender = true);
        }

        @Override
        protected void onClose(Viewer viewer)
        {
            closed++;
            if (throwOnClose)
                throw new IllegalStateException("close");
        }

        @Override
        public int refreshTicks()
        {
            return 20;
        }

        @Override
        protected boolean onTick()
        {
            if (throwOnTick)
                throw new IllegalStateException("tick");
            return true;
        }
    }

    @Test
    void aFailingClickClosesEveryMenu()
    {
        Fragile first = new Fragile();
        MenuTester menu = MenuTester.openKeepingErrors(first);
        menu.click("Deeper");
        Fragile second = menu.assertShowing(Fragile.class);
        menu.click("Break");

        assertTrue(menu.isClosed(), "a broken menu doesn't stay open");
        assertTrue(menu.session().hasEnded());
        assertEquals(1, first.closed, "the menu under it closes too, giving back what it holds");
        assertEquals(1, second.closed);
        assertEquals(List.of("Clicking slot 0 of Fragile: click"), menu.errors());
    }

    @Test
    void aMenuThatCantBeDrawnClosesAtOnce()
    {
        Fragile broken = new Fragile();
        broken.throwOnRender = true;
        MenuTester menu = MenuTester.openKeepingErrors(broken);
        assertTrue(menu.isClosed());
        assertEquals(1, broken.closed);
        assertEquals(List.of("Opening Fragile: render"), menu.errors());

        Fragile later = new Fragile();
        MenuTester redraw = MenuTester.openKeepingErrors(later);
        redraw.click("Redraw fails");
        assertTrue(redraw.isClosed(), "also when only a redraw fails");
        assertEquals(List.of("Drawing Fragile: render"), redraw.errors());
    }

    @Test
    void oneMenuFailingToCloseDoesntKeepTheOthersOpen()
    {
        Fragile first = new Fragile();
        MenuTester menu = MenuTester.openKeepingErrors(first);
        menu.click("Deeper");
        Fragile second = menu.assertShowing(Fragile.class);
        second.throwOnClose = true;

        menu.closeWindow();
        assertEquals(1, second.closed);
        assertEquals(1, first.closed, "closed although the one above it threw");
        assertEquals(List.of("Closing Fragile: close"), menu.errors());
    }

    @Test
    void aFailingTimerOrAnswerClosesTheMenu()
    {
        Fragile ticking = new Fragile();
        MenuTester timer = MenuTester.openKeepingErrors(ticking);
        timer.tick();
        assertFalse(timer.isClosed());
        ticking.throwOnTick = true;
        timer.tick();
        assertTrue(timer.isClosed());
        assertEquals(1, ticking.closed);

        Fragile asking = new Fragile();
        MenuTester prompt = MenuTester.openKeepingErrors(asking);
        prompt.click("Ask");
        assertNull(prompt.answer("x"), "the dialog closes");
        assertTrue(prompt.isClosed(), "and the menu doesn't come back");
        assertEquals(1, asking.closed);
    }
}
