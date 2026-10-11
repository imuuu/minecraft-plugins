package imu.GS.prices.profile;

import imu.GS.engine.pricing.SellLimit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Works out the price and tags of every material from a {@link PriceProfile}. Plain data in, plain data out,
 * so it can be tested without a server.
 *
 * <p>A material's price comes from, in this order:</p>
 * <ol>
 *   <li>its exact name in the profile,</li>
 *   <li>the most specific wildcard that matches it (the one with the most letters),</li>
 *   <li>its crafting recipe: the ingredients' prices times the crafted multiplier,</li>
 *   <li>the profile's default price.</li>
 * </ol>
 * Then every price is scaled from the profile's reference diamond price to the one asked for.
 *
 * <p>A material's sell limit group is the group that names it (exact names before wildcards, the most specific
 * wildcard first). A material no group names follows its crafting recipe: it is in the group of its most valuable
 * ingredient, so hay bales and bread count against the crops limit, and iron blocks and tools against iron.</p>
 */
public final class ProfileCalculator
{
    public enum Source
    {
        EXACT, WILDCARD, RECIPE, DEFAULT
    }

    /**
     * @param prices material -> price, rounded to cents
     * @param tags   material -> tags, only materials with at least one tag
     * @param source material -> where its price came from
     * @param limitGroups material -> its sell limit group, only materials in a group
     * @param limits      sell limit group -> its limit, scaled like the prices
     */
    public record Result(Map<String, Double> prices, Map<String, Set<String>> tags, Map<String, Source> source,
                         List<String> unusedNames, Map<String, String> limitGroups, Map<String, SellLimit> limits)
    {
        public long count(Source wanted)
        {
            return source.values().stream().filter(s -> s == wanted).count();
        }
    }

    private record Wildcard(String pattern, Pattern regex, int letters, double price) {}

    private record GroupWildcard(Pattern regex, int letters, String group) {}

    private final PriceProfile _profile;
    private final Function<String, Map<String, Double>> _recipes;
    private final Map<String, Double> _exact = new HashMap<>();
    private final List<Wildcard> _wildcards = new ArrayList<>();
    private final Map<String, Double> _resolved = new HashMap<>();
    private final Map<String, Source> _sources = new HashMap<>();
    private final Set<String> _inProgress = new HashSet<>();
    private final Map<String, String> _groupExact = new HashMap<>();
    private final List<GroupWildcard> _groupWildcards = new ArrayList<>();
    // Material -> its group, null when it has none
    private final Map<String, String> _groups = new HashMap<>();
    private final Set<String> _groupsInProgress = new HashSet<>();

    /**
     * @param recipes material -> ingredients per crafted unit (material -> amount); empty when it has no recipe
     */
    private ProfileCalculator(PriceProfile profile, Function<String, Map<String, Double>> recipes)
    {
        _profile = profile;
        _recipes = recipes;
        for (Map.Entry<String, Double> entry : profile.prices().entrySet())
        {
            String pattern = entry.getKey().toUpperCase(Locale.ROOT);
            if (pattern.contains("*"))
                _wildcards.add(new Wildcard(pattern, toRegex(pattern), pattern.replace("*", "").length(), entry.getValue()));
            else
                _exact.put(pattern, entry.getValue());
        }
        // Most specific first
        _wildcards.sort((a, b) -> Integer.compare(b.letters(), a.letters()));

        for (Map.Entry<String, PriceProfile.LimitGroup> group : profile.sellLimits().entrySet())
        {
            String groupName = group.getKey().toLowerCase(Locale.ROOT);
            for (String item : group.getValue().items())
            {
                String pattern = item.toUpperCase(Locale.ROOT);
                if (pattern.contains("*"))
                    _groupWildcards.add(new GroupWildcard(toRegex(pattern), pattern.replace("*", "").length(), groupName));
                else
                    _groupExact.putIfAbsent(pattern, groupName);
            }
        }
        _groupWildcards.sort((a, b) -> Integer.compare(b.letters(), a.letters()));
    }

    /**
     * @param materials    every material to price
     * @param diamondPrice what diamond should cost; every price is scaled by diamondPrice / reference price
     */
    public static Result calculate(PriceProfile profile, Collection<String> materials, Function<String, Map<String, Double>> recipes, double diamondPrice)
    {
        ProfileCalculator calculator = new ProfileCalculator(profile, recipes);
        double scale = diamondPrice / profile.referencePrice();

        Map<String, Double> prices = new TreeMap<>();
        Map<String, Source> sources = new TreeMap<>();
        for (String material : materials)
        {
            String name = material.toUpperCase(Locale.ROOT);
            prices.put(name, scale(calculator.price(name), scale));
            sources.put(name, calculator._sources.get(name));
        }

        Map<String, Set<String>> tags = new TreeMap<>();
        for (Map.Entry<String, List<String>> tag : profile.tags().entrySet())
        {
            List<Pattern> patterns = tag.getValue().stream().map(p -> toRegex(p.toUpperCase(Locale.ROOT))).toList();
            for (String name : prices.keySet())
            {
                if (patterns.stream().anyMatch(p -> p.matcher(name).matches()))
                    tags.computeIfAbsent(name, n -> new LinkedHashSet<>()).add(tag.getKey().toLowerCase(Locale.ROOT));
            }
        }

        Map<String, String> limitGroups = new TreeMap<>();
        for (String name : prices.keySet())
        {
            String group = calculator.group(name);
            if (group != null)
                limitGroups.put(name, group);
        }
        Map<String, SellLimit> limits = new TreeMap<>();
        for (Map.Entry<String, PriceProfile.LimitGroup> group : profile.sellLimits().entrySet())
        {
            PriceProfile.LimitGroup limit = group.getValue();
            limits.put(group.getKey().toLowerCase(Locale.ROOT),
                    new SellLimit(limit.allowance() * scale, limit.refillPerHour() * scale, limit.minFraction()));
        }

        List<String> unused = new ArrayList<>();
        for (String exact : calculator._exact.keySet())
        {
            if (!prices.containsKey(exact))
                unused.add(exact);
        }
        unused.sort(null);
        return new Result(prices, tags, sources, unused, limitGroups, limits);
    }

    /**
     * The group that names the material, or else the group of its most valuable ingredient.
     */
    private String group(String name)
    {
        if (_groups.containsKey(name))
            return _groups.get(name);
        String group = _groupExact.get(name);
        if (group == null)
        {
            for (GroupWildcard wildcard : _groupWildcards)
            {
                if (wildcard.regex().matcher(name).matches())
                {
                    group = wildcard.group();
                    break;
                }
            }
        }
        if (group == null)
        {
            // A recipe loop (ingot <-> block) gives no group from the recipe; the named one in the loop has its own
            if (!_groupsInProgress.add(name))
                return null;
            try
            {
                group = groupFromRecipe(name);
            } finally
            {
                _groupsInProgress.remove(name);
            }
        }
        _groups.put(name, group);
        return group;
    }

    private String groupFromRecipe(String name)
    {
        Map<String, Double> ingredients = _recipes.apply(name);
        if (ingredients == null || ingredients.isEmpty())
            return null;
        String mostValuable = null;
        double highest = -1;
        for (Map.Entry<String, Double> ingredient : ingredients.entrySet())
        {
            String ingredientName = ingredient.getKey().toUpperCase(Locale.ROOT);
            double worth = price(ingredientName) * ingredient.getValue();
            if (worth > highest)
            {
                highest = worth;
                mostValuable = ingredientName;
            }
        }
        return mostValuable == null ? null : group(mostValuable);
    }

    private static double scale(double price, double scale)
    {
        if (price <= 0)
            return 0;
        // Never rounds a priced item down to free
        return Math.max(0.01, Math.round(price * scale * 100) / 100.0);
    }

    private double price(String name)
    {
        Double price = priceOrLoop(name);
        // Only a loop back to an item still being priced gives null, and the outermost call never is one
        return price == null ? _profile.defaultPrice() : price;
    }

    // The item a recipe loop led back to; the loop is broken there, with that item's default price
    private String _loopStart;

    /**
     * The price, or null while a recipe loop is being unwound back to its start. Items in the middle of a loop
     * aren't remembered, so they are priced again once the loop's start has its price.
     */
    private Double priceOrLoop(String name)
    {
        Double known = _resolved.get(name);
        if (known != null)
            return known;
        if (_inProgress.contains(name))
        {
            _loopStart = name;
            return null;
        }

        Double exact = _exact.get(name);
        if (exact != null)
            return remember(name, exact, Source.EXACT);

        for (Wildcard wildcard : _wildcards)
        {
            if (wildcard.regex().matcher(name).matches())
                return remember(name, wildcard.price(), Source.WILDCARD);
        }

        Map<String, Double> ingredients = _recipes.apply(name);
        if (ingredients == null || ingredients.isEmpty())
            return remember(name, _profile.defaultPrice(), Source.DEFAULT);

        _inProgress.add(name);
        try
        {
            double total = 0;
            for (Map.Entry<String, Double> ingredient : ingredients.entrySet())
            {
                Double ingredientPrice = priceOrLoop(ingredient.getKey().toUpperCase(Locale.ROOT));
                if (ingredientPrice == null)
                {
                    if (!name.equals(_loopStart))
                        return null;
                    _loopStart = null;
                    return remember(name, _profile.defaultPrice(), Source.DEFAULT);
                }
                total += ingredientPrice * ingredient.getValue();
            }
            return remember(name, total * _profile.craftedMultiplier(), Source.RECIPE);
        } finally
        {
            _inProgress.remove(name);
        }
    }

    private double remember(String name, double price, Source source)
    {
        _resolved.put(name, price);
        _sources.put(name, source);
        return price;
    }

    /**
     * "*_LOG" matches every name ending in _LOG; a name without * matches only itself.
     */
    static Pattern toRegex(String wildcard)
    {
        List<String> parts = new ArrayList<>();
        for (String part : wildcard.split("\\*", -1))
            parts.add(Pattern.quote(part));
        return Pattern.compile(String.join(".*", parts));
    }

    /**
     * Ingredients merged by material, for tests and the Bukkit side.
     */
    public static Map<String, Double> ingredients(Object... nameAmountPairs)
    {
        Map<String, Double> map = new LinkedHashMap<>();
        for (int i = 0; i < nameAmountPairs.length; i += 2)
            map.merge((String) nameAmountPairs[i], ((Number) nameAmountPairs[i + 1]).doubleValue(), Double::sum);
        return map;
    }
}
