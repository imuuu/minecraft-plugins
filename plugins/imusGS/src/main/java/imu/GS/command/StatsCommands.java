package imu.GS.command;

import imu.GS.GsService;
import imu.GS.bukkit.ItemKeys;
import imu.GS.data.GsDatabase;
import imu.GS.engine.ItemKey;
import imu.GS.engine.live.FlashDeal;
import imu.GS.engine.live.FlashOffer;
import imu.GS.engine.live.WantedRequests;
import imu.GS.engine.shop.Shop;
import imu.GS.live.MarketEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import static imu.GS.command.CommandText.rest;
import static imu.GS.command.CommandText.send;

/**
 * /gs stats [days] [shop]: where the money went, from the trade log, and what the live events are doing now.
 * Made for balancing: is a farm paying too much, do flash deals sell, how much money leaves the economy.
 */
public class StatsCommands
{
    private final GsService _service;

    public StatsCommands(GsService service)
    {
        _service = service;
    }

    public void stats(CommandSender sender, String[] args)
    {
        int days = 7;
        int shopFrom = 1;
        if (args.length >= 2 && args[1].matches("\\d+"))
        {
            days = Math.max(1, Math.min(3650, Integer.parseInt(args[1])));
            shopFrom = 2;
        }
        String shopName = rest(args, shopFrom);
        Shop shop = shopName.isEmpty() ? null : _service.getShop(shopName);
        if (!shopName.isEmpty() && shop == null)
        {
            send(sender, "&cShop not found: &9" + shopName);
            return;
        }

        int finalDays = days;
        send(sender, "&7Reading the trade log...");
        Bukkit.getScheduler().runTaskAsynchronously(_service.getPlugin(), () ->
        {
            try
            {
                GsDatabase.TradeStats stats = _service.getDatabase().loadTradeStats(finalDays, shop == null ? null : shop.getId());
                Bukkit.getScheduler().runTask(_service.getPlugin(), () -> show(sender, finalDays, shop, stats));
            } catch (SQLException e)
            {
                _service.getPlugin().getLogger().log(Level.WARNING, "Couldn't read the trade statistics", e);
                Bukkit.getScheduler().runTask(_service.getPlugin(), () -> send(sender, "&cCouldn't read the trade log, see the console"));
            }
        });
    }

    private void show(CommandSender sender, int days, Shop shop, GsDatabase.TradeStats stats)
    {
        send(sender, "&9&l=== &6imusGS stats &9- last " + days + " day" + (days == 1 ? "" : "s") + " - "
                + (shop == null ? "every shop" : shop.getDisplayName()) + " &9&l===");
        GsDatabase.Total bought = stats.bought();
        GsDatabase.Total sold = stats.sold();
        send(sender, "&aPlayers paid the shops &e" + money(bought.money()) + " &7(" + bought.trades() + " buys, " + bought.units() + " items)");
        send(sender, "&cShops paid players &e" + money(sold.money()) + " &7(" + sold.trades() + " sells, " + sold.units() + " items)");
        double net = bought.money() - sold.money();
        send(sender, net >= 0
                ? "&7Net: &a" + money(net) + " &7taken out of the economy"
                : "&7Net: &c" + money(-net) + " &7put into the economy");
        if (stats.flash().trades() > 0)
            send(sender, "&6Flash deals: &e" + money(stats.flash().money()) + " &7(" + stats.flash().trades() + " buys, " + stats.flash().units() + " items)");

        if (!stats.topSold().isEmpty())
            send(sender, "&3Sold to shops most: &7" + items(stats.topSold()));
        if (!stats.topBought().isEmpty())
            send(sender, "&2Bought from shops most: &7" + items(stats.topBought()));
        if (!stats.sellers().isEmpty())
        {
            List<String> sellers = new ArrayList<>();
            for (GsDatabase.Total seller : stats.sellers())
                sellers.add(seller.name() + " &e" + money(seller.money()) + "&7");
            send(sender, "&3Top sellers: &7" + String.join(", ", sellers));
        }
        if (stats.days().size() > 1)
        {
            List<String> perDay = new ArrayList<>();
            for (Map.Entry<String, double[]> day : stats.days().entrySet())
                perDay.add(day.getKey().substring(5) + " &a+" + whole(day.getValue()[0]) + "&7/&c-" + whole(day.getValue()[1]) + "&7");
            send(sender, "&7Per day (paid in/out): " + String.join(", ", perDay));
        }

        showLive(sender, shop);
    }

    /**
     * What the live events are doing now, from memory.
     */
    private void showLive(CommandSender sender, Shop only)
    {
        long now = System.currentTimeMillis();
        List<Shop> shops = only == null ? new ArrayList<>(_service.getShops()) : List.of(only);
        for (Shop shop : shops)
        {
            FlashDeal deal = _service.getLive().getDeals().active(shop.getId(), now);
            if (deal != null)
            {
                int soldOut = 0;
                for (FlashOffer offer : deal.offers())
                {
                    if (offer.getStock() <= 0)
                        soldOut++;
                }
                send(sender, "&6Flash deal on in &r" + shop.getDisplayName() + "&6: " + duration(deal.millisLeft(now)) + " left, "
                        + soldOut + "/" + deal.offers().size() + " offers sold out");
            }
            List<WantedRequests.Wanted> wanted = _service.getMarket().activeWanted(shop);
            if (!wanted.isEmpty())
            {
                List<String> items = new ArrayList<>();
                for (WantedRequests.Wanted request : wanted)
                    items.add(MarketEvents.itemName(ItemKeys.template(request.item()).getType()) + " +" + Math.round(request.bonus() * 100) + "%");
                send(sender, "&aWanted in &r" + shop.getDisplayName() + "&a: " + String.join(", ", items) + " &7("
                        + duration(wanted.getFirst().endMillis() - now) + " left)");
            }
        }

        List<String> next = new ArrayList<>();
        nextLine(next, "flash deals", "live.flash-deals.enabled", _service.getLive().getNextFlashMillis(), now);
        nextLine(next, "wanted", "live.wanted.enabled", _service.getMarket().getNextWantedMillis(), now);
        nextLine(next, "rotation", "live.rotation.enabled", _service.getLive().getNextRotationMillis(), now);
        if (!next.isEmpty())
            send(sender, "&7Next: " + String.join(", ", next));

        Map<String, Double> moods = _service.getMarket().todaysMood();
        if (!moods.isEmpty())
        {
            List<String> line = new ArrayList<>();
            moods.forEach((tag, factor) ->
            {
                long percent = Math.round((factor - 1) * 100);
                if (percent != 0)
                    line.add(tag + (percent > 0 ? " &a+" : " &c") + percent + "%&7");
            });
            send(sender, "&7Mood today: " + (line.isEmpty() ? "all at normal prices" : String.join(", ", line)));
        }
    }

    private void nextLine(List<String> next, String name, String enabledPath, long at, long now)
    {
        if (!_service.getPlugin().getConfig().getBoolean(enabledPath, true))
            next.add(name + " &coff&7");
        else if (at > 0)
            next.add(name + " in " + duration(at - now));
    }

    private static String items(List<GsDatabase.Total> totals)
    {
        List<String> items = new ArrayList<>();
        for (GsDatabase.Total total : totals)
            items.add(itemName(total.name()) + " &e" + money(total.money()) + " &7(" + total.units() + ")");
        return String.join(", ", items);
    }

    private static String itemName(String itemId)
    {
        try
        {
            return MarketEvents.itemName(ItemKeys.template(new ItemKey(itemId)).getType());
        } catch (RuntimeException e)
        {
            return itemId.length() > 30 ? itemId.substring(0, 30) + "..." : itemId;
        }
    }

    private static String money(double amount)
    {
        return String.format(Locale.ROOT, "%,.0f $", amount);
    }

    private static String whole(double amount)
    {
        return String.format(Locale.ROOT, "%,.0f", amount);
    }

    private static String duration(long millis)
    {
        long minutes = Math.max(0, millis) / 60_000;
        return minutes >= 60 ? (minutes / 60) + " h " + (minutes % 60) + " min" : minutes + " min";
    }
}
