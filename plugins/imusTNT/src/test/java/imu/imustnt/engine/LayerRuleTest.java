package imu.imustnt.engine;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LayerRuleTest
{
    private static final LayerRule RULE = new LayerRule(1200, Set.of("chest", "SPAWNER"), true);

    private static LayerRule.Cell block(String material)
    {
        return new LayerRule.Cell(material, 6, false, false);
    }

    @Test
    void chunkTntClearsOrdinaryBlocksAndLiquids()
    {
        assertTrue(RULE.changes(TntType.CHUNK_TNT, block("DIRT")));
        assertTrue(RULE.changes(TntType.CHUNK_TNT, new LayerRule.Cell("LAVA", 100, false, false)));
        assertFalse(RULE.changes(TntType.CHUNK_TNT, new LayerRule.Cell("AIR", 0, true, false)), "air is already clear");
    }

    @Test
    void stoneTntFillsHolesButLeavesStone()
    {
        assertTrue(RULE.changes(TntType.CHUNK_STONE_TNT, new LayerRule.Cell("CAVE_AIR", 0, true, false)));
        assertTrue(RULE.changes(TntType.CHUNK_STONE_TNT, new LayerRule.Cell("WATER", 100, false, false)));
        assertFalse(RULE.changes(TntType.CHUNK_STONE_TNT, block("STONE")));
    }

    @Test
    void toughProtectedAndUsableBlocksStay()
    {
        assertFalse(RULE.changes(TntType.CHUNK_TNT, new LayerRule.Cell("OBSIDIAN", 1200, false, false)));
        assertFalse(RULE.changes(TntType.CHUNK_TNT, block("CHEST")), "protected, whatever the case in config");
        assertFalse(RULE.changes(TntType.CHUNK_TNT, block("spawner")));
        assertFalse(RULE.changes(TntType.CHUNK_TNT, new LayerRule.Cell("FURNACE", 3.5, false, true)));
        LayerRule usableToo = new LayerRule(1200, Set.of(), false);
        assertTrue(usableToo.changes(TntType.CHUNK_TNT, new LayerRule.Cell("FURNACE", 3.5, false, true)));
    }

    @Test
    void typesAreFoundByKeyOrName()
    {
        assertEquals(TntType.CHUNK_TNT, TntType.parse("chunk-tnt"));
        assertEquals(TntType.CHUNK_STONE_TNT, TntType.parse("CHUNK_STONE_TNT"));
        assertNull(TntType.parse("nuke"));
        assertNull(TntType.parse(null));
        assertEquals("imustnt:chunk_stone_tnt", TntType.CHUNK_STONE_TNT.id());
    }
}
