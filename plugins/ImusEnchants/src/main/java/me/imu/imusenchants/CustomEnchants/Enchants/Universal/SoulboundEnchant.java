package me.imu.imusenchants.CustomEnchants.Enchants.Universal;

import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.CustomEnchantData;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// The item stays with the player on death, once: the enchant is used up when it saves the item.
// DontLoseItems already keeps the hotbar and armor (it empties them from the drops before this
// runs), so in practice Soulbound saves items from the rest of the inventory and those aren't
// charged for nothing.
public class SoulboundEnchant extends CustomEnchant
{
	public static final String KEY = "soulbound";

	private final Map<UUID, List<ItemStack>> _pending = new HashMap<>();

	@Override public String GetKey() { return KEY; }
	@Override public String GetName() { return "Soulbound"; }
	@Override public int GetMaxLevel() { return 1; }
	@Override public Set<ItemTarget> GetTargets() { return ItemTarget.ALL; }

	@Override
	public String GetDescription(int level)
	{
		return "Keeps the item on death, used up when it does";
	}

	public void OnPlayerDeath(PlayerDeathEvent event)
	{
		if (event.getKeepInventory())
			return;

		List<ItemStack> saved = new ArrayList<>();
		Iterator<ItemStack> drops = event.getDrops().iterator();
		while (drops.hasNext())
		{
			ItemStack drop = drops.next();
			if (drop == null || drop.getType().isAir() || drop.getAmount() <= 0)
				continue;

			if (!CustomEnchantData.GetActive(drop).containsKey(this))
				continue;

			ItemStack kept = drop.clone();
			CustomEnchantData.Remove(kept, this);
			saved.add(kept);
			drops.remove();
		}

		if (!saved.isEmpty())
			_pending.computeIfAbsent(event.getEntity().getUniqueId(), uuid -> new ArrayList<>()).addAll(saved);
	}

	public void Discard(Player player)
	{
		_pending.remove(player.getUniqueId());
	}

	public void GiveBack(Player player)
	{
		List<ItemStack> items = _pending.remove(player.getUniqueId());
		if (items == null)
			return;

		for (ItemStack item : items)
		{
			for (ItemStack leftover : player.getInventory().addItem(item).values())
				player.getWorld().dropItemNaturally(player.getLocation(), leftover);
		}
	}
}
