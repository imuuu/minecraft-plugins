package imu.iAPI.Buttons;

import imu.iAPI.Interfaces.IBUTTONN;
import imu.iAPI.Interfaces.IGrid;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class GridButtonPart extends Button
{
    private IGrid _grid;
    public GridButtonPart(int position, ItemStack stack, Consumer<InventoryClickEvent> onClickAction)
    {
        super(position, stack, onClickAction);
    }

    public GridButtonPart(IBUTTONN button, IGrid grid)
    {
        super(button);
        _grid = grid;
    }

    public GridButtonPart(int position, ItemStack stack, IGrid grid)
    {
        super(position, stack);
        _grid = grid;
    }

    public void setGrid(IGrid grid)
    {
        _grid = grid;
    }

    public IGrid getGrid()
    {
        return _grid;
    }
}
