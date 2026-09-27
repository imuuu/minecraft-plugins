package imu.iAPI.Interfaces;

import imu.iAPI.Enums.INVENTORY_AREA;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

public interface IButtonHandler
{
    public void addButton(IBUTTONN button);
    public void addButton(int position, IBUTTONN button);
    public IBUTTONN removeButton(int position);
    public IBUTTONN removeButton(IBUTTONN button);
    public IBUTTONN getButton(int position);
    public IBUTTONN getButton(UUID uuid);
    public void setInventoryLock(INVENTORY_AREA lock);
    public void updateButtons(boolean clearEmpties);
    public void updateButton(IBUTTONN button);
    public void updateButton(int position);
    public void clearButtons();
    public void onHandlerOpen();
    public void onHandlerClose();
    public void handlePickupAll(InventoryClickEvent event, IBUTTONN button);
    public INVENTORY_AREA getInventoryArea(InventoryClickEvent e);

    public void addGrid(IGrid grid);
    public void removeGrid(IGrid grid);
    public void clearGrids();
}
