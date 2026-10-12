package imu.GS.engine;

import imu.GS.engine.customer.Customer;
import imu.GS.engine.customer.CustomerContext;
import imu.GS.engine.customer.CustomerInventory;
import imu.GS.engine.customer.Wallet;
import imu.GS.engine.pricing.Overflow;
import imu.GS.engine.pricing.PriceRules;
import imu.GS.engine.pricing.SellLimit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Test doubles for the Bukkit side of the engine.
 */
public final class Fakes
{
    private Fakes() {}

    public static ItemKey item(String id)
    {
        return new ItemKey(id);
    }

    public static class Rules implements PriceRules
    {
        public final Map<ItemKey, Double> values = new HashMap<>();
        public final Map<ItemKey, Overflow> overflows = new HashMap<>();
        public final Map<ItemKey, String> groups = new HashMap<>();
        public final Map<ItemKey, Double> uniques = new HashMap<>();
        public final Map<ItemKey, Double> conditions = new HashMap<>();
        public final Map<ItemKey, ItemKey> leftovers = new HashMap<>();
        public final Map<ItemKey, String> limitGroups = new HashMap<>();
        public final Map<String, SellLimit> limits = new HashMap<>();
        public final Set<ItemKey> neverBought = new HashSet<>();

        @Override
        public boolean neverBuys(ItemKey item)
        {
            return neverBought.contains(item);
        }

        @Override
        public String sellLimitGroup(ItemKey item)
        {
            return limitGroups.get(item);
        }

        @Override
        public SellLimit sellLimit(String group)
        {
            return limits.get(group);
        }

        public Rules value(String item, double value)
        {
            values.put(item(item), value);
            return this;
        }

        @Override
        public double value(ItemKey item)
        {
            return values.getOrDefault(item, 0.0);
        }

        @Override
        public Overflow overflow(ItemKey item)
        {
            return overflows.get(item);
        }

        @Override
        public String overflowGroup(ItemKey item)
        {
            return groups.get(item);
        }

        @Override
        public Double uniquePrice(ItemKey item)
        {
            return uniques.get(item);
        }

        @Override
        public double condition(ItemKey item)
        {
            return conditions.getOrDefault(item, 1.0);
        }

        @Override
        public ItemKey leftover(ItemKey item)
        {
            return leftovers.get(item);
        }
    }

    public static class Inventory implements CustomerInventory
    {
        /** slot -> stack */
        public final Map<Integer, Entry> slots = new LinkedHashMap<>();
        public final Map<Integer, Map<ItemKey, Integer>> shulkers = new HashMap<>();
        /** Items given that didn't come from a slot: what the player received */
        public final Map<ItemKey, Integer> received = new HashMap<>();
        /** Slots below this are the hotbar */
        public int hotbarSize = 9;

        public Inventory put(int slot, String item, int amount, ItemCategory category)
        {
            slots.put(slot, new Entry(slot, item(item), amount, category));
            return this;
        }

        public Inventory putShulker(int slot, String box, Map<ItemKey, Integer> contents)
        {
            slots.put(slot, new Entry(slot, item(box), 1, ItemCategory.SHULKER_BOX));
            shulkers.put(slot, new LinkedHashMap<>(contents));
            return this;
        }

        @Override
        public List<Entry> contents(boolean includeHotbarAndArmor)
        {
            List<Entry> entries = new ArrayList<>();
            for (Entry entry : slots.values())
            {
                if (includeHotbarAndArmor || entry.slot() >= hotbarSize)
                    entries.add(entry);
            }
            return entries;
        }

        @Override
        public int count(ItemKey item)
        {
            int count = received.getOrDefault(item, 0);
            for (Entry entry : slots.values())
            {
                if (entry.item().equals(item))
                    count += entry.amount();
            }
            return count;
        }

        @Override
        public boolean remove(ItemKey item, int amount)
        {
            if (count(item) < amount)
                return false;
            int left = amount;
            int fromReceived = Math.min(left, received.getOrDefault(item, 0));
            received.merge(item, -fromReceived, Integer::sum);
            left -= fromReceived;
            for (Integer slot : new ArrayList<>(slots.keySet()))
            {
                Entry entry = slots.get(slot);
                if (left == 0 || !entry.item().equals(item))
                    continue;
                int taken = Math.min(left, entry.amount());
                left -= taken;
                if (taken == entry.amount())
                    slots.remove(slot);
                else
                    slots.put(slot, new Entry(slot, item, entry.amount() - taken, entry.category()));
            }
            return true;
        }

        @Override
        public void give(ItemKey item, int amount)
        {
            received.merge(item, amount, Integer::sum);
        }

        @Override
        public Map<ItemKey, Integer> shulkerContents(int slot)
        {
            return shulkers.getOrDefault(slot, Map.of());
        }

        @Override
        public boolean emptyShulker(int slot, Set<ItemKey> keep)
        {
            if (!shulkers.containsKey(slot))
                return false;
            shulkers.get(slot).keySet().removeIf(item -> !keep.contains(item));
            return true;
        }
    }

    public static class Bank implements Wallet
    {
        public final Map<UUID, Double> balances = new HashMap<>();
        public boolean failPayments;

        @Override
        public double balance(UUID player)
        {
            return balances.getOrDefault(player, 0.0);
        }

        @Override
        public boolean withdraw(UUID player, double amount)
        {
            if (failPayments || balance(player) < amount)
                return false;
            balances.merge(player, -amount, Double::sum);
            return true;
        }

        @Override
        public boolean deposit(UUID player, double amount)
        {
            if (failPayments)
                return false;
            balances.merge(player, amount, Double::sum);
            return true;
        }
    }

    public static class Context implements CustomerContext
    {
        public long time = 6000;
        public String world = "world";
        public double x, y, z;
        public final Set<String> permissions = new HashSet<>();

        @Override
        public long worldTime()
        {
            return time;
        }

        @Override
        public String world()
        {
            return world;
        }

        @Override
        public double x()
        {
            return x;
        }

        @Override
        public double y()
        {
            return y;
        }

        @Override
        public double z()
        {
            return z;
        }

        @Override
        public boolean hasPermission(String permission)
        {
            return permissions.contains(permission);
        }
    }

    public static Customer customer(String name, Inventory inventory, Context context)
    {
        return new Customer(UUID.nameUUIDFromBytes(name.getBytes()), name, inventory, context);
    }
}
