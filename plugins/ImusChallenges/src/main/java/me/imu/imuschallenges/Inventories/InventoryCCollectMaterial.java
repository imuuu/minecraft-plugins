package me.imu.imuschallenges.Inventories;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Buttons.GridButton;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.Interfaces.IBUTTONN;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ItemUtils;
import me.imu.imuschallenges.Database.Tables.TableCollected_materials;
import me.imu.imuschallenges.ImusChallenges;
import me.imu.imuschallenges.Managers.ManagerCCollectMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.*;

public class InventoryCCollectMaterial extends CustomInventory
{
    private ManagerCCollectMaterial _managerCollectMaterial = ManagerCCollectMaterial.getInstance();

    public InventoryCCollectMaterial()
    {
        super(ImusChallenges.getInstance(), "&9Collect Material Challenge", 6 * 9);
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
        updateLowerBar();
        onButtonPlayersMaterials();
    }

    private void updateLowerBar()
    {
        ItemStack empty = ItemUtils.SetDisplayNameEmpty(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE));
        for (int i = 46; i < 53; i++)
        {
            IBUTTONN button = new Button(i, empty);
            button.setStatic(true);
            addButton(button);
            updateButton(button);
        }

        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        ItemUtils.SetDisplayName(stack, "&eCollected Materials");
        IBUTTONN button = new Button(47, stack, inventoryClickEvent ->
        {
            switchInv();
            onButtonPlayerCollectedMaterials(getPlayer());
        });
        button.setStatic(true);
        addButton(button);
        updateButton(button);

        stack = new ItemStack(Material.DIAMOND_BLOCK);
        ItemUtils.SetDisplayName(stack, "&5Unknown Materials");
        button = new Button(49, stack, inventoryClickEvent ->
        {
            switchInv();
            onButtonUnknownMaterials();
        });
        button.setStatic(true);
        addButton(button);
        updateButton(button);

        stack = new ItemStack(Material.PLAYER_HEAD);
        ItemUtils.SetDisplayName(stack, "&ePlayers Materials");
        button = new Button(51, stack, inventoryClickEvent ->
        {
            switchInv();
            onButtonPlayersMaterials();
        });
        button.setStatic(true);
        addButton(button);
        updateButton(button);


    }

    private boolean isPlayerOnline(Player player)
    {
        return Bukkit.getServer().getPlayer(player.getUniqueId()) != null;
    }

    private void switchInv()
    {
        clearButtons();
        clearGrids();
        //updateLowerBar();
        updateButtons(false);
    }

    private void onButtonPlayerCollectedMaterials(Player player)
    {
        if (!isPlayerOnline(player))
        {
            getPlayer().sendMessage("&9Player is not online!");
            getPlayer().closeInventory();
            return;
        }

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                List<TableCollected_materials> playerCollectedMaterials = _managerCollectMaterial.getPlayerCollectedMaterials(player);
                Collections.sort(playerCollectedMaterials, Comparator.comparing(TableCollected_materials::getCollection_time).reversed());

                List<IBUTTONN> buttonsCollected = new ArrayList<>();
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");

                int index = playerCollectedMaterials.size();
                for (TableCollected_materials playerMaterial : playerCollectedMaterials)
                {
                    ItemStack stack = new ItemStack(playerMaterial.getMaterial());
                    String formattedDate = dateFormat.format(playerMaterial.getCollection_time());
                    ItemUtils.AddLore(stack, "&9===> &6" + index--, true);
                    ItemUtils.AddLore(stack, "&e" + formattedDate, true);

                    if (getPlayer().isOp())
                    {
                        ItemUtils.AddLore(stack, " ", true);
                        ItemUtils.AddLore(stack, " ", true);
                        ItemUtils.AddLore(stack, "&6=== OP ONLY ===", true);
                        ItemUtils.AddLore(stack, "&4 REMOVE FROM DATABASE BY ==> &bM2", true);
                    }

                    Button button = new Button(0, stack);


                    if (getPlayer().isOp())
                    {
                        UUID uuid = button.getUUID();
                        button.setAction(inventoryClickEvent ->
                        {
                            if (!getPlayer().isOp()) return;

                            if (!inventoryClickEvent.isRightClick()) return;

                            IBUTTONN exludedButton = getButton(uuid);
                            exludedButton.setEnableAction(false);
                            ItemStack exStack = exludedButton.getItemStack();
                            ItemUtils.AddGlow(exStack);
                            ItemUtils.SetDisplayName(exStack, "&cRemoved");
                            exludedButton.setItemStack(exStack);

                            updateButton(exludedButton);

                            _managerCollectMaterial.removeCollectedMaterialPlayerSQL_async(player, playerMaterial.getMaterial());
                        });
                    }

                    buttonsCollected.add(button);

                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            GridButton gridButton = new GridButton(buttonsCollected, 0, 9, 5);
                            addGrid(gridButton);
                            gridButton.update();
                            gridButton.generateLeftRightButtons(45, 53);
                        }
                    }.runTask(ImusChallenges.getInstance());
                }
            }

        }.runTaskAsynchronously(ImusChallenges.getInstance());
    }

    private void onButtonUnknownMaterials()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Set<Material> unknownMaterials = _managerCollectMaterial.getUncollectedMaterials();

                List<IBUTTONN> unknownButtons = new ArrayList<>();
                int index = unknownMaterials.size();
                for (Material material : unknownMaterials)
                {
                    /* Bukkit.getLogger().info("material: " + material + " index: " + index + " isItem: "+ material.isItem());*/
                    ItemStack stack = new ItemStack(material);
                    ItemUtils.AddLore(stack, "&9=> &2" + index--, true);
                    ItemUtils.AddLore(stack, " ", true);
                    ItemUtils.AddLore(stack, "&9=> Called &6" + material.name(), true);

                    if (getPlayer().isOp())
                    {
                        ItemUtils.AddLore(stack, " ", true);
                        ItemUtils.AddLore(stack, " ", true);
                        ItemUtils.AddLore(stack, "&6=== OP ONLY ===", true);
                        ItemUtils.AddLore(stack, "&4 ADD TO EXCLUDED BY ==> &bM2", true);
                    }
                    Button button = new Button(0, stack);

                    if (getPlayer().isOp())
                    {
                        final UUID uuid = button.getUUID();
                        button.setAction(inventoryClickEvent ->
                        {

                            if (!getPlayer().isOp()) return;

                            if (!inventoryClickEvent.isRightClick()) return;

                            IBUTTONN exludedButton = getButton(uuid);
                            exludedButton.setEnableAction(false);
                            ItemStack exStack = exludedButton.getItemStack();
                            ItemUtils.AddGlow(exStack);
                            ItemUtils.SetDisplayName(exStack, "&cExcluded");
                            exludedButton.setItemStack(exStack);

                            updateButton(exludedButton);

                            _managerCollectMaterial.addExcludedMaterialSQL_async(material, () ->
                            {
                                getPlayer().sendMessage(Metods.msgC("&9Added &6" + material.name() + " &9to excluded database!"));
                            });
                        });
                    }


                    unknownButtons.add(button);
                }

                new BukkitRunnable()
                        {
                            @Override
                            public void run()
                            {
                                GridButton gridButton = new GridButton(unknownButtons, 0, 9, 5);
                                addGrid(gridButton);
                                gridButton.update();
                                gridButton.generateLeftRightButtons(45, 53);
                            }
                        }.

                        runTask(ImusChallenges.getInstance());
            }
        }.runTaskAsynchronously(ImusChallenges.getInstance());

    }

    private void onButtonPlayersMaterials()
    {
        _managerCollectMaterial.updateCollectedMaterialsCountCache(() ->
        {

            HashMap<UUID, Integer> collectedMaterialsCount = _managerCollectMaterial.getPlayerCollectedCount();
            ArrayList<IBUTTONN> buttons = new ArrayList<>();

            for (Player player : Bukkit.getServer().getOnlinePlayers())
            {
                int count = collectedMaterialsCount.getOrDefault(player.getUniqueId(), 0);
                ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
                ItemUtils.SetDisplayName(stack, "&e" + player.getName());
                ItemUtils.AddLore(stack, "&7=========================", true);
                ItemUtils.AddLore(stack, "&9Materials: &6" + count, true);
                ItemUtils.AddLore(stack, "    ", true);
                ItemUtils.AddLore(stack, "    ", true);
                ItemUtils.AddLore(stack, "&9(&eClick&9) To See More", true);
                IBUTTONN button = new Button(0, stack, inventoryClickEvent ->
                {
                    onButtonPlayerCollectedMaterials(player);
                });
                buttons.add(button);
            }

            GridButton gridButton = new GridButton(buttons, 0, 9, 5);
            addGrid(gridButton);
            gridButton.update();

            gridButton.generateLeftRightButtons(45, 53);
        });
    }


}
