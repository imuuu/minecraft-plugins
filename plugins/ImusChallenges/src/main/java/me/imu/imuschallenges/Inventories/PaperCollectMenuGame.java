package me.imu.imuschallenges.Inventories;

import me.imu.imuschallenges.Database.Tables.TableCollected_materials;
import me.imu.imuschallenges.Managers.ManagerCCollectMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * The collection menu's {@link CollectMenuGame} on the server.
 */
public final class PaperCollectMenuGame implements CollectMenuGame
{
    private final ManagerCCollectMaterial _manager = ManagerCCollectMaterial.getInstance();

    @Override
    public void playerCounts(Consumer<List<PlayerCount>> done)
    {
        _manager.updateCollectedMaterialsCountCache(() ->
        {
            Map<UUID, Integer> counts = _manager.getPlayerCollectedCount();
            List<PlayerCount> list = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers())
                list.add(new PlayerCount(player.getUniqueId(), player.getName(), counts.getOrDefault(player.getUniqueId(), 0)));
            done.accept(list);
        });
    }

    @Override
    public void collected(UUID player, Consumer<List<Collected>> done)
    {
        Player online = Bukkit.getPlayer(player);
        if (online == null)
        {
            done.accept(null);
            return;
        }
        // Read from the cache of online players, no wait
        done.accept(_manager.getPlayerCollectedMaterials(online).stream()
                .sorted(Comparator.comparing(TableCollected_materials::getCollection_time).reversed())
                .map(row -> new Collected(row.getMaterial().name(), row.getCollection_time().getTime()))
                .toList());
    }

    @Override
    public void uncollected(Consumer<List<String>> done)
    {
        done.accept(_manager.getUncollectedMaterials().stream().map(Material::name).toList());
    }

    @Override
    public void removeCollected(UUID player, String material)
    {
        Player online = Bukkit.getPlayer(player);
        Material type = Material.getMaterial(material);
        if (online != null && type != null)
            _manager.removeCollectedMaterialPlayerSQL_async(online, type);
    }

    @Override
    public void exclude(String material, Runnable done)
    {
        Material type = Material.getMaterial(material);
        if (type != null)
            _manager.addExcludedMaterialSQL_async(type, done::run);
    }
}
