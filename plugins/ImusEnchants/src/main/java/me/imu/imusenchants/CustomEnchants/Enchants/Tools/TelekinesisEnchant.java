package me.imu.imusenchants.CustomEnchants.Enchants.Tools;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Drops and XP go straight into the inventory. What doesn't fit falls on the ground as usual.
public class TelekinesisEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "telekinesis"; }
	@Override public String GetName() { return "Telekinesis"; }
	@Override public int GetMaxLevel() { return 1; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.TOOL; }

	// Runs after Smelting, Replanter, Wisdom and Scavenger have changed the drops,
	// and before the mining enchants that end the break handling
	@Override public int GetPriority() { return 100; }

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.Union(ItemTarget.MINING_TOOLS, ItemTarget.SetOf(ItemTarget.SWORD));
	}

	@Override
	public String GetDescription(int level)
	{
		return "Drops and XP go straight into your inventory";
	}

	@Override
	public boolean OnBlockBreak(BlockBreakEvent event, ItemStack tool, int level)
	{
		if (event.getExpToDrop() > 0)
		{
			event.getPlayer().giveExp(event.getExpToDrop());
			event.setExpToDrop(0);
		}
		return false;
	}

	@Override
	public void OnBlockDrop(BlockDropItemEvent event, ItemStack tool, int level)
	{
		Iterator<Item> drops = event.getItems().iterator();
		while (drops.hasNext())
		{
			Item drop = drops.next();
			ItemStack leftover = GiveOrLeftover(event.getPlayer(), drop.getItemStack());
			if (leftover == null)
				drops.remove();
			else
				drop.setItemStack(leftover);
		}
	}

	@Override
	public void OnKill(EntityDeathEvent event, Player killer, ItemStack weapon, int level)
	{
		List<ItemStack> drops = event.getDrops();
		for (int i = drops.size() - 1; i >= 0; i--)
		{
			// skip empty stacks (other plugins may empty the ones they keep)
			if (drops.get(i) == null || drops.get(i).getAmount() <= 0)
				continue;

			ItemStack leftover = GiveOrLeftover(killer, drops.get(i));
			if (leftover == null)
				drops.remove(i);
			else
				drops.set(i, leftover);
		}

		if (event.getDroppedExp() > 0)
		{
			killer.giveExp(event.getDroppedExp());
			event.setDroppedExp(0);
		}
	}

	// Null when everything fit
	private static ItemStack GiveOrLeftover(Player player, ItemStack stack)
	{
		HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(stack.clone());
		for (Map.Entry<Integer, ItemStack> entry : leftovers.entrySet())
			return entry.getValue();
		return null;
	}
}
