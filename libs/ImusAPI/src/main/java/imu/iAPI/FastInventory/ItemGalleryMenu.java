package imu.iAPI.FastInventory;

import imu.iAPI.Menus.Button;
import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.ClickKind;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.Pager;
import imu.iAPI.Menus.Paper.PaperIcons;
import imu.iAPI.Utilities.InvUtil;
import imu.iAPI.Utilities.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.List;

/**
 * A named list of items for admins to take copies of: click for one, right click for a stack (not of tools and
 * armor).
 */
public class ItemGalleryMenu extends Menu
{
    private final List<ItemStack> _stacks;
    private final Pager<ItemStack> _pager = Pager.rows(0, 5);

    public ItemGalleryMenu(String name, List<ItemStack> stacks)
    {
        super(name + " &3=> &bClick &3to Copy", 6);
        _stacks = List.copyOf(stacks);
        _pager.arrows(Icon.of(Material.DARK_OAK_SIGN, "&9<< Next"), Icon.of(Material.DARK_OAK_SIGN, "&9Next >>"));
    }

    @Override
    protected void render(Canvas canvas)
    {
        canvas.fillRow(5, Icon.filler(Material.RED_STAINED_GLASS_PANE));
        _pager.render(canvas, _stacks, stack -> Button.of(PaperIcons.of(stack), click ->
        {
            Player player = Bukkit.getPlayer(click.viewer().id());
            boolean stackOf = click.kind() == ClickKind.RIGHT && !ItemUtils.IsArmor(stack) && !ItemUtils.IsTool(stack);
            InvUtil.AddItemOrDropGetSlots(stack.clone(), player, stackOf ? 64 : 1);
        }), 45, 53);
    }
}
