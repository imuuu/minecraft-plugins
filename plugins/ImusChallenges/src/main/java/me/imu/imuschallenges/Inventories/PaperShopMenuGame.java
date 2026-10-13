package me.imu.imuschallenges.Inventories;

import imu.iAPI.Managers.Manager_Vault;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Paper.PaperIcons;
import imu.iAPI.Menus.Viewer;
import imu.iAPI.Utilities.TextUtils;
import me.imu.imuschallenges.Database.Tables.TablePlayerShopStats;
import me.imu.imuschallenges.Enums.POINT_TYPE;
import me.imu.imuschallenges.Managers.ManagerChallengeShop;
import me.imu.imuschallenges.Managers.ManagerPlayerPoints;
import me.imu.imuschallenges.Shop.ShopTier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * The challenge shop menu's {@link ShopMenuGame} on the server.
 */
public final class PaperShopMenuGame implements ShopMenuGame
{
    private final ManagerChallengeShop _shop = ManagerChallengeShop.getInstance();

    private static Player player(Viewer viewer)
    {
        return Objects.requireNonNull(Bukkit.getPlayer(viewer.id()), "The player is offline");
    }

    private ShopTier tier(boolean special)
    {
        return special ? _shop.getSpecial() : _shop.getNormal();
    }

    @Override
    public String resetsIn(boolean special)
    {
        return _shop.timeLeft(tier(special));
    }

    @Override
    public void load(Viewer viewer, Consumer<List<Tier>> done)
    {
        Player player = player(viewer);
        _shop.getShopStatsAsync(player, stats -> done.accept(List.of(view(tier(true), stats, player), view(tier(false), stats, player))));
    }

    private static Tier view(ShopTier tier, TablePlayerShopStats stats, Player player)
    {
        boolean closed = stats == null || tier.hasBought(player.getUniqueId());
        int bought = stats == null ? 0 : tier.getBoughtSlots(stats);
        int nextSlot = bought - tier.getDefaultSlots();
        List<Icon> items = tier.getItems().stream().map(PaperIcons::of).toList();
        return new Tier(tier.isSpecial(), closed, items, List.copyOf(tier.getCosts()), bought, nextSlot, tier.getSlotPrice(nextSlot));
    }

    @Override
    public boolean buyItem(Viewer viewer, boolean special, int index)
    {
        Player player = player(viewer);
        ShopTier tier = tier(special);
        UUID uuid = player.getUniqueId();
        if (tier.hasBought(uuid) || index >= tier.getItems().size())
            return true;

        ItemStack item = tier.getItems().get(index).clone();
        int cost = tier.getCosts().get(index);
        if (!_shop.canDeliver(item))
        {
            player.sendMessage(TextUtils.Color("&cThis item can't be bought right now!"));
            return false;
        }

        // Marked before the points are taken, so the shop can't be reopened for a second purchase meanwhile
        int generation = tier.getGeneration();
        tier.setBought(uuid, true);

        ManagerPlayerPoints.getInstance().trySpendPointsAsync(player, POINT_TYPE.CHALLENGE_POINT, cost, spent ->
        {
            if (!spent)
            {
                if (tier.getGeneration() == generation)
                    tier.setBought(uuid, false);
                player.sendMessage(TextUtils.Color("&cYou don't have enough points!"));
                return;
            }

            _shop.saveState();
            if (!player.isOnline())
            {
                ManagerPlayerPoints.getInstance().refundPointsAsync(player, POINT_TYPE.CHALLENGE_POINT, cost);
                if (tier.getGeneration() == generation)
                    tier.setBought(uuid, false);
                _shop.saveState();
                return;
            }
            _shop.onGiveItemStack(player, item);
            player.sendMessage(TextUtils.Color("&9You bought an item(s) with &2" + cost + " &6challenge points!"));
        });
        return true;
    }

    @Override
    public boolean buySlot(Viewer viewer, boolean special, int nextSlot)
    {
        Player player = player(viewer);
        int price = tier(special).getSlotPrice(nextSlot);
        if (!Manager_Vault.takeMoney(player, price))
        {
            player.sendMessage(TextUtils.Color("&cYou don't have enough money!"));
            return false;
        }
        _shop.addSlotsToPlayerShopAsync(player, 1, tier(special));
        player.sendMessage(TextUtils.Color("&aYou bought a slot! &9Cost: &2" + price));
        return true;
    }

    @Override
    public void opened(Viewer viewer)
    {
        _shop.addPlayerToShop(player(viewer));
    }

    @Override
    public void closed(Viewer viewer)
    {
        Player player = Bukkit.getPlayer(viewer.id());
        if (player != null)
            _shop.removePlayerFromShop(player);
    }
}
