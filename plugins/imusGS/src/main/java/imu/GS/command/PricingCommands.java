package imu.GS.command;

import imu.GS.GsService;
import imu.GS.engine.pricing.SellLimit;
import imu.GS.engine.shop.Shop;
import imu.GS.prices.profile.PriceProfiles;
import imu.GS.prices.profile.ProfileCalculator;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static imu.GS.command.CommandText.format;
import static imu.GS.command.CommandText.number;
import static imu.GS.command.CommandText.rest;
import static imu.GS.command.CommandText.send;

/**
 * /gs profile, /gs limits and /gs reset limits. They work from the console too.
 */
public class PricingCommands
{
    private final GsService _service;

    public PricingCommands(GsService service)
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

    // Price profiles =================================================================================================

    /**
     * /gs profile list, and /gs profile apply &lt;name&gt; [diamond price] [confirm]: without confirm it only shows what
     * the profile would do.
     */
    public void profile(CommandSender sender, String[] args)
    {
        if (args.length >= 2 && args[1].equalsIgnoreCase("list"))
        {
            send(sender, "&9Price profiles &7(plugins/imusGS/profiles): &e" + String.join(", ", _service.getProfiles().names()));
            send(sender, "&7/gs profile apply <name> [diamond price] confirm");
            return;
        }
        if (args.length < 3 || !args[1].equalsIgnoreCase("apply"))
        {
            send(sender, "&c/gs profile list  &7or  &c/gs profile apply <name> [diamond price] [confirm]");
            return;
        }

        String name = args[2];
        boolean confirm = args[args.length - 1].equalsIgnoreCase("confirm");
        double diamond = 1000;
        if (args.length >= 4 && !args[3].equalsIgnoreCase("confirm"))
        {
            Double value = number(args[3]);
            if (value == null || value <= 0)
            {
                send(sender, "&cThe diamond price must be a number over 0");
                return;
            }
            diamond = value;
        }

        ProfileCalculator.Result result;
        try
        {
            result = PriceProfiles.calculate(_service.getProfiles().load(name), _service.getMaterials(), diamond);
        } catch (IllegalArgumentException e)
        {
            send(sender, "&c" + e.getMessage());
            return;
        }

        if (!confirm)
        {
            send(sender, "&9Profile &e" + name + " &9with diamond = &e" + format(diamond) + "&9 would price &e"
                    + result.prices().size() + " &9materials:");
            send(sender, "&7  " + result.count(ProfileCalculator.Source.EXACT) + " by name, "
                    + result.count(ProfileCalculator.Source.WILDCARD) + " by wildcard, "
                    + result.count(ProfileCalculator.Source.RECIPE) + " from recipes, "
                    + result.count(ProfileCalculator.Source.DEFAULT) + " with the default price");
            StringBuilder examples = new StringBuilder("&7  ");
            for (String example : List.of("DIAMOND", "IRON_INGOT", "OAK_LOG", "OAK_PLANKS", "STONE", "BREAD", "DIAMOND_SWORD", "IRON_BLOCK"))
            {
                Double price = result.prices().get(example);
                if (price != null)
                    examples.append(example).append(" &e").append(format(price)).append("&7, ");
            }
            send(sender, examples.substring(0, Math.max(3, examples.length() - 2)));
            Set<String> tags = new HashSet<>();
            result.tags().values().forEach(tags::addAll);
            send(sender, "&7  Tags (&e" + tags.size() + "&7): " + String.join(", ", new TreeSet<>(tags)));
            for (Map.Entry<String, SellLimit> limit : result.limits().entrySet())
            {
                long count = result.limitGroups().values().stream().filter(limit.getKey()::equals).count();
                send(sender, "&7  Sell limit &e" + limit.getKey() + "&7: " + count + " materials, full price for &e"
                        + format(limit.getValue().allowance()) + "&7, &e" + format(limit.getValue().refillPerHour())
                        + "&7 back per hour, never under &e" + Math.round(limit.getValue().minFraction() * 100) + "%");
            }
            if (!result.unusedNames().isEmpty())
                send(sender, "&7  Not items in this version: " + String.join(", ", result.unusedNames()));
            send(sender, "&cThis replaces the price, tags and sell limits of every material. &eAdd &aconfirm &eto the end to apply it.");
            return;
        }

        _service.applyProfile(result);
        send(sender, "&aPriced &e" + result.prices().size() + " &amaterials with the profile &e" + name
                + " &a(diamond = " + format(diamond) + ")");
        for (Shop shop : _service.getShops())
            _service.warnAboutBuyBackProfit(sender, shop);
    }

    // Sell limits ====================================================================================================

    /**
     * /gs limits &lt;shop&gt;: how much of each sell limit group the shop still buys at the full price.
     */
    public void limits(CommandSender sender, String[] args)
    {
        Shop shop = shopOrMessage(sender, rest(args, 1));
        if (shop == null)
            return;
        Map<String, SellLimit> limits = _service.getMaterials().getLimits();
        if (limits.isEmpty())
        {
            send(sender, "&eThere are no sell limits. A price profile sets them: /gs profile");
            return;
        }
        send(sender, "&9Sell limits of &e" + shop.getName() + "&9 (shared by every seller):");
        for (Map.Entry<String, Double> entry : _service.sellPressure(shop).entrySet())
        {
            SellLimit limit = limits.get(entry.getKey());
            double sold = entry.getValue();
            String state = sold < limit.allowance()
                    ? "&a" + format(limit.allowance() - sold) + " &7left at the full price"
                    : "&cfull, &e" + Math.round(limit.multiplier(sold) * 100) + "% &cprice";
            send(sender, "&7  " + entry.getKey() + ": sold &e" + format(sold) + "&7 / " + format(limit.allowance())
                    + ", " + state + "&7, +" + format(limit.refillPerHour()) + "/h");
        }
    }

    /**
     * /gs reset limits &lt;shop&gt;
     */
    public void resetLimits(CommandSender sender, String[] args)
    {
        Shop shop = shopOrMessage(sender, rest(args, 2));
        if (shop == null)
            return;
        _service.resetSellPressure(shop);
        send(sender, "&aEvery sell limit of &e" + shop.getName() + "&a is back to the full price");
    }
}
