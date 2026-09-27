package imu.iAPI.LootTables;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Buttons.ButtonWithSelector;
import imu.iAPI.Buttons.GridButton;
import imu.iAPI.Buttons.SelectorString;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.Interfaces.IBUTTONN;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Utilities.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Inventory_LootTables extends CustomInventory
{

    public Inventory_LootTables()
    {
        super(ImusAPI._instance, "&9Drop Tables", 9 * 5);
    }

    @Override
    public INVENTORY_AREA setInventoryLock()
    {
        return INVENTORY_AREA.UPPER_LOWER_INV;
    }

    @Override
    public void onAwake()
    {
        initButtons();
    }

    @Override
    public void onOpen()
    {
        super.onOpen();
        System.out.println("open loot tables inventory");
    }

    @Override
    public void onClose()
    {
        super.onClose();
    }

    private void initButtons()
    {
        loadLootTables();
    }

    private void loadLootTables()
    {
        HashMap<String, ImusLootTable> _lootTables = Manager_ImusLootTable.getInstance().get_lootTables();

        List<String> keys = new ArrayList<>(_lootTables.keySet());
        List<ImusLootTable> values = new ArrayList<>(_lootTables.values());

        List<IBUTTONN> buttons = new ArrayList<>();

        for (int i = 0; i < keys.size(); i++)
        {
            ImusLootTable table = values.get(i);
            final String key = keys.get(i);

            ItemStack stack = new ItemStack(Material.CHEST);
            ItemUtils.SetDisplayName(stack, keys.get(i));
            ItemUtils.AddLore(stack, "Click to edit", true);

            Button button = new Button(i, stack);
            button.setAction(inventoryClickEvent ->
            {
                loadLootTable(key);
            });

            buttons.add(button);
        }

        GridButton grid = new GridButton(buttons, 0, 9, 4);
        addGrid(grid);
        grid.update();
        grid.generateLeftRightButtons(getSize() - 9, getSize() - 1);
    }

    private void loadLootTable(String name)
    {
        System.out.println("load loot table: " + name);
        ImusLootTable table = Manager_ImusLootTable.getInstance().get_lootTable(name);
        if (table == null)
        {
            System.out.println("table is null");
            return;
        }
        List<IBUTTONN> buttons = new ArrayList<>();

        for (int i = 0; i < table.getItems().size(); i++)
        {
            ILootTableItem<?> item = table.getItems().get(i);
            buttons.add(createDisplayItemButton(item));

        }

        GridButton grid = new GridButton(buttons, 0, 9, 4);
        addGrid(grid);
        grid.update();

        grid.generateLeftRightButtons(getSize() - 9, getSize() - 1);
    }

    @SuppressWarnings("rawtypes")
    private IBUTTONN createDisplayItemButton(ILootTableItem item)
    {
        int weight = item.get_weight();
        int maxAmount = item.get_maxAmount();
        int minAmount = item.get_minAmount();
        ItemStack stack;
        if (item instanceof LootItemStack)
        {
            stack = ((ItemStack) item.get_value()).clone();
        }
        else
        {
            stack = new ItemStack(Material.BARRIER);
            ItemUtils.SetDisplayName(stack, "Not ItemStack");

        }
        ButtonWithSelector buttonSelector = new ButtonWithSelector(0, stack, this);

        SelectorString selectorString_weight = new SelectorString("&9Weight: &e%value%", weight);
        selectorString_weight.setOnConfirm(buttonWithSelector ->
        {
            Manager_ImusLootTable.getInstance().updateItemStackAsync(((ItemStack) item.get_value()), null, null, (Integer) buttonWithSelector.getSelectorString().getValue(), success ->
            {
                if (success)  getPlayer().sendMessage("§6Weight §9updated successfully");
                else  getPlayer().sendMessage("§fWeight update §cfailed");
            });
        });
        buttonSelector.addLores(selectorString_weight);

        SelectorString selectorString_minAmount = new SelectorString("&9MinAmount: &e%value%", minAmount);
        selectorString_minAmount.setOnConfirm(buttonWithSelector ->
        {
            Manager_ImusLootTable.getInstance().updateItemStackAsync(((ItemStack) item.get_value()), (Integer) buttonWithSelector.getSelectorString().getValue(), null, null, success ->
            {
                if (success)  getPlayer().sendMessage("§6MinAmount §9updated successfully");
                else  getPlayer().sendMessage("§fMinAmount update §cfailed");
            });
        });
        buttonSelector.addLores(selectorString_minAmount);

        SelectorString selectorString_maxAmount = new SelectorString("&9MaxAmount: &e%value%", maxAmount);
        selectorString_maxAmount.setOnConfirm(buttonWithSelector ->
        {
            Manager_ImusLootTable.getInstance().updateItemStackAsync(((ItemStack) item.get_value()), null, (Integer) buttonWithSelector.getSelectorString().getValue(), null, success ->
            {
                if (success)  getPlayer().sendMessage("§6MaxAmount §9updated successfully");
                else  getPlayer().sendMessage("§fMaxAmount update §cfailed");
            });
        });
        buttonSelector.addLores(selectorString_maxAmount);


        return buttonSelector;
    }



}
