package me.imu.imusenchants.CustomEnchants.Enchants.Combat;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Set;

// Chance for a head drop: player heads and the vanilla mob skulls
public class BeheadingEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "beheading"; }
	@Override public String GetName() { return "Beheading"; }
	@Override public int GetMaxLevel() { return 3; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.WEAPON; }
	@Override public int GetPriority() { return 150; } // before Telekinesis collects drops

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.MELEE;
	}

	private static double GetChance(int level) { return Scale(level, 0.05, 0.10, 0.15); }

	@Override
	public String GetDescription(int level)
	{
		return Percent(GetChance(level)) + " chance to drop the head of a player or mob";
	}

	@Override
	public void OnKill(EntityDeathEvent event, Player killer, ItemStack weapon, int level)
	{
		ItemStack head = GetHead(event.getEntity());
		if (head == null || !Roll(GetChance(level)))
			return;

		for (ItemStack drop : event.getDrops())
		{
			if (drop != null && drop.getType() == head.getType())
				return;
		}
		event.getDrops().add(head);
	}

	private static ItemStack GetHead(LivingEntity entity)
	{
		switch (entity.getType())
		{
			case PLAYER:
				ItemStack head = new ItemStack(Material.PLAYER_HEAD);
				SkullMeta meta = (SkullMeta) head.getItemMeta();
				meta.setOwningPlayer((Player) entity);
				head.setItemMeta(meta);
				return head;
			case ZOMBIE: return new ItemStack(Material.ZOMBIE_HEAD);
			case SKELETON: return new ItemStack(Material.SKELETON_SKULL);
			case CREEPER: return new ItemStack(Material.CREEPER_HEAD);
			case WITHER_SKELETON: return new ItemStack(Material.WITHER_SKELETON_SKULL);
			case PIGLIN: return new ItemStack(Material.PIGLIN_HEAD);
			default: return null;
		}
	}
}
