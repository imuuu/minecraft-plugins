package me.imu.imuschallenges.Managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;
import imu.iAPI.LootTables.LootTableItemStack;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Managers.Manager_CommandSender;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.InvUtil;
import me.imu.imuschallenges.CONSTANTS;
import me.imu.imuschallenges.Database.Tables.TablePlayerShopStats;
import me.imu.imuschallenges.Database.Tables.TablePlayers;
import me.imu.imuschallenges.ImusChallenges;
import me.imu.imuschallenges.Interfaces.ShopStatsCallback;
import me.imu.imuschallenges.Inventories.InventoryChallengeShop;
import me.imu.imuschallenges.Factories.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ManagerChallengeShop
{
    private static ManagerChallengeShop _instance;

    public static ManagerChallengeShop getInstance()
    {
        return _instance;
    }

    private LootTableItemStack _tier_normal_lootTable;
    private LootTableItemStack _tier_special_lootTable;

    private ArrayList<ItemStack> _generatedNormalItems;
    private ArrayList<ItemStack> _generatedSpecialItems;
    private ArrayList<Integer> _itemCostsNormal;
    private ArrayList<Integer> _itemCostsSpecial;
    private HashSet<Player> _hasShopOpen = new HashSet<>();
    private final HashSet<UUID> _hasPlayerBuyNormal = new HashSet<>();
    private final HashSet<UUID> _hasPlayerBuySpecial = new HashSet<>();

    private Dao<TablePlayerShopStats, Integer> playerShopStatsDao;

    //private final int TIME_BETWEEN_GENERATIONS = 3 * 60 * 60 * 20; // 3 hours in server ticks (20 ticks = 1 second)
    private final int TIME_BETWEEN_NORMAL_GENERATIONS = 60 * 60 * 2 + 40 * 60; //2h 40min
    private final int TIME_BETWEEN_SPECIAL_GENERATIONS = 60 * 60 * 13; //13h

    private HashSet<Material> _excludedMaterials = new HashSet<>();

    private Cooldowns _cooldowns = new Cooldowns();

    private boolean _isGadgedMenuPluginEnabled = false;

    public ManagerChallengeShop()
    {
        _instance = this;
        try
        {
            InitSQLData();
            playerShopStatsDao = DaoManager.createDao(ImusChallenges.getInstance().getSource(), TablePlayerShopStats.class);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        _isGadgedMenuPluginEnabled = ImusAPI.isPluginEnabled("GadgedMenu");
        Bukkit.getLogger().info("GadgedMenu plugin enabled: " + _isGadgedMenuPluginEnabled);
        InitExcludedMaterials();
        initLoot();
        _generatedNormalItems = new ArrayList<>();
        _itemCostsNormal = new ArrayList<>();
        _generatedSpecialItems = new ArrayList<>();
        _itemCostsSpecial = new ArrayList<>();
        scheduleItemGeneration();
    }

    public void openShop(Player player)
    {
        new InventoryChallengeShop().open(player);
    }

    public void addPlayerToShop(Player player)
    {
        _hasShopOpen.add(player);
    }

    public void removePlayerFromShop(Player player)
    {
        _hasShopOpen.remove(player);
    }

    public ArrayList<Player> closeAllShops()
    {
        ArrayList<Player> players = new ArrayList<>();
        for (Player player : _hasShopOpen)
        {
            player.closeInventory();
            players.add(player);
        }
        _hasShopOpen.clear();
        return players;
    }

    private void InitExcludedMaterials()
    {
        _excludedMaterials.add(Material.NETHER_STAR);
        _excludedMaterials.add(Material.NETHERITE_SCRAP);
        _excludedMaterials.add(Material.NETHERITE_INGOT);
        _excludedMaterials.add(Material.NETHERITE_BLOCK);
        _excludedMaterials.add(Material.EMERALD_BLOCK);
        _excludedMaterials.add(Material.ELYTRA);
        _excludedMaterials.add(Material.EMERALD_ORE);
        _excludedMaterials.add(Material.DIAMOND_BLOCK);
        _excludedMaterials.add(Material.BEDROCK);
        _excludedMaterials.add(Material.BARRIER);
        _excludedMaterials.add(Material.DEBUG_STICK);

    }

    private void initLoot()
    {

        _tier_normal_lootTable = new LootTableItemStack();

        _tier_normal_lootTable.add(new ItemStack(Material.STONE), 250, 64);
        _tier_normal_lootTable.add(new ItemStack(Material.DIAMOND), 22, 64);
        _tier_normal_lootTable.add(new ItemStack(Material.DIAMOND_BLOCK), 10, 16);
        _tier_normal_lootTable.add(new ItemStack(Material.NETHER_STAR), 1, 1);
        _tier_normal_lootTable.add(new ItemStack(Material.NETHERITE_SCRAP), 15, 64);
        _tier_normal_lootTable.add(new ItemStack(Material.NETHERITE_BLOCK), 1, 1);
        _tier_normal_lootTable.add(new ItemStack(Material.NETHERITE_INGOT), 4, 12);
        _tier_normal_lootTable.add(new ItemStack(Material.EMERALD_BLOCK), 30, 33);
        _tier_normal_lootTable.add(new ItemStack(Material.ELYTRA), 1, 1);
        _tier_normal_lootTable.add(new ItemStack(Material.EMERALD_ORE), 35, 64);
        _tier_normal_lootTable.add(new ItemStack(Material.IRON_INGOT), 40, 64);
        _tier_normal_lootTable.add(new ItemStack(Material.GOLD_INGOT), 40, 64);
        _tier_normal_lootTable.add(new ItemStack(Material.LAPIS_LAZULI), 37, 64);
        _tier_normal_lootTable.add(new ItemStack(Material.COAL), 70, 64);
        _tier_normal_lootTable.add(new ItemStack(Material.COAL_BLOCK), 22, 64);
        _tier_normal_lootTable.add(new ItemStack(Material.IRON_BLOCK), 25, 64);
        _tier_normal_lootTable.add(new ItemStack(Material.GOLD_BLOCK), 23, 64);
        _tier_normal_lootTable.add(new ItemStack(Material.LAPIS_BLOCK), 16, 30);
        _tier_normal_lootTable.add(ItemFactory.createMysteryDust(), 3, 1);
        _tier_normal_lootTable.add(ItemFactory.createMysteryBox(), 1, 1);

        _tier_special_lootTable = new LootTableItemStack();
        //_tier_special_lootTable.Add(new ItemStack(Material.STONE), 10, 64);
        _tier_special_lootTable.add(new ItemStack(Material.DIAMOND), 22, 64);
        _tier_special_lootTable.add(new ItemStack(Material.DIAMOND_BLOCK), 10, 64);
        _tier_special_lootTable.add(new ItemStack(Material.NETHER_STAR), 1, 1);
        _tier_special_lootTable.add(new ItemStack(Material.NETHERITE_SCRAP), 15, 64);
        _tier_special_lootTable.add(new ItemStack(Material.NETHERITE_BLOCK), 1, 3);
        _tier_special_lootTable.add(new ItemStack(Material.NETHERITE_INGOT), 4, 22);
        _tier_special_lootTable.add(new ItemStack(Material.ELYTRA), 1, 1);
        _tier_special_lootTable.add(new ItemStack(Material.GOLD_BLOCK), 28, 64);
        _tier_special_lootTable.add(new ItemStack(Material.LAPIS_BLOCK), 20, 64);
        _tier_special_lootTable.add(new ItemStack(Material.TOTEM_OF_UNDYING), 2, 1);
        _tier_special_lootTable.add(ItemFactory.createMysteryDust(), 4, 1);
        _tier_special_lootTable.add(ItemFactory.createMysteryBox(), 2, 1);

    }

    private int getRandomCost()
    {
        return ThreadLocalRandom.current().nextInt(CONSTANTS.SHOP_COST_MIN_CHALLENGE_POINTS_NORMAL, CONSTANTS.SHOP_COST_MAX_CHALLENGE_POINTS_NORMAL);
    }

    public ArrayList<ItemStack> getGeneratedItems()
    {
        return _generatedNormalItems;
    }

    public ArrayList<ItemStack> getGeneratedSpecialItems()
    {
        return _generatedSpecialItems;
    }

    public ArrayList<Integer> getItemCostsNormal()
    {
        return _itemCostsNormal;
    }

    public ArrayList<Integer> getItemCostsSpecial()
    {
        return _itemCostsSpecial;
    }

    public int getNormalSlotPrice(int slotNumber)
    {
        return (int) (CONSTANTS.SHOP_COST_FIRST_NORMAL_SLOT_PRICE * Math.pow(CONSTANTS.SHOP_COST_FIRST_NORMAL_SLOT_PRICE_POW, slotNumber));
    }

    public int getSpecialSlotPrice(int slotNumber)
    {
        return (int) (CONSTANTS.SHOP_COST_FIRST_SPECIAL_SLOT_PRICE * Math.pow(CONSTANTS.SHOP_COST_FIRST_SPECIAL_SLOT_PRICE_POW, slotNumber));
    }

    private void generateSpecialItems()
    {
        _generatedSpecialItems.clear();
        _hasPlayerBuySpecial.clear();

        for (int i = 0; i < (CONSTANTS.SPECIAL_SLOTS); i++)
        {
            ItemStack item = _tier_special_lootTable.getLoot();

            if (item == null)
                continue;

            if (item.getType() == Material.STONE)
            {
                item = getRandomItem();
            }

            if (generatedItemContainsMaterial(item.getType(), true))
            {
                i--;
                if (i < 0) i = 0;
                continue;
            }

            _generatedSpecialItems.add(item);
            _itemCostsSpecial.add(getRandomCost() + CONSTANTS.SHOP_COST_SPECIAL_BASE_ITEM_COST);
        }
    }

    private void generateNormalItems()
    {
        _generatedNormalItems.clear();
        _itemCostsNormal.clear();
        _hasPlayerBuyNormal.clear();

        for (int i = 0; i < (CONSTANTS.NORMAL_SLOT_COLUMNS * CONSTANTS.NORMAL_SLOT_ROWS); i++)
        {
            ItemStack item = _tier_normal_lootTable.getLoot();

            if (item == null)
                continue;

            if (item.getType() == Material.STONE)
            {
                item = getRandomItem();
            }

            if (generatedItemContainsMaterial(item.getType(), false))
            {
                i--;
                if (i < 0) i = 0;
                continue;
            }

            _generatedNormalItems.add(item);
            _itemCostsNormal.add(getRandomCost());
        }
    }

    private boolean generatedItemContainsMaterial(Material material, boolean special)
    {
        for (ItemStack item : (special ? _generatedSpecialItems : _generatedNormalItems))
        {
            if (item.getType() == material)
                return true;
        }
        return false;
    }

    private ItemStack getRandomItem()
    {
        Material[] materials = Material.values();
        ItemStack stack = null;

        while (stack == null)
        {
            int index = (int) (Math.random() * materials.length);
            Material material = materials[index];

            if (ManagerCCollectMaterial.getInstance().isExcludedMaterial(material))
                continue;

            if (_excludedMaterials.contains(material))
                continue;


            if (material.isItem())
            {
                stack = new ItemStack(material);
                stack.setAmount(ThreadLocalRandom.current().nextInt(1, 64));
            }
        }

        if (stack.getAmount() > stack.getMaxStackSize())
            stack.setAmount(stack.getMaxStackSize());

        return stack;
    }

    public boolean hasPlayerBuyNormal(UUID uuid)
    {
        return _hasPlayerBuyNormal.contains(uuid);
    }

    public boolean hasPlayerBuySpecial(UUID uuid)
    {
        return _hasPlayerBuySpecial.contains(uuid);
    }

    public void setPlayerBuyNormal(UUID uuid, boolean value)
    {
        if (value)
        {
            _hasPlayerBuyNormal.add(uuid);
        }
        else
        {
            _hasPlayerBuyNormal.remove(uuid);
        }
    }

    public void setPlayerBuySpecial(UUID uuid, boolean value)
    {
        if (value)
        {
            _hasPlayerBuySpecial.add(uuid);
        }
        else
        {
            _hasPlayerBuySpecial.remove(uuid);
        }
    }

    public String timeLeftNormal()
    {
        return _cooldowns.getCdInReadableTime("normal");
    }

    public String timeLeftSpecial()
    {
        return _cooldowns.getCdInReadableTime("special");
    }

    private void scheduleItemGeneration()
    {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(ImusChallenges.getInstance(), new Runnable()
        {
            @Override
            public void run()
            {
                if (_cooldowns.isCooldownReady("special"))
                {
                    broadcastShopUpdate(true);
                    closeShops(true);
                    generateSpecialItems();
                    _cooldowns.addCooldownInSeconds("special", TIME_BETWEEN_SPECIAL_GENERATIONS);

                }

                if (_cooldowns.isCooldownReady("normal"))
                {
                    broadcastShopUpdate(false);
                    closeShops(false);
                    generateNormalItems();
                    _cooldowns.addCooldownInSeconds("normal", TIME_BETWEEN_NORMAL_GENERATIONS);

                }
            }

            private void broadcastShopUpdate(boolean special)
            {
                for (Player player : Bukkit.getServer().getOnlinePlayers())
                {
                    if(!player.hasPermission(CONSTANTS.PERM_BROADCAST_CHALLENGE_SHOP_UPDATE)) continue;

                    if (special)
                    {
                        player.sendMessage(Metods.msgC("&6Special &9Challenge shop has been updated!"));
                    }
                    else
                    {
                        player.sendMessage(Metods.msgC("&2Normal &9Challenge shop has been updated!"));
                    }
                }
            }
            private void closeShops(boolean special)
            {
                ArrayList<Player> players = closeAllShops();
                for (Player player : players)
                {
                    if(!player.hasPermission(CONSTANTS.PERM_BROADCAST_CHALLENGE_SHOP_UPDATE))
                    {
                        player.sendMessage(Metods.msgC("&9Challenge shop has been updated!"));
                    }
                    if (special)
                    {
                        player.sendMessage(Metods.msgC("&9You can now buy &2new &6special &9items!"));
                    }
                    else
                    {
                        player.sendMessage(Metods.msgC("&9You can now buy &2new &9items!"));
                    }
                }
            }

        }, 0L, 20);
    }

    public void onGiveItemStack(Player player, ItemStack itemStack)
    {
        if (itemStack == null)
            return;

        if (ItemFactory.isMysteryBox(itemStack))
        {
            int quality = ItemFactory.getMysteryBoxQuality(itemStack);
            final String cmd = "gmysterybox give " + player.getName() + " " + itemStack.getAmount() + " " + quality;
            Manager_CommandSender.getInstance().executeCommandAsConsole(cmd);
            return;
        }

        if (ItemFactory.isMysteryDust(itemStack))
        {
            final String cmd = "mysterydust add " + player.getName() + " " + ItemFactory.getMysteryDustAmount(itemStack);
            Manager_CommandSender.getInstance().executeCommandAsConsole(cmd);
            return;
        }
        InvUtil.AddItemToInventoryOrDrop(player, itemStack);
    }

    // SQL ============================================================================================================

    private void InitSQLData() throws SQLException
    {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() throws SQLException
    {
        TableUtils.createTableIfNotExists(ImusChallenges.getInstance().getSource(), TablePlayerShopStats.class);
    }

    private void addShopStats(TablePlayerShopStats playerShopStats) throws SQLException
    {
        playerShopStatsDao.create(playerShopStats);
    }

    private TablePlayerShopStats getShopStatsByPlayerId(Player player) throws SQLException
    {
        TablePlayers tablePlayer = ManagerPlayers.getInstance().findOrCreatePlayer(player);

        List<TablePlayerShopStats> stats = playerShopStatsDao.queryForEq("player_id", tablePlayer.getId());
        if (!stats.isEmpty())
        {
            return stats.get(0);
        }
        else
        {
            TablePlayerShopStats newShopStats = new TablePlayerShopStats();
            newShopStats.setPlayer(tablePlayer);
            newShopStats.setBought_normal_slots(CONSTANTS.PLAYER_SHOP_STATS_DEFAULT_VALUE);
            newShopStats.setBought_special_slots(CONSTANTS.PLAYER_SHOP_STATS_DEFAULT_SPECIAL); // Assuming default value constant for special slots
            playerShopStatsDao.create(newShopStats);
            return newShopStats;
        }
    }


    public void addSlotsToPlayerShopAsync(Player player, int addSlotAmount, boolean isSpecial)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                addSlotsToPlayerShop(player, addSlotAmount, isSpecial);
            }
        }.runTaskAsynchronously(ImusChallenges.getInstance());
    }

    private void addSlotsToPlayerShop(Player player, int addSlotAmount, boolean isSpecial)
    {
        try
        {
            TablePlayers tablePlayer = ManagerPlayers.getInstance().findOrCreatePlayer(player);
            TablePlayerShopStats currentStats = getShopStatsByPlayerId(player);

            if (currentStats == null)
            {
                currentStats = new TablePlayerShopStats();
                currentStats.setPlayer(tablePlayer);
                if (isSpecial)
                {
                    currentStats.setBought_special_slots(addSlotAmount);
                }
                else
                {
                    currentStats.setBought_normal_slots(addSlotAmount);
                }
                playerShopStatsDao.create(currentStats);
            }
            else
            {
                if (isSpecial)
                {
                    int newSpecialSlotCount = currentStats.getBought_special_slots() + addSlotAmount;
                    currentStats.setBought_special_slots(newSpecialSlotCount);
                }
                else
                {
                    int newNormalSlotCount = currentStats.getBought_normal_slots() + addSlotAmount;
                    currentStats.setBought_normal_slots(newNormalSlotCount);
                }
                playerShopStatsDao.update(currentStats);
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Asynchronously gets the shop stats for a player and executes a callback with the retrieved stats.
     *
     * @param player   The player whose stats are to be fetched.
     * @param callback The callback to be executed with the fetched stats main thread.
     */
    public void getShopStatsAsync(Player player, ShopStatsCallback callback)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    TablePlayerShopStats shopStats = getShopStatsByPlayerId(player);
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            callback.onShopStatsRetrieved(shopStats);
                        }
                    }.runTask(ImusChallenges.getInstance());
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(ImusChallenges.getInstance());
    }

}
