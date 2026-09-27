package imu.iAPI.Buttons;

import imu.iAPI.Enums.VALUE_TYPE;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class SelectorString
{
    private String _string;
    private VALUE_TYPE _type;
    private String _value;

    private Consumer<InventoryClickEvent> _onAction;
    private Consumer<ButtonWithSelector> _onConfirm;
    public SelectorString(String string, String value, VALUE_TYPE type)
    {
        _string = string;
        _type = type;
        _value = value;
    }

    public SelectorString(String string, String value, VALUE_TYPE type, Consumer<InventoryClickEvent> onAction)
    {
        _string = string;
        _type = type;
        _value = value;
        _onAction = onAction;
    }

    public SelectorString(String string, int value)
    {
        _string = string;
        _type = VALUE_TYPE.INT;
        _value = String.valueOf(value);
    }

    public SelectorString(String string, double value)
    {
        _string = string;
        _type = VALUE_TYPE.DOUBLE;
        _value = String.valueOf(value);
    }

    public SelectorString(String string, boolean value)
    {
        _string = string;
        _type = VALUE_TYPE.BOOLEAN;
        _value = String.valueOf(value);
    }

    public String getStringWithValue()
    {
        if(_type == VALUE_TYPE.NONE)
            return _string;

        return _string.replace("%value%", _value);
    }

    public void setOnAction(Consumer<InventoryClickEvent> onAction)
    {
    	_onAction = onAction;
    }

    public Consumer<InventoryClickEvent> getOnAction()
    {
    	return _onAction;
    }

    public void triggerAction(InventoryClickEvent event)
    {
    	if(_onAction != null)
    	{
    		_onAction.accept(event);
    	}
    }

    public void setOnConfirm(Consumer<ButtonWithSelector> onActionConfirm)
    {
        _onConfirm = onActionConfirm;
    }

    public Consumer<ButtonWithSelector> getOnConfirm()
    {
        return _onConfirm;
    }

    public void triggerActionConfirm(ButtonWithSelector customInventory)
    {
        if(_onConfirm != null)
        {
            _onConfirm.accept(customInventory);
        }
    }

    public String get_string()
    {
        return _string;
    }

    public void set_string(String _string)
    {
        this._string = _string;
    }

    public VALUE_TYPE get_type()
    {
        return _type;
    }

    public void set_type(VALUE_TYPE _type)
    {
        this._type = _type;
    }

    public void set_value(String _value)
    {
        this._value = _value;
    }


    public Object getValue()
    {
    	switch(_type)
    	{
    		case INT:
    			return Integer.parseInt(_value);
    		case DOUBLE:
    			return Double.parseDouble(_value);
    		case BOOLEAN:
    			return Boolean.parseBoolean(_value);
    		case NONE:
    			return null;
    	}
    	return null;
    }
}
