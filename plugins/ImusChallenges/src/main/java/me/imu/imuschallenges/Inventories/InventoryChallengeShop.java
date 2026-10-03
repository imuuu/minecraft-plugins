package me.imu.imuschallenges.Inventories;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Buttons.GridButton;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.Interfaces.IBUTTONN;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Managers.Manager_Vault;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ItemUtils;
import me.imu.imuschallenges.CONSTANTS;
import me.imu.imuschallenges.Database.Tables.TablePlayerShopStats;
import me.imu.imuschallenges.Enums.POINT_TYPE;
import me.imu.imuschallenges.ImusChallenges;
import me.imu.imuschallenges.Managers.ManagerChallengeShop;
import me.imu.imuschallenges.Managers.ManagerPlayerPoints;
import me.imu.imuschallenges.Shop.ShopTier;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

public class InventoryChallengeShop extends CustomInventory
{
    private ManagerChallengeShop _managerChallengeShop = ManagerChallengeShop.getInstance();

    private final int _normalOffset = 3 + 9 + 9;
    private final int _specialOffset = 1 + 9;

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
        InitButtons();

    }

    @Override
    public void onClose()
    {
        super.onClose();
        ManagerChallengeShop.getInstance().removePlayerFromShop(getPlayer());
    }

    private void InitButtons()
    {
        for (int i = 0; i < getSize(); i++)
        {
            IBUTTONN button = getEmptyButton(i, Material.BLACK_STAINED_GLASS_PANE);
            addButton(button);
        }

        ShopTier special = _managerChallengeShop.getSpecial();
        IBUTTONN button = getEmptyButton(1, Material.BLUE_STAINED_GLASS_PANE);
        ItemUtils.SetDisplayName(button.getItemStack(), Metods.msgC("&6&lResets in &c&l" + _managerChallengeShop.timeLeft(special)));
        addButton(button);

        button = getEmptyButton(1+9*5, Material.BLUE_STAINED_GLASS_PANE);
        ItemUtils.SetDisplayName(button.getItemStack(), Metods.msgC("&6&lResets in &c&l" + _managerChallengeShop.timeLeft(special)));
        addButton(button);

        updateButtons(false);

        ItemStack stack = getEmptyButton(-1, Material.BLUE_STAINED_GLASS_PANE).getItemStack();
        ItemUtils.SetDisplayName(stack, Metods.msgC("&9&lResets in &c&l" + _managerChallengeShop.timeLeft(_managerChallengeShop.getNormal())));
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

        _managerChallengeShop.getShopStatsAsync(getPlayer(), shopStats ->
        {
            initTierSlots(_managerChallengeShop.getSpecial(), shopStats, _specialOffset, 1, CONSTANTS.SPECIAL_SLOTS);
            initTierSlots(_managerChallengeShop.getNormal(), shopStats, _normalOffset, CONSTANTS.NORMAL_SLOT_COLUMNS, CONSTANTS.NORMAL_SLOT_ROWS);
        });
    }

    private void setTierClosed(int offset, int columns, int rows)
    {
        GridButton gridButton = new GridButton(new ArrayList<>(), offset, columns, rows);
        gridButton.setEmptyStack(ItemUtils.SetDisplayNameEmpty(new ItemStack(Material.RED_STAINED_GLASS_PANE)));
        addGrid(gridButton);
        gridButton.update();
    }

    private void initTierSlots(ShopTier tier, TablePlayerShopStats shopStats, int offset, int columns, int rows)
    {
        if (shopStats == null || tier.hasBought(getPlayer().getUniqueId()))
        {
            setTierClosed(offset, columns, rows);
            return;
        }

        String tierName = tier.isSpecial() ? "&6Special" : "&2Normal";
        ArrayList<IBUTTONN> buttons = new ArrayList<>();
        int boughtSlots = tier.getBoughtSlots(shopStats);
        final int nextSlot = boughtSlots - tier.getDefaultSlots();

        for (int i = 0; i < tier.getItems().size(); i++)
        {
            final int index = i;
            if (i < boughtSlots)
            {
                ItemStack stack = tier.getItems().get(i).clone();
                ItemUtils.AddLore(stack, Metods.msgC(" "), true);
                ItemUtils.AddLore(stack, Metods.msgC("&a&lCost: " + tier.getCosts().get(i) + " &4Challenge points"), true);
                Button button = new Button(i, stack, inventoryClickEvent ->
                {
                    onBuyItem(tier, index);
                });
                buttons.add(button);
                continue;
            }

            ItemStack stack = getEmptyButton(i, Material.PURPLE_STAINED_GLASS_PANE).getItemStack();
            String displayName = Metods.msgC("&c&lBuy " + tierName + " &9slot");
            ItemUtils.SetDisplayName(stack, displayName);
            ItemUtils.AddLore(stack, Metods.msgC(" "), true);
            ItemUtils.AddLore(stack, Metods.msgC("&aCost: " + tier.getSlotPrice(nextSlot) + " &2$"), true);
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
                    onBuySlot(tier, nextSlot);
                }
            });
            buttons.add(button);
        }

        GridButton gridButton = new GridButton(buttons, offset, columns, rows);
        gridButton.setEmptyStack(ItemUtils.SetDisplayNameEmpty(new ItemStack(Material.PURPLE_STAINED_GLASS_PANE)));
        addGrid(gridButton);
        gridButton.update();
    }

    private void onBuyItem(ShopTier tier, int index)
    {
        Player player = getPlayer();
        UUID uuid = player.getUniqueId();
        if (tier.hasBought(uuid) || index >= tier.getItems().size())
        {
            player.closeInventory();
            return;
        }

        ItemStack item = tier.getItems().get(index).clone();
        int cost = tier.getCosts().get(index);
        if (!_managerChallengeShop.canDeliver(item))
        {
            player.sendMessage(Metods.msgC("&cThis item can't be bought right now!"));
            return;
        }

        // Marked before the points are taken, so the shop can't be reopened for a second purchase meanwhile
        int generation = tier.getGeneration();
        tier.setBought(uuid, true);
        player.closeInventory();

        ManagerPlayerPoints.getInstance().trySpendPointsAsync(player, POINT_TYPE.CHALLENGE_POINT, cost, spent ->
        {
            if (!spent)
            {
                if (tier.getGeneration() == generation)
                    tier.setBought(uuid, false);
                player.sendMessage(Metods.msgC("&cYou don't have enough points!"));
                return;
            }

            _managerChallengeShop.saveState();
            if (!player.isOnline())
            {
                ManagerPlayerPoints.getInstance().refundPointsAsync(player, POINT_TYPE.CHALLENGE_POINT, cost);
                if (tier.getGeneration() == generation)
                    tier.setBought(uuid, false);
                _managerChallengeShop.saveState();
                return;
            }
            _managerChallengeShop.onGiveItemStack(player, item);
            player.sendMessage(Metods.msgC("&9You bought an item(s) with &2" + cost + " &6challenge points!"));
        });
    }

    private void onBuySlot(ShopTier tier, int slotNumber)
    {
        Player player = getPlayer();
        int price = tier.getSlotPrice(slotNumber);
        if (!Manager_Vault.takeMoney(player, price))
        {
            player.sendMessage(Metods.msgC("&cYou don't have enough money!"));
            return;
        }

        _managerChallengeShop.addSlotsToPlayerShopAsync(player, 1, tier);
        player.closeInventory();
        player.sendMessage(Metods.msgC("&aYou bought a slot! &9Cost: &2" + price));
    }
}
