package imu.iAPI.Utilities;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import imu.iAPI.Other.Metods;

public class InvUtil
{
	/**
     * Adds an ItemStack to the player's inventory. If the inventory is full, it drops the overflow items on the ground.
     * @param player The player to add the ItemStack to.
     * @param item The ItemStack to add.
     */
    public static void AddItemToInventoryOrDrop(Player player, ItemStack item) 
    {
        PlayerInventory inventory = player.getInventory();
        HashMap<Integer, ItemStack> overflow = inventory.addItem(item);

        if (!overflow.isEmpty()) 
        {
            Location location = player.getLocation();
            World world = player.getWorld();
            for (ItemStack overflowItem : overflow.values()) 
            {
                world.dropItemNaturally(location, overflowItem);
                player.sendMessage(Metods.msgC("&9Dropped: &e" + overflowItem.getType() + " &9x &e" + overflowItem.getAmount()));
            }
        }
    }
}
