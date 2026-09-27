package me.imu.imuschallenges.Managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;
import imu.iAPI.Other.Metods;
import me.imu.imuschallenges.CONSTANTS;
import me.imu.imuschallenges.Database.Tables.TableCollected_materials;
import me.imu.imuschallenges.Database.Tables.TableExludedCollectMaterials;
import me.imu.imuschallenges.Enums.POINT_TYPE;
import me.imu.imuschallenges.ImusChallenges;
import me.imu.imuschallenges.Interfaces.CacheUpdateCallback;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ManagerCCollectMaterial implements Listener
{
    private final ImusChallenges _main = ImusChallenges.getInstance();
    private static ManagerCCollectMaterial _instance;

    public static ManagerCCollectMaterial getInstance() {return _instance;}
    private final Set<Material> _collectedMaterials;
    private Set<Material> _unCollectedMaterials;
    private final Set<Material> _excludedCollectedMaterials;
    private final Map<UUID, List<TableCollected_materials>> _playerCollectedMaterials;

    private final ConcurrentLinkedQueue<PlayerMaterialPair> buffer = new ConcurrentLinkedQueue<>();
    private final BukkitScheduler scheduler = Bukkit.getScheduler();

    private final Dao<TableCollected_materials, String> _collectedMaterialsDao;
    private final Dao<TableExludedCollectMaterials, String> _excludedMaterialsDao;

    private final double POINT_PER_MATERIAL = 1;
    public ManagerCCollectMaterial(ImusChallenges main)
    {
        _instance = this;
        _collectedMaterials = new HashSet<>();
        _playerCollectedMaterials = new HashMap<>();
        _excludedCollectedMaterials = new HashSet<>();

        createTables();
        loadOtherTablesSQL_async();
        startBufferProcessor();

        try
        {
            _collectedMaterialsDao = DaoManager.createDao(_main.getSource(), TableCollected_materials.class);
            _excludedMaterialsDao = DaoManager.createDao(_main.getSource(), TableExludedCollectMaterials.class);
        } catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Could not create Dao");
        }

    }

    private ManagerPlayerPoints getManagerPlayerPoints()
    {
        return ManagerPlayerPoints.getInstance();
    }

    private static class PlayerMaterialPair
    {
        final Player player;
        final Material material;

        PlayerMaterialPair(Player player, Material material)
        {
            this.player = player;
            this.material = material;
        }
    }

    private void startBufferProcessor()
    {
        scheduler.runTaskTimerAsynchronously(_main, () -> {
            while (!buffer.isEmpty())
            {
                PlayerMaterialPair pair = buffer.poll();
                if (pair != null)
                {
                    saveMaterialCollectionSQL_async(pair.player, pair.material);
                }

                if (pair != null && !_collectedMaterials.contains(pair.material))
                {
                    _collectedMaterials.add(pair.material);
                }
            }
        }, 40L, 100L); // Runs every 5 seconds, adjustable
    }

    private void createUncollectedMaterials()
    {
        _unCollectedMaterials = new HashSet<>();
        Bukkit.getLogger().info("[ImusChallenges] Creating uncollected materials... total of: " + Material.values().length + " materials");
        for (Material material : Material.values())
        {
            if (!material.isItem())
            {
                continue;
            }

            if (_excludedCollectedMaterials.contains(material))
            {
                continue;
            }

            if (!_collectedMaterials.contains(material))
            {
                _unCollectedMaterials.add(material);
            }
        }
    }


    private void createTables()
    {
        Bukkit.getLogger().info("[ImusChallenges] Creating tables...");
        try
        {
            TableUtils.createTableIfNotExists(_main.getSource(), TableCollected_materials.class);
            TableUtils.createTableIfNotExists(_main.getSource(), TableExludedCollectMaterials.class);
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally
        {
            Bukkit.getLogger().info("[ImusChallenges] Tables created!");
        }
    }

    private void loadOtherTablesSQL_async()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    loadExcludedMaterialsSQL();
                    loadCollectedMaterialsSQL();
                    Bukkit.getScheduler().runTask(_main, () -> createUncollectedMaterials());
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(_main);
    }

    private void loadExcludedMaterialsSQL_async()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    loadExcludedMaterialsSQL();
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(_main);
    }

    private void loadExcludedMaterialsSQL() throws SQLException
    {
        List<TableExludedCollectMaterials> excludedMaterials = _excludedMaterialsDao.queryForAll();
        for (TableExludedCollectMaterials excluded : excludedMaterials)
        {
            _excludedCollectedMaterials.add(Material.valueOf(excluded.getMaterial()));
        }
    }

    private void loadCollectedMaterialsSQL_async()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    loadCollectedMaterialsSQL();
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(_main);
    }

    public void addExcludedMaterialSQL_async(Material material, CacheUpdateCallback callback)
    {
        if (_excludedCollectedMaterials.contains(material))
        {
            return;
        }

        _excludedCollectedMaterials.add(material);
        _unCollectedMaterials.remove(material);
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    TableExludedCollectMaterials excludedMaterial = new TableExludedCollectMaterials();
                    excludedMaterial.setMaterial(material.name());

                    _excludedMaterialsDao.createOrUpdate(excludedMaterial);

                    Bukkit.getScheduler().runTask(_main, () -> {
                        if (callback != null)
                        {
                            callback.onCacheUpdated();
                        }
                    });
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(_main);
    }

    private void loadCollectedMaterialsSQL() throws SQLException
    {
        List<TableCollected_materials> collectedMaterialsList = _collectedMaterialsDao.queryForAll();

        for (TableCollected_materials cm : collectedMaterialsList)
        {
            Material material = cm.getMaterial();
            if (material != null)
            {
                _collectedMaterials.add(material);
            }
        }
    }

    private void loadPlayerCollectedMaterialsSQL_async(UUID playerUuid)
    {
        Bukkit.getLogger().info("[ImusChallenges] Loading collected materials for player: " + playerUuid.toString());

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Map<String, Object> queryMap = new HashMap<>();
                    queryMap.put("player_uuid", playerUuid.toString());

                    List<TableCollected_materials> playerMaterialsList = _collectedMaterialsDao.queryForFieldValues(queryMap);
                    Bukkit.getScheduler().runTask(_main, () -> _playerCollectedMaterials.put(playerUuid, playerMaterialsList));
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(_main);
    }


    public boolean isMaterialCollected(Material material)
    {
        return _collectedMaterials.contains(material);
    }

    public Set<Material> getCollectedMaterials()
    {
        return _collectedMaterials;
    }

    public Set<Material> getUncollectedMaterials()
    {
        return _unCollectedMaterials;
    }

    public boolean isExcludedMaterial(Material material)
    {
        return _excludedCollectedMaterials.contains(material);
    }

    public List<TableCollected_materials> getPlayerCollectedMaterials(Player player)
    {
        return _playerCollectedMaterials.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        UUID playerUuid = event.getPlayer().getUniqueId();
        loadPlayerCollectedMaterialsSQL_async(playerUuid);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        UUID playerUuid = event.getPlayer().getUniqueId();
        _playerCollectedMaterials.remove(playerUuid);
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event)
    {
        if (!(event.getEntity() instanceof Player))
        {
            return;
        }

        Player player = (Player) event.getEntity();

        if (!player.hasPermission(CONSTANTS.PERM_SERVER_WIDE_COLLECTION_CHALLENGE))
        {
            return;
        }

        Material material = event.getItem().getItemStack().getType();

        if (isMaterialCollected(material))
        {
            return;
        }
        //Bukkit.getLogger().info(ChatColor.GREEN + "Player picked up item: "+event.getEntity());
        informPlayer(player, material);
        markMaterialAsCollected(material, player);

    }

    public void markMaterialAsCollected(Material material, Player player)
    {
        // Check if this material is already collected globally
        if (!_collectedMaterials.contains(material))
        {
            _collectedMaterials.add(material);
            _unCollectedMaterials.remove(material);

            TableCollected_materials collectedMaterial = new TableCollected_materials(player, material, new Date());

            List<TableCollected_materials> playerMaterials = _playerCollectedMaterials.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
            playerMaterials.add(collectedMaterial);

            // Add to buffer for asynchronous database update
            buffer.offer(new PlayerMaterialPair(player, material));

            getManagerPlayerPoints().addPointsAsync(player, POINT_TYPE.CHALLENGE_POINT, POINT_PER_MATERIAL);
        }
    }


    private void saveMaterialCollectionSQL_async(Player player, Material material)
    {

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                TableCollected_materials collectedMaterial = new TableCollected_materials();
                collectedMaterial.setMaterial(material.name());
                collectedMaterial.setPlayer_name(player.getName());
                collectedMaterial.setPlayer_uuid(player.getUniqueId().toString());
                collectedMaterial.setCollection_time(new Date());
                try
                {
                    _collectedMaterialsDao.create(collectedMaterial);
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(_main);
    }

    public void removeCollectedMaterialPlayerSQL_async(Player player, Material material)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // Build query to find the specific material collected by the player
                    Map<String, Object> fieldValues = new HashMap<>();
                    fieldValues.put("player_uuid", player.getUniqueId().toString());
                    fieldValues.put("material", material.name());

                    List<TableCollected_materials> materials = _collectedMaterialsDao.queryForFieldValues(fieldValues);

                    // Delete all found records
                    for (TableCollected_materials collectedMaterial : materials)
                    {
                        _collectedMaterialsDao.delete(collectedMaterial);
                    }

                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            _collectedMaterials.remove(material);
                            _unCollectedMaterials.add(material);
                            if (_playerCollectedMaterials.containsKey(player.getUniqueId()))
                            {
                                _playerCollectedMaterials.get(player.getUniqueId()).removeIf(cm -> cm.getMaterial().equals(material.name()));
                            }
                        }
                    }.runTask(_main);


                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(_main);
    }

    private final Map<UUID, Integer> playerCollectedCountCache = new HashMap<>();
    private LocalDateTime lastCacheUpdateTime;

    public HashMap<UUID, Integer> getPlayerCollectedCount()
    {
        return new HashMap<>(playerCollectedCountCache);
    }

    public void updateCollectedMaterialsCountCache(CacheUpdateCallback callback)
    {
        if (lastCacheUpdateTime != null && ChronoUnit.MINUTES.between(lastCacheUpdateTime, LocalDateTime.now()) < 5)
        {
            if (callback != null)
            {
                callback.onCacheUpdated();
            }
            return;
        }

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Map<UUID, Integer> newCounts = new HashMap<>();
                    List<TableCollected_materials> allMaterials = _collectedMaterialsDao.queryForAll();
                    for (TableCollected_materials material : allMaterials)
                    {
                        UUID playerUuid = UUID.fromString(material.getPlayer_uuid());
                        newCounts.put(playerUuid, newCounts.getOrDefault(playerUuid, 0) + 1);
                    }

                    Bukkit.getScheduler().runTask(_main, () -> {
                        playerCollectedCountCache.clear();
                        playerCollectedCountCache.putAll(newCounts);
                        lastCacheUpdateTime = LocalDateTime.now();

                        if (callback != null)
                        {
                            callback.onCacheUpdated();
                        }
                    });
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(_main);
    }


    private void informPlayer(Player player, Material material)
    {
        if (player.hasPermission(CONSTANTS.PERM_SERVER_WIDE_COLLECTION_CHALLENGE_BROADCAST))
        {
            Bukkit.getServer().broadcast(Metods.msgC("&9" + player.getName() + " &9has &6Researched &3" + material.name()), CONSTANTS.PERM_SERVER_WIDE_COLLECTION_CHALLENGE_BROADCAST);
            return;
        }

        String message;
        message = Metods.msgC("&9New &6Research &9found! The &3" + material.name());

        player.sendMessage(message);


    }
}
