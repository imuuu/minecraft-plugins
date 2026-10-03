package me.imu.imusenchants.CustomEnchants.Enchants.Universal;

import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.CustomEnchantData;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import me.imu.imusenchants.ImusEnchants;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

// The item stays with the player on death, once: the enchant is used up when it saves the item.
// DontLoseItems already keeps the hotbar and armor (it empties them from the drops before this
// runs), so in practice Soulbound saves items from the rest of the inventory and those aren't
// charged for nothing.
// Between death and respawn the saved items are kept in the player's own data, so a restart
// while the player is on the death screen doesn't lose them.
public class SoulboundEnchant extends CustomEnchant
{
	public static final String KEY = "soulbound";

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

		if (saved.isEmpty())
			return;

		PersistentDataContainer pdc = event.getEntity().getPersistentDataContainer();
		List<byte[]> stored = new ArrayList<>(pdc.getOrDefault(PendingKey(), PersistentDataType.LIST.byteArrays(), List.of()));
		for (ItemStack item : saved)
			stored.add(item.serializeAsBytes());
		pdc.set(PendingKey(), PersistentDataType.LIST.byteArrays(), stored);
	}

	public void Discard(Player player)
	{
		player.getPersistentDataContainer().remove(PendingKey());
	}

	public void GiveBack(Player player)
	{
		PersistentDataContainer pdc = player.getPersistentDataContainer();
		List<byte[]> stored = pdc.get(PendingKey(), PersistentDataType.LIST.byteArrays());
		if (stored == null)
			return;
		pdc.remove(PendingKey());

		for (byte[] bytes : stored)
		{
			ItemStack item = ItemStack.deserializeBytes(bytes);
			for (ItemStack leftover : player.getInventory().addItem(item).values())
				player.getWorld().dropItemNaturally(player.getLocation(), leftover);
		}
	}

	private static NamespacedKey PendingKey()
	{
		return new NamespacedKey(ImusEnchants.Instance, "ie_soulbound_pending");
	}
}
