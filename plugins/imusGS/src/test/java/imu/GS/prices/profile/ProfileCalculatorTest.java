package imu.GS.prices.profile;

import imu.GS.engine.pricing.SellLimit;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static imu.GS.prices.profile.ProfileCalculator.ingredients;
import static org.junit.jupiter.api.Assertions.*;

class ProfileCalculatorTest
{
    private static PriceProfile profile(Map<String, Double> prices, Map<String, List<String>> tags)
    {
        return new PriceProfile("test", 1000, 1.1, 1, prices, tags);
    }

    private static Map<String, Double> prices(Object... pairs)
    {
        Map<String, Double> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2)
            map.put((String) pairs[i], ((Number) pairs[i + 1]).doubleValue());
        return map;
    }

    private static final Map<String, Map<String, Double>> RECIPES = Map.of(
            "OAK_PLANKS", ingredients("OAK_LOG", 0.25),
            "STICK", ingredients("OAK_PLANKS", 0.5),
            "DIAMOND_SWORD", ingredients("DIAMOND", 2, "STICK", 1),
            "IRON_BLOCK", ingredients("IRON_INGOT", 9),
            // A loop: the ingot from the block, the block from the ingot
            "IRON_INGOT", ingredients("IRON_BLOCK", 1.0 / 9));

    private static ProfileCalculator.Result calculate(PriceProfile profile, List<String> materials, double diamond)
    {
        return ProfileCalculator.calculate(profile, materials, name -> RECIPES.getOrDefault(name, Map.of()), diamond);
    }

    @Test
    void anExactNameBeatsAWildcard()
    {
        PriceProfile profile = profile(prices("*_LOG", 4, "CHERRY_LOG", 6), Map.of());
        ProfileCalculator.Result result = calculate(profile, List.of("OAK_LOG", "CHERRY_LOG"), 1000);
        assertEquals(4, result.prices().get("OAK_LOG"));
        assertEquals(6, result.prices().get("CHERRY_LOG"));
    }

    @Test
    void theMostSpecificWildcardWins()
    {
        PriceProfile profile = profile(prices("*_ORE", 20, "*DIAMOND_ORE", 1100), Map.of());
        ProfileCalculator.Result result = calculate(profile, List.of("DEEPSLATE_DIAMOND_ORE", "COAL_ORE"), 1000);
        assertEquals(1100, result.prices().get("DEEPSLATE_DIAMOND_ORE"));
        assertEquals(20, result.prices().get("COAL_ORE"));
    }

    @Test
    void craftedItemsCostTheirIngredientsTimesTheMultiplier()
    {
        PriceProfile profile = profile(prices("DIAMOND", 1000, "OAK_LOG", 4), Map.of());
        ProfileCalculator.Result result = calculate(profile, List.of("DIAMOND_SWORD", "STICK"), 1000);
        // planks 4 / 4 * 1.1 = 1.1, stick 1.1 / 2 * 1.1 = 0.605, sword (2000 + 0.605) * 1.1
        assertEquals(0.61, result.prices().get("STICK"));
        assertEquals(Math.round((2000 + 0.605) * 1.1 * 100) / 100.0, result.prices().get("DIAMOND_SWORD"));
        assertEquals(ProfileCalculator.Source.RECIPE, result.source().get("DIAMOND_SWORD"));
    }

    @Test
    void aRecipeLoopFallsBackToTheDefaultPrice()
    {
        ProfileCalculator.Result result = calculate(profile(prices(), Map.of()), List.of("IRON_INGOT", "IRON_BLOCK"), 1000);
        assertEquals(1, result.prices().get("IRON_INGOT"));
        assertEquals(9.9, result.prices().get("IRON_BLOCK"));
    }

    @Test
    void anExactPriceBreaksTheLoop()
    {
        ProfileCalculator.Result result = calculate(profile(prices("IRON_INGOT", 80), Map.of()), List.of("IRON_BLOCK"), 1000);
        assertEquals(792, result.prices().get("IRON_BLOCK"));
    }

    @Test
    void anItemWithoutPriceOrRecipeGetsTheDefault()
    {
        ProfileCalculator.Result result = calculate(profile(prices(), Map.of()), List.of("POPPY"), 1000);
        assertEquals(1, result.prices().get("POPPY"));
        assertEquals(ProfileCalculator.Source.DEFAULT, result.source().get("POPPY"));
    }

    @Test
    void zeroMeansFree()
    {
        ProfileCalculator.Result result = calculate(profile(prices("*_SPAWN_EGG", 0), Map.of()), List.of("PIG_SPAWN_EGG"), 1000);
        assertEquals(0, result.prices().get("PIG_SPAWN_EGG"));
    }

    @Test
    void everyPriceScalesWithTheDiamondPrice()
    {
        PriceProfile profile = profile(prices("DIAMOND", 1000, "COBBLESTONE", 0.5), Map.of());
        ProfileCalculator.Result result = calculate(profile, List.of("DIAMOND", "COBBLESTONE"), 100);
        assertEquals(100, result.prices().get("DIAMOND"));
        // 0.05 would round to 0.05; anything priced never rounds down to free
        assertEquals(0.05, result.prices().get("COBBLESTONE"));
        assertEquals(0.01, calculate(profile, List.of("COBBLESTONE"), 1).prices().get("COBBLESTONE"));
    }

    @Test
    void tagsMatchNamesAndWildcards()
    {
        PriceProfile profile = profile(prices(), Map.of("wood", List.of("*_LOG", "STICK"), "Ores", List.of("*_ORE")));
        ProfileCalculator.Result result = calculate(profile, List.of("OAK_LOG", "STICK", "COAL_ORE", "STONE"), 1000);
        assertEquals(Set.of("wood"), result.tags().get("OAK_LOG"));
        assertEquals(Set.of("wood"), result.tags().get("STICK"));
        assertEquals(Set.of("ores"), result.tags().get("COAL_ORE"));
        assertNull(result.tags().get("STONE"));
    }

    @Test
    void wildcardsMatchTheWholeName()
    {
        assertTrue(ProfileCalculator.toRegex("*_AXE").matcher("DIAMOND_AXE").matches());
        assertFalse(ProfileCalculator.toRegex("*_AXE").matcher("DIAMOND_PICKAXE").matches());
        assertTrue(ProfileCalculator.toRegex("*DEEPSLATE*").matcher("COBBLED_DEEPSLATE").matches());
        assertFalse(ProfileCalculator.toRegex("STONE").matcher("REDSTONE").matches());
    }

    @Test
    void namesNotInTheGameAreReported()
    {
        ProfileCalculator.Result result = calculate(profile(prices("NOT_AN_ITEM", 5, "DIAMOND", 1000), Map.of()), List.of("DIAMOND"), 1000);
        assertEquals(List.of("NOT_AN_ITEM"), result.unusedNames());
    }

    private static PriceProfile limited(Map<String, Double> prices)
    {
        Map<String, PriceProfile.LimitGroup> limits = new LinkedHashMap<>();
        limits.put("iron", new PriceProfile.LimitGroup(25600, 6400, 0.2, List.of("IRON_INGOT")));
        limits.put("wood", new PriceProfile.LimitGroup(8000, 4000, 0.2, List.of("*_LOG")));
        return new PriceProfile("test", 1000, 1.1, 1, prices, Map.of(), limits);
    }

    @Test
    void sellLimitGroupsMatchNamesAndWildcards()
    {
        ProfileCalculator.Result result = calculate(limited(prices("IRON_INGOT", 80, "*_LOG", 4, "DIAMOND", 1000)),
                List.of("IRON_INGOT", "OAK_LOG", "DIAMOND"), 1000);
        assertEquals("iron", result.limitGroups().get("IRON_INGOT"));
        assertEquals("wood", result.limitGroups().get("OAK_LOG"));
        assertNull(result.limitGroups().get("DIAMOND"));
    }

    @Test
    void craftedItemsJoinTheGroupOfTheirMostValuableIngredient()
    {
        // The block is made of ingots (and the ingot of the block); the stick of planks of logs;
        // the sword's diamonds are worth more than its stick, and diamond is in no group
        ProfileCalculator.Result result = calculate(limited(prices("IRON_INGOT", 80, "*_LOG", 4, "DIAMOND", 1000)),
                List.of("IRON_BLOCK", "STICK", "DIAMOND_SWORD"), 1000);
        assertEquals("iron", result.limitGroups().get("IRON_BLOCK"));
        assertEquals("wood", result.limitGroups().get("STICK"));
        assertNull(result.limitGroups().get("DIAMOND_SWORD"));
    }

    @Test
    void sellLimitsScaleWithTheDiamondPrice()
    {
        ProfileCalculator.Result result = calculate(limited(prices("DIAMOND", 1000)), List.of("DIAMOND"), 500);
        assertEquals(new SellLimit(12800, 3200, 0.2), result.limits().get("iron"));
    }
}
