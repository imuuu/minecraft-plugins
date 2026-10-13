package imu.iAPI.Menus.Paper;

import imu.iAPI.Menus.Icon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Icons to item stacks and back.
 */
public final class PaperIcons
{
    private PaperIcons() {}

    /**
     * An icon showing a ready item as it is, e.g. a plugin's custom item. Its name and lore are copied into the
     * icon, so lore added to the icon comes after the item's own.
     */
    public static Icon of(ItemStack stack)
    {
        Icon icon = Icon.of(stack.getType().getKey().toString()).amount(stack.getAmount()).base(stack.clone());
        ItemMeta meta = stack.getItemMeta();
        if (meta == null)
            return icon;
        if (meta.hasCustomName())
            icon = icon.name(meta.customName());
        List<Component> lore = meta.lore();
        return lore == null ? icon : icon.lore(lore);
    }

    public static ItemStack stack(Icon icon)
    {
        if (icon == null)
            return null;

        ItemStack stack;
        if (icon.base() instanceof ItemStack base)
            stack = base.clone();
        else
        {
            Material material = Material.matchMaterial(icon.material());
            stack = new ItemStack(material == null || !material.isItem() || material.isAir() ? Material.BARRIER : material);
        }
        stack.setAmount(Math.max(1, Math.min(stack.getMaxStackSize(), icon.amount())));

        ItemMeta meta = stack.getItemMeta();
        if (meta == null)
            return stack;
        if (icon.name() != null)
            meta.customName(upright(icon.name()));
        if (!icon.lore().isEmpty())
            meta.lore(icon.lore().stream().map(PaperIcons::upright).toList());
        if (icon.glow())
            meta.setEnchantmentGlintOverride(true);
        if (icon.hideTooltip())
            meta.setHideTooltip(true);
        if (!(icon.base() instanceof ItemStack))
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        stack.setItemMeta(meta);
        return stack;
    }

    // Item names and lore are italic unless told otherwise
    private static Component upright(Component text)
    {
        return text.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }
}
