package imu.iAPI.CustomEnchants;

import imu.iAPI.CustomItems.CustomItemProvider;
import imu.iAPI.CustomItems.CustomItems;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CustomRegistriesTest
{
    private static class Enchants implements CustomEnchantProvider
    {
        @Override
        public Collection<Info> enchants()
        {
            return List.of(new Info("test:tunnel", "Tunnel", 3, 3), new Info("test:haste", "Haste", 2, 2));
        }

        @Override
        public Map<String, Integer> read(ItemStack item)
        {
            return Map.of("test:tunnel", 1);
        }

        @Override
        public boolean canApply(ItemStack item, String key)
        {
            return key.equals("test:haste");
        }

        @Override
        public boolean apply(ItemStack item, String key)
        {
            return canApply(item, key);
        }

        @Override
        public ItemStack book(String key, int level)
        {
            return null;
        }
    }

    private static class Items implements CustomItemProvider
    {
        @Override
        public List<CustomItem> items()
        {
            return List.of(new CustomItem("test:chunk_tnt", "Chunk TNT", null, 3, List.of("redstone", "explosives")),
                    new CustomItem("test:booster", "Booster", null, 1, List.of("enchanting")));
        }

        @Override
        public String idOf(ItemStack item)
        {
            return null;
        }
    }

    private final Enchants _enchants = new Enchants();
    private final Items _items = new Items();

    @AfterEach
    void tearDown()
    {
        CustomEnchants.unregister(_enchants);
        CustomItems.unregister(_items);
    }

    @Test
    void withoutProvidersEverythingIsEmpty()
    {
        assertTrue(CustomEnchants.isEmpty());
        assertTrue(CustomEnchants.enchants().isEmpty());
        assertTrue(CustomEnchants.read(null).isEmpty());
        assertNull(CustomEnchants.info("test:tunnel"));
        assertFalse(CustomEnchants.apply(null, "test:haste"));
        assertTrue(CustomItems.items().isEmpty());
        assertNull(CustomItems.idOf(null));
    }

    @Test
    void enchantsAreFoundByKeyWhateverTheCase()
    {
        CustomEnchants.register(_enchants);
        CustomEnchants.register(_enchants);
        assertEquals(2, CustomEnchants.enchants().size(), "registering twice adds it once");
        assertEquals(3, CustomEnchants.info("TEST:Tunnel").tier());
        assertSame(_enchants, CustomEnchants.providerOf("test:haste"));
        assertNull(CustomEnchants.providerOf("other:thing"));
    }

    @Test
    void applyingGoesThroughTheProvidersOwnRules()
    {
        CustomEnchants.register(_enchants);
        assertTrue(CustomEnchants.canApply(null, "test:haste"));
        assertFalse(CustomEnchants.canApply(null, "test:tunnel"));
        assertFalse(CustomEnchants.apply(null, "other:thing"), "nobody knows the key");
    }

    @Test
    void itemsAreFoundByIdAndTag()
    {
        CustomItems.register(_items);
        assertEquals("Chunk TNT", CustomItems.item("TEST:CHUNK_TNT").name());
        assertEquals(List.of("test:chunk_tnt"), CustomItems.withTags(List.of("Redstone")).stream().map(CustomItemProvider.CustomItem::id).toList());
        assertTrue(CustomItems.withTags(List.of("farming")).isEmpty());
    }

    @Test
    void unregisteringRemovesTheProvider()
    {
        CustomItems.register(_items);
        CustomItems.unregister(_items);
        assertNull(CustomItems.item("test:booster"));
    }
}
