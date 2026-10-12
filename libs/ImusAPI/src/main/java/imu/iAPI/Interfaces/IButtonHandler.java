package imu.iAPI.Interfaces;

import imu.iAPI.Enums.INVENTORY_AREA;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

public interface IButtonHandler
{
    public void addButton(IMenuButton button);
    public void addButton(int position, IMenuButton button);
    public IMenuButton removeButton(int position);
    public IMenuButton removeButton(IMenuButton button);
    public IMenuButton getButton(int position);
    public IMenuButton getButton(UUID uuid);
    public void setInventoryLock(INVENTORY_AREA lock);
    public void updateButtons(boolean clearEmpties);
    public void updateButton(IMenuButton button);
    public void updateButton(int position);
    public void clearButtons();
    public void onHandlerOpen();
    public void onHandlerClose();
    public void handlePickupAll(InventoryClickEvent event, IMenuButton button);
    public INVENTORY_AREA getInventoryArea(InventoryClickEvent e);

    public void addGrid(IGrid grid);
    public void removeGrid(IGrid grid);
    public void clearGrids();
}
