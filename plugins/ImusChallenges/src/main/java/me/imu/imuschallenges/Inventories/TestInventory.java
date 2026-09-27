package me.imu.imuschallenges.Inventories;


import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.Enums.LINE_DIRECTION;
import imu.iAPI.Interfaces.IBUTTONN;
import imu.iAPI.InvUtil.CustomInventory;
import me.imu.imuschallenges.ImusChallenges;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class TestInventory extends CustomInventory
{
    public TestInventory()
    {
        super(ImusChallenges.getInstance(), "Testing", 54);
    }

    @Override
    public INVENTORY_AREA setInventoryLock()
    {
        return INVENTORY_AREA.UPPER_LOWER_INV;
    }

    @Override
    public void onAwake()
    {

    }

    @Override
    public void onOpen()
    {
        super.onOpen();

        initButtons();
    }

    private void initButtons()
    {
        List<ItemStack> items = new ArrayList<>();
        items.add(new ItemStack(Material.APPLE));
        items.add(new ItemStack(Material.WHITE_WOOL));
        items.add(new ItemStack(Material.STONE));

        items.add(new ItemStack(Material.PODZOL));
        items.add(new ItemStack(Material.YELLOW_BED));
        items.add(new ItemStack(Material.NETHER_STAR));

        items.add(new ItemStack(Material.NETHER_BRICK));
        items.add(new ItemStack(Material.BLUE_BANNER));
        items.add(new ItemStack(Material.EGG));

        items.add(new ItemStack(Material.DIAMOND));
        items.add(new ItemStack(Material.LAPIS_BLOCK));
        items.add(new ItemStack(Material.GOLD_BLOCK));

        items.add(new ItemStack(Material.EMERALD));


        items.add(new ItemStack(Material.DIRT));
        items.add(new ItemStack(Material.BLACK_BANNER));
        items.add(new ItemStack(Material.BONE));

        items.add(new ItemStack(Material.GUNPOWDER));
        items.add(new ItemStack(Material.BROWN_BED));
        items.add(new ItemStack(Material.NETHERITE_INGOT));

        items.add(new ItemStack(Material.NETHER_BRICK));
        items.add(new ItemStack(Material.DIAMOND_AXE));
        items.add(new ItemStack(Material.GOLDEN_APPLE));

        items.add(new ItemStack(Material.OBSERVER));
        items.add(new ItemStack(Material.OBSIDIAN));
        items.add(new ItemStack(Material.OAK_BOAT));

        items.add(new ItemStack(Material.DIAMOND_BLOCK));


       /* LineButton lineButton = new LineButton(getButtonHandler(), 1, 6, items, LINE_DIRECTION.VERTICAL);
        lineButton.update();

        IBUTTONN nextButton = lineButton.getNextSlotButton();
        nextButton.setPosition(53);
        addButton(nextButton);

        IBUTTONN prevButton = lineButton.getPreviousSlotButton();
        prevButton.setPosition(45);
        addButton(prevButton);*/

        /*LineButton lineButton2 = new LineButton(getButtonHandler(), 1, 1, 6,items);
        lineButton2.update();

        IBUTTONN nextButton2 = lineButton2.getNextButton();
        nextButton2.setPosition(53);
        addButton(nextButton2);

        IBUTTONN prevButton2 = lineButton2.getPreviousButton();
        prevButton2.setPosition(45);
        addButton(prevButton2);

        IBUTTONN nextButton = lineButton2.getNextSlotButton();
        nextButton.setPosition(53-9);
        addButton(nextButton);

        IBUTTONN prevButton = lineButton2.getPreviousSlotButton();
        prevButton.setPosition(45-9);
        addButton(prevButton);*/

        updateButtons(false);



    }
}
