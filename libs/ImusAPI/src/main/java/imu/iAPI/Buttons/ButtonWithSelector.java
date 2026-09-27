package imu.iAPI.Buttons;

import imu.iAPI.Interfaces.ICustomInventory;
import imu.iAPI.Inventories.Inventory_ValueSetter;
import imu.iAPI.Utilities.ItemUtils;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.function.Consumer;

public class ButtonWithSelector extends Button
{

    private ICustomInventory _customInventory;
    private int _selectorIndex = 0;
    private int _selectorStartIndex = -1;

    private ArrayList<SelectorString> _lores = new ArrayList<>();

    private final String[] _firstLores = new String[]
            {
                    " ",
                    "§eM&b1 §5Edit §9& §eM§b2 §fSelect",
                    " "
            };

    public ButtonWithSelector(int position, ItemStack stack, ICustomInventory customInventory)
    {
        super(position, stack);
        _customInventory = customInventory;
    }

    public void addLores(SelectorString selectorString)
    {
        _lores.add(selectorString);
    }

    private int getEndIndex()
    {
        return _selectorStartIndex + _lores.size() - 1 + _firstLores.length;
    }

    private int getSelectorIndex()
    {
        return _selectorIndex - _selectorStartIndex - _firstLores.length;
    }

    public SelectorString getSelectorString()
    {
    	return _lores.get(getSelectorIndex());
    }

    @Override
    public boolean onClick(InventoryClickEvent event)
    {
        if (event.isRightClick())
        {
            _selectorIndex++;
        }

        if(_selectorIndex > getEndIndex())
        {
            _selectorIndex = _selectorStartIndex + _firstLores.length;
        }

        if (event.isRightClick() && getButtonHandler() != null)
        {
            getButtonHandler().updateButton(this);
        }

        if(event.isLeftClick() && getSelectorString().getOnAction() == null)
        {
            editValue(getSelectorIndex());
        }

        if(event.isLeftClick() && getSelectorString().getOnAction() != null)
        {
        	getSelectorString().triggerAction(event);
        }


        return event.isLeftClick();
    }

    public void updateToCustomInventory()
    {
        _customInventory.getButtonHandler().updateButton(this);
    }
    @Override
    public void onUpdate()
    {
        ItemStack stack = getItemStack();
        createLores(stack);
    }

    private void createLores(ItemStack stack)
    {
        if (_selectorStartIndex == -1)
        {
            _selectorStartIndex = getLoreAmount(stack);
            _selectorIndex = _selectorStartIndex + _firstLores.length;
        }

        for (int i = 0; i < _firstLores.length; i++)
        {
            ItemUtils.setLore(stack, _selectorStartIndex + i, _firstLores[i]);
        }

        for (int i = 0; i < _lores.size(); i++)
        {
            SelectorString selectorString = _lores.get(i);
            String lore = selectorString.getStringWithValue();
            if (i + _selectorStartIndex + _firstLores.length == _selectorIndex)
            {
                lore = "§e=>§r" + lore;
            }
            ItemUtils.setLore(stack, _selectorStartIndex + _firstLores.length + i, lore);
        }
    }

    private int getLoreAmount(ItemStack stack)
    {
        int amount = 0;
        ItemMeta meta = stack.getItemMeta();
        if (meta != null)
        {
            if (meta.getLore() != null)
            {
                amount = meta.getLore().size();
            }
        }

        return amount;
    }

    private void editValue(int index)
    {
    	SelectorString selectorString = _lores.get(index);
    	switch(selectorString.get_type())
    	{
    		case INT:
    		case DOUBLE:
                new Inventory_ValueSetter(this, _customInventory).open(_customInventory.getPlayer());
    			break;
    		case BOOLEAN:
    			break;
    		case NONE:
    			break;
    	}
    }

    public String getSelectedValue()
    {
        return _lores.get(getSelectorIndex()).getValue().toString();
    }

    public void setSelectedValue(String value)
    {
        _lores.get(getSelectorIndex()).set_value(value);
    }

    public void setSelectedValue(int value)
    {
        _lores.get(getSelectorIndex()).set_value(String.valueOf(value));
    }

    public void setSelectedValue(double value)
    {
        _lores.get(getSelectorIndex()).set_value(String.valueOf(value));
    }

    public void setSelectedValue(boolean value)
    {
        _lores.get(getSelectorIndex()).set_value(String.valueOf(value));
    }



}
