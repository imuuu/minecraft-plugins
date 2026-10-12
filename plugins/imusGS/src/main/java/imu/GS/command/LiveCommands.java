package imu.GS.command;

import imu.GS.GsService;
import imu.GS.bukkit.ItemKeys;
import imu.GS.engine.live.FlashDeal;
import imu.GS.engine.live.WantedRequests;
import imu.GS.engine.shop.Shop;
import imu.GS.live.MarketEvents;
import imu.GS.menu.FlashDealMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static imu.GS.command.CommandText.rest;
import static imu.GS.command.CommandText.send;

/**
 * /gs peek, /gs flash start|end, /gs wanted start|end and /gs restock.
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
            send(player, "&eThere are no flash deals on right now");
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
