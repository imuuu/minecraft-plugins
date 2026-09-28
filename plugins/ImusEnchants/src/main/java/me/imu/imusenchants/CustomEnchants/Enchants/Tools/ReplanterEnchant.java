package me.imu.imusenchants.CustomEnchants.Enchants.Tools;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import me.imu.imusenchants.ImusEnchants;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

// Harvesting a fully grown crop plants it again, using one seed from its own drops
public class ReplanterEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "replanter"; }
	@Override public String GetName() { return "Replanter"; }
	@Override public int GetMaxLevel() { return 1; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.TOOL; }
	@Override public int GetPriority() { return 150; } // takes the seed before Telekinesis picks drops up

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.SetOf(ItemTarget.HOE);
	}

	@Override
	public String GetDescription(int level)
	{
		return "Harvested crops are planted again";
	}

	@Override
	public void OnBlockDrop(BlockDropItemEvent event, ItemStack tool, int level)
	{
		BlockState state = event.getBlockState();
		if (!CropUtil.IsMatureCrop(state))
			return;

		Material crop = state.getType();
		Material seed = CropUtil.GetSeed(crop);
		if (!TakeSeed(event, seed))
			return;

		Block block = event.getBlock();
		Bukkit.getScheduler().runTask(ImusEnchants.Instance, () ->
		{
			if (block.getType().isAir() && CropUtil.CanPlant(crop, block.getRelative(0, -1, 0)))
				block.setType(crop);
		});
	}

	private static boolean TakeSeed(BlockDropItemEvent event, Material seed)
	{
		for (Item drop : event.getItems())
		{
			ItemStack stack = drop.getItemStack();
			if (stack.getType() != seed)
				continue;

			if (stack.getAmount() <= 1)
			{
				event.getItems().remove(drop);
			}
			else
			{
				stack.setAmount(stack.getAmount() - 1);
				drop.setItemStack(stack);
			}
			return true;
		}
		return false;
	}
}
