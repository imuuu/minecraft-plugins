package imu.iAPI.Buttons;

import imu.iAPI.Interfaces.IBUTTONN;
import imu.iAPI.Interfaces.IButtonHandler;
import imu.iAPI.Interfaces.IGrid;
import imu.iAPI.Utilities.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GridButton implements IGrid
{
    private int _startPosition;
    private final int _lineLenght;
    private List<IBUTTONN> _parts;
    private List<ItemStack> _items;
    private List<IBUTTONN> _buttons;
    private int _pageID = 0;
    private IButtonHandler _buttonHandler;
    private ItemStack _emptyStack;

    private final int _height;

    public GridButton(int startPosition, int lineLenght, int height, List<ItemStack> items)
    {
        _startPosition = startPosition;
        _lineLenght = lineLenght;
        _parts = new ArrayList<>();
        _pageID = 0;
        _height = height;
        createEmptyStack();
        convertItemsToButtons(items);
    }

    public GridButton(List<IBUTTONN> buttons, int startPosition, int lineLenght, int height)
    {
        _startPosition = startPosition;
        _lineLenght = lineLenght;
        _parts = new ArrayList<>();
        _pageID = 0;
        _height = height;
        createEmptyStack();
        _buttons = buttons;
    }

    public void registerButtonHandler(IButtonHandler buttonHandler)
    {
        _buttonHandler = buttonHandler;
    }

    @Override
    public void unregisterButtonHandler()
    {
        _buttonHandler = null;
    }

    private void convertItemsToButtons(List<ItemStack> items)
    {
        _buttons = new ArrayList<>();
        for (ItemStack item : items)
        {
            Button button = new Button(0, item);
            _buttons.add(button);
        }
    }
    @Override
    public void loadButtons()
    {
        if (_buttons == null || _buttons.isEmpty())
        {
            loadEmptyGrid();
            return;
        }

        loadButtons(_buttons);
       /* int itemsPerPage = getLength() * _height;
        for (int i = 0; i < itemsPerPage; i++)
        {
            int row = (i / getLength()) % _height;
            int column = i % getLength();
            int slot = _startPosition + (row * 9) + column;
            IBUTTONN button = createPlaceholderButton(slot);
            _buttonHandler.addButton(button);
            _buttonHandler.updateButton(button);
        }*/
    }

    private void createEmptyStack()
    {
        setEmptyStack(ItemUtils.SetDisplayNameEmpty(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));
    }

    private ItemStack getEmptyStack()
    {
        return _emptyStack;
    }

    public void setEmptyStack(ItemStack stack)
    {
        ItemUtils.SetTag(stack, "empty");
        _emptyStack = stack;
    }

    private boolean isItemEmpty(ItemStack item)
    {
        return ItemUtils.HasTag(item, "empty");
    }

    private void loadButtons(List<IBUTTONN> buttons)
    {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        for(IBUTTONN button : buttons)
        {
            stacks.add(button.getItemStack());
        }
        _items = stacks;

        int buttonsPerPage = getLength() * _height;
        int totalSlots = (int) Math.ceil((double) buttons.size() / buttonsPerPage) * buttonsPerPage;

        for (int i = 0; i < totalSlots; i++)
        {
            int row = (i / getLength()) % _height;
            int column = i % getLength();
            IBUTTONN button = i < buttons.size() ? buttons.get(i) : createPlaceholderButton(0);

            loadButton(column, row, button);
        }
    }

    private void loadEmptyGrid()
    {
        int itemsPerPage = getLength() * _height;
        for (int i = 0; i < itemsPerPage; i++) {
            int row = (i / getLength()) % _height;
            int column = i % getLength();
            int slot = _startPosition + (row * 9) + column;
            IBUTTONN button = createPlaceholderButton(slot);
            _buttonHandler.addButton(button);
            _buttonHandler.updateButton(button);
        }
    }

    private void loadButton(int column, int row, IBUTTONN button)
    {
        int slot = _startPosition + (row * 9) + column;
        //GridButtonPart buttonPart = new GridButtonPart(button, this);
        //buttonPart.setPosition(slot);
        button.setPosition(slot);
        _parts.add(button);
    }

   /* private void loadItem(int column, int row, ItemStack item)
    {
        int slot = _startPosition + (row * 9) + column;
        GridButtonPart buttonPart = new GridButtonPart(slot, item, this);
        _parts.add(buttonPart);
    }*/

    private IBUTTONN createPlaceholderButton(int slot)
    {
        //return new GridButtonPart(slot,getEmptyStack(), this);
        return new Button(slot, getEmptyStack());
    }

    private int calculateNewPosition(int indexInList)
    {
        int row = indexInList / getLength();
        int column = indexInList % getLength();
        return _startPosition + column + (row * 9);
    }

 /*   private void loadItem(int slot, ItemStack item)
    {
        GridButtonPart buttonPart = new GridButtonPart(_startPosition + slot, item, this);
        _parts.add(buttonPart);
    }*/

    private boolean canShift(int offset)
    {
        if (_parts.isEmpty())
        {
            return false;
        }

        final int itemsPerPage = getLength() * _height;
        final int currentPageStartIndex = _pageID * itemsPerPage;
        //final int currentPageEndIndex = Math.min((_pageID + 1) * itemsPerPage, _parts.size()) - 1;

        // Adjusted to include _startPosition in the index calculation
        int adjustedStartIndex = Math.max(currentPageStartIndex - _startPosition, 0);
        //int adjustedEndIndex = Math.min(currentPageEndIndex - _startPosition, _parts.size() - 1);

        if (offset == 1)
        {
            // Check if the last item on the current page is not empty (shift left)
            return !isItemEmpty(_parts.get(_parts.size() - 1).getItemStack());
        }
        else if (offset == -1)
        {
            // Check if the first item on the current page is not empty (shift right)
            return adjustedStartIndex+itemsPerPage < _parts.size() && !isItemEmpty(_parts.get(adjustedStartIndex+itemsPerPage).getItemStack());
        }

        return false;
    }


    private void shiftPositions(int offset)
    {
        if (!canShift(offset))
        {
            return;
        }

        if (offset == 1)
        {
            // Shift left - move the last item to the front
            IBUTTONN lastPart = _parts.remove(_parts.size() - 1);
            _parts.add(0, lastPart);
        }
        else if (offset == -1)
        {
            // Shift right - move the first item to the end
            IBUTTONN firstPart = _parts.remove(0);
            _parts.add(firstPart);
        }

        // Update positions of parts considering _startPosition
        for (int i = 0; i < _parts.size(); i++)
        {
            IBUTTONN part = _parts.get(i);
            int newPosition = calculateNewPosition(i); // calculateNewPosition accounts for _startPosition
            part.setPosition(newPosition);
        }
    }

    public void nextSlot()
    {
        shiftPositions(-1);
        update();
    }

    public void previousSlot()
    {
        shiftPositions(1);
        update();
    }

    public void nextPage()
    {
        _pageID++;
        if (_pageID >= getTotalPages())
        {
            _pageID = 0;
        }
        update();
    }

    public void previousPage()
    {
        _pageID--;
        if (_pageID < 0)
        {
            _pageID = getTotalPages() - 1; // Wrap around to the last page if less than 0
        }
        update();
    }

    private int getTotalPages()
    {
        final int itemsPerPage = getLength() * _height;
        return (int) Math.ceil((double) _parts.size() / itemsPerPage);
    }

    public void update()
    {
        final int itemsPerPage = getLength() * _height;
        final int offset = _pageID * itemsPerPage;

        for (int i = 0; i < itemsPerPage; i++)
        {
            int index = offset + i;

            if (index >= _parts.size() || index < 0)
            {
                break;
            }

            IBUTTONN buttonPart = _parts.get(index);
            int slot = buttonPart.getPosition();

            if (slot < 0 || slot > 53)
            {
                Bukkit.getLogger().info("slot: " + slot + " is out of bounds, material is: " + buttonPart.getItemStack().getType());
                continue;
            }

            _buttonHandler.addButton(buttonPart);
            _buttonHandler.updateButton(buttonPart);
        }
    }

    public void generateLeftRightButtons(int leftSlot, int rightSlot)
    {
        IBUTTONN leftButton = getPreviousButton();
        leftButton.setPosition(leftSlot);

        IBUTTONN rightButton = getNextButton();
        rightButton.setPosition(rightSlot);

        _buttonHandler.addButton(leftButton);
        _buttonHandler.addButton(rightButton);
        _buttonHandler.updateButton(leftButton);
        _buttonHandler.updateButton(rightButton);
    }
    public IBUTTONN getPreviousButton()
    {
        ItemStack stack = new ItemStack(Material.BIRCH_SIGN);
        ItemUtils.SetDisplayName(stack, "&b<< Page");

        return new Button(-1, stack, inventoryClickEvent ->
        {
            previousPage();
        });
    }

    public IBUTTONN getNextButton()
    {
        ItemStack stack = new ItemStack(Material.BIRCH_SIGN);
        ItemUtils.SetDisplayName(stack, "&bPage >>");

        return new Button(-1, stack, inventoryClickEvent ->
        {
            nextPage();
        });
    }

    public IBUTTONN getPreviousSlotButton()
    {
        ItemStack stack = new ItemStack(Material.BIRCH_SIGN);
        ItemUtils.SetDisplayName(stack, "&b<< Scroll");

        return new Button(-1, stack, inventoryClickEvent ->
        {
            previousSlot();
        });
    }

    public IBUTTONN getNextSlotButton()
    {
        ItemStack stack = new ItemStack(Material.BIRCH_SIGN);
        ItemUtils.SetDisplayName(stack, "&bScroll >>");

        return new Button(-1, stack, inventoryClickEvent ->
        {
            nextSlot();
        });
    }

    private int getLength()
    {
        return _lineLenght;
    }


}
