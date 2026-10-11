package imu.GS.command;

import imu.GS.GsService;
import imu.GS.bukkit.ItemKeys;
import imu.GS.engine.ItemKey;
import imu.GS.engine.pricing.Overflow;
import imu.GS.engine.pricing.PriceRule;
import imu.GS.engine.pricing.SellLimit;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Shop;
import imu.GS.engine.shop.StockRules;
import imu.GS.menu.admin.EnchantPricesMenu;
import imu.GS.menu.admin.ShopEditorMenu;
import imu.GS.menu.admin.UniquesMenu;
import imu.GS.npc.ShopNpcAction;
import imu.GS.prices.MaterialPrices;
import imu.GS.prices.UniqueItems;
import imu.GS.prices.profile.PriceProfiles;
import imu.GS.prices.profile.ProfileCalculator;
import imu.iAPI.Config.ConfigMenu;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Every /gs sub command. Each gets the whole argument list, its own words included: for "/gs create shop Smith"
 * args is [create, shop, Smith].
 */
public class GsCommands
{
    private final GsService _service;
    private final ConfigMenu _configMenu;

    public GsCommands(GsService service, ConfigMenu configMenu)
    {
        _service = service;
        _configMenu = configMenu;
    }

    private static void send(Player player, String message)
    {
        player.sendMessage(Metods.msgC(message));
    }

    /**
     * The words from {@code from} on, joined with spaces, without quotes.
     */
    private static String rest(String[] args, int from)
    {
        if (args.length <= from)
            return "";
        return String.join(" ", Arrays.copyOfRange(args, from, args.length)).replace("\"", "").trim();
    }

    private static String format(double value)
    {
        if (value == Math.rint(value) && Math.abs(value) < 1e15)
            return String.valueOf((long) value);
        return String.valueOf(Math.round(value * 100) / 100.0);
    }

    private static Double number(String text)
    {
        try
        {
            return Double.parseDouble(text.replace(',', '.'));
        } catch (NumberFormatException e)
        {
            return null;
        }
    }

    private Shop shopOrMessage(Player player, String name)
    {
        Shop shop = _service.getShop(name);
        if (shop == null)
            send(player, "&cShop not found: &9" + name);
        return shop;
    }

    // Shops ==========================================================================================================

    public void help(Player player)
    {
        send(player, "&9[imusGS] &7/gs create shop <name>, /gs open shop <name>, /gs modify shop <name>, /gs add <hand|hotbar|inventory> <shop>,");
        send(player, "&7/gs create npc <shop>, /gs create unique [price], /gs modify uniques, /gs modify enchantments,");
        send(player, "&7/gs set|unset material ..., /gs tag ..., /gs config, /g getprice");
    }

    public void createShop(Player player, String[] args)
    {
        String name = rest(args, 2);
        if (GsService.plainName(name).isEmpty())
        {
            send(player, "&c/gs create shop <name>");
            return;
        }
        if (_service.getShop(name) != null)
        {
            send(player, "&cA shop named &9" + name + " &calready exists!");
            return;
        }
        _service.createShop(name);
        send(player, "&6New Shop has been made! Named as &9" + name);
    }

    public void openShop(Player player, String[] args)
    {
        Shop shop = shopOrMessage(player, rest(args, 2));
        if (shop != null)
            _service.openShop(player, shop);
    }

    public void modifyShop(Player player, String[] args)
    {
        Shop shop = shopOrMessage(player, rest(args, 2));
        if (shop != null)
            new ShopEditorMenu(_service, shop).open(player);
    }

    public void createNpc(Player player, String[] args)
    {
        Shop shop = shopOrMessage(player, rest(args, 2));
        if (shop == null)
            return;
        ImusAPI._instance.GetNpcManager().createAndEdit(player, ShopNpcAction.ID, shop.getId().toString(), shop.getDisplayName());
        send(player, "&aShop NPC for &9" + shop.getName() + " &acreated. &7Sneak + click it later to change it.");
    }

    /**
     * /gs add &lt;hand|hotbar|inventory&gt; &lt;shop&gt;: the stacks become stockable items that never run out, as many as
     * the stack has. The player keeps the items.
     */
    public void add(Player player, String[] args)
    {
        if (args.length < 3)
        {
            send(player, "&c/gs add <hand|hotbar|inventory> <shop>");
            return;
        }
        ItemSelection selection = ItemSelection.parse(args[1]);
        if (selection == null)
        {
            send(player, "&c/gs add <hand|hotbar|inventory> <shop>");
            return;
        }
        Shop shop = shopOrMessage(player, rest(args, 2));
        if (shop == null)
            return;

        for (ItemStack stack : selection.items(player))
        {
            ItemKey item = ItemKeys.of(stack);
            Listing listing = shop.findSameKind(item);
            if (listing == null || !listing.isStockable())
            {
                listing = new Listing(UUID.randomUUID(), item, stack.getAmount(), PriceRule.MATERIAL);
                shop.addListing(listing);
            }
            shop.setStock(listing, new StockRules(stack.getAmount(), -1, -1));
            shop.setAmount(listing, stack.getAmount());
            send(player, "&9" + Metods._ins.GetItemDisplayName(stack) + " &ahas been added to &3" + shop.getDisplayName());
        }
        _service.saveNow(shop);
    }

    // Uniques and enchants ===========================================================================================

    public void createUnique(Player player, String[] args)
    {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.AIR)
        {
            send(player, "&3You need to have item in hand!");
            return;
        }
        Double price = args.length > 2 ? number(args[2]) : Double.valueOf(0);
        if (price == null || price < 0)
        {
            send(player, "&c/gs create unique [price]");
            return;
        }
        UniqueItems.Unique unique = _service.getUniques().add(hand, Math.round(price * 100) / 100.0);
        _service.getDatabase().saveUnique(unique.key(), unique.price());
        _service.refreshMenus();
        send(player, "&3You have created &5Unique&3 item with price tag of &2" + unique.price());
    }

    public void modifyUniques(Player player, String[] args)
    {
        new UniquesMenu(_service).open(player);
    }

    public void modifyEnchantments(Player player, String[] args)
    {
        new EnchantPricesMenu(_service).open(player);
    }

    public void config(Player player, String[] args)
    {
        _configMenu.open(player);
    }

    // Material prices ================================================================================================

    /**
     * /gs set material price &lt;hand|hotbar|inventory&gt; &lt;price&gt;
     */
    public void setMaterialPrice(Player player, String[] args)
    {
        ItemSelection selection = args.length == 5 ? ItemSelection.parse(args[3]) : null;
        Double price = args.length == 5 ? number(args[4]) : null;
        if (selection == null || price == null || price < 0)
        {
            send(player, "&c/gs set material price <hand|hotbar|inventory> <price>");
            return;
        }
        MaterialPrices materials = _service.getMaterials();
        List<Material> changed = new java.util.ArrayList<>();
        for (Material material : selection.materials(player))
        {
            if (materials.get(material).getSmartMultiplier() != null)
            {
                send(player, "&b" + material + " &4Price didn't change because it has a smart price! &7(/gs unset material smart_price)");
                continue;
            }
            double old = materials.get(material).getPrice();
            double now = Math.round(price * 100) / 100.0;
            materials.setPrice(material, now);
            changed.add(material);
            String change = now == 0 ? "" : " &9( " + (old <= now ? "&a+" : "&c") + Math.round((1 - old / now) * 100) + " &e%&9 )";
            send(player, "&b" + material + " &eprice set to: &2 " + now + " &7From " + old + change);
        }
        _service.saveMaterials(changed);
    }

    /**
     * /gs set material overflow &lt;hand|hotbar|inventory&gt; &lt;softcap&gt; &lt;minPrice&gt; &lt;dropPercent&gt;
     */
    public void setMaterialOverflow(Player player, String[] args)
    {
        ItemSelection selection = args.length == 7 ? ItemSelection.parse(args[3]) : null;
        Double softCap = args.length == 7 ? number(args[4]) : null;
        Double minPrice = args.length == 7 ? number(args[5]) : null;
        Double dropPercent = args.length == 7 ? number(args[6]) : null;
        if (selection == null || softCap == null || minPrice == null || dropPercent == null || softCap < 0 || minPrice < 0
                || dropPercent < 0 || dropPercent > 100)
        {
            send(player, "&c/gs set material overflow <hand|hotbar|inventory> <softcap> <minPrice> <dropPercent>");
            return;
        }
        List<Material> materials = selection.materials(player);
        Overflow overflow = new Overflow(softCap.intValue(), 1.0 - dropPercent / 100.0, minPrice);
        for (Material material : materials)
            _service.getMaterials().setOverflow(material, overflow);
        _service.saveMaterials(materials);
        send(player, "&bMaterial overflow set to &2" + materials.size() + " &ecap: " + softCap.intValue() + " &cminprice: " + minPrice
                + " &3drop percent each sell: " + dropPercent);
    }

    public void unsetMaterialOverflow(Player player, String[] args)
    {
        ItemSelection selection = args.length == 4 ? ItemSelection.parse(args[3]) : null;
        if (selection == null)
        {
            send(player, "&c/gs unset material overflow <hand|hotbar|inventory>");
            return;
        }
        List<Material> changed = new java.util.ArrayList<>();
        for (Material material : selection.materials(player))
        {
            if (_service.getMaterials().get(material).getOverflow() == null)
                continue;
            _service.getMaterials().setOverflow(material, null);
            changed.add(material);
            send(player, "&eMaterial overflow &4unset &eto &b" + material);
        }
        _service.saveMaterials(changed);
    }

    public void setSmartPrice(Player player, String[] args)
    {
        ItemSelection selection = args.length == 5 ? ItemSelection.parse(args[3]) : null;
        Double multiplier = args.length == 5 ? number(args[4]) : null;
        if (selection == null || multiplier == null || multiplier < 0)
        {
            send(player, "&c/gs set material smart_price <hand|hotbar|inventory> <multiplier>");
            return;
        }
        List<Material> changed = new java.util.ArrayList<>();
        for (Material material : selection.materials(player))
        {
            if (_service.getMaterials().recipe(material).isEmpty())
            {
                send(player, "&bMaterial " + material + " &4doesn't have a recipe");
                continue;
            }
            _service.getMaterials().setSmartMultiplier(material, multiplier);
            changed.add(material);
        }
        _service.saveMaterials(changed);
        send(player, "&bSmart price calculation set to &2" + changed.size() + " &9materials");
    }

    public void unsetSmartPrice(Player player, String[] args)
    {
        ItemSelection selection = args.length == 4 ? ItemSelection.parse(args[3]) : null;
        if (selection == null)
        {
            send(player, "&c/gs unset material smart_price <hand|hotbar|inventory>");
            return;
        }
        List<Material> materials = selection.materials(player);
        for (Material material : materials)
            _service.getMaterials().setSmartMultiplier(material, null);
        _service.saveMaterials(materials);
        send(player, "&bSmart price has been &cremoved &bfrom &2" + materials.size() + " &9materials");
    }

    // Price profiles =================================================================================================

    /**
     * /gs profile list, and /gs profile apply &lt;name&gt; [diamond price] [confirm]: without confirm it only shows what
     * the profile would do.
     */
    public void profile(Player player, String[] args)
    {
        if (args.length >= 2 && args[1].equalsIgnoreCase("list"))
        {
            send(player, "&9Price profiles &7(plugins/imusGS/profiles): &e" + String.join(", ", _service.getProfiles().names()));
            send(player, "&7/gs profile apply <name> [diamond price] confirm");
            return;
        }
        if (args.length < 3 || !args[1].equalsIgnoreCase("apply"))
        {
            send(player, "&c/gs profile list  &7or  &c/gs profile apply <name> [diamond price] [confirm]");
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
                send(player, "&cThe diamond price must be a number over 0");
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
            send(player, "&c" + e.getMessage());
            return;
        }

        if (!confirm)
        {
            send(player, "&9Profile &e" + name + " &9with diamond = &e" + format(diamond) + "&9 would price &e"
                    + result.prices().size() + " &9materials:");
            send(player, "&7  " + result.count(ProfileCalculator.Source.EXACT) + " by name, "
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
            send(player, examples.substring(0, Math.max(3, examples.length() - 2)));
            Set<String> tags = new HashSet<>();
            result.tags().values().forEach(tags::addAll);
            send(player, "&7  Tags (&e" + tags.size() + "&7): " + String.join(", ", new TreeSet<>(tags)));
            for (Map.Entry<String, SellLimit> limit : result.limits().entrySet())
            {
                long count = result.limitGroups().values().stream().filter(limit.getKey()::equals).count();
                send(player, "&7  Sell limit &e" + limit.getKey() + "&7: " + count + " materials, full price for &e"
                        + format(limit.getValue().allowance()) + "&7, &e" + format(limit.getValue().refillPerHour())
                        + "&7 back per hour, never under &e" + Math.round(limit.getValue().minFraction() * 100) + "%");
            }
            if (!result.unusedNames().isEmpty())
                send(player, "&7  Not items in this version: " + String.join(", ", result.unusedNames()));
            send(player, "&cThis replaces the price, tags and sell limits of every material. &eAdd &aconfirm &eto the end to apply it.");
            return;
        }

        _service.applyProfile(result);
        send(player, "&aPriced &e" + result.prices().size() + " &amaterials with the profile &e" + name
                + " &a(diamond = " + format(diamond) + ")");
    }

    // Sell limits ====================================================================================================

    /**
     * /gs limits &lt;shop&gt;: how much of each sell limit group the shop still buys at the full price.
     */
    public void limits(Player player, String[] args)
    {
        Shop shop = shopOrMessage(player, rest(args, 1));
        if (shop == null)
            return;
        Map<String, SellLimit> limits = _service.getMaterials().getLimits();
        if (limits.isEmpty())
        {
            send(player, "&eThere are no sell limits. A price profile sets them: /gs profile");
            return;
        }
        send(player, "&9Sell limits of &e" + shop.getName() + "&9 (shared by every seller):");
        for (Map.Entry<String, Double> entry : _service.sellPressure(shop).entrySet())
        {
            SellLimit limit = limits.get(entry.getKey());
            double sold = entry.getValue();
            String state = sold < limit.allowance()
                    ? "&a" + format(limit.allowance() - sold) + " &7left at the full price"
                    : "&cfull, &e" + Math.round(limit.multiplier(sold) * 100) + "% &cprice";
            send(player, "&7  " + entry.getKey() + ": sold &e" + format(sold) + "&7 / " + format(limit.allowance())
                    + ", " + state + "&7, +" + format(limit.refillPerHour()) + "/h");
        }
    }

    /**
     * /gs reset limits &lt;shop&gt;
     */
    public void resetLimits(Player player, String[] args)
    {
        Shop shop = shopOrMessage(player, rest(args, 2));
        if (shop == null)
            return;
        _service.resetSellPressure(shop);
        send(player, "&aEvery sell limit of &e" + shop.getName() + "&a is back to the full price");
    }

    // Tags ===========================================================================================================

    /**
     * /gs tag materials &lt;add|remove|remove_all_tags|set_price|increase_price&gt; ... and
     * /gs tag shopitems &lt;set_price|increase_price&gt; &lt;tag&gt; &lt;value&gt;
     */
    public void tag(Player player, String[] args)
    {
        if (args.length < 3)
        {
            send(player, "&c/gs tag <materials|shopitems> ...");
            return;
        }
        if (args[1].equalsIgnoreCase("materials"))
            tagMaterials(player, args);
        else if (args[1].equalsIgnoreCase("shopitems"))
            tagShopItems(player, args);
        else
            send(player, "&c/gs tag <materials|shopitems> ...");
    }

    private void tagMaterials(Player player, String[] args)
    {
        String action = args[2].toLowerCase();
        MaterialPrices materials = _service.getMaterials();
        switch (action)
        {
            case "add", "remove" ->
            {
                ItemSelection selection = args.length == 5 ? ItemSelection.parse(args[3]) : null;
                if (selection == null)
                {
                    send(player, "&c/gs tag materials " + action + " <hand|hotbar|inventory> <tag>");
                    return;
                }
                String tag = args[4].toLowerCase();
                List<Material> changed = selection.materials(player);
                for (Material material : changed)
                {
                    if (action.equals("add"))
                    {
                        materials.addTag(material, tag);
                        send(player, "&2Tag named &3" + tag + " &2has been &eadded &2to material &b" + material);
                    } else
                    {
                        materials.removeTag(material, tag);
                        send(player, "&2Tag named &3" + tag + " &2has been &cremoved &2from material &b" + material);
                    }
                }
                _service.saveMaterials(changed);
                _service.getPlugin().updateTabCompletion();
            }
            case "remove_all_tags" ->
            {
                ItemSelection selection = args.length >= 4 ? ItemSelection.parse(args[3]) : null;
                if (selection == null)
                {
                    send(player, "&c/gs tag materials remove_all_tags <hand|hotbar|inventory>");
                    return;
                }
                List<Material> changed = selection.materials(player);
                for (Material material : changed)
                {
                    materials.clearTags(material);
                    send(player, "&2All tags from material &b" + material + " &2have been &cremoved");
                }
                _service.saveMaterials(changed);
                _service.getPlugin().updateTabCompletion();
            }
            case "set_price", "increase_price" ->
            {
                Double value = args.length == 5 ? number(args[4]) : null;
                if (value == null || value < 0)
                {
                    send(player, "&c/gs tag materials " + action + " <tag> <" + (action.equals("set_price") ? "price" : "multiplier") + ">");
                    return;
                }
                String tag = args[3].toLowerCase();
                Set<Material> tagged = materials.withTag(tag);
                for (Material material : tagged)
                {
                    double price = action.equals("set_price") ? value : materials.get(material).getPrice() * value;
                    materials.setPrice(material, Math.round(price * 100) / 100.0);
                }
                _service.saveMaterials(tagged);
                send(player, "&2All materials&7(&e" + tagged.size() + "&7)&2 with tag '&b" + tag + "&2' "
                        + (action.equals("set_price") ? "the price is set to: &a" : "the price is increased by: &b") + value);
            }
            default -> send(player, "&c/gs tag materials <add|remove|remove_all_tags|set_price|increase_price> ...");
        }
    }

    private void tagShopItems(Player player, String[] args)
    {
        String action = args[2].toLowerCase();
        Double value = args.length == 5 ? number(args[4]) : null;
        if (!(action.equals("set_price") || action.equals("increase_price")) || value == null || value < 0)
        {
            send(player, "&c/gs tag shopitems <set_price|increase_price> <tag> <value>");
            return;
        }
        String tag = args[3].toLowerCase();
        int count = 0;
        for (Shop shop : _service.getShops())
        {
            for (Listing listing : shop.getListings())
            {
                if (!listing.hasTag(tag) || !(listing.getPrice() instanceof PriceRule.Fixed fixed))
                    continue;
                double price = action.equals("set_price") ? value : fixed.price() * value;
                shop.setPrice(listing, new PriceRule.Fixed(Math.round(price * 100) / 100.0));
                count++;
            }
        }
        send(player, "&2All shopitems&7(&e" + count + "&7)&2 with tag '&b" + tag + "&2' "
                + (action.equals("set_price") ? "the price is set to: &a" : "the price is increased by: &b") + value);
    }

    /**
     * Tags of shop items in every shop, for tab completion.
     */
    public Set<String> shopItemTags()
    {
        Set<String> tags = new HashSet<>();
        for (Shop shop : _service.getShops())
        {
            for (Listing listing : shop.getListings())
                tags.addAll(listing.getTags());
        }
        return tags;
    }
}
