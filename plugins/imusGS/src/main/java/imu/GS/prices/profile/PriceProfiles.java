package imu.GS.prices.profile;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import imu.GS.prices.MaterialPrices;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The price profiles in plugins/imusGS/profiles. The default profile is written there on every start, so it is
 * always the one of this version; copies of it are profiles of their own, to edit.
 */
public class PriceProfiles
{
    public static final String DEFAULT = "default";

    private final JavaPlugin _plugin;
    private final File _folder;

    public PriceProfiles(JavaPlugin plugin)
    {
        _plugin = plugin;
        _folder = new File(plugin.getDataFolder(), "profiles");
        plugin.saveResource("profiles/" + DEFAULT + ".yml", true);
    }

    public List<String> names()
    {
        List<String> names = new ArrayList<>();
        File[] files = _folder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files != null)
        {
            for (File file : files)
                names.add(file.getName().substring(0, file.getName().length() - 4));
        }
        names.sort(null);
        return names;
    }

    /**
     * @throws IllegalArgumentException with a message for the admin when the profile is missing or broken
     */
    public PriceProfile load(String name)
    {
        File file = new File(_folder, name + ".yml");
        if (!file.exists())
            throw new IllegalArgumentException("No profile named " + name + ". Profiles: " + String.join(", ", names()));

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        Map<String, Double> prices = new LinkedHashMap<>();
        ConfigurationSection priceSection = yaml.getConfigurationSection("prices");
        if (priceSection != null)
        {
            for (String key : priceSection.getKeys(false))
                prices.put(key, priceSection.getDouble(key));
        }
        Map<String, List<String>> tags = new LinkedHashMap<>();
        ConfigurationSection tagSection = yaml.getConfigurationSection("tags");
        if (tagSection != null)
        {
            for (String key : tagSection.getKeys(false))
                tags.put(key, tagSection.getStringList(key));
        }
        Map<String, PriceProfile.LimitGroup> limits = new LinkedHashMap<>();
        ConfigurationSection limitSection = yaml.getConfigurationSection("sell-limits");
        if (limitSection != null)
        {
            for (String key : limitSection.getKeys(false))
            {
                ConfigurationSection group = limitSection.getConfigurationSection(key);
                if (group == null)
                    continue;
                double allowance = group.getDouble("allowance");
                if (allowance <= 0)
                    throw new IllegalArgumentException("Profile " + name + ": sell-limits." + key + ".allowance must be over 0");
                limits.put(key, new PriceProfile.LimitGroup(allowance, group.getDouble("refill-per-hour", allowance),
                        Math.max(0, Math.min(100, group.getDouble("min-percent", 20))) / 100.0, group.getStringList("items")));
            }
        }
        return new PriceProfile(name, yaml.getDouble("reference-price", 1000), yaml.getDouble("crafted-multiplier", 1.1),
                yaml.getDouble("default-price", 0), prices, tags, limits);
    }

    /**
     * Prices the server's items with the profile. Main thread: crafting recipes come from the server.
     */
    public static ProfileCalculator.Result calculate(PriceProfile profile, MaterialPrices materials, double diamondPrice)
    {
        List<String> names = Arrays.stream(Material.values())
                .filter(m -> m.isItem() && !m.isAir() && !m.isLegacy())
                .map(Material::name)
                .toList();
        return ProfileCalculator.calculate(profile, names, name ->
        {
            Material material = Material.matchMaterial(name);
            if (material == null || !material.isItem())
                return Map.of();
            Map<String, Double> ingredients = new LinkedHashMap<>();
            for (Map.Entry<Material, Double> ingredient : materials.recipe(material).entrySet())
                ingredients.put(ingredient.getKey().name(), ingredient.getValue());
            return ingredients;
        }, diamondPrice);
    }
}
