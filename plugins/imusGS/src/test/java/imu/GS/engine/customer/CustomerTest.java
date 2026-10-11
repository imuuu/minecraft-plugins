package imu.GS.engine.customer;

import imu.GS.engine.Fakes;
import imu.GS.engine.ItemCategory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static imu.GS.engine.Fakes.item;
import static org.junit.jupiter.api.Assertions.*;

class CustomerTest
{
    @Test
    void stacksOfTheSameItemAreOneEntry()
    {
        Fakes.Inventory inventory = new Fakes.Inventory()
                .put(10, "stone", 64, ItemCategory.BLOCKS)
                .put(11, "dirt", 3, ItemCategory.BLOCKS)
                .put(12, "stone", 20, ItemCategory.BLOCKS);
        Customer customer = Fakes.customer("Alex", inventory, new Fakes.Context());

        List<Customer.SellEntry> entries = customer.sellEntries(ItemCategory.BLOCKS, false);
        assertEquals(2, entries.size());
        assertEquals("stone", entries.get(0).getItem().id());
        assertEquals(84, entries.get(0).getAmount());
    }

    @Test
    void tabsShowTheirOwnCategoryOnly()
    {
        Fakes.Inventory inventory = new Fakes.Inventory()
                .put(10, "stone", 64, ItemCategory.BLOCKS)
                .put(11, "sword", 1, ItemCategory.GEAR);
        Customer customer = Fakes.customer("Alex", inventory, new Fakes.Context());

        assertEquals(List.of("sword"), customer.sellEntries(ItemCategory.GEAR, false).stream().map(e -> e.getItem().id()).toList());
    }

    @Test
    void hotbarIsLeftOutUnlessAsked()
    {
        Fakes.Inventory inventory = new Fakes.Inventory().put(0, "stone", 64, ItemCategory.BLOCKS);
        Customer customer = Fakes.customer("Alex", inventory, new Fakes.Context());

        assertTrue(customer.sellEntries(ItemCategory.BLOCKS, false).isEmpty());
        assertEquals(1, customer.sellEntries(ItemCategory.BLOCKS, true).size());
    }

    @Test
    void everyShulkerBoxIsItsOwnEntry()
    {
        Fakes.Inventory inventory = new Fakes.Inventory()
                .putShulker(10, "box", Map.of(item("stone"), 64))
                .putShulker(11, "box", Map.of(item("dirt"), 64));
        Customer customer = Fakes.customer("Alex", inventory, new Fakes.Context());

        List<Customer.SellEntry> entries = customer.sellEntries(ItemCategory.SHULKER_BOX, false);
        assertEquals(2, entries.size());
        assertTrue(entries.get(0).isShulker());
        assertEquals(11, entries.get(1).getShulkerSlot());
    }
}
