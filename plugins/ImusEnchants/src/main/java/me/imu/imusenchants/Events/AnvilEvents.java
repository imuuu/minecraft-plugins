package me.imu.imusenchants.Events;

import imu.iAPI.Utilities.InvUtil;
import imu.iAPI.Utilities.ItemUtilToolsArmors;
import imu.iAPI.Utilities.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class AnvilEvents implements Listener
{
    private final int MAX_REPAIR_COUNT = 64;
    @EventHandler
    public void onAnvilUse(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack[] contents = inventory.getContents();

        if(!(ItemUtilToolsArmors.IsTool(contents[0]) || ItemUtilToolsArmors.IsArmor(contents[0])))
        {
            return;
        }
        if (contents[0] != null && contents[1] != null && contents[1].getType() == Material.DIAMOND) {

            ItemStack itemToRepair = contents[0];
            int repairCount = getRepairCount(itemToRepair);
            int diamondCost = calculateDiamondCost(repairCount);
            if (contents[1].getAmount() >= diamondCost)
            {
                ItemStack newItem = itemToRepair.clone();
                ItemUtils.SetDamage(newItem, 0);
                ItemUtils.SetPersistenData(newItem, "ie_repairCount", PersistentDataType.INTEGER, getNextRepairCount(repairCount));

                event.setResult(newItem);
            }
        }
    }

    @EventHandler
    public void onAnvil(InventoryClickEvent e)
    {
        if(e.isCancelled()) return;

        if(!(e.getInventory() instanceof AnvilInventory)) return;

        if(e.getSlotType() != InventoryType.SlotType.RESULT) return;


        AnvilInventory inv = (AnvilInventory)e.getInventory();

        if(inv.getItem(2) == null) return;

        if(inv.getItem(0) == null || inv.getItem(1) == null) return;

        if(e.getCurrentItem() != null && getRepairCount(e.getCurrentItem()) > 0)
        {
            int repairCount = getRepairCount(inv.getItem(0));
            int diamondCost = calculateDiamondCost(repairCount);

            if(inv.getItem(1).getAmount() < diamondCost) return;

            inv.getItem(1).setAmount(inv.getItem(1).getAmount() - diamondCost);
            inv.getItem(0).setAmount(0);
            InvUtil.AddItemToInventoryOrDrop((Player) e.getWhoClicked(), e.getCurrentItem().clone());
            e.getCurrentItem().setAmount(0);

        }
    }

    private int getRepairCount(ItemStack item)
    {
        Integer repairCount = ItemUtils.GetPersistenData(item, "ie_repairCount", PersistentDataType.INTEGER);
        return repairCount == null ? 0 : repairCount;
    }

    private int getNextRepairCount(int currentRepairCount)
    {
        if(currentRepairCount >= MAX_REPAIR_COUNT)
        {
            return MAX_REPAIR_COUNT;
        }

        return currentRepairCount + 1;
    }

    private int calculateDiamondCost(int repairCount)
    {
        return 5 + repairCount;
    }
}
