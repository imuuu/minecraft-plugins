package imu.iAPI.Inventories;

import imu.iAPI.Buttons.ButtonWithSelector;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.Enums.VALUE_TYPE;
import imu.iAPI.Interfaces.IBUTTONN;
import imu.iAPI.Interfaces.IButtonHandler;
import imu.iAPI.Interfaces.ICustomInventory;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Buttons.Button;
import imu.iAPI.Utilities.ImusUtilities;
import imu.iAPI.Utilities.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Inventory_ValueSetter extends CustomInventory
{
    private IButtonHandler _buttonHandler;
    private ICustomInventory _customInventory;
    private ButtonWithSelector _button;
    private double _value = 0;
    private String _oldValue = "";

    private VALUE_TYPE _valueType;
    public Inventory_ValueSetter(ButtonWithSelector button, ICustomInventory customInventory)
    {
        super(ImusAPI._instance, "§9Set Value", 3 * 9);

        _customInventory = customInventory;
        _button = button;
        _oldValue = _button.getSelectedValue();
        _valueType = _button.getSelectorString().get_type();
    }

    @Override
    public INVENTORY_AREA setInventoryLock()
    {
        return INVENTORY_AREA.UPPER_LOWER_INV;
    }

    @Override
    public void onAwake()
    {
        InitButtons();
    }

    private void confirm()
    {
        if(_valueType == VALUE_TYPE.DOUBLE)
        {
            _button.setSelectedValue(_value);
        }
        else if(_valueType == VALUE_TYPE.INT)
        {
            _button.setSelectedValue((int)_value);
        }

        _button.updateToCustomInventory();
        _button.getSelectorString().triggerActionConfirm(_button);
        _customInventory.open(getPlayer());
    }

    private void InitButtons()
    {
        for(int i = 0; i < 9; i++)
        {
            final int value = i + 1;
            ItemStack stack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            String name = getButtonValueName(value);
            //ItemUtils.SetDisplayName(stack, "§a+§f/§c- §b" + value+ " ( " + _value + ")");
            ItemUtils.SetDisplayName(stack, name);
            ItemUtils.AddLore(stack, "&bM1 &a++ &f| &bM2 &c--", true);
            final int index = i;
            Button button = new Button(index, stack);
            button.setAction(event ->
            {
                IBUTTONN thisButton = getButton(button.getUUID());
                String name2 = getButtonValueName(value);
                ItemUtils.SetDisplayName(thisButton.getItemStack(), name2);
                if(event.isLeftClick())
                {
                    add(value);
                }
                else if(event.isRightClick())
                {
                    add(-value);
                }
                updateButton(thisButton);
            });

           /* Button button = new Button(index, stack, event ->
            {

                if(event.isLeftClick())
                {
                    add(value);
                    String name2 = getButtonValueName(value);
                    IBUTTONN newButton = getButton(index);
                    ItemUtils.SetDisplayName(newButton.getItemStack(), name2);
                    newButton.onUpdate();

                }
                else if(event.isRightClick())
                {
                    add(-value);
                    String name2 = getButtonValueName(value);
                    IBUTTONN newButton = getButton(index);
                    ItemUtils.SetDisplayName(newButton.getItemStack(), name2);
                    newButton.onUpdate();
                }
            });*/
            addButton(button);
        }
        add(0);

        updateButtons(false);


    }

    private String getButtonValueName(int value)
    {
        return "§a+§f/§c- §b" + value + " ( " + _value + ")";
    }

    private void add(double amount)
    {
    	_value += amount;
        if(_valueType == VALUE_TYPE.DOUBLE)
        {
            _value = Math.round(_value * 100.0) / 100.0;
        }
        else if(_valueType == VALUE_TYPE.INT)
        {
            _value = Math.round(_value);
        }

        ItemStack stack = new ItemStack(Material.PAPER);
        ItemUtils.AddLore(stack, "§eOld Value: §f" + _oldValue, true);
        ItemUtils.SetDisplayName(stack, "§eValue: §f" + _value);
        Button button = new Button(getSize()-5,stack);
        button.setAction(event ->
        {
           confirm();
        });
        addButton(button);
        updateButton(button);
    }
}
