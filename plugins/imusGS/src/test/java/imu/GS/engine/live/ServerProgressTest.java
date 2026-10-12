package imu.GS.engine.live;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerProgressTest
{
    @Test
    void itemsAreSeenOnce()
    {
        ServerProgress progress = new ServerProgress();
        assertTrue(progress.seeItem("DIAMOND_SWORD"));
        assertFalse(progress.seeItem("diamond_sword"));
        assertTrue(progress.hasSeen("Diamond_Sword"));
        assertFalse(progress.hasSeen("netherite_sword"));
    }

    @Test
    void anEnchantKeepsItsHighestLevel()
    {
        ServerProgress progress = new ServerProgress();
        assertEquals(0, progress.seenLevel("minecraft:sharpness"));
        assertTrue(progress.seeEnchant("minecraft:sharpness", 3));
        assertFalse(progress.seeEnchant("minecraft:sharpness", 2));
        assertTrue(progress.seeEnchant("minecraft:sharpness", 4));
        assertEquals(4, progress.seenLevel("minecraft:sharpness"));
    }

    @Test
    void generatedLevelsStayWithinWhatWasSeen()
    {
        assertEquals(0, ServerProgress.levelCap(5, 0));
        assertEquals(3, ServerProgress.levelCap(5, 3));
        // Higher than the max was seen (e.g. from a command): still only the max
        assertEquals(5, ServerProgress.levelCap(5, 7));
        assertFalse(ServerProgress.mayGoOverMax(5, 4));
        assertTrue(ServerProgress.mayGoOverMax(5, 5));
    }

    @Test
    void changesAreTakenOnce()
    {
        ServerProgress progress = new ServerProgress();
        assertFalse(progress.takeDirty());
        progress.seeItem("stone");
        assertTrue(progress.takeDirty());
        assertFalse(progress.takeDirty());
        progress.seeItem("stone");
        assertFalse(progress.takeDirty());
    }
}
