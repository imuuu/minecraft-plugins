package me.imu.imusunstablefearsystem.Inventories;

import imu.iAPI.Menus.Testing.MenuTester;
import imu.iAPI.Menus.Viewer;
import me.imu.imusunstablefearsystem.Enums.DIFFICULT;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SelectDifficultyTest
{
    private final List<String> _done = new ArrayList<>();

    private MenuTester open()
    {
        return MenuTester.open(new Inv_SelectDifficulty(new Inv_SelectDifficulty.Choice()
        {
            @Override
            public void choose(Viewer viewer, DIFFICULT difficult)
            {
                _done.add(difficult.name());
            }

            @Override
            public void closed(Viewer viewer)
            {
                _done.add("portal back");
            }
        }));
    }

    @Test
    void pickingClosesAndGivesThePortalBack()
    {
        MenuTester menu = open();
        assertEquals("FEAR SYSTEM ENABLED", menu.name(Inv_SelectDifficulty.FEAR));
        menu.click(Inv_SelectDifficulty.FEAR);
        assertTrue(menu.isClosed());
        assertEquals(List.of("FEAR", "portal back"), _done);
    }

    @Test
    void closingWithoutPickingGivesThePortalBack()
    {
        open().closeWindow();
        assertEquals(List.of("portal back"), _done);
    }
}
