package imu.GS.presets;

import org.bukkit.Material;

import java.util.List;

/**
 * A ready kind of shop for a village: a blacksmith, a farmer... What it sells, who the NPC is, and how its live
 * events go. Read from plugins/imusGS/presets/&lt;id&gt;.yml.
 *
 * @param id              file name without .yml; "general" is a general store that also buys
 * @param buyOnly         players can only buy here
 * @param profession      the NPC villager's profession, e.g. "weaponsmith"
 * @param npcNames        the NPC gets one of these names, followed by {@code npcTitle}
 * @param tags            material tags the shop is about: its rotating stock and flash deals come from these
 * @param foundTags       items of these tags players find turn up here; empty = the same as {@code tags}
 * @param quality         over 1 brings rarer, pricier items more often (the value scale of picking is multiplied)
 * @param rotationGearShare share of rotating stock that is enchanted gear (of {@code gear})
 * @param books           its rotating stock and flash deals are enchanted books
 * @param customBooks     ...books of other plugins' custom enchants (ImusEnchants), level I
 * @param flashGearShare  share of flash offers that are enchanted gear; null = the config.yml value
 * @param gear            gear this shop enchants and sells; empty = the config.yml flash deal gear
 * @param fun             flash deals may have fun items (TNT, fireworks...)
 * @param specials        flash deals may have rare specials (totems, nether stars...)
 * @param stock           what the shop always has, refilling over time
 */
public record ShopPreset(String id, String name, String displayName, boolean buyOnly, String profession,
                         List<String> npcNames, String npcTitle, List<String> tags, List<String> foundTags,
                         double quality, int rotationMin, int rotationMax, double rotationGearShare, boolean books, boolean customBooks,
                         Double flashGearShare, List<Material> gear, boolean fun, boolean specials, List<StockItem> stock)
{
    public static final String GENERAL = "general";

    /**
     * @param item    a material, or a custom item of another plugin ("imustnt:chunk_tnt")
     * @param max     most the shop has
     * @param fill    how many come back every {@code minutes}
     */
    public record StockItem(String item, int max, int fill, int minutes) {}

    public ShopPreset
    {
        npcNames = List.copyOf(npcNames);
        tags = List.copyOf(tags);
        foundTags = List.copyOf(foundTags);
        gear = List.copyOf(gear);
        stock = List.copyOf(stock);
        quality = Math.max(0.1, quality);
    }

    public boolean sellsBooks()
    {
        return books || customBooks;
    }

    public boolean isGeneral()
    {
        return GENERAL.equals(id);
    }

    /**
     * Tags whose found items come here.
     */
    public List<String> foundOrShopTags()
    {
        return foundTags.isEmpty() ? tags : foundTags;
    }
}
