package imu.iAPI.LootTables;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ImusLootTable
{
	protected List<ILootTableItem<?>> items = new ArrayList<>();
	private int totalWeight = 0;

	public int getTotalWeight() {
		return totalWeight;
	}

	public void add(ILootTableItem<?> item) {
		items.add(item);
		totalWeight += item.get_weight();
	}

	public <T> void add(T value, int weight) {
		totalWeight += weight;
		items.add(new LootTableItem<>(value, weight));
	}

	public <T> void add(T value, int weight, int maxAmount) {
		totalWeight += weight;
		items.add(new LootTableItem<>(value, weight, maxAmount));
	}

	public Object getLoot() {
		int randomIndex = ThreadLocalRandom.current().nextInt(totalWeight);

		for (ILootTableItem<?> item : items) {
			if (randomIndex < item.get_weight()) {
				return item.get_value();
			}
			randomIndex -= item.get_weight();
		}
		return null;
	}

	public LinkedList<Object> getLoot(int rolls) {
		LinkedList<Object> _items = new LinkedList<>();

		for (int i = 0; i < rolls; i++) {
			_items.add(getLoot());
		}

		return _items;
	}

	public void addLootAsItemStack(Inventory inv, int rolls) {
		List<Object> loots = getLoot(rolls);

		for (Object loot : loots) {
			if (loot instanceof ItemStack) {
				ItemStack stack = (ItemStack) loot;
				if (stack != null) {
					inv.addItem(stack);
				}
			}
			// Handle other types if needed
		}
	}

	public List<ILootTableItem<?>> getItems() {
		return Collections.unmodifiableList(items);
	}

	public void printLoot()
	{
		for(ILootTableItem<?> item : items)
		{
			System.out.println(item.get_value() + " " + item.get_weight());
		}
	}
}
