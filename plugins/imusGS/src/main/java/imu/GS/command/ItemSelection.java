package imu.GS.command;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Which of the player's items a command uses: hand, hotbar or inventory.
 */
public enum ItemSelection
{
    HAND,
    HOTBAR,
    INVENTORY;

    public static final List<String> NAMES = List.of("hand", "hotbar", "inventory");

    /**
     * Null when the text isn't one of hand, hotbar, inventory (inv works too).
     */
    public static ItemSelection parse(String text)
    {
        return switch (text.toLowerCase(Locale.ROOT))
        {
            case "hand" -> HAND;
            case "hotbar" -> HOTBAR;
            case "inventory", "inv" -> INVENTORY;
            default -> null;
        };
    }

    /**
     * The selected stacks, empty slots left out.
     */
    public List<ItemStack> items(Player player)
    {
        List<ItemStack> items = new ArrayList<>();
        switch (this)
        {
            case HAND -> items.add(player.getInventory().getItemInMainHand());
            case HOTBAR ->
            {
                for (int slot = 0; slot < 9; slot++)
                    items.add(player.getInventory().getItem(slot));
            }
            case INVENTORY ->
            {
                for (int slot = 0; slot < 36; slot++)
                    items.add(player.getInventory().getItem(slot));
            }
        }
        items.removeIf(stack -> stack == null || stack.getType() == Material.AIR);
        return items;
    }

    /**
     * The different materials of the selected stacks.
     */
    public List<Material> materials(Player player)
    {
        List<Material> materials = new ArrayList<>();
        for (ItemStack stack : items(player))
        {
            if (!materials.contains(stack.getType()))
                materials.add(stack.getType());
        }
        return materials;
    }
}
