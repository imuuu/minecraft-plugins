package me.imu.imuschallenges.Inventories;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Buttons.GridButton;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.Interfaces.IBUTTONN;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Managers.Manager_CommandSender;
import imu.iAPI.Managers.Manager_Vault;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.InvUtil;
import imu.iAPI.Utilities.ItemUtils;
import me.imu.imuschallenges.CONSTANTS;
import me.imu.imuschallenges.Database.Tables.TablePlayerShopStats;
import me.imu.imuschallenges.Enums.POINT_TYPE;
import me.imu.imuschallenges.ImusChallenges;
import me.imu.imuschallenges.Managers.ManagerChallengeShop;
import me.imu.imuschallenges.Managers.ManagerPlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class InventoryChallengeShop extends CustomInventory
{
    private ManagerChallengeShop _managerChallengeShop = ManagerChallengeShop.getInstance();

    private final int _normalOffset = 3 + 9 + 9;
    private final int _specialOffset = 1 + 9;

    private double _playerPoints = -1;
    public InventoryChallengeShop()
    {
        super(ImusChallenges.getInstance(), "&6Challenge Shop", 6 * 9);

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
        ManagerChallengeShop.getInstance().addPlayerToShop(getPlayer());
        getPlayerPointsAsync();
        InitButtons();

    }

    @Override
    public void onClose()
    {
        super.onClose();
        ManagerChallengeShop.getInstance().removePlayerFromShop(getPlayer());
    }

    private void getPlayerPointsAsync()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                   _playerPoints = ManagerPlayerPoints.getInstance().getPoints(POINT_TYPE.CHALLENGE_POINT.toString(),getPlayer());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(ImusChallenges.getInstance());
    }
    private void setNormalShopClose()
    {
        ArrayList<IBUTTONN> buttons = new ArrayList<>();
        GridButton gridButton = new GridButton(buttons, _normalOffset, CONSTANTS.NORMAL_SLOT_COLUMNS, CONSTANTS.NORMAL_SLOT_ROWS);
        gridButton.setEmptyStack(ItemUtils.SetDisplayNameEmpty(new ItemStack(Material.RED_STAINED_GLASS_PANE)));
        addGrid(gridButton);
        gridButton.update();
        return;
    }

    private void setSpecialShopClose()
    {
        ArrayList<IBUTTONN> buttons = new ArrayList<>();
        GridButton gridButton = new GridButton(buttons, _specialOffset, 1,CONSTANTS.SPECIAL_SLOTS);
        gridButton.setEmptyStack(ItemUtils.SetDisplayNameEmpty(new ItemStack(Material.RED_STAINED_GLASS_PANE)));
        addGrid(gridButton);
        gridButton.update();
        return;
    }

    private void InitButtons()
    {
        for (int i = 0; i < getSize(); i++)
        {
            IBUTTONN button = getEmptyButton(i, Material.BLACK_STAINED_GLASS_PANE);
            addButton(button);
        }

        IBUTTONN button = getEmptyButton(1, Material.BLUE_STAINED_GLASS_PANE);
        ItemUtils.SetDisplayName(button.getItemStack(), Metods.msgC("&6&lResets in &c&l" + _managerChallengeShop.timeLeftSpecial()));
        addButton(button);

        button = getEmptyButton(1+9*5, Material.BLUE_STAINED_GLASS_PANE);
        ItemUtils.SetDisplayName(button.getItemStack(), Metods.msgC("&6&lResets in &c&l" + _managerChallengeShop.timeLeftSpecial()));
        addButton(button);

        updateButtons(false);

        ItemStack stack = getEmptyButton(-1, Material.BLUE_STAINED_GLASS_PANE).getItemStack();
        ItemUtils.SetDisplayName(stack, Metods.msgC("&9&lResets in &c&l" + _managerChallengeShop.timeLeftNormal()));
        ArrayList<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < 4; i++)
        {
            items.add(stack.clone());
        }

        GridButton gridButton = new GridButton(_normalOffset-9, 4, 1, items);
        addGrid(gridButton);
        gridButton.update();

        gridButton = new GridButton(_normalOffset-9 + 9 * 3, 4, 1, items);
        addGrid(gridButton);
        gridButton.update();



        InitNormalAndSpecialSlots();
    }

    private void InitSpecialSlots(TablePlayerShopStats shopStats)
    {
        if(_managerChallengeShop.hasPlayerBuySpecial(getPlayer().getUniqueId()))
        {
            setSpecialShopClose();
            return;
        }

        if (shopStats == null)
        {
            setNormalShopClose();
            return;
        }

        ArrayList<IBUTTONN> buttons = new ArrayList<>();
        ArrayList<ItemStack> items = ManagerChallengeShop.getInstance().getGeneratedSpecialItems();
        ArrayList<Integer> cost = ManagerChallengeShop.getInstance().getItemCostsSpecial();
        final int nextSlot = shopStats.getBought_special_slots() - CONSTANTS.PLAYER_SHOP_STATS_DEFAULT_SPECIAL;

        for (int i = 0; i < items.size(); i++)
        {
            final int index = i;
            if (i < shopStats.getBought_special_slots())
            {
                ItemStack item = items.get(i);
                ItemStack stack = item.clone();
                ItemUtils.AddLore(stack, Metods.msgC(" "), true);
                ItemUtils.AddLore(stack, Metods.msgC("&a&lCost: " + cost.get(i) + " &4Challenge points"), true);
                Button button = new Button(i, stack, inventoryClickEvent ->
                {
                    OnButtonBuySpecialItem(index);
                });
                buttons.add(button);
                continue;
            }

            ItemStack stack = getEmptyButton(i, Material.PURPLE_STAINED_GLASS_PANE).getItemStack();
            String displayName = Metods.msgC("&c&lBuy &6Special &9slot");
            ItemUtils.SetDisplayName(stack, displayName);
            ItemUtils.AddLore(stack, Metods.msgC(" "), true);
            ItemUtils.AddLore(stack, Metods.msgC("&aCost: " + _managerChallengeShop.getSpecialSlotPrice(nextSlot) + " &2$"), true);
            Button button = new Button(i, stack);
            button.setAction(inventoryClickEvent ->
            {
                IBUTTONN thisButton = getButton(button.getUUID());
                if (inventoryClickEvent.isLeftClick())
                {
                    ItemUtils.SetDisplayName(thisButton.getItemStack(), displayName + " &6&k#&r &e(Confirm by &bM2)");
                    ItemUtils.AddGlow(thisButton.getItemStack());
                    ItemUtils.SetTag(thisButton.getItemStack(), "buy");
                    updateButton(thisButton);

                }
                if (inventoryClickEvent.isRightClick() && ItemUtils.HasTag(button.getItemStack(), "buy"))
                {
                    OnButtonBuySpecialSlot(nextSlot);
                }
            });
            buttons.add(button);

        }

        GridButton gridButton = new GridButton(buttons, _specialOffset, 1,CONSTANTS.SPECIAL_SLOTS);
        gridButton.setEmptyStack(ItemUtils.SetDisplayNameEmpty(new ItemStack(Material.PURPLE_STAINED_GLASS_PANE)));
        addGrid(gridButton);
        gridButton.update();

    }


    private void InitNormalAndSpecialSlots()
    {
        _managerChallengeShop.getShopStatsAsync(getPlayer(), shopStats ->
        {
            InitSpecialSlots(shopStats);

            if (_managerChallengeShop.hasPlayerBuyNormal(getPlayer().getUniqueId()))
            {
                setNormalShopClose();
                return;
            }

            if (shopStats == null)
            {
                setNormalShopClose();
                return;
            }
            ArrayList<IBUTTONN> buttons = new ArrayList<>();
            ArrayList<ItemStack> items = ManagerChallengeShop.getInstance().getGeneratedItems();
            ArrayList<Integer> cost = ManagerChallengeShop.getInstance().getItemCostsNormal();
            final int nextSlot = shopStats.getBought_normal_slots() - CONSTANTS.PLAYER_SHOP_STATS_DEFAULT_VALUE;

            for (int i = 0; i < items.size(); i++)
            {
                final int index = i;
                if (i < shopStats.getBought_normal_slots())
                {
                    ItemStack item = items.get(i);
                    ItemStack stack = item.clone();
                    ItemUtils.AddLore(stack, Metods.msgC(" "), true);
                    ItemUtils.AddLore(stack, Metods.msgC("&a&lCost: " + cost.get(i) + " &4Challenge points"), true);
                    Button button = new Button(i, stack, inventoryClickEvent ->
                    {
                        OnButtonBuyNormalItem(index);
                    });
                    buttons.add(button);
                    continue;
                }

                ItemStack stack = getEmptyButton(i, Material.PURPLE_STAINED_GLASS_PANE).getItemStack();
                String displayName = Metods.msgC("&c&lBuy &2Normal &9slot");
                ItemUtils.SetDisplayName(stack, displayName);
                ItemUtils.AddLore(stack, Metods.msgC(" "), true);
                ItemUtils.AddLore(stack, Metods.msgC("&aCost: " + _managerChallengeShop.getNormalSlotPrice(nextSlot) + " &2$"), true);
                Button button = new Button(i, stack);
                button.setAction(inventoryClickEvent ->
                {
                    IBUTTONN thisButton = getButton(button.getUUID());
                    if (inventoryClickEvent.isLeftClick())
                    {
                        ItemUtils.SetDisplayName(thisButton.getItemStack(), displayName + " &6&k#&r &e(Confirm by &bM2)");
                        ItemUtils.AddGlow(thisButton.getItemStack());
                        ItemUtils.SetTag(thisButton.getItemStack(), "buy");
                        updateButton(thisButton);

                    }
                    if (inventoryClickEvent.isRightClick() && ItemUtils.HasTag(button.getItemStack(), "buy"))
                    {
                        OnButtonBuyNormalSlot(nextSlot);
                    }
                });
                buttons.add(button);


            }

            GridButton gridButton = new GridButton(buttons, _normalOffset, CONSTANTS.NORMAL_SLOT_COLUMNS, CONSTANTS.NORMAL_SLOT_ROWS);
            gridButton.setEmptyStack(ItemUtils.SetDisplayNameEmpty(new ItemStack(Material.PURPLE_STAINED_GLASS_PANE)));
            addGrid(gridButton);
            gridButton.update();
            //gridButton.generateLeftRightButtons(45, 53);
        });
    }

    private void OnButtonBuyNormalItem(int index)
    {
        if(_playerPoints < _managerChallengeShop.getItemCostsNormal().get(index))
        {
            getPlayer().sendMessage(Metods.msgC("&cYou don't have enough points!"));
            return;
        }



        getPlayer().sendMessage(Metods.msgC("&9You bought an item(s) with &2" + _managerChallengeShop.getItemCostsNormal().get(index) + " &6challenge points!"));
        _managerChallengeShop.setPlayerBuyNormal(getPlayer().getUniqueId(), true);

        _managerChallengeShop.onGiveItemStack(getPlayer(), ManagerChallengeShop.getInstance().getGeneratedItems().get(index).clone());
        ManagerPlayerPoints.getInstance().addPointsAsync(getPlayer(), POINT_TYPE.CHALLENGE_POINT, -_managerChallengeShop.getItemCostsNormal().get(index));
        getPlayer().closeInventory();
    }

    private void OnButtonBuyNormalSlot(int index)
    {
        //int cost = ManagerChallengeShop.getInstance().
        if (Manager_Vault.hasMoney(getPlayer(), _managerChallengeShop.getNormalSlotPrice(index)))
        {
            Manager_Vault.takeMoney(getPlayer(), _managerChallengeShop.getNormalSlotPrice(index));
            ManagerChallengeShop.getInstance().addSlotsToPlayerShopAsync(getPlayer(), 1, false);
            getPlayer().closeInventory();
            getPlayer().sendMessage(Metods.msgC("&aYou bought a slot! &9Cost: &2" + _managerChallengeShop.getNormalSlotPrice(index)));
        }
        else
        {
            getPlayer().sendMessage(Metods.msgC("&cYou don't have enough money!"));
        }
    }

    private void OnButtonBuySpecialSlot(int index)
    {
        if (Manager_Vault.hasMoney(getPlayer(), _managerChallengeShop.getSpecialSlotPrice(index)))
        {
            Manager_Vault.takeMoney(getPlayer(), _managerChallengeShop.getSpecialSlotPrice(index));
            ManagerChallengeShop.getInstance().addSlotsToPlayerShopAsync(getPlayer(), 1, true);
            getPlayer().closeInventory();
            getPlayer().sendMessage(Metods.msgC("&aYou bought a slot! &9Cost: &2" + _managerChallengeShop.getNormalSlotPrice(index)));
        }
        else
        {
            getPlayer().sendMessage(Metods.msgC("&cYou don't have enough money!"));
        }
    }

    private void OnButtonBuySpecialItem(int index)
    {
        if(_playerPoints < _managerChallengeShop.getItemCostsSpecial().get(index))
        {
            getPlayer().sendMessage(Metods.msgC("&cYou don't have enough points!"));
            return;
        }

        getPlayer().sendMessage(Metods.msgC("&9You bought an item(s) with &2" + _managerChallengeShop.getItemCostsSpecial().get(index) + " &6challenge points!"));
        _managerChallengeShop.setPlayerBuySpecial(getPlayer().getUniqueId(), true);
        _managerChallengeShop.onGiveItemStack(getPlayer(), ManagerChallengeShop.getInstance().getGeneratedSpecialItems().get(index).clone());
        ManagerPlayerPoints.getInstance().addPointsAsync(getPlayer(), POINT_TYPE.CHALLENGE_POINT, -_managerChallengeShop.getItemCostsSpecial().get(index));
        getPlayer().closeInventory();
    }


}
