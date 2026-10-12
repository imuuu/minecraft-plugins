package imu.GS.command;

import imu.GS.text.Messages;
import imu.GS.GsService;
import imu.GS.bukkit.ItemKeys;
import imu.GS.engine.live.FlashDeal;
import imu.GS.engine.live.ServerProgress;
import imu.GS.engine.live.WantedRequests;
import imu.GS.engine.shop.Shop;
import imu.GS.live.MarketEvents;
import imu.GS.live.ProgressTracker;
import imu.GS.menu.FlashDealMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static imu.GS.command.CommandText.rest;
import static imu.GS.command.CommandText.send;

/**
 * /gs peek, /gs flash start|end, /gs wanted start|end, /gs progress and /gs restock.
 */
public class LiveCommands
{
    private final GsService _service;

    public LiveCommands(GsService service)
    {
        _service = service;
    }

    private Shop shopOrMessage(CommandSender sender, String name)
    {
        Shop shop = _service.getShop(name);
        if (shop == null)
            send(sender, "&cShop not found: &9" + name);
        return shop;
    }

    /**
     * /gs peek &lt;shop&gt;: a look at a few of the shop's flash deals, from anywhere.
     */
    public void peek(Player player, String[] args)
    {
        Shop shop = shopOrMessage(player, rest(args, 1));
        if (shop == null)
            return;
        if (_service.getLive().getDeals().active(shop.getId(), System.currentTimeMillis()) == null)
        {
            Messages.send(player, "flash.no-deals");
            return;
        }
        new FlashDealMenu(_service, shop, true).open(player);
    }

    /**
     * /gs flash &lt;start|end&gt; &lt;shop&gt;
     */
    public void flash(CommandSender sender, String[] args)
    {
        if (args.length < 3 || !(args[1].equalsIgnoreCase("start") || args[1].equalsIgnoreCase("end")))
        {
            send(sender, "&c/gs flash <start|end> <shop>");
            return;
        }
        Shop shop = shopOrMessage(sender, rest(args, 2));
        if (shop == null)
            return;
        if (args[1].equalsIgnoreCase("end"))
        {
            _service.getLive().endFlashDeal(shop);
            send(sender, "&eThe flash deal of &r" + shop.getDisplayName() + " &eended");
            return;
        }
        FlashDeal deal = _service.getLive().startFlashDeal(shop, System.currentTimeMillis(), true);
        if (deal == null)
        {
            send(sender, "&eNothing to offer: players have had nothing a flash deal could have yet (/gs progress)");
            return;
        }
        send(sender, "&aStarted a flash deal with &e" + deal.offers().size() + " &aoffers in &r" + shop.getDisplayName());
    }

    /**
     * /gs wanted &lt;start|end&gt; &lt;shop&gt;
     */
    public void wanted(CommandSender sender, String[] args)
    {
        if (args.length < 3 || !(args[1].equalsIgnoreCase("start") || args[1].equalsIgnoreCase("end")))
        {
            send(sender, "&c/gs wanted <start|end> <shop>");
            return;
        }
        Shop shop = shopOrMessage(sender, rest(args, 2));
        if (shop == null)
            return;
        if (args[1].equalsIgnoreCase("end"))
        {
            _service.getMarket().endWanted(shop);
            send(sender, "&e" + shop.getDisplayName() + " &ewants nothing now");
            return;
        }
        List<WantedRequests.Wanted> wanted = _service.getMarket().startWanted(shop, System.currentTimeMillis(), true);
        List<String> items = new ArrayList<>();
        for (WantedRequests.Wanted request : wanted)
            items.add(MarketEvents.itemName(ItemKeys.template(request.item()).getType()) + " +" + Math.round(request.bonus() * 100) + "%");
        send(sender, "&a" + shop.getDisplayName() + " &anow wants: &e" + String.join(", ", items));
    }

    /**
     * /gs progress [scan|reset]: what non-op players have had, which flash deals follow.
     */
    public void progress(CommandSender sender, String[] args)
    {
        ProgressTracker tracker = _service.getLive().getTracker();
        ServerProgress progress = tracker.getProgress();
        if (args.length >= 2 && args[1].equalsIgnoreCase("scan"))
        {
            tracker.scanAll();
            send(sender, "&aLooked at every player's items now");
        } else if (args.length >= 2 && args[1].equalsIgnoreCase("reset"))
        {
            progress.clear();
            send(sender, "&eThe server's progress is forgotten; it is gathered again from what players have");
            return;
        }
        send(sender, "&9Players have had &e" + progress.items().size() + " &9kinds of items. Enchants:");
        List<String> enchants = new ArrayList<>();
        new TreeMap<>(progress.enchants()).forEach((enchant, level) -> enchants.add(enchant.replace("minecraft:", "") + " " + level));
        send(sender, "&7" + (enchants.isEmpty() ? "none yet" : String.join(", ", enchants)));
    }

    /**
     * /gs restock &lt;shop&gt;: brings rotating stock now.
     */
    public void restock(CommandSender sender, String[] args)
    {
        Shop shop = shopOrMessage(sender, rest(args, 1));
        if (shop == null)
            return;
        int added = _service.getLive().rotate(shop);
        send(sender, "&a" + added + " &akinds of items came to &r" + shop.getDisplayName());
    }
}
