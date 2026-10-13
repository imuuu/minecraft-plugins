package imu.imustnt;

import imu.iAPI.Menus.ClickKind;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Testing.MenuTester;
import imu.imustnt.engine.TntType;
import org.junit.jupiter.api.Test;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TntMenuTest
{
    private final List<String> _given = new ArrayList<>();

    private MenuTester open()
    {
        return MenuTester.open(new TntMenu("&5Special TNT", type -> Icon.of(Material.TNT, type.key()),
                (viewer, type, amount) -> _given.add(viewer.name() + " " + type + " " + amount)));
    }

    @Test
    void everyTntIsInTheMenu()
    {
        MenuTester menu = open();
        assertEquals("Special TNT", menu.title());
        for (int i = 0; i < TntType.values().length; i++)
            assertEquals(TntType.values()[i].key(), menu.name(i));
        assertTrue(menu.icon(8).hideTooltip(), "the rest is background");
    }

    @Test
    void aClickGivesOneAndARightOrShiftClickAStack()
    {
        MenuTester menu = open();
        menu.click(0).click(1, ClickKind.RIGHT).click(0, ClickKind.SHIFT_LEFT).click(8);
        assertEquals(List.of("Tester CHUNK_TNT 1", "Tester CHUNK_STONE_TNT 64", "Tester CHUNK_TNT 64"), _given);
    }
}
